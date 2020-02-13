// bitcoin public key
//
// uncompressed bytes:
//    1 byte, 0x04
//    byte[32], x
//    byte[32], y
//
// compressed bytes:
//    1 byte, even - 0x02, odd - 0x03
//    byte[32], x
package space.aqoleg.keys;

import space.aqoleg.crypto.Ecc;
import space.aqoleg.crypto.Ripemd160;
import space.aqoleg.crypto.Sha256;

import java.math.BigInteger;
import java.util.Arrays;

public class PublicKey {
    public final Ecc.Point point;
    public final boolean compressed;

    /**
     * @param point      public key as a Point
     * @param compressed type of the public key
     * @throws NullPointerException if point == null
     */
    public PublicKey(Ecc.Point point, boolean compressed) {
        if (point == null) {
            throw new NullPointerException("the point is null");
        }
        this.point = point;
        this.compressed = compressed;
    }

    /**
     * @param bytes 65 bytes array {0x04, 32 bytes x, 32 bytes y} or 33 bytes array {0x02 or 0x03, 32 bytes x}
     * @return PublicKey created from this byte array
     * @throws NullPointerException          if bytes == null
     * @throws UnsupportedOperationException if length or format is incorrect, or this key does not exist
     */
    public static PublicKey createFromBytes(byte[] bytes) {
        if (bytes.length == 65) {
            if (bytes[0] != 0x04) {
                throw new UnsupportedOperationException("first byte of uncompressed key is incorrect, requires 0x04");
            }
        } else if (bytes.length == 33) {
            if (bytes[0] != 0x02 && bytes[0] != 0x03) {
                throw new UnsupportedOperationException("first byte of compressed key is incorrect, requires 0x02 or 0x03");
            }
        } else {
            throw new UnsupportedOperationException("key length is incorrect, requires 33 or 65 bytes");
        }
        BigInteger x = new BigInteger(1, Arrays.copyOfRange(bytes, 1, 33));
        if (bytes[0] == 0x04) {
            Ecc.Point point = Ecc.secp256k1.createPoint(x, new BigInteger(1, Arrays.copyOfRange(bytes, 33, 65)));
            return new PublicKey(point, false);
        } else {
            Ecc.Point point = Ecc.secp256k1.createPoint(x, bytes[0] == 0x02);
            return new PublicKey(point, true);
        }
    }

    /**
     * @return 33-bytes or 65-bytes array containing this public key
     */
    public byte[] toByteArray() {
        byte[] bytes;
        if (compressed) {
            bytes = new byte[33];
            bytes[0] = (byte) (point.y.testBit(0) ? 0x03 : 0x02);
            byte[] x = point.x.toByteArray();
            // BigInteger byte array is the signed two's-complement representation, so the first byte can be 0
            int xStart = x[0] == 0 ? 1 : 0;
            System.arraycopy(x, xStart, bytes, 33 - (x.length - xStart), x.length - xStart);
        } else {
            bytes = new byte[65];
            bytes[0] = 0x04;
            byte[] x = point.x.toByteArray();
            int xStart = x[0] == 0 ? 1 : 0;
            System.arraycopy(x, xStart, bytes, 33 - (x.length - xStart), x.length - xStart);
            byte[] y = point.y.toByteArray();
            int yStart = y[0] == 0 ? 1 : 0;
            System.arraycopy(y, yStart, bytes, 65 - (y.length - yStart), y.length - yStart);
        }
        return bytes;
    }

    /**
     * @return 20 bytes hash of this public key
     */
    public byte[] getHash() {
        // hash = ripemd160(sha256(key));
        return Ripemd160.getHash(Sha256.getHash(toByteArray()));
    }
}