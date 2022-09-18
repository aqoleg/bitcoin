/*
requests Addr message

usage:
    byte[] message = GetAddr.toByteArray();

no payload
*/

package com.aqoleg.messages;

public class GetAddr extends Message {
    public static final String command = "getaddr";

    private GetAddr() {
    }

    /**
     * @return byte array with GetAddr message
     */
    public static byte[] toByteArray() {
        return Message.toByteArray(command, new byte[0]);
    }
}