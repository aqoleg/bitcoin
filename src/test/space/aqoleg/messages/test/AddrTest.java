package space.aqoleg.messages.test;

import org.junit.jupiter.api.Test;
import space.aqoleg.messages.Addr;
import space.aqoleg.messages.NetAddress;
import space.aqoleg.utils.Converter;

import static org.junit.jupiter.api.Assertions.*;

class AddrTest {

    @Test
    void test() {
        assertThrows(NullPointerException.class, () -> Addr.parse(null));
        assertThrows(IndexOutOfBoundsException.class, () -> Addr.parse(new byte[]{(byte) 0xFE}));
        assertThrows(UnsupportedOperationException.class, () -> Addr.parse(new byte[]{0, 0}));

        Addr addr = Addr.parse(new byte[]{0});
        assertEquals(0, addr.count);
        assertFalse(addr.hasNext());
        assertNull(addr.getNext());

        byte[] payload = Converter.hexToBytes("01" + "e215104d" + "0100000000000000"
                + "00000000000000000000FFFF0A000001" + "208D");
        addr = Addr.parse(payload);
        assertEquals(1, addr.count);
        assertTrue(addr.hasNext());
        NetAddress netAddress = addr.getNext();
        assertEquals(0x4d1015e2, netAddress.time);
        assertEquals(1, netAddress.services);
        assertEquals("10.0.0.1 8333", netAddress.addressToString());
        assertFalse(addr.hasNext());
        assertNull(addr.getNext());
        addr.reset();
        assertEquals(1, addr.getNext().services);

        payload = Converter.hexToBytes("02" + "e215104d" + "0100000000000000" + "00000000000000000000FFFF0A000001"
                + "208D" + "d91f4854" + "0100000000000000" + "00000000000000000000ffffc0000233" + "208d");
        addr = Addr.parse(payload);
        assertEquals(2, addr.count);
        assertTrue(addr.hasNext());
        assertNotNull(addr.getNext());
        netAddress = addr.getNext();
        assertEquals(1414012889, netAddress.time);
        assertEquals(1, netAddress.services);
        assertEquals("192.0.2.51 8333", netAddress.addressToString());
        assertNull(addr.getNext());
        addr.reset();
        assertEquals(2, addr.count);
        assertEquals(0x4d1015e2, addr.getNext().time);
    }
}