/*
data structure used in Addr, Version messages and stored in the filesystem

usage:
    NetAddress emptyNetAddress = NetAddress.empty;
    NetAddress netAddress = new NetAddress(bytes);
    NetAddress netAddress = NetAddress.read(bytesInput, isVersion);
    NetAddress netAddress = NetAddress.fromInetAddress(inetAddress);
    netAddress.write(outputStream);
    String string = netAddress.toString();
    boolean equals = netAddress.equals(otherNetAddress);
    InetAddress inetAddress = netAddress.getInetAddress();
    int port = netAddress.getPort();
    netAddress.write(bytesOutput, isVersion);

NetAddress bytes:
    if not a version message, intLE, time
    longLE, services
    byte[16], ip
    shortBE, port
*/

package com.aqoleg.messages;

import com.aqoleg.data.Storage;
import com.aqoleg.utils.BytesInput;
import com.aqoleg.utils.BytesOutput;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;

public class NetAddress implements Storage.Writable {
    public static final NetAddress empty = new NetAddress(new byte[30]); // all zeroes
    private final byte[] bytes; // 30

    /**
     * @param bytes byte array with 30 bytes of NetAddress
     * @throws NullPointerException if bytes == null
     * @throws Message.Exception    if bytes are incorrect
     */
    public NetAddress(byte[] bytes) {
        if (bytes.length != 30) {
            throw new Message.Exception("incorrect length");
        }
        this.bytes = bytes;
    }

    /**
     * @param bytesInput       BytesInput containing NetAddress
     * @param isVersionMessage true if use for Version message
     * @return NetAddress read from bytes
     * @throws NullPointerException if bytesInput == null
     * @throws Message.Exception    if bytesInput is incorrect
     */
    public static NetAddress read(BytesInput bytesInput, boolean isVersionMessage) {
        try {
            byte[] bytes = new byte[30];
            if (isVersionMessage) {
                bytesInput.readBytes(bytes, 4, 26);
            } else {
                bytesInput.readBytes(bytes);
            }
            return new NetAddress(bytes);
        } catch (IndexOutOfBoundsException exception) {
            throw new Message.Exception(exception.getMessage());
        }
    }

    /**
     * @param inetAddress InetAddress from which will be created NetAddress
     * @return NetAddress with port 8333 and zero services and time
     * @throws NullPointerException if inetAddress == null
     * @throws Message.Exception    if inetAddress is incorrect
     */
    public static NetAddress fromInetAddress(InetAddress inetAddress) {
        NetAddress netAddress = new NetAddress(new byte[30]);
        byte[] bytes = inetAddress.getAddress();
        if (bytes.length == 16) {
            System.arraycopy(bytes, 0, netAddress.bytes, 12, 16);
        } else if (bytes.length == 4) {
            netAddress.bytes[22] = (byte) 0xFF;
            netAddress.bytes[23] = (byte) 0xFF;
            System.arraycopy(bytes, 0, netAddress.bytes, 24, 4);
        } else {
            throw new Message.Exception("incorrect inetAddress");
        }
        netAddress.bytes[28] = 8333 >>> 8;
        netAddress.bytes[29] = (byte) 8333;
        return netAddress;
    }

    /**
     * write this NetAddress into the outputStream
     *
     * @param outputStream OutputStream in which will be written 30 bytes of this NetAddress
     * @return number of bytes written (30)
     * @throws NullPointerException if outputStream == null
     * @throws IOException          if cannot write
     */
    @Override
    public int write(OutputStream outputStream) throws IOException {
        outputStream.write(bytes);
        return bytes.length;
    }

    /**
     * @return "time: 2000-01-22T12:00:00Z, services: 1, ip: 1.1.1.1, port: 8333"
     */
    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        int pos = 0;
        int time = 0;
        for (int i = 0; i <= 24; i += 8) {
            time |= (bytes[pos++] & 0xFF) << i;
        }
        if (time != 0) {
            out.append("time: ").append(Instant.ofEpochSecond(time));
        } else {
            out.append("time: 0");
        }
        long services = 0;
        for (int i = 0; i <= 56; i += 8) {
            services |= (long) (bytes[pos++] & 0xFF) << i;
        }
        out.append(", services: ").append(Long.toBinaryString(services));
        out.append(", ip: ").append(getInetAddress().getHostAddress());
        out.append(", port: ").append(getPort());
        return out.toString();
    }

    /**
     * @param object object to be compared with
     * @return true if ip address and the port are the same
     */
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof NetAddress)) {
            return false;
        }
        NetAddress netAddress = (NetAddress) object;
        for (int i = 12; i < 30; i++) {
            if (bytes[i] != netAddress.bytes[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return InetAddress created from this NetAddress
     */
    public InetAddress getInetAddress() {
        boolean v4 = true;
        for (int i = 12; i < 22; i++) {
            if (bytes[i] != 0) {
                v4 = false;
                break;
            }
        }
        if (bytes[22] != (byte) 0xFF || bytes[23] != (byte) 0xFF) {
            v4 = false;
        }
        byte[] ip;
        if (v4) {
            ip = new byte[4];
            System.arraycopy(bytes, 24, ip, 0, 4);
        } else {
            ip = new byte[16];
            System.arraycopy(bytes, 12, ip, 0, 16);
        }
        try {
            return InetAddress.getByAddress(ip);
        } catch (UnknownHostException exception) {
            exception.printStackTrace();
            return InetAddress.getLoopbackAddress();
        }
    }

    /**
     * @return port number
     */
    public int getPort() {
        int port = (bytes[28] & 0xFF) << 8;
        port |= bytes[29] & 0xFF;
        return port;
    }

    /**
     * write this NetAddress into the BytesOutput
     *
     * @param bytesOutput      BytesOutput in which will be written this NetAddress
     * @param isVersionMessage true if use for Version message
     * @throws NullPointerException if bytesOutput == null
     */
    public void write(BytesOutput bytesOutput, boolean isVersionMessage) {
        if (isVersionMessage) {
            bytesOutput.writeBytes(bytes, 4, 26);
        } else {
            bytesOutput.writeBytes(bytes);
        }
    }
}