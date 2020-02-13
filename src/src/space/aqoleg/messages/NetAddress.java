// data structure used in Addr and Version messages
//
// NetAddress bytes:
//    if not a version message, intLE, time
//    longLE, services
//    byte[16], ip
//    shortBE, port
package space.aqoleg.messages;

import space.aqoleg.utils.BytesInput;
import space.aqoleg.utils.BytesOutput;

public class NetAddress {
    public static final NetAddress empty = new NetAddress(0, 0, new byte[16], 0);
    public final int time;
    public final long services;
    private final byte[] ip;
    public final int port;

    private NetAddress(int time, long services, byte[] ip, int port) {
        this.time = time;
        this.services = services;
        this.ip = ip;
        this.port = port;
    }

    /**
     * @param bytes          BytesInput containing NetAddress
     * @param versionMessage true if use for Version message
     * @return NetAddress read from bytes
     * @throws NullPointerException      if bytes == null
     * @throws IndexOutOfBoundsException if bytes are incorrect
     */
    public static NetAddress read(BytesInput bytes, boolean versionMessage) {
        int time = versionMessage ? 0 : bytes.readIntLE();
        long services = bytes.readLongLE();
        byte[] ip = new byte[16];
        bytes.readBytes(ip);
        int port = bytes.read() << 8 | bytes.read();
        return new NetAddress(time, services, ip, port);
    }

    /**
     * @param ip   String with ipv4 address
     * @param port port number
     * @return new instance of NetAddress with now time and no services
     * @throws NullPointerException            if ip == null
     * @throws StringIndexOutOfBoundsException if ip is incorrect
     * @throws NumberFormatException           if ip is incorrect
     */
    public static NetAddress create(String ip, int port) {
        byte[] bytes = new byte[16];
        bytes[10] = (byte) 0xFF;
        bytes[11] = (byte) 0xFF;
        int startPos;
        int endPos = -1;
        for (int i = 0; i < 4; i++) {
            startPos = endPos + 1;
            if (i != 3) {
                endPos = ip.indexOf(".", startPos);
            } else {
                endPos = ip.length();
            }
            bytes[12 + i] = (byte) Integer.parseInt(ip.substring(startPos, endPos));
        }
        return new NetAddress((int) (System.currentTimeMillis() / 1000), 0, bytes, port);
    }

    /**
     * write this NetAddress into the BytesOutput
     *
     * @param bytes          BytesOutput in which will be written this NetAddress
     * @param versionMessage true if use for Version message
     * @throws NullPointerException if bytes == null
     */
    public void write(BytesOutput bytes, boolean versionMessage) {
        if (!versionMessage) {
            bytes.writeIntLE(time);
        }
        bytes.writeLongLE(services);
        bytes.writeBytes(ip);
        bytes.write((port >>> 8));
        bytes.write(port);
    }

    /**
     * @return String <ipv4><one space><port> or null if ipv6
     */
    public String addressToString() {
        for (int i = 0; i < 10; i++) {
            if (ip[i] != 0) {
                return null;
            }
        }
        for (int i = 10; i < 12; i++) {
            if ((ip[i] & 0xFF) != 0xFF) {
                return null;
            }
        }
        StringBuilder out = new StringBuilder();
        for (int i = 12; i < 16; i++) {
            out.append(ip[i] & 0xFF);
            if (i != 15) {
                out.append(".");
            }
        }
        out.append(" ");
        out.append(port);
        return out.toString();
    }
}