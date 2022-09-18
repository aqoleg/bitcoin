/*
response to the Ping message, version >= 60001

usage:
    byte[] message = Pong.toByteArray(nonce);

payload bytes:
    longLE, nonce
*/

package com.aqoleg.messages;

import com.aqoleg.utils.BytesOutput;

public class Pong extends Message {
    public static final String command = "pong";

    private Pong() {
    }

    /**
     * @param nonce nonce from Ping message
     * @return byte array with Pong message
     */
    public static byte[] toByteArray(long nonce) {
        BytesOutput bytes = new BytesOutput();
        bytes.writeLongLE(nonce);
        return Message.toByteArray(command, bytes.toByteArray());
    }
}