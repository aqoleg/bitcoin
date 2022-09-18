package com.aqoleg.messages.test;

import com.aqoleg.Test;
import com.aqoleg.messages.Message;
import com.aqoleg.messages.Reject;

@SuppressWarnings("unused")
public class RejectTest extends Test {

    public static void main(String[] args) {
        new RejectTest().testAll();
    }

    public void test() {
        assertThrows(NullPointerException.class, () -> new Reject(null));
        assertThrows(Message.Exception.class, () -> new Reject(new byte[]{22, 0}));
        String input = "02" + "7478" + "12" + "15" + "6261642d74786e732d696e707574732d7370656e74"
                + "394715fcab51093be7bfca5a31005972947baf86a31017939575fb2354222821";
        String output = "message: tx, ccode: 18, reason: bad-txns-inputs-spent, "
                + "data: 394715fcab51093be7bfca5a31005972947baf86a31017939575fb2354222821";
        assertEquals(output, new Reject(hexToBytes(input)).toString());
    }
}