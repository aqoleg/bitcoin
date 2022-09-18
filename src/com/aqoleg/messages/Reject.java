/*
response to one of the previous messages, version >= 70002

usage:
    Reject reject = (Reject) Message.read(inputStream);
    Reject reject = new Reject(bytes);
    String string = reject.toString();

payload bytes:
    varString, message
    1 byte, ccode
    varString, reason
    byte[] data
*/

package com.aqoleg.messages;

import com.aqoleg.utils.BytesInput;
import com.aqoleg.utils.Converter;

public class Reject extends Message {
    public static final String command = "reject";
    private final String string;

    /**
     * @param bytes byte array with payload
     * @throws NullPointerException if bytes == null
     * @throws Message.Exception    if bytes are incorrect
     */
    public Reject(byte[] bytes) {
        StringBuilder out = new StringBuilder();
        BytesInput bytesInput = new BytesInput(bytes);
        try {
            out.append("message: ").append(bytesInput.readVariableLengthString());
            out.append(", ccode: ").append(bytesInput.read());
            out.append(", reason: ").append(bytesInput.readVariableLengthString());
        } catch (IndexOutOfBoundsException exception) {
            throw new Message.Exception(exception.getMessage());
        }
        byte[] data = bytesInput.readBytes(new byte[bytesInput.available()]);
        out.append(", data: ").append(Converter.bytesToHex(data, false, false));
        string = out.toString();
    }

    /**
     * @return "message: message, ccode: 1, reason: reason, data: 1"
     */
    @Override
    public String toString() {
        return string;
    }
}