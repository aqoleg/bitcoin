package com.aqoleg.messages.test;

import com.aqoleg.Test;
import com.aqoleg.messages.Inventory;
import com.aqoleg.messages.Message;
import com.aqoleg.messages.NotFound;

@SuppressWarnings("unused")
public class NotFoundTest extends Test {

    public static void main(String[] args) {
        new NotFoundTest().testAll();
    }

    public void test() {
        assertThrows(NullPointerException.class, () -> new NotFound(null));
        assertThrows(Message.Exception.class, () -> new NotFound(new byte[]{3, 3, 3}));
        assertThrows(Message.Exception.class, () -> new NotFound(new byte[]{0, 3, 3}));

        String hash0 = "1122334455667788990011223344556677889900112233445566778899001122";
        String hash1 = "00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff";
        Inventory inventory0 = Inventory.create(1, hash0);
        Inventory inventory1 = Inventory.create(1, hash1);
        Inventory inventory2 = Inventory.create(2, hash1);
        byte[] bytes = hexToBytes("01" + "01000000" +
                "2211009988776655443322110099887766554433221100998877665544332211");
        assertTrue(!new NotFound(bytes).contains(null));
        assertTrue(new NotFound(bytes).contains(inventory0));

        byte[] payload = hexToBytes("02" + "01000000" +
                "2211009988776655443322110099887766554433221100998877665544332211" + "03000000" +
                "ffeeddccbbaa99887766554433221100ffeeddccbbaa99887766554433221100");
        NotFound notFound = new NotFound(payload);
        assertEquals(
                "count: 2, 0: (type: 1, hash: " + hash0 + "), " + "1: (type: 3, hash: " + hash1 + ")",
                notFound.toString()
        );
        assertTrue(notFound.contains(inventory0));
        assertTrue(!notFound.contains(inventory1));
        assertTrue(!notFound.contains(inventory2));
    }
}