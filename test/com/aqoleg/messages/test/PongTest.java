package com.aqoleg.messages.test;

import com.aqoleg.Test;
import com.aqoleg.messages.Pong;

@SuppressWarnings("unused")
public class PongTest extends Test {

    public static void main(String[] args) {
        new PongTest().testAll();
    }

    public void test() {
        assertEquals(
                "f9beb4d9" + "706f6e670000000000000000" + "08000000" + "88ea8176" + "0094102111e2af4d",
                Pong.toByteArray(0x4dafe21121109400L)
        );
    }
}