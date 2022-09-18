package com.aqoleg.messages.test;

import com.aqoleg.Test;
import com.aqoleg.messages.Message;
import com.aqoleg.messages.Ping;

@SuppressWarnings("unused")
public class PingTest extends Test {

    public static void main(String[] args) {
        new PingTest().testAll();
    }

    public void test() {
        assertThrows(NullPointerException.class, () -> new Ping(null));
        assertThrows(Message.Exception.class, () -> new Ping(new byte[9]));
        assertEquals(0x4dafe21121109400L, new Ping(hexToBytes("0094102111e2af4d")).nonce);
        assertEquals(0, new Ping(new byte[0]).nonce);
    }
}