package com.aqoleg.messages.test;

import com.aqoleg.Test;
import com.aqoleg.messages.Message;
import com.aqoleg.messages.NetAddress;
import com.aqoleg.utils.BytesInput;
import com.aqoleg.utils.BytesOutput;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;

@SuppressWarnings("unused")
public class NetAddressTest extends Test {

    public static void main(String[] args) {
        new NetAddressTest().testAll();
    }

    public void test() {
        assertEquals("time: 0, services: 0, ip: 0:0:0:0:0:0:0:0, port: 0", NetAddress.empty.toString());

        assertThrows(NullPointerException.class, () -> NetAddress.read(null, true));
        assertThrows(Message.Exception.class, () -> NetAddress.read(new BytesInput(new byte[10]), true));
        byte[] bytes = hexToBytes("d91f4854" + "0100000000000000" + "00000000000000000000ffffc0000233"
                + "208d");
        NetAddress netAddress = NetAddress.read(new BytesInput(bytes), false);
        assertThrows(NullPointerException.class, () -> netAddress.write(null));
        assertThrows(IOException.class, () -> netAddress.write(new OutputStream() {
            @Override
            public void write(int i) throws IOException {
                throw new IOException();
            }
        }));
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        assertNotThrows(() -> netAddress.write(stream));
        assertEquals(bytes, stream.toByteArray());
        assertEquals("time: 2014-10-22T21:21:29Z, services: 1, ip: 192.0.2.51, port: 8333", netAddress.toString());
        assertTrue(netAddress.equals(NetAddress.read(new BytesInput(bytes), false)));
        //noinspection ObjectEqualsNull
        assertTrue(!netAddress.equals(null));
        assertTrue(!netAddress.equals(NetAddress.empty));
        assertEquals("192.0.2.51", netAddress.getInetAddress().getHostAddress());
        assertEquals(8333, netAddress.getPort());
        BytesOutput bytesOutput = new BytesOutput();
        netAddress.write(bytesOutput, false);
        assertEquals(bytes, bytesOutput.toByteArray());

        assertThrows(NullPointerException.class, () -> new NetAddress(null));
        assertThrows(Message.Exception.class, () -> new NetAddress(new byte[10]));
        bytes = hexToBytes("00000000" + "1090FF0001FF5512" + "2a020c7d28949c00f9f0f8dd46e497a1" + "10F0");
        NetAddress netAddress1 = new NetAddress(bytes);
        ByteArrayOutputStream stream1 = new ByteArrayOutputStream();
        assertNotThrows(() -> netAddress1.write(stream1));
        assertEquals(bytes, stream1.toByteArray());
        assertEquals(
                "time: 0, services: 1001001010101111111110000000100000000111111111001000000010000" +
                        ", ip: 2a02:c7d:2894:9c00:f9f0:f8dd:46e4:97a1, port: 4336",
                netAddress1.toString()
        );
        assertTrue(!netAddress1.equals(netAddress));
        assertTrue(netAddress1.equals(new NetAddress(bytes)));
        byte[] bytes1 = hexToBytes("d91f4854" + "1090FF0001FF5512" + "2a020c7d28949c00f9f0f8dd46e497a1" + "10F0");
        assertTrue(netAddress1.equals(new NetAddress(bytes1)));
        bytes1 = hexToBytes("00000000" + "0090FF0001FF5512" + "2a020c7d28949c00f9f0f8dd46e497a1" + "10F0");
        assertTrue(netAddress1.equals(new NetAddress(bytes1)));
        bytes1 = hexToBytes("00000000" + "0090FF0001FF5512" + "2a020c7d28949c00f9f0f8dd46e497a1" + "00F0");
        assertTrue(!netAddress1.equals(new NetAddress(bytes1)));
        assertEquals("2a02:c7d:2894:9c00:f9f0:f8dd:46e4:97a1", netAddress1.getInetAddress().getHostAddress());
        assertEquals(4336, netAddress1.getPort());
        bytesOutput = new BytesOutput();
        netAddress1.write(bytesOutput, false);
        assertEquals(bytes, bytesOutput.toByteArray());

        try {
            InetAddress inetAddress = InetAddress.getByName("2a02:c7d:2894:9c00:f9f0:f8dd:46e4:97a1");
            NetAddress netAddress2 = NetAddress.fromInetAddress(inetAddress);
            assertEquals(
                    "time: 0, services: 0, ip: 2a02:c7d:2894:9c00:f9f0:f8dd:46e4:97a1, port: 8333",
                    netAddress2.toString()
            );
            assertTrue(!netAddress1.equals(netAddress2));
            assertEquals(8333, netAddress2.getPort());
            ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
            assertNotThrows(() -> netAddress2.write(stream2));
            bytesOutput = new BytesOutput();
            netAddress2.write(bytesOutput, false);
            assertEquals(stream2.toByteArray(), bytesOutput.toByteArray());
        } catch (UnknownHostException exception) {
            System.out.println("cannot test...");
            exception.printStackTrace();
        }
    }
}