package com.aqoleg.messages.test;

import com.aqoleg.Test;
import com.aqoleg.messages.GetData;
import com.aqoleg.messages.Inventory;
import com.aqoleg.messages.Message;

@SuppressWarnings("unused")
public class GetDataTest extends Test {

    public static void main(String[] args) {
        new GetDataTest().testAll();
    }

    public void test() {
        assertThrows(NullPointerException.class, () -> new GetData(null));
        assertThrows(Message.Exception.class, () -> new GetData(new byte[]{(byte) 0xFE}));
        assertThrows(Message.Exception.class, () -> new GetData(new byte[]{4, 3, 3}));

        String hash0 = "1122334455667788990011223344556677889900112233445566778899001122";
        String hash0Reversed = "2211009988776655443322110099887766554433221100998877665544332211";
        String hash1 = "00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff";
        String hash1Reversed = "ffeeddccbbaa99887766554433221100ffeeddccbbaa99887766554433221100";
        Inventory inventory0 = Inventory.create(1, hash0Reversed);
        Inventory inventory1 = Inventory.create(1, hash1Reversed);
        Inventory inventory2 = Inventory.create(2, hash1Reversed);

        GetData getData = new GetData(new byte[]{0});
        assertEquals(0, getData.size());
        assertTrue(!getData.contains(inventory0));
        assertTrue(!getData.contains(inventory1));
        assertTrue(!getData.contains(inventory2));

        byte[] payload = hexToBytes("02" + "01000000" + hash0 + "03000000" + hash1);
        getData = new GetData(payload);
        assertEquals(2, getData.size());
        assertTrue(getData.contains(inventory0));
        assertTrue(!getData.contains(inventory1));
        assertTrue(!getData.contains(inventory2));

        payload = hexToBytes("03" + "01000000" + hash1 + "01000000" + hash0 + "02000000" + hash0);
        getData = new GetData(payload);
        assertEquals(3, getData.size());
        assertTrue(getData.contains(inventory0));
        assertTrue(getData.contains(inventory1));
        assertTrue(!getData.contains(inventory2));
        assertTrue(getData.contains(inventory0));
        assertTrue(getData.remove(inventory0));
        assertTrue(!getData.remove(inventory0));
        assertTrue(!getData.contains(inventory0));

        getData = new GetData();
        assertTrue(getData.add(inventory0));
        assertTrue(!getData.add(inventory0));
        assertTrue(getData.add(inventory1));
        assertTrue(getData.add(inventory2));
        assertEquals(3, getData.size());
        assertTrue(getData.contains(inventory0));
        assertTrue(!getData.contains(Inventory.create(1, bytesToHex(new byte[32]))));
        assertEquals(
                "f9beb4d9" + "676574646174610000000000" + "6d000000" + "27ae3625" + "03" + "01000000" + hash0
                        + "01000000" + hash1 + "02000000" + hash1,
                getData.toByteArray()
        );
    }
}