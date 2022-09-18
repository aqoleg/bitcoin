/*
bitcoin private and public keys

usage:
    $ java com.aqoleg.keys.KeyPair compressed
    $ java com.aqoleg.keys.KeyPair uncompressed
    $ java com.aqoleg.keys.KeyPair 1 compressed
    $ java com.aqoleg.keys.KeyPair 0x10 uncompressed
    $ java com.aqoleg.keys.KeyPair KwDiBf89QgGbjEhKnhXJuH7LrciVrZi3qYjgd9M7rFU7BLjN42GK

    KeyPair keyPair = new KeyPair(isCompressed);
    KeyPair keyPair = new KeyPair(privateKeyBigInteger, isCompressed);
    KeyPair keyPair = KeyPair.decode(wifString);
    String wif = keyPair.encode();
    String address = keyPair.getAddress();
    BigInteger privateKey = keyPair.privateKey;
    PublicKey publicKey = keyPair.publicKey;

wif bytes:
    1 byte, version, 0x80
    byte[32], private key
    if compressed, 1 byte, flag, 0x01
    byte[4], checksum, sha256(sha256(bytes without last 4 bytes))
*/

package com.aqoleg.keys;

import com.aqoleg.crypto.Ecc;
import com.aqoleg.crypto.Sha256;
import com.aqoleg.utils.Base58;
import com.aqoleg.utils.Converter;

import java.math.BigInteger;
import java.util.Arrays;

public class KeyPair {
    public final BigInteger privateKey;
    public final PublicKey publicKey;

    /**
     * creates new random KeyPair
     *
     * @param compressedPublic true if public key is compressed
     */
    public KeyPair(boolean compressedPublic) {
        this.privateKey = Ecc.createRandomPrivateKey();
        // publicKey = g * privateKey
        publicKey = new PublicKey(Ecc.multiplyG(privateKey), compressedPublic);
    }

    /**
     * @param privateKey       BigInteger 0 < privateKey < n
     * @param compressedPublic true if public key is compressed
     * @throws NullPointerException if privateKey == null
     * @throws KeyPair.Exception    if privateKey is incorrect
     */
    public KeyPair(BigInteger privateKey, boolean compressedPublic) {
        try {
            Ecc.checkPrivateKey(privateKey);
        } catch (Ecc.Exception exception) {
            throw new Exception(exception.getMessage());
        }
        this.privateKey = privateKey;
        // publicKey = g * privateKey
        publicKey = new PublicKey(Ecc.multiplyG(privateKey), compressedPublic);
    }

    /**
     * @param wif a wif String to be decoded
     * @return KeyPair created from the decoded private key
     * @throws NullPointerException if wif == null
     * @throws KeyPair.Exception    if wif string or private key is incorrect
     */
    public static KeyPair decode(String wif) {
        byte[] bytes;
        try {
            bytes = Base58.decode(wif);
        } catch (Base58.Exception exception) {
            throw new Exception(exception.getMessage());
        }
        if (bytes.length != 37 && bytes.length != 38) {
            throw new Exception("incorrect length " + bytes.length + ", requires 37 or 38 bytes");
        }
        if (bytes[0] != (byte) 0x80) {
            throw new Exception("incorrect version " + bytes[0] + ", requires 0x80");
        }
        boolean compressed = bytes.length == 38;
        if (compressed && bytes[33] != 0x01) {
            throw new Exception("incorrect compressed flag " + bytes[33] + ", requires 0x01");
        }
        byte[] checksum = Sha256.getHash(Sha256.getHash(bytes, 0, compressed ? 34 : 33));
        for (int i = 0; i < 4; i++) {
            if (checksum[i] != bytes[(compressed ? 34 : 33) + i]) {
                throw new Exception("incorrect checksum");
            }
        }
        byte[] privateKey = Arrays.copyOfRange(bytes, 1, 33);
        return new KeyPair(new BigInteger(1, privateKey), compressed);
    }

