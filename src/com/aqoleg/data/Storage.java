/*
all local files

~/.bitcoin.aqoleg.com/   - app directory
    addresses/   - node addresses directory
        new   - new loaded addresses
        prev   - previous loaded addresses
    blocks/   - blocks directory
        000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f   - block hash

usage:
    Storage.resetAddressesPosition();
    NetAddress netAddress = Storage.readNextAddress();
    Storage.writeAddress(netAddress);
    Block block = Storage.readBlock(blockHashString);
    Storage.writeBlock(blockHashString, block);
*/

package com.aqoleg.data;

import com.aqoleg.messages.Block;
import com.aqoleg.messages.Message;
import com.aqoleg.messages.NetAddress;
import com.aqoleg.utils.Converter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.StandardCopyOption;

public class Storage {
    private static final String genesisHash = "000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f";
    private static final String genesis = "01000000" // version
            + "0000000000000000000000000000000000000000000000000000000000000000" // prevBlock
            + "3BA3EDFD7A7B12B27AC72C3E67768F617FC81BC3888A51323A9FB8AA4B1E5E4A" // merkleRoot
            + "29AB5F49" // timestamp
            + "FFFF001D" // bits
            + "1DAC2B7C" // nonce
            + "01" // tx#
            + "01000000" // version
            + "01" // vin#
            + "0000000000000000000000000000000000000000000000000000000000000000" // previousTransactionHash
            + "FFFFFFFF" // previousOutIndex
            + "4D" // scriptSigLen
            + "04FFFF001D0104455468652054696D65732030332F4A616E2F32303039204368"
            + "616E63656C6C6F72206F6E206272696E6B206F66207365636F6E64206261696C6F757420666F722062616E6B73"
            + "FFFFFFFF" // sequence
            + "01" // vout#
            + "00F2052A01000000" // value
            + "43" // scriptPubKeyLen
            + "4104678AFDB0FE5548271967F1A67130B7105CD6A828E03909A67962E0EA1F61"
            + "DEB649F6BC3F4CEF38C4F35504E51EC112DE5C384DF7BA0B8D578A4C702B6BF11D5FAC"
            + "00000000"; // lockTime
    private static final int addressesCapacity = 30 * 500;
    private static boolean addressesSkipNew = false; // if true reads addresses from prevAddresses
    private static int addressesBytesSkip = -1; // if < 0 there are no addresses to read
    private static File root;
    private static File newAddresses;
    private static File prevAddresses;
    private static File blocks;

    /**
     * synchronize it outside
     * makes readNextAddress() return the first address
     */
    public static void resetAddressesPosition() {
        if (newAddresses == null) {
            initAddresses();
            return;
        }
        setAddressesPosition();
    }

