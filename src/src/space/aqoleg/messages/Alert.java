// alert system message, 311 <= version < 70013
//
// payload bytes:
//    varInt, alertPayloadLength
//    byte[alertPayloadLength], alertPayload:
//       intLE, version
//       longLE, relayUntil
//       longLE, expiration
//       intLE, id
//       intLE, cancel
//       varInt, setCancelLength
//       setCancelLength * intLE, setCancel
//       intLE, minVer
//       intLE, maxVer
//       varInt, setSubVerLength
//       setSubVerLength * varString, setSubVer
//       intLE, priority
//       varString, comment
//       varString, statusBar
//       varString, reserved
//    varInt, signatureLength
//    byte[signatureLength], signature:
//       1 byte, type, object
//       1 byte, length of the object
//       1 byte, type, integer
//       1 byte, length of the integer
//       byte[length], r, big-endian, signed, two's complement
//       1 byte, type, integer
//       1 byte, length of the integer
//       byte[length], s, big-endian, signed, two's complement
package space.aqoleg.messages;

import space.aqoleg.crypto.Ecc;
import space.aqoleg.crypto.Sha256;
import space.aqoleg.keys.PublicKey;
import space.aqoleg.utils.BytesInput;
import space.aqoleg.utils.Converter;

import java.math.BigInteger;

public class Alert {
    public static final String command = "alert";
    private static final int object = 0x30;
    private static final int integer = 0x02;
    private static final PublicKey publicKey = PublicKey.createFromBytes(Converter.hexToBytes("04"
            + "fc9702847840aaf195de8442ebecedf5b095cdbb9bc716bda9110971b28a49e0"
            + "ead8564ff0db22209e0374782c093bb899692d524e9d6a6956e7c5ecbcd68284"));

    /**
     * @param payload byte array with payload
     * @return String representation of this Alert
     * @throws NullPointerException          if payload == null
     * @throws IndexOutOfBoundsException     if payload is incorrect
     * @throws UnsupportedOperationException if payload is incorrect
     */
    public static String toString(byte[] payload) {
        StringBuilder out = new StringBuilder();
        BytesInput bytes = new BytesInput(payload);
        int alertPayloadLength = (int) bytes.readVariableLengthInt();
        int alertPayloadStartPos = payload.length - bytes.available();
        out.append("version: ").append(bytes.readIntLE());
        out.append(", relayUntil: ").append(bytes.readLongLE());
        out.append(", expiration: ").append(bytes.readLongLE());
        out.append(", id: ").append(bytes.readIntLE());
        out.append(", cancel: ").append(bytes.readIntLE());
        int setLength = (int) bytes.readVariableLengthInt();
        out.append(", setCancelLength: ").append(setLength);
        for (int i = 0; i < setLength; i++) {
            out.append(", setCancel").append(i).append(": ").append(bytes.readIntLE());
        }
        out.append(", minVer: ").append(bytes.readIntLE());
        out.append(", maxVer: ").append(bytes.readIntLE());
        setLength = (int) bytes.readVariableLengthInt();
        out.append(", setSubVerLength: ").append(setLength);
        for (int i = 0; i < setLength; i++) {
            out.append(", setSubVer").append(i).append(": ").append(bytes.readVariableLengthString());
        }
        out.append(", priority: ").append(bytes.readIntLE());
        out.append(", comment: ").append(bytes.readVariableLengthString());
        out.append(", statusBar: ").append(bytes.readVariableLengthString());
        out.append(", reserved: ").append(bytes.readVariableLengthString());
        if (alertPayloadStartPos + alertPayloadLength != payload.length - bytes.available()) {
            throw new UnsupportedOperationException("alert payload length is incorrect");
        }

        if (bytes.readVariableLengthInt() != bytes.available()) {
            throw new UnsupportedOperationException("signature length is incorrect");
        }
        if (bytes.read() != object) {
            throw new UnsupportedOperationException("type is incorrect, requires object");
        }
        int length = bytes.read();
        if (bytes.read() != integer) {
            throw new UnsupportedOperationException("type is incorrect, requires integer");
        }
        byte[] rBytes = new byte[bytes.read()];
        bytes.readBytes(rBytes);
        if (bytes.read() != integer) {
            throw new UnsupportedOperationException("type is incorrect, requires integer");
        }
        byte[] sBytes = new byte[bytes.read()];
        bytes.readBytes(sBytes);
        if (length != 4 + rBytes.length + sBytes.length) {
            throw new UnsupportedOperationException("object length is incorrect");
        }
        boolean verified = Ecc.secp256k1.verify(
                Sha256.getHash(Sha256.getHash(payload, alertPayloadStartPos, alertPayloadLength)),
                publicKey.point,
                new BigInteger[]{new BigInteger(rBytes), new BigInteger(sBytes)}
        );
        if (bytes.available() != 0) {
            throw new UnsupportedOperationException("payload length is incorrect");
        }
        out.append(", verified: ").append(verified);
        return out.toString();
    }
}