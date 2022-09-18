/*
alert system message, 311 <= version < 70013

usage:
    Alert alert = (Alert) Message.read(inputStream);
    Alert alert = new Alert(bytes);
    String string = alert.toString();

payload bytes:
    varInt, alertPayloadLength
    byte[alertPayloadLength], alertPayload:
        intLE, version
        longLE, relayUntil
        longLE, expiration
        intLE, id
        intLE, cancel
        varInt, setCancelLength
        setCancelLength * intLE, setCancel
        intLE, minVer
        intLE, maxVer
        varInt, setSubVerLength
        setSubVerLength * varString, setSubVer
        intLE, priority
        varString, comment
        varString, statusBar
        varString, reserved
    varInt, signatureLength
    byte[signatureLength], signature:
        1 byte, type, object
        1 byte, length of the object
        1 byte, type, integer
        1 byte, length of the integer
        byte[length], r, big-endian, signed, two's complement
        1 byte, type, integer
        1 byte, length of the integer
        byte[length], s, big-endian, signed, two's complement
*/

package com.aqoleg.messages;

import com.aqoleg.crypto.Ecc;
import com.aqoleg.crypto.Sha256;
import com.aqoleg.keys.PublicKey;
import com.aqoleg.utils.BytesInput;
import com.aqoleg.utils.Converter;

import java.math.BigInteger;
import java.time.Instant;

public class Alert extends Message {
    public static final String command = "alert";
    private static final int object = 0x30;
    private static final int integer = 0x02;
    private static final PublicKey publicKey = PublicKey.createFromBytes(Converter.hexToBytes("04"
            + "fc9702847840aaf195de8442ebecedf5b095cdbb9bc716bda9110971b28a49e0"
            + "ead8564ff0db22209e0374782c093bb899692d524e9d6a6956e7c5ecbcd68284"));
    private final String string;

    /**
     * @param bytes byte array with payload
     * @throws NullPointerException if bytes == null
     * @throws Message.Exception    if bytes are incorrect
     */
    public Alert(byte[] bytes) {
        StringBuilder out = new StringBuilder();
        BytesInput bytesInput = new BytesInput(bytes);
        try {
            int alertPayloadLength = (int) bytesInput.readVariableLengthInt();
            int alertPayloadStartPos = bytes.length - bytesInput.available();
            out.append("version: ").append(bytesInput.readIntLE());
            out.append(", relayUntil: ");
            long time = bytesInput.readLongLE();
            if (time != 0) {
                out.append(Instant.ofEpochSecond(time));
            } else {
                out.append(0);
            }
            time = bytesInput.readLongLE();
            out.append(", expiration: ");
            if (time != 0) {
                out.append(Instant.ofEpochSecond(time));
            } else {
                out.append(0);
            }
            out.append(", id: ").append(bytesInput.readIntLE());
            out.append(", cancel: ").append(bytesInput.readIntLE());
            int setLength = (int) bytesInput.readVariableLengthInt();
            out.append(", setCancel: ").append(setLength);
            for (int i = 0; i < setLength; i++) {
                out.append(", ").append(i).append(": ").append(bytesInput.readIntLE());
            }
            out.append(", minVer: ").append(bytesInput.readIntLE());
            out.append(", maxVer: ").append(bytesInput.readIntLE());
            setLength = (int) bytesInput.readVariableLengthInt();
            out.append(", setSubVer: ").append(setLength);
            for (int i = 0; i < setLength; i++) {
                out.append(", ").append(i).append(": ").append(bytesInput.readVariableLengthString());
            }
            out.append(", priority: ").append(bytesInput.readIntLE());
            String s = bytesInput.readVariableLengthString();
            if (!s.isEmpty()) {
                out.append(", comment: ").append(s);
            }
            s = bytesInput.readVariableLengthString();
            if (!s.isEmpty()) {
                out.append(", statusBar: ").append(s);
            }
            s = bytesInput.readVariableLengthString();
            if (!s.isEmpty()) {
                out.append(", reserved: ").append(s);
            }
            if (alertPayloadStartPos + alertPayloadLength != bytes.length - bytesInput.available()) {
                throw new Message.Exception("incorrect alert payload length");
            }

            if (bytesInput.readVariableLengthInt() != bytesInput.available()) {
                throw new Message.Exception("incorrect signature length");
            }
            if (bytesInput.read() != object) {
                throw new Message.Exception("incorrect type object");
            }
            int length = bytesInput.read();
            if (bytesInput.read() != integer) {
                throw new Message.Exception("incorrect type r");
            }
            byte[] rBytes = bytesInput.readBytes(new byte[bytesInput.read()]);
            if (bytesInput.read() != integer) {
                throw new Message.Exception("incorrect type s");
            }
            byte[] sBytes = bytesInput.readBytes(new byte[bytesInput.read()]);
            if (length != 4 + rBytes.length + sBytes.length) {
                throw new Message.Exception("incorrect object length");
            }
            Ecc.verify(
                    Sha256.getHash(Sha256.getHash(bytes, alertPayloadStartPos, alertPayloadLength)),
                    publicKey.point,
                    new BigInteger[]{new BigInteger(rBytes), new BigInteger(sBytes)}
            );
            if (bytesInput.available() != 0) {
                throw new Message.Exception("big length");
            }
        } catch (IndexOutOfBoundsException | NegativeArraySizeException | Ecc.Exception exception) {
            throw new Message.Exception(exception.getMessage());
        }
        string = out.toString();
    }

    /**
     * @return "version: 1, relayUntil: 2012-02-19T03:02:15Z, expiration: 2012-02-21T02:47:15Z, id: 0,
     * cancel: 1, setCancel: 0, minVer: 0, maxVer: 0, setSubVer: 0, priority: 0, comment: comment,
     * statusBar: status bar, reserved: reserved"
     */
    @Override
    public String toString() {
        return string;
    }
}