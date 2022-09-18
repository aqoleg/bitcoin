/*
loads blocks from the memory or nodes; synchronized

usage:
    $ java com.aqoleg.data.BlockLoader 000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f

    Block block = BlockLoader.getBlock(blockHashString);
    boolean hasData = BlockLoader.hasDataToDownload();
    BlockLoader.writeGetData(outputStream);
    BlockLoader.onBlockReceive(blockReceived);
*/

package com.aqoleg.data;

import com.aqoleg.messages.Block;
import com.aqoleg.messages.GetData;
import com.aqoleg.messages.Inventory;
import com.aqoleg.messages.Message;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class BlockLoader {
    private static final ArrayList<String> hashes = new ArrayList<>(); // synchronized and wait/notify object
    private static final HashMap<String, Block> blocks = new HashMap<>();
    private static final GetData getData = new GetData(); // blocks to download

    /**
     * prints info about block with entered hash
     *
     * @param args String block hash or empty
     */
    public static void main(String[] args) {
        if (args.length == 1) {
            try {
                System.out.println(getBlock(args[0]));
            } catch (Message.Exception exception) {
                System.out.println(exception.getMessage());
            }
        } else if (System.console() == null) {
            System.out.println("use with hash");
        } else {
            while (true) {
                String input = System.console().readLine("enter block hash or 'exit': ");
                if (input.equals("exit")) {
                    ConnectionManager.stop();
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException exception) {
                        exception.printStackTrace();
                    }
                    System.exit(0);
                }
                try {
                    System.out.println(getBlock(input));
                } catch (Message.Exception exception) {
                    System.out.println(exception.getMessage());
                }
            }
        }
    }

    /**
     * this method blocks the thread
     * if there is no block in the storage, it downloads it from nodes and saves it
     *
     * @param hash 64 lower-case hex symbols with reversed endianness
     * @return Block with this hash
     * @throws NullPointerException if hash == null
     * @throws Message.Exception    if hash is incorrect
     */
    public static Block getBlock(String hash) {
        synchronized (hashes) {
            Block block = Storage.readBlock(hash);
            if (block != null) {
                return block;
            }

            Inventory inventory = Inventory.create(Inventory.typeMsgBlock, hash); // throws if hash is incorrect
            hash = inventory.getHash(); // normalize
            hashes.add(hash);
            getData.add(inventory);
            while (true) {
                block = blocks.get(hash);
                if (block != null) {
                    if (!hashes.remove(hash)) {
                        new RuntimeException().printStackTrace();
                    }
                    if (!hashes.contains(hash)) {
                        Storage.writeBlock(hash, block);
                        blocks.remove(hash);
                        if (!getData.remove(inventory)) {
                            new RuntimeException().printStackTrace();
                        }
                    }
                    return block;
                }
                ConnectionManager.downloadBlock();
                try {
                    hashes.wait();
                } catch (InterruptedException exception) {
                    exception.printStackTrace();
                }
            }
        }
    }

    /**
     * @return true if there are blocks to download
     */
    static boolean hasDataToDownload() {
        synchronized (hashes) {
            return getData.size() > 0;
        }
    }

    /**
     * writes GetData message
     *
     * @param outputStream to write message in
     * @throws IOException if cannot write
     */
    static void writeGetData(OutputStream outputStream) throws IOException {
        synchronized (hashes) {
            if (getData.size() > 0) {
                outputStream.write(getData.toByteArray());
            }
        }
    }

    /**
     * @param block new Block has been received
     */
    static void onBlockReceive(Block block) {
        synchronized (hashes) {
            String hash = block.getHash();
            if (!hashes.contains(hash)) {
                return;
            }
            if (!blocks.containsKey(hash)) {
                blocks.put(hash, block);
            }
            hashes.notifyAll();
        }
    }
}