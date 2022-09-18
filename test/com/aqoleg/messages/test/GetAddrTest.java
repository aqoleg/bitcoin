package com.aqoleg.messages.test;

import com.aqoleg.Test;
import com.aqoleg.messages.GetAddr;

@SuppressWarnings("unused")
public class GetAddrTest extends Test {

    public static void main(String[] args) {
        new GetAddrTest().testAll();
    }

    public void test() {
        assertEquals(
                "f9beb4d9" + "676574616464720000000000" + "00000000" + "5df6e0e2",
                GetAddr.toByteArray()
        );
    }
}