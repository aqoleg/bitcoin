package space.aqoleg.messages.test;

import org.junit.jupiter.api.Test;
import space.aqoleg.messages.Inv;
import space.aqoleg.messages.Inventory;
import space.aqoleg.utils.Converter;

import static org.junit.jupiter.api.Assertions.*;

class InvTest {

    @Test
    void test() {
        assertThrows(NullPointerException.class, () -> new Inv(null));
        assertThrows(NullPointerException.class, () -> Inv.parse(null));
        assertThrows(IndexOutOfBoundsException.class, () -> Inv.parse(new byte[]{3, 3, 3}));
        assertThrows(UnsupportedOperationException.class, () -> Inv.parse(new byte[]{0, 3, 3}));

        String hash0 = "1122334455667788990011223344556677889900112233445566778899001122";
        String hash1 = "00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff";
        Inventory inventory0 = Inventory.create(1, Converter.hexToBytes(hash0));
        Inventory inventory1 = Inventory.create(1, Converter.hexToBytes(hash1));
        Inventory inventory2 = Inventory.create(2, Converter.hexToBytes(hash1));
        assertThrows(
                NullPointerException.class,
                () -> Inv.parse(Converter.hexToBytes("02" + "01000000" + hash0 + "03000000" + hash1)).hasInventory(null)
        );

        Inv inv0 = Inv.parse(new byte[]{0});
        assertEquals(0, inv0.count);
        assertFalse(inv0.hasInventory(inventory0));
        assertFalse(inv0.hasInventory(inventory1));
        assertFalse(inv0.hasInventory(inventory2));
        assertThrows(IndexOutOfBoundsException.class, () -> inv0.getInventory(0));

        byte[] payload = Converter.hexToBytes("02" + "01000000" + hash0 + "03000000" + hash1);
        Inv inv = Inv.parse(payload);
        assertEquals(2, inv.count);
        assertEquals(hash0, inv.getInventory(0).hashToString());
        assertEquals(3, inv.getInventory(1).type);
        assertTrue(inv.hasInventory(inventory0));
        assertFalse(inv.hasInventory(inventory1));
        assertFalse(inv.hasInventory(inventory2));

        inv = new Inv(new Inventory[]{});
        assertEquals(0, inv.count);
        assertFalse(inv.hasInventory(inventory0));
        assertFalse(inv.hasInventory(inventory1));
        assertFalse(inv.hasInventory(inventory2));


        Inventory[] inventoryArray = new Inventory[]{inventory0, inventory1, inventory2};
        inv = new Inv(inventoryArray);
        inventoryArray[0] = null;
        assertEquals(3, inv.count);
        assertEquals(hash0, inv.getInventory(0).hashToString());
        assertEquals(hash1, inv.getInventory(1).hashToString());
        assertTrue(inv.hasInventory(inventory0));
        assertTrue(inv.hasInventory(inventory1));
        assertTrue(inv.hasInventory(inventory2));
        String output = Converter.bytesToHex(inv.toByteArray(), false, false);
        assertEquals("f9beb4d9" + "696e76000000000000000000" + "6d000000" + "27ae3625" + "03" + "01000000"
                + "1122334455667788990011223344556677889900112233445566778899001122" + "01000000"
                + "00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff" + "02000000"
                + "00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff", output);
    }
}