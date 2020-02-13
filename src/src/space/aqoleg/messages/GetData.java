// this message requests data, as a response to Inv message containing unknown data
//
// payload bytes:
//    varInt, count
//    count * byte[36], inventory
package space.aqoleg.messages;

import space.aqoleg.utils.BytesInput;

public class GetData {
    public static final String command = "getdata";
    public final int count;
    private final BytesInput bytes;

    private GetData(int count, BytesInput bytes) {
        this.count = count;
        this.bytes = bytes;
    }

    /**
     * @param payload byte array with payload
     * @return instance of GetData created from payload
     * @throws NullPointerException          if payload == null
     * @throws IndexOutOfBoundsException     if payload is incorrect
     * @throws UnsupportedOperationException if payload is incorrect
     */
    public static GetData parse(byte[] payload) {
        BytesInput bytes = new BytesInput(payload);
        int count = (int) bytes.readVariableLengthInt();
        if (bytes.available() != count * 36) {
            throw new UnsupportedOperationException("payload length is incorrect");
        }
        return new GetData(count, bytes);
    }

    /**
     * @param inventory Inventory which is searching in this GetData
     * @return true if this message contains inventory
     * @throws NullPointerException if inventory == null
     */
    public boolean hasInventory(Inventory inventory) {
        boolean has = false;
        while (bytes.available() != 0) {
            if (Inventory.read(bytes).theSame(inventory)) {
                has = true;
                break;
            }
        }
        bytes.reset();
        bytes.readVariableLengthInt();
        return has;
    }
}