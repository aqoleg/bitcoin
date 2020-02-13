// response to one of the previous messages, version >= 70002
//
// payload bytes:
//    varString, message
//    1 byte, ccode
//    varString, reason
//    byte[] data
package space.aqoleg.messages;

import space.aqoleg.utils.BytesInput;
import space.aqoleg.utils.Converter;

public class Reject {
    public static final String command = "reject";

    /**
     * @param payload byte array with payload
     * @return String representation of this Reject
     * @throws NullPointerException      if payload == null
     * @throws IndexOutOfBoundsException if payload is incorrect
     */
    public static String toString(byte[] payload) {
        StringBuilder out = new StringBuilder();
        BytesInput bytes = new BytesInput(payload);
        out.append("message: ").append(bytes.readVariableLengthString());
        out.append(", ccode: 0x").append(String.format("%02x", bytes.read()));
        out.append(", reason: ").append(bytes.readVariableLengthString());
        byte[] data = new byte[bytes.available()];
        bytes.readBytes(data);
        out.append(", data: ").append(Converter.bytesToHex(data, false, false));
        return out.toString();
    }
}