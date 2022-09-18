package com.aqoleg.messages.test;

import com.aqoleg.Test;
import com.aqoleg.messages.Inv;
import com.aqoleg.messages.Inventory;
import com.aqoleg.messages.Message;

@SuppressWarnings("unused")
public class InvTest extends Test {

    public static void main(String[] args) {
        new InvTest().testAll();
    }

    public void test() {
        assertThrows(NullPointerException.class, () -> new Inv(null));
        assertThrows(Message.Exception.class, () -> new Inv(new byte[]{3, 3, 3}));
        assertThrows(Message.Exception.class, () -> new Inv(new byte[]{0, 3, 3}));

        String hash0 = "1122334455667788990011223344556677889900112233445566778899001122";
        String hash0Reversed = "2211009988776655443322110099887766554433221100998877665544332211";
        String hash1 = "00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff";
        String hash1Reversed = "ffeeddccbbaa99887766554433221100ffeeddccbbaa99887766554433221100";
        Inventory inventory0 = Inventory.create(1, hash0Reversed);
        Inventory inventory1 = Inventory.create(1, hash1Reversed);
        Inventory inventory2 = Inventory.create(2, hash1Reversed);

        Inv inv = new Inv();
        assertEquals("count: 0", inv.toString());
        assertEquals(0, inv.size());
        assertTrue(!inv.contains(inventory0));
        assertTrue(!inv.contains(inventory1));
        assertTrue(!inv.contains(null));
        assertThrows(NullPointerException.class, () -> inv.add(null));
        assertTrue(!inv.remove(inventory0));
        assertEquals("f9beb4d9696e76000000000000000000010000001406e058" + "00", inv.toByteArray());

        byte[] payload = hexToBytes("02" + "01000000" + hash0 + "01000000" + hash1);
        Inv inv2 = new Inv(payload);
        assertEquals(
                "count: 2, 0: (type: 1, hash: " + hash0Reversed + "), " + "1: (type: 1, hash: " + hash1Reversed + ")",
                inv2.toString()
        );
        assertEquals(2, inv2.size());
        assertTrue(inv2.contains(inventory0));
        assertTrue(inv2.contains(inventory1));
        assertTrue(!inv2.contains(inventory2));
        assertTrue(inv2.remove(inventory0));
        assertTrue(!inv2.remove(inventory0));
        assertEquals(1, inv2.size());
        assertTrue(!inv2.add(inventory1));
        assertTrue(inv2.remove(inventory1));
        assertTrue(inv2.add(inventory0));
        assertTrue(inv2.add(inventory1));
        assertTrue(inv2.add(inventory2));
        assertEquals(3, inv2.size());
        assertEquals(
                "f9beb4d9" + "696e76000000000000000000" + "6d000000" + "27ae3625" + "03" + "01000000" + hash0 +
                        "01000000" + hash1 + "02000000" + hash1,
                inv2.toByteArray()
        );
    }
}