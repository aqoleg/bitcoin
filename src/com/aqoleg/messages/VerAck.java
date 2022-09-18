/*
the second message, response to the VerAck message

usage:
    VerAck verAck = (VerAck) Message.read(inputStream);
    VerAck verAck = new VerAck(bytes);
    byte[] message = VerAck.toByteArray();

no payload
*/

package com.aqoleg.messages;

public class VerAck extends Message {
    public static final String command = "verack";

    /**
     * @param bytes byte array with payload
     * @throws NullPointerException if bytes == null
     * @throws Message.Exception    if bytes are incorrect
     */
    public VerAck(byte[] bytes) {
        if (bytes.length != 0) {
            throw new Message.Exception("big length");
        }
    }

    /**
     * @return byte array with VerAck message
     */
    public static byte[] toByteArray() {
        return Message.toByteArray(command, new byte[0]);
    }
}