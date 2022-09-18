package com.aqoleg.messages.test;

import com.aqoleg.Test;
import com.aqoleg.messages.Message;
import com.aqoleg.messages.VerAck;

@SuppressWarnings("unused")
public class VerAckTest extends Test {

    public static void main(String[] args) {
        new VerAckTest().testAll();
    }

    public void test() {
        assertThrows(NullPointerException.class, () -> new VerAck(null));
        assertThrows(Message.Exception.class, () -> new VerAck(new byte[]{2}));
        assertNotThrows(() -> new VerAck(new byte[]{}));
        assertEquals(
                "f9beb4d9" + "76657261636b000000000000" + "00000000" + "5df6e0e2",
                VerAck.toByteArray()
        );
    }
}