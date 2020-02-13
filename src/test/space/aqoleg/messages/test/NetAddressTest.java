package space.aqoleg.messages.test;

import org.junit.jupiter.api.Test;
import space.aqoleg.messages.NetAddress;
import space.aqoleg.utils.BytesInput;
import space.aqoleg.utils.BytesOutput;
import space.aqoleg.utils.Converter;

import static org.junit.jupiter.api.Assertions.*;

class NetAddressTest {

    @Test
    void test() {
        assertThrows(NullPointerException.class, () -> NetAddress.read(null, true));
        assertThrows(IndexOutOfBoundsException.class, () -> NetAddress.read(new BytesInput(new byte[10]), true));

        byte[] bytes = Converter.hexToBytes("d91f4854" + "0100000000000000" + "00000000000000000000ffffc0000233"
                + "208d");
        NetAddress netAddress = NetAddress.read(new BytesInput(bytes), false);
        assertEquals(1414012889, netAddress.time);
        assertEquals(1, netAddress.services);
        assertEquals(8333, netAddress.port);
        assertEquals("192.0.2.51 8333", netAddress.addressToString());
        BytesOutput bytesOutput = new BytesOutput();
        netAddress.write(bytesOutput, false);
        assertArrayEquals(bytes, bytesOutput.toByteArray());

        bytes = Converter.hexToBytes("0100000000000000" + "AA00BB00000000000000FFFF0A000001" + "208D");
        netAddress = NetAddress.read(new BytesInput(bytes), true);
        assertEquals(0, netAddress.time);
        assertEquals(1, netAddress.services);
        assertEquals(8333, netAddress.port);
        assertNull(netAddress.addressToString());
        bytesOutput = new BytesOutput();
        netAddress.write(bytesOutput, true);
        assertArrayEquals(bytes, bytesOutput.toByteArray());
        assertThrows(NullPointerException.class, () -> NetAddress.create("1.1.1.1", 1).write(null, true));

        assertEquals(0, NetAddress.empty.time);
        assertEquals(0, NetAddress.empty.services);
        assertEquals(0, NetAddress.empty.port);
        assertNull(NetAddress.empty.addressToString());

        assertThrows(NullPointerException.class, () -> NetAddress.create(null, 10));
        assertThrows(StringIndexOutOfBoundsException.class, () -> NetAddress.create("112.", 10));
        assertThrows(NumberFormatException.class, () -> NetAddress.create("1.2.3.j", 10));
        netAddress = NetAddress.create("1.12.123.0", 22342);
        bytesOutput = new BytesOutput();
        netAddress.write(bytesOutput, false);
        netAddress = NetAddress.read(new BytesInput(bytesOutput.toByteArray()), false);
        assertTrue(System.currentTimeMillis() / 1000 - netAddress.time < 2);
        assertEquals(0, netAddress.services);
        assertEquals(22342, netAddress.port);
        assertEquals("1.12.123.0 22342", netAddress.addressToString());
    }
}