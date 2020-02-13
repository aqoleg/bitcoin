package space.aqoleg.messages.test;

import org.junit.jupiter.api.Test;
import space.aqoleg.messages.Inventory;
import space.aqoleg.utils.BytesInput;
import space.aqoleg.utils.BytesOutput;
import space.aqoleg.utils.Converter;

import static org.junit.jupiter.api.Assertions.*;

class InventoryTest {

    @Test
    void test() {
        assertThrows(NullPointerException.class, () -> Inventory.read(null));
        assertThrows(IndexOutOfBoundsException.class, () -> Inventory.read(new BytesInput(new byte[35])));
        assertThrows(NullPointerException.class, () -> Inventory.create(1, null));
        assertThrows(UnsupportedOperationException.class, () -> Inventory.create(-1, new byte[32]));
        assertThrows(UnsupportedOperationException.class, () -> Inventory.create(1, new byte[31]));

        String hash0 = "de55ffd709ac1f5dc509a0925d0b1fc442ca034f224732e429081da1b621f55a";
        String hash1 = "de55ffd709ac1f5dc509a0925d0b1fc442ca034f224732e429081da1b621f55b";
        byte[] hash0bytes = Converter.hexToBytes(hash0);
        Inventory inventory0 = Inventory.create(Inventory.typeMsgTx, hash0bytes);
        Inventory inventory1 = Inventory.create(Inventory.typeMsgTx, Converter.hexToBytes(hash1));
        Inventory inventory2 = Inventory.create(2, Converter.hexToBytes(hash1));
        hash0bytes[0] = 0;
        assertEquals(Inventory.typeMsgTx, inventory0.type);
        assertEquals(hash0, inventory0.hashToString());
        assertEquals(Inventory.typeMsgTx, inventory1.type);
        assertEquals(hash1, inventory1.hashToString());
        assertEquals(2, inventory2.type);
        assertEquals(hash1, inventory2.hashToString());
        BytesOutput bytes = new BytesOutput();
        inventory0.write(bytes);
        assertThrows(NullPointerException.class, () -> inventory0.write(null));
        assertTrue(inventory0.theSame(Inventory.read(new BytesInput(bytes.toByteArray()))));
        assertTrue(inventory0.theSame(inventory0));
        assertTrue(inventory1.theSame(inventory1));
        assertTrue(inventory2.theSame(inventory2));
        assertFalse(inventory0.theSame(inventory1));
        assertFalse(inventory0.theSame(inventory2));
        assertFalse(inventory1.theSame(inventory0));
        assertFalse(inventory1.theSame(inventory2));
        assertFalse(inventory2.theSame(inventory0));
        assertFalse(inventory2.theSame(inventory1));
        assertThrows(NullPointerException.class, () -> inventory0.theSame(null));
    }
}