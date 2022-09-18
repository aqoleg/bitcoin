package com.aqoleg.messages.test;

import com.aqoleg.Test;
import com.aqoleg.messages.Addr;
import com.aqoleg.messages.Message;
import com.aqoleg.messages.NetAddress;

import java.util.Iterator;

@SuppressWarnings("unused")
public class AddrTest extends Test {

    public static void main(String[] args) {
        new AddrTest().testAll();
    }

    public void test() {
        assertThrows(NullPointerException.class, () -> new Addr(null));
        assertThrows(Message.Exception.class, () -> new Addr(new byte[]{(byte) 0xFE}));
        assertThrows(Message.Exception.class, () -> new Addr(new byte[]{0, 0}));

        Addr addr = new Addr(new byte[]{0});
        assertEquals("count: 0", addr.toString());
        assertTrue(!addr.getIterator().hasNext());
        assertNull(addr.getIterator().next());

        byte[] payload = hexToBytes("01" + "e215104d" + "0100000000000000"
                + "00000000000000000000FFFF0A000001" + "208D");
        addr = new Addr(payload);
        assertEquals(
                "count: 1, 0: (time: 2010-12-21T02:50:10Z, services: 1, ip: 10.0.0.1, port: 8333)",
                addr.toString()
        );
        Iterator<NetAddress> iterator = addr.getIterator();
        assertTrue(iterator.hasNext());
        NetAddress netAddress = iterator.next();
        assertEquals("time: 2010-12-21T02:50:10Z, services: 1, ip: 10.0.0.1, port: 8333", netAddress.toString());
        Iterator<NetAddress> iterator1 = addr.getIterator();
        assertTrue(!iterator.hasNext());
        assertNull(iterator.next());
        assertTrue(iterator1.hasNext());
        iterator1.next();
        assertTrue(!iterator.hasNext());
        assertNull(iterator.next());
        assertTrue(!iterator1.hasNext());
        assertNull(iterator1.next());

        payload = hexToBytes("02" + "e215104d" + "0100000000000000" + "00000000000000000000FFFF0A000001"
                + "208D" + "d91f4854" + "0100000000000000" + "00000000000000000000ffffc0000233" + "208d");
        addr = new Addr(payload);
        assertEquals("count: 2, 0: (time: 2010-12-21T02:50:10Z, services: 1, ip: 10.0.0.1, port: 8333), " +
                "1: (time: 2014-10-22T21:21:29Z, services: 1, ip: 192.0.2.51, port: 8333)", addr.toString());
        iterator = addr.getIterator();
        assertTrue(iterator.hasNext());
        assertEquals(8333, iterator.next().getPort());
        assertTrue(iterator.hasNext());
        assertEquals("time: 2014-10-22T21:21:29Z, services: 1, ip: 192.0.2.51, port: 8333", iterator.next().toString());
        assertTrue(!iterator.hasNext());
        assertNull(iterator.next());
    }
}