    /**
     * synchronize it outside
     * reads bytes from addresses/new, then from addresses/prev
     *
     * @return NetAddress or null if there is no more addresses
     */
    public static NetAddress readNextAddress() {
        initAddresses();
        if (addressesSkipNew && addressesBytesSkip < 0) {
            return null;
        }
        try {
            FileInputStream inputStream = new FileInputStream(addressesSkipNew ? prevAddresses : newAddresses);
            if (inputStream.skip(addressesBytesSkip) != addressesBytesSkip) {
                throw new IOException("incorrect amount of skipped bytes");
            }
            addressesBytesSkip -= 30;
            if (!addressesSkipNew && addressesBytesSkip < 0) {
                addressesSkipNew = true;
                addressesBytesSkip = ((int) prevAddresses.length() / 30) * 30 - 30;
            }
            byte[] address = new byte[30];
            if (inputStream.read(address) != 30) {
                throw new IOException("incorrect amount of bytes read");
            }
            inputStream.close();
            try {
                return new NetAddress(address);
            } catch (Message.Exception exception) {
                exception.printStackTrace();
                return readNextAddress();
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    /**
     * synchronize it outside
     * writes NetAddress to addresses/new, if it became too big, moves it to addresses/prev
     * does not move the cursor
     *
     * @param address to write
     * @throws NullPointerException if address == null
     * @throws Message.Exception    if address length is incorrect
     */
    public static void writeAddress(NetAddress address) {
        initAddresses();
        try {
            FileOutputStream stream = new FileOutputStream(newAddresses, true);
            if (address.write(stream) != 30) {
                throw new Message.Exception("incorrect length");
            }
            stream.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        long length = newAddresses.length();
        if (length % 30 == 0 && length < addressesCapacity) {
            return;
        }
        try {
            Files.move(newAddresses.toPath(), prevAddresses.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        if (addressesSkipNew) {
            addressesBytesSkip = -1;
        } else {
            addressesSkipNew = true;
        }
    }

    /**
     * synchronize it outside
     * reads block from blocks/blockHash
     *
     * @param hash 64 lower-case hex symbols with reversed endianness
     * @return Block or null if there is no such block or it is incorrect
     * @throws NullPointerException if hash == null
     */
    public static Block readBlock(String hash) {
        initBlocks();
        File file = new File(blocks, hash);
        if (!file.isFile()) {
            return null;
        }
        try {
            return new Block(Files.readAllBytes(file.toPath()));
        } catch (IOException | Message.Exception exception) {
            exception.printStackTrace();
            try {
                Files.delete(file.toPath());
            } catch (IOException deleteException) {
                deleteException.printStackTrace();
            }
        }
        return null;
    }

    /**
     * synchronize it outside
     * writes Block to blocks/blockHash
     *
     * @param hash  64 lower-case hex symbols with reversed endianness
     * @param block to write, null to delete
     * @throws NullPointerException if hash == null
     */
    public static void writeBlock(String hash, Block block) {
        initBlocks();
        File file = new File(blocks, hash);
        try {
            if (block == null) {
                try {
                    Files.delete(file.toPath());
                } catch (NoSuchFileException ignored) {
                }
            } else {
                FileOutputStream stream = new FileOutputStream(file);
                block.write(stream);
                stream.close();
            }
        } catch (IOException exception) {
            exception.printStackTrace();
            try {
                Files.delete(file.toPath());
            } catch (IOException deleteException) {
                deleteException.printStackTrace();
            }
        }
    }

    private static void setAddressesPosition() {
        addressesSkipNew = false;
        addressesBytesSkip = ((int) newAddresses.length() / 30) * 30 - 30;
        if (addressesBytesSkip >= 0) {
            return;
        }
        addressesSkipNew = true;
        addressesBytesSkip = ((int) prevAddresses.length() / 30) * 30 - 30;
    }

    private static void initAddresses() {
        if (newAddresses != null) {
            return;
        }
        initRoot();
        File addresses = new File(root, "addresses");
        if (!addresses.isDirectory()) {
            if (!addresses.mkdirs()) {
                new IOException("can not create directory " + addresses.toString()).printStackTrace();
            }
        }
        newAddresses = new File(addresses, "new");
        prevAddresses = new File(addresses, "prev");
        setAddressesPosition();
    }

    private static void initBlocks() {
        if (blocks != null) {
            return;
        }
        initRoot();
        blocks = new File(root, "blocks");
        if (!blocks.isDirectory()) {
            if (!blocks.mkdirs()) {
                new IOException("can not create directory " + blocks.toString()).printStackTrace();
                return;
            }
        }
        // write genesis
        File file = new File(blocks, genesisHash);
        if (file.isFile()) {
            return;
        }
        try {
            Files.write(file.toPath(), Converter.hexToBytes(genesis));
        } catch (IOException exception) {
            exception.printStackTrace();
            try {
                Files.delete(file.toPath());
            } catch (IOException deleteException) {
                deleteException.printStackTrace();
            }
        }
    }

    private static void initRoot() {
        if (root != null) {
            return;
        }
        root = new File(System.getProperty("user.home"), ".bitcoin.aqoleg.com");
        if (!root.isDirectory()) {
            if (!root.mkdirs()) {
                new IOException("can not create directory " + root.toString()).printStackTrace();
            }
        }
    }

    public interface Writable {

        /**
         * @param outputStream strim to write in
         * @return number of bytes written
         * @throws IOException if cannot write
         */
        int write(OutputStream outputStream) throws IOException;
    }
}