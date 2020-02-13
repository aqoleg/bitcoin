// response to the Ping message, version >= 60001
//
// payload bytes:
//    longLE, nonce
package space.aqoleg.messages;

import space.aqoleg.utils.BytesOutput;

public class Pong {

    private Pong() {
    }

    /**
     * @param nonce nonce from Ping message
     * @return byte array with Pong message
     */
    public static byte[] toByteArray(long nonce) {
        BytesOutput bytes = new BytesOutput();
        bytes.writeLongLE(nonce);
        return Message.toByteArray("pong", bytes.toByteArray());
    }
}