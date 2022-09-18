package com.aqoleg.messages.test;

import com.aqoleg.Test;
import com.aqoleg.messages.Inventory;
import com.aqoleg.messages.Message;
import com.aqoleg.utils.BytesInput;
import com.aqoleg.utils.BytesOutput;

@SuppressWarnings("unused")
public class InventoryTest extends Test {

    public static void main(String[] args) {
        new InventoryTest().testAll();
    }

    @SuppressWarnings("EqualsWithItself")
    public void test() {
        assertThrows(NullPointerException.class, () -> Inventory.read(null));
        assertThrows(Message.Exception.class, () -> Inventory.read(new BytesInput(new byte[35])));
        assertThrows(NullPointerException.class, () -> Inventory.create(1, null));
        String hash0 = "de55ffd709ac1f5dc509a0925d0b1fc442ca034f224732e429081da1b621f55a";
        assertThrows(Message.Exception.class, () -> Inventory.create(-1, hash0));
        assertThrows(Message.Exception.class, () -> Inventory.create(1, "ik"));

        String hash1 = "de55ffd709ac1f5dc509a0925d0b1fc442ca034f224732e429081da1b621f55b";
        Inventory inventory0 = Inventory.create(Inventory.typeMsgTx, hash0);
        Inventory inventory1 = Inventory.create(Inventory.typeMsgTx, hash1);
        Inventory inventory2 = Inventory.create(2, hash1);
        assertEquals(Inventory.typeMsgTx, inventory0.type);
        assertEquals("type: 1, hash: " + hash0, inventory0.toString());
        assertEquals(hash0, inventory0.getHash());
        assertEquals(Inventory.typeMsgTx, inventory1.type);
        assertEquals("type: 1, hash: " + hash1, inventory1.toString());
        assertEquals(2, inventory2.type);
        assertEquals(hash1, inventory2.getHash());
        assertEquals("type: 2, hash: " + hash1, inventory2.toString());
        BytesOutput bytes = new BytesOutput();
        inventory0.write(bytes);
        assertThrows(NullPointerException.class, () -> inventory0.write(null));
        assertTrue(inventory0.equals(Inventory.read(new BytesInput(bytes.toByteArray()))));
        assertTrue(inventory0.equals(inventory0));
        assertTrue(inventory1.equals(inventory1));
        assertTrue(inventory2.equals(inventory2));
        assertTrue(!inventory0.equals(inventory1));
        assertTrue(!inventory0.equals(inventory2));
        assertTrue(!inventory1.equals(inventory0));
        assertTrue(!inventory1.equals(inventory2));
        assertTrue(!inventory2.equals(inventory0));
        assertTrue(!inventory2.equals(inventory1));
        //noinspection ObjectEqualsNull
        assertTrue(!inventory0.equals(null));
    }
}