// byte[4], magic, {0xF9, 0xBE, 0xB4, 0xD9}
// header:
//    byte[12], command
//    intLE, length of payload
//    byte[4], checksum, first 4 bytes of sha256(sha256(payload))
// byte[length of payload], payload
package space.aqoleg.messages.test;

import org.junit.jupiter.api.Test;
import space.aqoleg.messages.Message;
import space.aqoleg.utils.Converter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MessageTest {

    @Test
    void readTest0() throws Exception {
        assertThrows(NullPointerException.class, () -> new Message().read(null));
        assertThrows(IOException.class, () -> new Message().read(new ByteArrayInputStream(Converter.hexToBytes(
                ""))));
        assertThrows(IOException.class, () -> new Message().read(new ByteArrayInputStream(Converter.hexToBytes(
                "F9BE"))));
        assertThrows(IOException.class, () -> new Message().read(new ByteArrayInputStream(Converter.hexToBytes(
                "F9BEB4D9"))));
        assertThrows(IOException.class, () -> new Message().read(new ByteArrayInputStream(Converter.hexToBytes(
                "F9BEB4D900000000000000000000"))));
        assertThrows(IOException.class, () -> new Message().read(new ByteArrayInputStream(Converter.hexToBytes(
                "F9BEB4D90000000000000000000000000400000000000000"))));
        assertThrows(IOException.class, () -> new Message().read(new ByteArrayInputStream(Converter.hexToBytes(
                "F9BEB4D90000000000000000000000000400000000000000AAAA"))));
        assertThrows(IOException.class, () -> new Message().read(new ByteArrayInputStream(Converter.hexToBytes(
                "F9BEB4D90000000000000000000000000400000000000000AAAAAA"))));
        assertThrows(UnsupportedOperationException.class, () -> new Message().finish());

        Message message = new Message();
        assertEquals("UUUUU", message.read(new ByteArrayInputStream(Converter.hexToBytes(
                "F9BEB4D9" + "555555555500000000000000" + "00000000" + "5DF6E0E1"))));
        assertThrows(UnsupportedOperationException.class, message::finish);
        message.reset();
        assertEquals("UUUUU", message.read(new ByteArrayInputStream(Converter.hexToBytes(
                "F9BEB4D9" + "555555555500000000000000" + "00000000" + "5DF6E0E2"))));
        assertEquals("UUUUU", message.read(new ByteArrayInputStream(Converter.hexToBytes("F9"))));
        assertEquals(4, message.getMagicPos());
        assertEquals("", Converter.bytesToHex(message.finish(), false, false));
        assertEquals(0, message.getMagicPos());
        assertEquals("", Converter.bytesToHex(message.getHeader(), false, false));
        assertEquals("", Converter.bytesToHex(message.getPayload(), false, false));
        assertEquals("UUUUU", message.read(new ByteArrayInputStream(Converter.hexToBytes(
                "F9BEB4D9" + "555555555500000000000000" + "00000000" + "5DF6E0E2"))));
        assertEquals(4, message.getMagicPos());
        assertEquals(
                "555555555500000000000000" + "00000000" + "5DF6E0E2",
                Converter.bytesToHex(message.getHeader(), false, true)
        );
        assertEquals("", Converter.bytesToHex(message.getPayload(), false, false));
        message.reset();
        assertEquals(0, message.getMagicPos());
        assertEquals("", Converter.bytesToHex(message.getHeader(), false, false));
        assertEquals("", Converter.bytesToHex(message.getPayload(), false, false));
        assertEquals("awesome", message.read(new ByteArrayInputStream(Converter.hexToBytes(
                "11F9BEB4D9" + "617765736F6D650000000000" + "0A000000" + "72BD2E30" + "2273748673128677AABB"))));
        assertEquals("2273748673128677AABB", Converter.bytesToHex(message.finish(), false, true));
        assertEquals("", message.read(new ByteArrayInputStream(Converter.hexToBytes(
                "F9BEB4D9" + "000000000000000000000000" + "03000000" + "BCE4672A" + "9975AA09"))));
        assertEquals("9975AA", Converter.bytesToHex(message.finish(), false, true));
    }

    @Test
    void readTest1() throws Exception {
        String[] inputArray = new String[]{"1984387487874883", "F98998F9", "BEB4", "BEB4F9BEB455F9", "BEF9BEB4",
                "BEF9BEB4F9F9", "BEB4D9" + "55", "5555555500000000000000" + "00", "000000" + "5DF6E0E2" + "F9"};
        int[] magicPosArray = new int[]{0, 1, 3, 1, 3, 1, 4, 4, 4};
        String[] headerArray = new String[]{"", "", "", "", "", "", "55", "555555555500000000000000" + "00",
                "555555555500000000000000" + "00000000" + "5DF6E0E2"};
        String[] payloadArray = new String[]{"", "", "", "", "", "", "", "", ""};
        test(inputArray, "UUUUU", magicPosArray, headerArray, payloadArray);

        inputArray = new String[]{"11F9BEB4D9", "617765736F6D650000000000" + "0A000000" + "72BD2E30",
                "2273748673128677AABB"};
        magicPosArray = new int[]{4, 4, 4};
        headerArray = new String[]{"", "617765736F6D650000000000" + "0A000000" + "72BD2E30",
                "617765736F6D650000000000" + "0A000000" + "72BD2E30"};
        payloadArray = new String[]{"", "", "2273748673128677AABB"};
        test(inputArray, "awesome", magicPosArray, headerArray, payloadArray);

        inputArray = new String[]{"", "F9BE", "F9BEB4D9", "000000000000000000000000", "03000000" + "BCE4672A",
                "9975AA09"};
        magicPosArray = new int[]{0, 2, 4, 4, 4, 4};
        headerArray = new String[]{"", "", "", "000000000000000000000000",
                "000000000000000000000000" + "03000000" + "BCE4672A",
                "000000000000000000000000" + "03000000" + "BCE4672A"};
        payloadArray = new String[]{"", "", "", "", "", "9975AA"};
        test(inputArray, "", magicPosArray, headerArray, payloadArray);
    }

    private void test(
            String[] inputArray,
            String command,
            int[] magicPosArray,
            String[] headerArray,
            String[] payloadArray
    ) throws Exception {
        Message message = new Message();
        new Thread(() -> {
            ServerSocket server = null;
            Socket socket = null;
            try {
                server = new ServerSocket(8333);
                socket = server.accept();
                OutputStream outputStream = socket.getOutputStream();
                for (String input : inputArray) {
                    outputStream.write(Converter.hexToBytes(input));
                    Thread.sleep(1000);
                }
            } catch (Exception e) {
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

        new Thread(() -> {
            Socket socket = null;
            try {
                Thread.sleep(500);
                socket = new Socket("localhost", 8333);
                message.read(socket.getInputStream());
            } catch (Exception e) {
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
        }).start();

        for (int i = 0; i < inputArray.length; i++) {
            Thread.sleep(1000);
            assertEquals(magicPosArray[i], message.getMagicPos());
            assertEquals(headerArray[i], Converter.bytesToHex(message.getHeader(), false, true));
            assertEquals(payloadArray[i], Converter.bytesToHex(message.getPayload(), false, true));
        }
        Thread.sleep(1500);
        assertEquals(command, message.read(new ByteArrayInputStream(new byte[0])));
    }

    @Test
    void writeTest() {
        assertThrows(NullPointerException.class, () -> Message.toByteArray(null, new byte[5]));
        assertThrows(NullPointerException.class, () -> Message.toByteArray("cmd", null));
        assertThrows(UnsupportedOperationException.class, () -> Message.toByteArray("toobigmessage", new byte[5]));

        String input = "";
        assertEquals(
                "F9BEB4D9" + "555555555500000000000000" + "00000000" + "5DF6E0E2",
                Converter.bytesToHex(Message.toByteArray("UUUUU", Converter.hexToBytes(input)), false, true)
        );
        input = "2273748673128677AABB";
        assertEquals(
                "F9BEB4D9" + "617765736F6D650000000000" + "0A000000" + "72BD2E302" + "273748673128677AABB",
                Converter.bytesToHex(Message.toByteArray("awesome", Converter.hexToBytes(input)), false, true)
        );
        input = "9975AA";
        assertEquals(
                "F9BEB4D9" + "000000000000000000000000" + "03000000" + "BCE4672A" + "9975AA",
                Converter.bytesToHex(Message.toByteArray("", Converter.hexToBytes(input)), false, true)
        );
    }
}