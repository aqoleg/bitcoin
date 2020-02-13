// message reader with buffer and message writer
// call read(inputStream), then call finish(), if you want read this message, or reset(), then again read(inputStream)
//
// message bytes:
//    byte[4], magic
//    header:
//       byte[12], command
//       intLE, length of payload
//       byte[4], checksum, first 4 bytes of sha256(sha256(payload))
//    byte[length of payload], payload
package space.aqoleg.messages;

import space.aqoleg.crypto.Sha256;
import space.aqoleg.utils.BytesOutput;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class Message {
    private static final int[] magic = {0xF9, 0xBE, 0xB4, 0xD9}; // main network
    // buffer
    private int magicPos = 0; // == 4, if magic has found
    private final byte[] header = new byte[20];
    private int headerPos = 0; // == 20, if header has filled
    private byte[] payload = {};
    private int payloadPos = 0; // == payload.length, if payload has filled
    private boolean finished = false;

    /**
     * @return magicPos, number of found magic bytes
     */
    public int getMagicPos() {
        return magicPos;
    }

    /**
     * @return byte array with loaded header
     */
    public byte[] getHeader() {
        return Arrays.copyOf(header, headerPos);
    }

    /**
     * @return byte array with loaded payload
     */
    public byte[] getPayload() {
        return Arrays.copyOf(payload, payloadPos);
    }

    /**
     * this method is blocked when inputStream is blocked
     * read inputStream till the end of the message
     *
     * @param inputStream InputStream from which will be read the message
     * @return command in this message
     * @throws NullPointerException if inputStream == null
     * @throws IOException          if inputStream error occurs or inputStream have been closed
     */
    public String read(InputStream inputStream) throws IOException {
        // find magic
        while (magicPos != 4) {
            int b = inputStream.read();
            if (b == -1) {
                throw new IOException("stream have been closed");
            }
            if (b == magic[magicPos]) {
                magicPos++;
            } else if (magicPos != 0) {
                // incorrect magic, start from the beginning and check the first byte
                magicPos = b == magic[0] ? 1 : 0;
            }
        }
        // fill header buffer
        while (headerPos != 20) {
            int readBytes = inputStream.read(header, headerPos, 20 - headerPos);
            if (readBytes <= 0) {
                throw new IOException("stream have been closed");
            }
            headerPos += readBytes;
        }
        // determine payload length, header[12...15], little endian
        int length = header[12] & 0xFF |
                (header[13] & 0xFF) << 8 |
                (header[14] & 0xFF) << 16 |
                (header[15] & 0xFF) << 24;
        // create and fill payload buffer
        payload = new byte[length];
        while (payloadPos != length) {
            int readBytes = inputStream.read(payload, payloadPos, payload.length - payloadPos);
            if (readBytes <= 0) {
                throw new IOException("stream have been closed");
            }
            payloadPos += readBytes;
        }
        // parse command, header[0...11], ascII text, null padded
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            char c = (char) header[i];
            if (c == 0) {
                break;
            }
            builder.append(c);
        }
        finished = true;
        return builder.toString();
    }

    /**
     * checks payload checksum, clears buffer, ready to read next message
     *
     * @return byte array with payload
     * @throws UnsupportedOperationException if message not read yet or checksum is incorrect
     */
    public byte[] finish() {
        if (!finished) {
            throw new UnsupportedOperationException("uncompleted message");
        }
        // check checksum, header[16...19], first 4 bytes of sha256(sha256(payload))
        byte[] checksum = Sha256.getHash(Sha256.getHash(payload));
        for (int i = 0; i < 4; i++) {
            if (checksum[i] != header[i + 16]) {
                throw new UnsupportedOperationException("incorrect checksum");
            }
        }
        // clear buffer
        byte[] out = payload;
        reset();
        return out;
    }

    /**
     * clear buffer, ready to read next message
     */
    public void reset() {
        magicPos = 0;
        headerPos = 0;
        payloadPos = 0;
        finished = true;
    }

    /**
     * @param command String with command
     * @param payload byte array with payload
     * @return byte array with message
     * @throws NullPointerException          if command == null or payload == null
     * @throws UnsupportedOperationException if command is too long
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
            throw new UnsupportedOperationException("too big command");
        }
        bytes.writeBytes(cmd);
        int padding = 12 - cmd.length;
        while (padding != 0) {
            bytes.write(0);
            padding--;
        }
        // length of payload
        bytes.writeIntLE(payload.length);
        // checksum, first 4 bytes of sha256(sha256(payload))
        byte[] checksum = Sha256.getHash(Sha256.getHash(payload));
        bytes.writeBytes(checksum, 0, 4);
        // payload
        bytes.writeBytes(payload);
        return bytes.toByteArray();
    }
}