package com.aqoleg.data.test;

import com.aqoleg.Test;
import com.aqoleg.data.BlockLoader;
import com.aqoleg.data.ConnectionManager;
import com.aqoleg.data.Storage;
import com.aqoleg.messages.Block;

@SuppressWarnings("unused")
public class BlockLoaderTest extends Test {

    public static void main(String[] args) {
        new BlockLoaderTest().testAll();
    }

    public void test() {
        String hash0 = "0000000000011bc2675148710038d131bad83cf6a3fb7da3f8ebff2c9cde68df";
        String hash1 = "000000000003b97cec3e714136796147fb7ac36cd18eba941dffe346b330e7e1";
        Block[] block = new Block[4];
        new Thread(() -> block[0] = BlockLoader.getBlock(hash0)).start();
        new Thread(() -> block[1] = BlockLoader.getBlock(hash1)).start();
        new Thread(() -> block[2] = BlockLoader.getBlock(hash0)).start();
        new Thread(() -> block[3] = BlockLoader.getBlock(hash0)).start();
        for (int i = 0; i < 100; i++) {
            System.out.print('.');
            try {
                Thread.sleep(800);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (block[0] != null && block[1] != null) {
                break;
            }
        }
        ConnectionManager.stop();
        Storage.writeBlock(hash0, null);
        Storage.writeBlock(hash1, null);
        boolean ok = block[0] != null && block[1] != null && block[2] != null && block[3] != null;
        assertTrue(ok);
        if (!ok) {
            System.out.println("timeout, cannot download blocks...");
            return;
        }
        assertEquals(134, block[0].getTx(0).getSize());
        assertEquals(7, block[1].txNumber());
        assertEquals(
                "1HsNLpKVDgFdxiN66STm9GeRCvgG5KyeoW",
                block[2].getTx(0).getTxOutput(0).getScriptPubKey().address.toString()
        );
        assertEquals(5000000000L, block[3].getTx(0).getTxOutput(0).value);
    }
}