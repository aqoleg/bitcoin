// message with version, the first message for both nodes
//
// payload bytes:
//    intLE, version
//    longLE, services
//    longLE, timestamp, unix in seconds
//    byte[26], addrRecv, address of the node receiving this message
//    byte[26], addrFrom, address of the node emitting this message
//    longLE, nonce
//    varString, userAgent, /Satoshi:5.64/bitcoin-qt:0.4/
//    intLE, startHeight, the last block
//    if version >= 70001, 1 byte, relay

package space.aqoleg.messages;

import space.aqoleg.utils.BytesInput;
import space.aqoleg.utils.BytesOutput;

import java.util.Random;

public class Version {
    public static final String command = "version";
    public final int version;
    public final long services;
    public final long timestamp;
    public final NetAddress addrRecv;
    public final NetAddress addrFrom;
    public final long nonce;
    public final String userAgent;
    public final int startHeight;
    public final boolean relay;

    /**
     * @param version     version
     * @param services    services
     * @param timestamp   timestamp, unix in seconds
     * @param addrRecv    address of the node receiving this message
     * @param addrFrom    address of the node emitting this message
     * @param nonce       nonce
     * @param userAgent   userAgent
     * @param startHeight the last block
     * @param relay       relay
     */
    public Version(
            int version,
            long services,
            long timestamp,
            NetAddress addrRecv,
            NetAddress addrFrom,
            long nonce,
            String userAgent,
            int startHeight,
            boolean relay
    ) {
        this.version = version;
        this.services = services;
        this.timestamp = timestamp;
        this.addrRecv = addrRecv == null ? NetAddress.empty : addrRecv;
        this.addrFrom = addrFrom == null ? NetAddress.empty : addrFrom;
        this.nonce = nonce;
        this.userAgent = userAgent == null ? "" : userAgent;
        this.startHeight = startHeight;
        this.relay = relay;
    }

    /**
     * @param payload byte array with payload
     * @return instance of Version created from payload
     * @throws NullPointerException          if payload == null
     * @throws IndexOutOfBoundsException     if payload is incorrect
     * @throws UnsupportedOperationException if payload is incorrect
     */
    public static Version parse(byte[] payload) {
        BytesInput bytes = new BytesInput(payload);
        int version = bytes.readIntLE();
        long services = bytes.readLongLE();
        long timestamp = bytes.readLongLE();
        NetAddress addrRecv = NetAddress.read(bytes, true);
        NetAddress addrFrom = NetAddress.read(bytes, true);
        long nonce = bytes.readLongLE();
        String userAgent = bytes.readVariableLengthString();
        int startHeight = bytes.readIntLE();
        boolean relay = false;
        // version >= 70001
        if (bytes.available() > 0) {
            relay = bytes.read() != 0;
        }
        if (bytes.available() != 0) {
            throw new UnsupportedOperationException("payload length is incorrect");
        }
        return new Version(version, services, timestamp, addrRecv, addrFrom, nonce, userAgent, startHeight, relay);
    }

    /**
     * @param version version
     * @param ip      String with ipv4 address
     * @param port    port number
     * @return new instance of Version with no services, now time, empty emitting address, random nonce,
     * userAgent "/crypto:0.0.1/" and 0 startHeight
     * @throws NullPointerException            if ip == null
     * @throws StringIndexOutOfBoundsException if ip is incorrect
     * @throws NumberFormatException           if ip is incorrect
     */
    public static Version create(int version, String ip, int port) {
        return new Version(
                version,
                0,
                System.currentTimeMillis() / 1000,
                NetAddress.create(ip, port),
                NetAddress.empty,
                new Random().nextLong(),
                "/crypto:0.0.1/",
                0,
                false
        );
    }

    /**
     * @return byte array with this Version message
     */
    public byte[] toByteArray() {
        BytesOutput bytes = new BytesOutput();
        bytes.writeIntLE(version);
        bytes.writeLongLE(services);
        bytes.writeLongLE(timestamp);
        addrRecv.write(bytes, true);
        addrFrom.write(bytes, true);
        bytes.writeLongLE(nonce);
        bytes.writeVariableLength(userAgent);
        bytes.writeIntLE(startHeight);
        if (version >= 70001) {
            bytes.write(0);
        }
        return Message.toByteArray(command, bytes.toByteArray());
    }

    /**
     * @return String representation of this Version
     */
    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append("version: ").append(version);
        out.append(", services: ").append(services);
        out.append(", timestamp: ").append(timestamp);
        out.append(", addrRecvServices: ").append(addrRecv.services);
        out.append(", addrRecvTime: ").append(addrRecv.time);
        out.append(", addrRecv: ").append(addrRecv.addressToString());
        out.append(", addrFromServices: ").append(addrFrom.services);
        out.append(", addrFromTime: ").append(addrFrom.time);
        out.append(", addrFrom: ").append(addrFrom.addressToString());
        out.append(", nonce: ").append(nonce);
        out.append(", userAgent: ").append(userAgent);
        out.append(", startHeight: ").append(startHeight);
        if (version >= 70001) {
            out.append(", relay: ").append(relay);
        }
        return out.toString();
    }
}