// data structure used in Inv and GetData messages
//
// Inventory bytes:
//    intLE, type
//    byte[32], hash
package space.aqoleg.messages;

import space.aqoleg.utils.BytesInput;
import space.aqoleg.utils.BytesOutput;
import space.aqoleg.utils.Converter;

import java.util.Arrays;

public class Inventory {
    public static final int typeMsgTx = 1;
    public final int type;
    private final byte[] hash;

    private Inventory(int type, byte[] hash) {
        this.type = type;
        this.hash = hash;
    }

    /**
     * @param bytes BytesInput containing Inventory
     * @return Inventory read from bytes
     * @throws NullPointerException      if bytes == null
     * @throws IndexOutOfBoundsException if bytes are incorrect
     */
    public static Inventory read(BytesInput bytes) {
        int type = bytes.readIntLE();
        byte[] hash = new byte[32];
        bytes.readBytes(hash);
        return new Inventory(type, hash);
    }

    /**
     * @param type type of data
     * @param hash 32-bytes array with hash of data
     * @return new instance of Inventory
     * @throws NullPointerException          if hash == null
     * @throws UnsupportedOperationException if type is incorrect or hash.length != 32
     */
    public static Inventory create(int type, byte[] hash) {
        if (type < 0 || type > 4) {
            throw new UnsupportedOperationException("incorrect inventory type");
        }
        if (hash.length != 32) {
            throw new UnsupportedOperationException("incorrect inventory hash length");
        }
        return new Inventory(type, Arrays.copyOf(hash, 32));
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

    /**
     * @return hex String representation of the hash of this Inventory
     */
    public String hashToString() {
        return Converter.bytesToHex(hash, false, false);
    }

    /**
     * @param inventory to which will be compared this Inventory
     * @return true if inventory is the same as this Inventory
     * @throws NullPointerException if inventory == null
     */
    public boolean theSame(Inventory inventory) {
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
}