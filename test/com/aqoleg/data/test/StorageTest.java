package com.aqoleg.data.test;

import com.aqoleg.Test;
import com.aqoleg.data.Storage;
import com.aqoleg.messages.Block;
import com.aqoleg.messages.NetAddress;

@SuppressWarnings("unused")
public class StorageTest extends Test {

    public static void main(String[] args) {
        new StorageTest().testAll();
    }

    public void addresses() {
        NetAddress netAddress = Storage.readNextAddress();
        if (netAddress == null) {
            netAddress = NetAddress.empty;
        }
        Storage.readNextAddress();
        Storage.writeAddress(netAddress);
        Storage.readNextAddress();
        Storage.resetAddressesPosition();
        assertTrue(netAddress.equals(Storage.readNextAddress()));
        assertThrows(NullPointerException.class, () -> Storage.writeAddress(null));
    }

    public void blocks() {
        String hash = "000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f";
        assertThrows(NullPointerException.class, () -> Storage.readBlock(null));
        assertTrue(Storage.readBlock("nonExistedBlock") == null);
        Block block = Storage.readBlock(hash);
        assertTrue(block != null);
        //noinspection ConstantConditions
        assertEquals(hash, block.getHash());
        assertThrows(NullPointerException.class, () -> Storage.writeBlock(null, block));
        Storage.writeBlock("testBlock", block);
        Block testBlock = Storage.readBlock("testBlock");
        assertTrue(testBlock != null);
        //noinspection ConstantConditions
        assertEquals(hash, testBlock.getHash());
        Storage.writeBlock("testBlock", null);
        assertTrue(Storage.readBlock("testBlock") == null);
    }
}