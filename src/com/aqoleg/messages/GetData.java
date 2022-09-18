/*
requests data as a response to Inv message containing unknown data
not synchronized, synchronize it outside!

usage:
    GetData getData = (GetData) Message.read(inputStream);
    GetData getData = new GetData(bytes);
    GetData getData = new GetData();
    String string = getData.toString();
    boolean contains = getData.contains(inventory);
    int size = getData.size();
    boolean added = getData.add(inventory);
    boolean removed = getData.remove(inventory);
    byte[] message = getData.toByteArray();

payload bytes:
    varInt, count
    count * byte[36], Inventory
*/

package com.aqoleg.messages;

import com.aqoleg.utils.BytesInput;
import com.aqoleg.utils.BytesOutput;

import java.util.ArrayList;

public class GetData extends Message {
    public static final String command = "getdata";
    private final ArrayList<Inventory> inventories = new ArrayList<>();

    /**
     * no inventories
     */
    public GetData() {
    }

    /**
     * @param bytes byte array with payload
     * @throws NullPointerException if bytes == null
     * @throws Message.Exception    if bytes are incorrect
     */
    public GetData(byte[] bytes) {
        BytesInput bytesInput = new BytesInput(bytes);
        int count;
        try {
            count = (int) bytesInput.readVariableLengthInt();
        } catch (IndexOutOfBoundsException exception) {
            throw new Message.Exception(exception.getMessage());
        }
        while (count-- > 0) {
            inventories.add(Inventory.read(bytesInput));
        }
        if (bytesInput.available() != 0) {
            throw new Message.Exception("big payload length");
        }
    }

    /**
     * @return "count: 1, 0: (Inv.toString())"
     */
    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        int count = inventories.size();
        out.append("count: ").append(count);
        for (int i = 0; i < count; i++) {
            out.append(", ").append(i).append(": (").append(inventories.get(i)).append(')');
        }
        return out.toString();
    }

    /**
     * @param inventory Inventory which is searching in this GetData
     * @return true if this GetData contains inventory
     */
    public boolean contains(Inventory inventory) {
        return inventories.contains(inventory);
    }

    /**
     * @return number of inventories
     */
    public int size() {
        return inventories.size();
    }

    /**
     * @param inventory Inventory to be added to this GetData
     * @return false if this GetData already contains this inventory
     * @throws NullPointerException if inventory == null
     */
    public boolean add(Inventory inventory) {
        if (inventory == null) {
            throw new NullPointerException("null inventory");
        }
        if (contains(inventory)) {
            return false;
        }
        inventories.add(inventory);
        return true;
    }

    /**
     * @param inventory Inventory to be removed from this GetData
     * @return false if this GetData contains no such inventory
     */
    public boolean remove(Inventory inventory) {
        return inventories.remove(inventory);
    }

    /**
     * @return byte array with this Inv message
     */
    public byte[] toByteArray() {
        BytesOutput bytes = new BytesOutput();
        bytes.writeVariableLength(inventories.size());
        for (Inventory i : inventories) {
            i.write(bytes);
        }
        return Message.toByteArray(command, bytes.toByteArray());
    }
}