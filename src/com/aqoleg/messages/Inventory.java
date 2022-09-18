/*
data structure used in Inv, GetData and NotFound messages

usage:
    Inventory inventory = Inventory.read(bytesInput);
    Inventory inventory = Inventory.create(Inventory.typeMsgTx, hashBytes);
    int type = inventory.type;
    String string = inventory.toString();
    String hash = inventory.getHash();
    boolean equals = inventory.equals(otherInventory);
    inventory.write(bytesOutput);

Inventory bytes:
    intLE, type
    byte[32], hash
*/

package com.aqoleg.messages;

import com.aqoleg.utils.BytesInput;
import com.aqoleg.utils.BytesOutput;
import com.aqoleg.utils.Converter;

public class Inventory {
    public static final int typeMsgTx = 1;
    public static final int typeMsgBlock = 2;
    public final int type;
    private final byte[] hash;

    private Inventory(int type, byte[] hash) {
        this.type = type;
        this.hash = hash;
    }

    /**
     * @param bytes BytesInput containing Inventory
     * @return Inventory read from bytes
     * @throws NullPointerException if bytes == null
     * @throws Message.Exception    if bytes are incorrect
     */
    public static Inventory read(BytesInput bytes) {
        try {
            int type = bytes.readIntLE();
            byte[] hash = bytes.readBytes(new byte[32]);
            return new Inventory(type, hash);
        } catch (IndexOutOfBoundsException exception) {
            throw new Message.Exception(exception.getMessage());
        }
    }

    /**
     * @param type type of data
     * @param hash reversed 32-bytes hex hash of data
     * @return Inventory with this type and hash
     * @throws NullPointerException if hash == null
     * @throws Message.Exception    if type is incorrect or hash is incorrect
     */
    public static Inventory create(int type, String hash) {
        if (type < 0 || type > 4) {
            throw new Message.Exception("incorrect inventory type");
        }
        byte[] hashBytes;
        try {
            hashBytes = Converter.hexToBytesReverse(hash);
        } catch (Converter.Exception exception) {
            throw new Message.Exception(exception.getMessage());
        }
        if (hashBytes.length != 32) {
            throw new Message.Exception("incorrect length");
        }
        return new Inventory(type, hashBytes);
    }

    /**
     * @return "type: 1, hash: 5a..de"
     * hash is lower case, hex, reversed
     */
    @Override
    public String toString() {
        return "type: " + type + ", hash: " + getHash();
    }

    /**
     * @param object object to be compared with
     * @return true if type and hash are the same
     */
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Inventory)) {
            return false;
        }
        Inventory inventory = (Inventory) object;
        if (type != inventory.type) {
            return false;
        }
        for (int i = 0; i < 32; i++) {
            if (hash[i] != inventory.hash[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return lower case, hex, reversed hash
     */
    public String getHash() {
        return Converter.bytesToHexReverse(hash, false, false);
    }

    /**
     * write this Inventory into the BytesOutput
     *
     * @param bytes BytesOutput in which will be written this Inventory
     * @throws NullPointerException if bytes == null
     */
    public void write(BytesOutput bytes) {
        bytes.writeIntLE(type);
        bytes.writeBytes(hash);
    }
}