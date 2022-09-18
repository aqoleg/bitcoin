/*
message to confirm that peer is still connected

usage:
    Ping ping = (Ping) Message.read(inputStream);
    Ping ping = new Ping(bytes);
    long nonce = ping.nonce;

payload bytes:
    if version >= 60001, longLE, nonce
*/

package com.aqoleg.messages;

import com.aqoleg.utils.BytesInput;

public class Ping extends Message {
    public static final String command = "ping";
    public final long nonce;

    /**
     * @param bytes byte array with payload
     * @throws NullPointerException if bytes == null
     * @throws Message.Exception    if bytes are incorrect
     */
    public Ping(byte[] bytes) {
        if (bytes.length != 0) {
            BytesInput bytesInput = new BytesInput(bytes);
            try {
                nonce = bytesInput.readLongLE();
            } catch (IndexOutOfBoundsException exception) {
                throw new Message.Exception(exception.getMessage());
            }
            if (bytesInput.available() != 0) {
                throw new Message.Exception("big length");
            }
        } else {
            nonce = 0;
        }
    }
}