    /**
     * prints info about random public key, public key created from private key or wif
     *
     * @param args String "compressed", "uncompressed"; hex or decimal private key, then "compressed" or "uncompressed";
     *             wif or empty
     */
    public static void main(String[] args) {
        if (args.length == 1) {
            switch (args[0]) {
                case "compressed":
                    System.out.println(randomKeyPairToString(true));
                    break;
                case "uncompressed":
                    System.out.println(randomKeyPairToString(false));
                    break;
                default:
                    System.out.println(wifToString(args[0]));
                    break;
            }
        } else if (args.length == 2) {
            System.out.println(dToString(args[0], !args[1].equals("uncompressed")));
        } else if (System.console() == null) {
            System.out.println(randomKeyPairToString(true));
        } else {
            while (true) {
                String input = System.console().readLine("enter wif, 'pk', 'compressed', 'uncompressed', or 'exit': ");
                switch (input) {
                    case "pk":
                        String d = System.console().readLine("enter hex or decimal private key: ");
                        String compressed = System.console().readLine("enter 'compressed' or 'uncompressed': ");
                        System.out.println(dToString(d, !compressed.equals("uncompressed")));
                        break;
                    case "compressed":
                        System.out.println(randomKeyPairToString(true));
                        break;
                    case "uncompressed":
                        System.out.println(randomKeyPairToString(false));
                        break;
                    case "exit":
                        System.exit(0);
                    default:
                        System.out.println(wifToString(input));
                        break;
                }
            }
        }
    }

    /**
     * @return the private key of this KeyPair encoded as a wif String
     */
    public String encode() {
        byte[] bytes = new byte[publicKey.compressed ? 38 : 37];
        bytes[0] = (byte) 0x80;
        byte[] key = privateKey.toByteArray();
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

    private static String randomKeyPairToString(boolean compressed) {
        KeyPair keyPair = new KeyPair(compressed);
        String out = "new random ";
        out += compressed ? "compressed" : "uncompressed";
        out += " key pair, d: " + keyPair.privateKey.toString(16);
        out += ", wif: " + keyPair.encode();
        out += ", x: " + keyPair.publicKey.point.x.toString(16);
        out += ", y: " + keyPair.publicKey.point.y.toString(16);
        out += ", hash: " + Converter.bytesToHex(keyPair.publicKey.getHash(), false, false);
        out += ", address: " + keyPair.getAddress();
        return out;
    }

    private static String dToString(String d, boolean compressed) {
        try {
            String out = compressed ? "compressed" : "uncompressed";
            BigInteger privateKey;
            if (d.startsWith("0x")) {
                out += " hex";
                privateKey = new BigInteger(d.substring(2), 16);
            } else {
                out += " decimal";
                privateKey = new BigInteger(d);
            }
            KeyPair keyPair = new KeyPair(privateKey, compressed);
            out += " private key '" + d;
            out += "', d: " + privateKey.toString(16);
            out += ", wif: " + keyPair.encode();
            out += ", x: " + keyPair.publicKey.point.x.toString(16);
            out += ", y: " + keyPair.publicKey.point.y.toString(16);
            out += ", hash: " + Converter.bytesToHex(keyPair.publicKey.getHash(), false, false);
            out += ", address: " + keyPair.getAddress();
            return out;
        } catch (NumberFormatException exception) {
            return "incorrect number " + exception.getMessage();
        } catch (KeyPair.Exception exception) {
            return "incorrect private key '" + d + "': " + exception.getMessage();
        }
    }

    private static String wifToString(String wif) {
        try {
            KeyPair keyPair = KeyPair.decode(wif);
            String out = "wif '" + wif;
            out += "', d: " + keyPair.privateKey.toString(16);
            out += keyPair.publicKey.compressed ? ", compressed" : ", uncompressed";
            out += ", x: " + keyPair.publicKey.point.x.toString(16);
            out += ", y: " + keyPair.publicKey.point.y.toString(16);
            out += ", hash: " + Converter.bytesToHex(keyPair.publicKey.getHash(), false, false);
            out += ", address: " + keyPair.getAddress();
            return out;
        } catch (KeyPair.Exception exception) {
            return "incorrect wif '" + wif + "': " + exception.getMessage();
        }
    }

    public static class Exception extends RuntimeException {
        protected Exception(String message) {
            super(message);
        }
    }
}