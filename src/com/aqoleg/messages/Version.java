/*
the first message, response to the Version message

usage:
    Version version = (Version) Message.read(inputStream);
    Version version = new Version(bytes);
    Version version = Version.create(intVersion, services, timestamp, receiverNetAddress, fromNetAddress,
     nonce, userAgent, startHeight);
    Version version = Version.create(intVersion, receiverNetAddress);
    String string = version.toString();
    int version = version.getVersion();
    byte[] message = version.toByteArray();

payload bytes:
    intLE, version
    longLE, services
    longLE, timestamp, unix in seconds
    byte[26], addrRecv, address of the node receiving this message
    byte[26], addrFrom, address of the node emitting this message
    longLE, nonce
    varString, userAgent, /Satoshi:5.64/bitcoin-qt:0.4/
    intLE, startHeight, the last block
    if version >= 70001, 1 byte, relay
*/

package com.aqoleg.messages;

import com.aqoleg.utils.BytesInput;
import com.aqoleg.utils.BytesOutput;

import java.time.Instant;
import java.util.Random;

public class Version extends Message {
    public static final String command = "version";
    private final byte[] bytes;

    /**
     * @param bytes byte array with payload
     * @throws NullPointerException if bytes == null
     * @throws Message.Exception    if bytes are incorrect
     */
    public Version(byte[] bytes) {
        BytesInput bytesInput = new BytesInput(bytes);
        int version;
        try {
            version = bytesInput.readIntLE();
        } catch (IndexOutOfBoundsException exception) {
            throw new Message.Exception(exception.getMessage());
        }
        if (bytesInput.skip(76) != 76) { // services, timestamp, addrRecv, addrFrom, nonce
            throw new Message.Exception("short length before useragent");
        }
        try {
            bytesInput.readVariableLengthString(); // userAgent
        } catch (IndexOutOfBoundsException exception) {
            throw new Message.Exception(exception.getMessage());
        }
        if (bytesInput.skip(4) != 4) { // startHeight
            throw new Message.Exception("short length startHeight");
        }
        if (bytesInput.available() != (version >= 70001 ? 1 : 0)) { // relay
            throw new Message.Exception("incorrect relay length");
        }
        this.bytes = bytes;
    }

    /**
     * @param version     version
     * @param services    services
     * @param timestamp   timestamp, unix seconds
     * @param addrRecv    address of the node receiving this message
     * @param addrFrom    address of the node emitting this message
     * @param nonce       nonce
     * @param userAgent   userAgent
     * @param startHeight the last block
     * @return instance of Version
     * @throws NullPointerException if addrRecv == null or addrFrom == null
     */
    public static Version create(
            int version,
            long services,
            long timestamp,
            NetAddress addrRecv,
            NetAddress addrFrom,
            long nonce,
            String userAgent,
            int startHeight
    ) {
        BytesOutput bytesOutput = new BytesOutput();
        bytesOutput.writeIntLE(version).writeLongLE(services).writeLongLE(timestamp);
        addrRecv.write(bytesOutput, true);
        addrFrom.write(bytesOutput, true);
        bytesOutput.writeLongLE(nonce).writeVariableLength(userAgent).writeIntLE(startHeight);
        if (version >= 70001) {
            bytesOutput.write(0);
        }
        return new Version(bytesOutput.toByteArray());
    }

    /**
     * @param version  version
     * @param addrRecv address of the node receiving this message
     * @return instance of Version with no services, time now, empty emitting address, random nonce,
     * userAgent /aqoleg:1.0.0/" and startHeight 0
     * @throws NullPointerException if addrRecv == null
     */
    public static Version create(int version, NetAddress addrRecv) {
        return create(
                version,
                0,
                System.currentTimeMillis() / 1000,
                addrRecv,
                NetAddress.empty,
                new Random().nextLong(),
                "/aqoleg:1.0.0/",
                0
        );
    }

    /**
     * @return "version: 1, services: 0, time: 2000-01-22T12:00:00Z, addrRecv: (NetAddress.toString()),
     * addrFrom: (NetAddress.toString()), nonce: 0, userAgent: /aqoleg/, startHeight: 0, relay: false"
     */
    @Override
    public String toString() {
        StringBuilder string = new StringBuilder();
        BytesInput bytesInput = new BytesInput(bytes);
        int version = bytesInput.readIntLE();
        string.append("version: ").append(version);
        string.append(", services: ").append(Long.toBinaryString(bytesInput.readLongLE()));
        long time = bytesInput.readLongLE();
        if (time != 0) {
            string.append(", time: ").append(Instant.ofEpochSecond(time));
        } else {
            string.append(", time: 0");
        }
        string.append(", addrRecv: (").append(NetAddress.read(bytesInput, true));
        string.append("), addrFrom: (").append(NetAddress.read(bytesInput, true));
        string.append("), nonce: ").append(bytesInput.readLongLE());
        string.append(", userAgent: ").append(bytesInput.readVariableLengthString());
        string.append(", startHeight: ").append(bytesInput.readIntLE());
        if (version >= 70001) {
            string.append(", relay: ").append(bytesInput.read() != 0);
        }
        return string.toString();
    }

    /**
     * @return version
     */
    public int getVersion() {
        return new BytesInput(bytes).readIntLE();
    }

    /**
     * @return byte array with this Version message
     */
    public byte[] toByteArray() {
        return Message.toByteArray(command, bytes);
    }
}