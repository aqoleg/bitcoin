/*
bitcoin public key

usage:
    $ java com.aqoleg.keys.PublicKey 0250863ad64a87ae8a2fe83c1af1a8403cb53f53e486d8511dad8a04887e5b2352
    $ java com.aqoleg.keys.PublicKey 0450863ad64a87ae8a2fe83c1af1a8403cb53f53e486d8511dad8a04887e5b2352
     2cd470243453a299fa9e77237716103abc11a1df38855ed6f2ee187e9c582ba6

    PublicKey publicKey = new PublicKey(eccPoint, compressed);
    PublicKey publicKey = PublicKey.createFromBytes(bytes);
    Ecc.Point point = publicKey.point;
    boolean compressed = publicKey.compressed;
    byte[] bytes = publicKey.toByteArray();
    byte[] hash = publicKey.getHash();

uncompressed bytes:
    1 byte, 0x04
    byte[32], x
    byte[32], y
compressed bytes:
    1 byte, even - 0x02, odd - 0x03
    byte[32], x
*/

package com.aqoleg.keys;

import com.aqoleg.crypto.Ecc;
import com.aqoleg.crypto.Ripemd160;
import com.aqoleg.crypto.Sha256;
import com.aqoleg.utils.Converter;

import java.math.BigInteger;
import java.util.Arrays;

public class PublicKey {
    public final Ecc.Point point;
    public final boolean compressed;

    /**
     * @param point      Ecc.Point = g * privateKey
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
     * @throws NullPointerException if bytes == null
     * @throws PublicKey.Exception  if format is incorrect or key is incorrect
     */
    public static PublicKey createFromBytes(byte[] bytes) {
        if (bytes.length == 65) {
            if (bytes[0] != 0x04) {
                throw new Exception("first byte of uncompressed key is incorrect, requires 0x04");
            }
        } else if (bytes.length == 33) {
            if (bytes[0] != 0x02 && bytes[0] != 0x03) {
                throw new Exception("first byte of compressed key is incorrect, requires 0x02 or 0x03");
            }
        } else {
            throw new Exception("key length is incorrect, requires 33 or 65 bytes");
        }
        BigInteger x = new BigInteger(1, Arrays.copyOfRange(bytes, 1, 33));
        try {
            if (bytes[0] == 0x04) {
                Ecc.Point point = Ecc.createPoint(x, new BigInteger(1, Arrays.copyOfRange(bytes, 33, 65)));
                return new PublicKey(point, false);
            } else {
                Ecc.Point point = Ecc.createPoint(x, bytes[0] == 0x02);
                return new PublicKey(point, true);
            }
        } catch (Ecc.Exception exception) {
            throw new Exception(exception.getMessage());
        }
    }

    /**
     * prints info about entered public key
     *
     * @param args hex String with public key or empty
     */
    public static void main(String[] args) {
        if (args.length == 1) {
            System.out.println(toString(args[0]));
        } else if (System.console() == null) {
            System.out.println(toString(""));
        } else {
            while (true) {
                String input = System.console().readLine("enter public key or 'exit': ");
                if (input.equals("exit")) {
                    System.exit(0);
                }
                System.out.println(toString(input));
            }
        }
    }

    /**
     * @return 33-bytes or 65-bytes array with this public key
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
     * @return 20 bytes hash = ripemd160(sha256(publicKey));
     */
    public byte[] getHash() {
        return Ripemd160.getHash(Sha256.getHash(toByteArray()));
    }

    private static String toString(String input) {
        try {
            PublicKey publicKey = PublicKey.createFromBytes(Converter.hexToBytes(input));
            String out = "public key '" + input;
            out += publicKey.compressed ? "', compressed" : "', uncompressed";
            out += ", x: " + publicKey.point.x.toString(16);
            out += ", y: " + publicKey.point.y.toString(16);
            out += ", hash: " + Converter.bytesToHex(publicKey.getHash(), false, false);
            out += ", address: " + Address.createFromPublicKey(publicKey);
            return out;
        } catch (Converter.Exception | PublicKey.Exception exception) {
            return "incorrect public key '" + input + "': " + exception.getMessage();
        }
    }

    public static class Exception extends RuntimeException {
        private Exception(String message) {
            super(message);
        }
    }
}