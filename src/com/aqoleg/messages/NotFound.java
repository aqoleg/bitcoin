/*
response to the GetData message if requested inventory is not found, version >= 70001

usage:
    NotFound notFound = (NotFound) Message.read(inputStream);
    NotFound notFound = new NotFound(bytes);
    String string = notFound.toString();
    boolean contains = notFound.contains(inventory);

payload bytes:
    varInt, count
    count * byte[36], Inventory
*/

package com.aqoleg.messages;

import com.aqoleg.utils.BytesInput;

public class NotFound extends Message {
    public static final String command = "notfound";
    private final byte[] bytes;

    /**
     * @param bytes byte array with payload
     * @throws NullPointerException if bytes == null
     * @throws Message.Exception    if bytes are incorrect
     */
    public NotFound(byte[] bytes) {
        BytesInput bytesInput = new BytesInput(bytes);
        int count;
        try {
            count = (int) bytesInput.readVariableLengthInt();
        } catch (IndexOutOfBoundsException exception) {
            throw new Message.Exception(exception.getMessage());
        }
        if (bytesInput.available() != count * 36) {
            throw new Message.Exception("incorrect length");
        }
        this.bytes = bytes;
    }

    /**
     * @return "count: 1, 0: (Inv.toString())"
     */
    @Override
    public String toString() {
        BytesInput bytesInput = new BytesInput(bytes);
        int count = (int) bytesInput.readVariableLengthInt();
        StringBuilder out = new StringBuilder();
        out.append("count: ").append(count);
        for (int i = 0; i < count; i++) {
            out.append(", ").append(i).append(": (").append(Inventory.read(bytesInput)).append(')');
        }
        return out.toString();
    }

    /**
     * @param inventory Inventory which is searching in this NotFound
     * @return true if this message contains inventory
     */
    public boolean contains(Inventory inventory) {
        BytesInput bytesInput = new BytesInput(bytes);
        int count = (int) bytesInput.readVariableLengthInt();
        while (count-- > 0) {
            if (Inventory.read(bytesInput).equals(inventory)) {
                return true;
            }
        }
        return false;
    }
}