/*
message reader and creator

usage:
    Message subclassMessage = Message.read(inputStream);
    Message subclassMessage = Message.read(inputStream, stringArrayWithCommands);
    String command = message.command;
    byte[] message = Message.toByteArray(command, payloadBytes);

message bytes:
    byte[4], magic
    header:
        byte[12], command
        intLE, length of payload
        byte[4], checksum, first 4 bytes of sha256(sha256(payload))
    byte[length of payload], payload
*/

package com.aqoleg.messages;

import com.aqoleg.crypto.Sha256;
import com.aqoleg.utils.BytesOutput;

import java.io.IOException;
import java.io.InputStream;

public abstract class Message {
    private static final int[] magic = {0xF9, 0xBE, 0xB4, 0xD9}; // main network

    /**
     * blocked when inputStream is blocked, reads one message from the inputStream
     *
     * @param inputStream InputStream from which will be read the message
     * @return the first message read
     * @throws NullPointerException if inputStream == null
     * @throws IOException          if inputStream error occurs
     * @throws Message.Exception    if the message is incorrect
     */
    public static Message read(InputStream inputStream) throws IOException {
        byte[] header = readHeader(inputStream);
        String command = getCommand(header);
        return readPayload(inputStream, header, command);
    }

    /**
     * blocked when inputStream is blocked, reads the inputStream until it finds the message with requested command
     *
     * @param inputStream InputStream from which will be read the message
     * @param commands    array of commands of requested messages
     * @return first message with the requested command
     * @throws NullPointerException if inputStream == null or commands == null
     * @throws IOException          if inputStream error occurs
     * @throws Message.Exception    if the message is incorrect
     */
    public static Message read(InputStream inputStream, String[] commands) throws IOException {
        do {
            byte[] header = readHeader(inputStream);
            String command = getCommand(header);
            for (String c : commands) {
                if (command.equals(c)) {
                    return readPayload(inputStream, header, command);
                }
            }
            skipPayload(inputStream, header);
        } while (true);
    }

    /**
     * @param command String with command
     * @param payload byte array with payload
     * @return byte array with message
     * @throws NullPointerException if command == null or payload == null
     * @throws Message.Exception    if command is too long
     */
    public static byte[] toByteArray(String command, byte[] payload) {
        BytesOutput bytes = new BytesOutput();
        // magic
        bytes.write(magic[0]);
        bytes.write(magic[1]);
        bytes.write(magic[2]);
        bytes.write(magic[3]);
        // command
        byte[] cmd = command.getBytes();
        if (cmd.length > 12) {
            throw new Message.Exception("command is too big");
        }
        bytes.writeBytes(cmd);
        int padding = 12 - cmd.length;
        while (padding != 0) {
            bytes.write(0);
            padding--;
        }
        // length of the payload
        bytes.writeIntLE(payload.length);
        // checksum, first 4 bytes of sha256(sha256(payload))
        byte[] checksum = Sha256.getHash(Sha256.getHash(payload));
        bytes.writeBytes(checksum, 0, 4);
        // payload
        bytes.writeBytes(payload);
        return bytes.toByteArray();
    }

    private static byte[] readHeader(InputStream inputStream) throws IOException {
        // find magic
        int pos = 0;
        while (pos != 4) {
            int b;
            b = inputStream.read();
            if (b == -1) {
                throw new IOException("the end of stream");
            }
            if (b == magic[pos]) {
                pos++;
            } else if (pos != 0) {
                // incorrect magic, start from the beginning and check the first byte
                pos = b == magic[0] ? 1 : 0;
            }
        }
        // read header
        byte[] header = new byte[20];
        pos = 0;
        while (pos != 20) {
            int readBytes = inputStream.read(header, pos, 20 - pos);
            if (readBytes <= 0) {
                throw new IOException("the end of stream");
            }
            pos += readBytes;
        }
        return header;
    }

    private static String getCommand(byte[] header) {
        // header[0...11], ascII text, null padded
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            char c = (char) header[i];
            if (c == 0) {
                break;
            }
            builder.append(c);
        }
        return builder.toString();
    }

    private static void skipPayload(InputStream inputStream, byte[] header) throws IOException {
        // payload length, header[12...15], little endian
        int length = header[12] & 0xFF | (header[13] & 0xFF) << 8 | (header[14] & 0xFF) << 16
                | (header[15] & 0xFF) << 24;
        int pos = 0;
        while (pos != length) {
            int skippedBytes = (int) inputStream.skip(length - pos);
            if (skippedBytes <= 0) {
                throw new IOException("the end of stream");
            }
            pos += skippedBytes;
        }
    }

    private static Message readPayload(InputStream inputStream, byte[] header, String command) throws IOException {
        // payload length, header[12...15], little endian
        int length = header[12] & 0xFF | (header[13] & 0xFF) << 8 | (header[14] & 0xFF) << 16
                | (header[15] & 0xFF) << 24;
        // read the payload
        byte[] payload = new byte[length];
        int pos = 0;
        while (pos != length) {
            int readBytes = inputStream.read(payload, pos, length - pos);
            if (readBytes <= 0) {
                throw new IOException("the end of stream");
            }
            pos += readBytes;
        }
        // check the checksum, header[16...19], first 4 bytes of sha256(sha256(payload))
        byte[] checksum = Sha256.getHash(Sha256.getHash(payload));
        for (int i = 0; i < 4; i++) {
            if (checksum[i] != header[i + 16]) {
                throw new Message.Exception("incorrect checksum");
            }
        }
        switch (command) {
            case Addr.command:
                return new Addr(payload);
            case Alert.command:
                return new Alert(payload);
            case Block.command:
                return new Block(payload);
            case GetData.command:
                return new GetData(payload);
            case Inv.command:
                return new Inv(payload);
            case NotFound.command:
                return new NotFound(payload);
            case Ping.command:
                return new Ping(payload);
            case Reject.command:
                return new Reject(payload);
            case Transaction.command:
                return Transaction.fromBytes(payload);
            case VerAck.command:
                return new VerAck(payload);
            case Version.command:
                return new Version(payload);
            default:
                return new Unknown(command);
        }
    }

    public static class Unknown extends Message {
        public final String command;

        private Unknown(String command) {
            this.command = command;
        }
    }

    public static class Exception extends RuntimeException {
        public Exception(String message) {
            super(message);
        }
    }
}