package space.aqoleg.messages.test;

import org.junit.jupiter.api.Test;
import space.aqoleg.messages.GetData;
import space.aqoleg.messages.Inventory;
import space.aqoleg.utils.Converter;

import static org.junit.jupiter.api.Assertions.*;

class GetDataTest {

    @Test
    void test() {
        assertThrows(NullPointerException.class, () -> GetData.parse(null));
        assertThrows(IndexOutOfBoundsException.class, () -> GetData.parse(new byte[]{(byte) 0xFE}));
        assertThrows(UnsupportedOperationException.class, () -> GetData.parse(new byte[]{4, 3, 3}));

        String hash0 = "1122334455667788990011223344556677889900112233445566778899001122";
        String hash1 = "00112233445566778899AABBCCDDEEFF00112233445566778899AABBCCDDEEFF";
        assertThrows(
                NullPointerException.class,
                () -> GetData.parse(Converter.hexToBytes("01" + "01000000" + hash0)).hasInventory(null)
        );
        Inventory inventory0 = Inventory.create(1, Converter.hexToBytes(hash0));
        Inventory inventory1 = Inventory.create(1, Converter.hexToBytes(hash1));
        Inventory inventory2 = Inventory.create(2, Converter.hexToBytes(hash1));

        GetData getData = GetData.parse(new byte[]{0});
        assertEquals(0, getData.count);
        assertFalse(getData.hasInventory(inventory0));
        assertFalse(getData.hasInventory(inventory1));
        assertFalse(getData.hasInventory(inventory2));

        byte[] payload = Converter.hexToBytes("02" + "01000000" + hash0 + "03000000" + hash1);
        getData = GetData.parse(payload);
        assertEquals(2, getData.count);
        assertTrue(getData.hasInventory(inventory0));
        assertFalse(getData.hasInventory(inventory1));
        assertFalse(getData.hasInventory(inventory2));

        payload = Converter.hexToBytes("03" + "01000000" + hash1 + "01000000" + hash0 + "02000000" + hash0);
        getData = GetData.parse(payload);
        assertEquals(3, getData.count);
        assertTrue(getData.hasInventory(inventory0));
        assertTrue(getData.hasInventory(inventory1));
        assertFalse(getData.hasInventory(inventory2));
        assertTrue(getData.hasInventory(inventory0));
    }
}
