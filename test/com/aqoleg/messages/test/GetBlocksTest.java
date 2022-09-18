package com.aqoleg.messages.test;

import com.aqoleg.Test;
import com.aqoleg.messages.GetBlocks;
import com.aqoleg.messages.Message;

@SuppressWarnings("unused")
public class GetBlocksTest extends Test {

    public static void main(String[] args) {
        new GetBlocksTest().testAll();
    }

    public void test() {
        assertThrows(NullPointerException.class, () -> GetBlocks.create(1, null, ""));
        assertThrows(NullPointerException.class, () -> GetBlocks.create(1, new String[]{null}, ""));
        assertThrows(Message.Exception.class, () -> GetBlocks.create(1, new String[]{"String"}, null));
        String hash1 = "00000000000000001bd3146aa1555e10b23b63e6d484987237b575778a609fd3";
        String hash2 = "00000000000000000aea3be27cda4b71011c2b60fb8a2e0a113708d403643e5c";
        GetBlocks getBlocks = GetBlocks.create(70001, new String[]{hash1, hash2}, null);
        assertEquals(
                "version: 70001, hashes: 2, 0: 00000000000000001bd3146aa1555e10b23b63e6d484987237b575778a609fd3, "
                        + "1: 00000000000000000aea3be27cda4b71011c2b60fb8a2e0a113708d403643e5c, "
                        + "stopHash: 0000000000000000000000000000000000000000000000000000000000000000",
                getBlocks.toString()
        );
        assertEquals(
                "f9beb4d9676574626c6f636b7300000065000000452a4648" + "71110100" + "02" +
                        "d39f608a7775b537729884d4e6633bb2105e55a16a14d31b0000000000000000" +
                        "5c3e6403d40837110a2e8afb602b1c01714bda7ce23bea0a0000000000000000" +
                        "0000000000000000000000000000000000000000000000000000000000000000",
                getBlocks.toByteArray()
        );
    }
}