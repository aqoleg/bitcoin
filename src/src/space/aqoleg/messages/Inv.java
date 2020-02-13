// message containing data known to transmitting peer
//
// payload bytes:
//    varInt, count
//    count * byte[36], inventory
package space.aqoleg.messages;

import space.aqoleg.utils.BytesInput;
import space.aqoleg.utils.BytesOutput;

import java.util.Arrays;

public class Inv {
    public static final String command = "inv";
    public final int count;
    private final Inventory[] inventoryArray;

    /**
     * @param inventoryArray array with Inventories
     * @throws NullPointerException if inventoryArray == null
     */
    public Inv(Inventory[] inventoryArray) {
        count = inventoryArray.length;
        this.inventoryArray = Arrays.copyOf(inventoryArray, count);
    }

    /**
     * @param payload byte array with payload
     * @return instance of Inv created from payload
     * @throws NullPointerException          if payload == null
     * @throws IndexOutOfBoundsException     if payload is incorrect
     * @throws UnsupportedOperationException if payload is incorrect
     */
    public static Inv parse(byte[] payload) {
        BytesInput bytes = new BytesInput(payload);
        int count = (int) bytes.readVariableLengthInt();
        Inventory[] inventoryArray = new Inventory[count];
        for (int i = 0; i < count; i++) {
            inventoryArray[i] = Inventory.read(bytes);
        }
        if (bytes.available() != 0) {
            throw new UnsupportedOperationException("payload length is incorrect");
        }
        return new Inv(inventoryArray);
    }

    /**
     * @param inventoryN number of Inventory
     * @return Inventory with number inventoryN
     * @throws IndexOutOfBoundsException if inventoryN is incorrect
     */
    public Inventory getInventory(int inventoryN) {
        return inventoryArray[inventoryN];
    }

    /**
     * @param inventory Inventory which is searching in this Inv
     * @return true if this message contains inventory
     * @throws NullPointerException if inventory == null
     */
    public boolean hasInventory(Inventory inventory) {
        for (Inventory thisInventory : inventoryArray) {
            if (thisInventory.theSame(inventory)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return byte array with this Inv message
     */
    public byte[] toByteArray() {
        BytesOutput bytes = new BytesOutput();
        bytes.writeVariableLength(count);
        for (Inventory inventory : inventoryArray) {
            inventory.write(bytes);
        }
        return Message.toByteArray(command, bytes.toByteArray());
    }
}