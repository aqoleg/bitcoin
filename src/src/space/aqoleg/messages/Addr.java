// message with addresses of nodes
//
// payload bytes:
//    varInt, count
//    count * byte[30], addrList, addresses of other nodes
package space.aqoleg.messages;

import space.aqoleg.utils.BytesInput;

public class Addr {
    public static final String command = "addr";
    public final int count;
    private final BytesInput bytes;

    private Addr(int count, BytesInput bytes) {
        this.count = count;
        this.bytes = bytes;
    }

    /**
     * @param payload byte array with payload
     * @return instance of Addr created from payload
     * @throws NullPointerException          if payload == null
     * @throws IndexOutOfBoundsException     if payload is incorrect
     * @throws UnsupportedOperationException if payload is incorrect
     */
    public static Addr parse(byte[] payload) {
        BytesInput bytes = new BytesInput(payload);
        int count = (int) bytes.readVariableLengthInt();
        if (bytes.available() != count * 30) {
            throw new UnsupportedOperationException("payload length is incorrect");
        }
        return new Addr(count, bytes);
    }

    // Iterator-style methods

    /**
     * @return true if there is next NetAddress which getNext() return
     */
    public boolean hasNext() {
        return bytes.available() != 0;
    }

    /**
     * @return next NetAddress or null if this is the last NetAddress
     */
    public NetAddress getNext() {
        if (bytes.available() == 0) {
            return null;
        }
        return NetAddress.read(bytes, false);
    }

    /**
     * go to the first NetAddress
     */
    public void reset() {
        bytes.reset();
        bytes.readVariableLengthInt();
    }
}