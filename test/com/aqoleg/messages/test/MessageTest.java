package com.aqoleg.messages.test;

import com.aqoleg.Test;
import com.aqoleg.messages.Message;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

@SuppressWarnings("unused")
public class MessageTest extends Test {

    public static void main(String[] args) {
        new MessageTest().testAll();
    }

    public void stream() {
        String[] stream = new String[]{
                "CC00" + "F9", "00" + "F9BEB4", "0088" + "F9BE", "F9BEB4D9" + "61", // magic
                "7765736F6D", "650000", "000000" + "0A00", // awesome
                "00", "00" + "72BD", // length 10
                "2E30" + "22", // checksum
                "737486", "73128677", "AA", "BB" + "33", // payload
                "0099" + "F9", "F9BEB4D9" + "5555", // magic
                "5555", "440000000000", "0000" + "0800", // UUUUD
                "0000" + "5DF6", // length 8
                "E0E2" + "00", // incorrect checksum
                "0900", "88888888", "9090", "00" + "F9", // skipped payload
                "BEB4D9" + "55", // magic
                "5555", "55550000", "0000000000", // UUUUU
                "0000", "0000", // length 0
                "5DF6E0E2", // checksum
                "12" // no payload
        };
        new Thread(() -> {
            ServerSocket server = null;
            Socket socket = null;
            try {
                server = new ServerSocket(18333);
                socket = server.accept();
                OutputStream outputStream = socket.getOutputStream();
                for (String s : stream) {
                    outputStream.write(hexToBytes(s));
                    Thread.sleep(100);
                }
            } catch (InterruptedException | IOException e) {
                System.out.print(" cannot test...");
                e.printStackTrace();
            } finally {
                try {
                    if (socket != null) {
                        socket.close();
                    }
                    if (server != null) {
                        server.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        Socket socket = null;
        try {
            Thread.sleep(300);
            socket = new Socket("localhost", 18333);
            InputStream inputStream = socket.getInputStream();
            System.out.print(".");
            Message message = Message.read(inputStream);
            assertEquals("awesome", ((Message.Unknown) message).command);
            System.out.print(".");
            message = Message.read(inputStream, new String[]{"awesome", "UUUUU"});
            assertEquals("UUUUU", ((Message.Unknown) message).command);
            assertThrows(IOException.class, () -> Message.read(inputStream));
        } catch (IOException | InterruptedException e) {
            System.out.print(" cannot test...");
            e.printStackTrace();
        } finally {
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void read() {
        assertThrows(NullPointerException.class, () -> Message.read(null));
        String noMagic = "F9BEB4";
        assertThrows(IOException.class, () -> Message.read(new ByteArrayInputStream(hexToBytes(noMagic))));
        String noHeader = "F9BEB4D9" + "00000000000000000000";
        assertThrows(IOException.class, () -> Message.read(new ByteArrayInputStream(hexToBytes(noHeader))));
        String noPayload = "F9BEB4D9" + "000000000000000000000000" + "04000000" + "00000000";
        assertThrows(IOException.class, () -> Message.read(new ByteArrayInputStream(hexToBytes(noPayload))));
        String checksum = "F9BEB4D9" + "000000000000000000000000" + "04000000" + "00000000" + "AAAAAAAA";
        assertThrows(Message.Exception.class, () -> Message.read(new ByteArrayInputStream(hexToBytes(checksum))));
        String uuuuu = "F9BEB4D9" + "555555555500000000000000" + "00000000" + "5DF6E0E2";
        assertNotThrows(() -> Message.read(new ByteArrayInputStream(hexToBytes(uuuuu))));

        String[] commands = new String[]{"a", "UUUUU", "33"};
        assertThrows(NullPointerException.class, () -> Message.read(null, commands));
        assertThrows(
                NullPointerException.class,
                () -> Message.read(new ByteArrayInputStream(hexToBytes(checksum)), null)
        );
        assertThrows(IOException.class, () -> Message.read(new ByteArrayInputStream(hexToBytes(noMagic)), commands));
        assertThrows(IOException.class, () -> Message.read(new ByteArrayInputStream(hexToBytes(noHeader)), commands));
        String noPayload2 = "F9BEB4D9" + "555555555500000000000000" + "04000000" + "00000000";
        assertThrows(IOException.class, () -> Message.read(new ByteArrayInputStream(hexToBytes(noPayload2)), commands));
        String checksum2 = "F9BEB4D9" + "555555555500000000000000" + "04000000" + "00000000" + "AAAAAAAA";
        assertThrows(
                Message.Exception.class,
                () -> Message.read(new ByteArrayInputStream(hexToBytes(checksum2)), commands)
        );
        String skip = "F9BEB4D9" + "555555554400000000000000" + "04000000" + "00000000";
        assertThrows(IOException.class, () -> Message.read(new ByteArrayInputStream(hexToBytes(skip)), commands));
        String uuuuu2 = skip + "00" + skip +
                "F9BEB4D9" + "555555555500000000000000" + "00000000" + "5DF6E0E2";
        assertNotThrows(() -> {
            Message.Unknown u = (Message.Unknown) Message.read(new ByteArrayInputStream(hexToBytes(uuuuu2)), commands);
            if (!u.command.equals("UUUUU")) {
                throw new Exception("incorrect command");
            }
        });
    }

    public void write() {
        assertThrows(NullPointerException.class, () -> Message.toByteArray(null, new byte[5]));
        assertThrows(NullPointerException.class, () -> Message.toByteArray("cmd", null));
        assertThrows(Message.Exception.class, () -> Message.toByteArray("toobigmessage", new byte[5]));

        String input = "";
        assertEquals(
                "f9beb4d9" + "555555555500000000000000" + "00000000" + "5df6e0e2",
                Message.toByteArray("UUUUU", hexToBytes(input))
        );
        input = "2273748673128677AABB";
        assertEquals(
                "f9beb4d9" + "617765736f6d650000000000" + "0a000000" + "72bd2e302" + "273748673128677aabb",
                Message.toByteArray("awesome", hexToBytes(input))
        );
        input = "9975AA";
        assertEquals(
                "f9beb4d9" + "000000000000000000000000" + "03000000" + "bce4672a" + "9975aa",
                Message.toByteArray("", hexToBytes(input))
        );
    }
}