// message to confirm that peer is still connected
//
// payload bytes:
//    if version >= 60001, longLE, nonce
package space.aqoleg.messages;

import space.aqoleg.utils.BytesInput;

public class Ping {
    public static final String command = "ping";

    /**
     * @param payload byte array with payload
     * @return nonce or zero if version < 60001
     * @throws NullPointerException          if payload == null
     * @throws IndexOutOfBoundsException     if payload is incorrect
     * @throws UnsupportedOperationException if payload is incorrect
     */
    public static long getNonce(byte[] payload) {
        long nonce = 0;
        if (payload.length != 0) {
            BytesInput bytes = new BytesInput(payload);
            nonce = bytes.readLongLE();
            if (bytes.available() != 0) {
                throw new UnsupportedOperationException("payload length is incorrect");
            }
        }
        return nonce;
    }
}