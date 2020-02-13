// bitcoin private and public keys
//
// wif bytes:
//    1 byte, version, 0x80
//    byte[32], private key
//    if compressed, 1 byte, flag, 0x01
//    byte[4], checksum, sha256(sha256(bytes without last 4 bytes))
package space.aqoleg.keys;

import space.aqoleg.crypto.Ecc;
import space.aqoleg.crypto.Sha256;
import space.aqoleg.utils.Base58;

import java.math.BigInteger;
import java.util.Arrays;

public class KeyPair {
    public final BigInteger d; // private key as a number
    public final PublicKey publicKey;

    /**
     * @param d                private key as number
     * @param compressedPublic true if public key is compressed
     * @throws NullPointerException          if d == null
     * @throws UnsupportedOperationException if d <= 0 or d >= n
     */
    public KeyPair(BigInteger d, boolean compressedPublic) {
        if (d.signum() <= 0) {
            throw new UnsupportedOperationException("non-positive private key, requires d > 0");
        } else if (d.compareTo(Ecc.secp256k1.getN()) >= 0) {
            throw new UnsupportedOperationException("private key is too big, requires d < n");
        }
        this.d = d;
        // publicKey = g * privateKey
        publicKey = new PublicKey(Ecc.secp256k1.gMultiply(d), compressedPublic);
    }

    /**
     * @param wif a wif String to be decoded
     * @return KeyPair created from the decoded private key
     * @throws NullPointerException          if wif == null
     * @throws UnsupportedOperationException if wif string is incorrect or private key is not valid
     */
    public static KeyPair decode(String wif) {
        byte[] bytes = Base58.decode(wif);
        if (bytes.length != 37 && bytes.length != 38) {
            throw new UnsupportedOperationException("incorrect length " + bytes.length + ", requires 37 or 38 bytes");
        }
        if (bytes[0] != (byte) 0x80) {
            throw new UnsupportedOperationException("incorrect version " + bytes[0] + ", requires 0x80");
        }
        // check if this private key corresponded to a compressed public key
        boolean compressed = bytes.length == 38;
        if (compressed && bytes[33] != 0x01) {
            throw new UnsupportedOperationException("incorrect compressed flag " + bytes[33] + ", requires 0x01");
        }
        byte[] checksum = Sha256.getHash(Sha256.getHash(bytes, 0, compressed ? 34 : 33));
        for (int i = 0; i < 4; i++) {
            if (checksum[i] != bytes[(compressed ? 34 : 33) + i]) {
                throw new UnsupportedOperationException("incorrect checksum");
            }
        }
        byte[] privateKey = Arrays.copyOfRange(bytes, 1, 33);
        return new KeyPair(new BigInteger(1, privateKey), compressed); // throw exception if key is not valid
    }

    /**
     * @return the private key of this KeyPair encoded as wif String
     */
    public String encode() {
        byte[] bytes = new byte[publicKey.compressed ? 38 : 37];
        bytes[0] = (byte) 0x80;
        byte[] key = d.toByteArray();
        // BigInteger byte array is the signed two's-complement representation, so the first byte can be 0
        int keyStart = key[0] == 0 ? 1 : 0;
        System.arraycopy(key, keyStart, bytes, 33 - (key.length - keyStart), key.length - keyStart);
        if (publicKey.compressed) {
            bytes[33] = 0x01;
        }
        byte[] checksum = Sha256.getHash(Sha256.getHash(bytes, 0, publicKey.compressed ? 34 : 33));
        System.arraycopy(checksum, 0, bytes, publicKey.compressed ? 34 : 33, 4);
        return Base58.encode(bytes);
    }

    /**
     * @return address created from this public key
     */
    public String getAddress() {
        return Address.createFromPublicKey(publicKey).toString();
    }
}