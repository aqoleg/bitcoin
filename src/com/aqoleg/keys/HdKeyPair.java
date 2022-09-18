/*
hierarchical deterministic keys created from the seed, see bip32
blockchain.com   m/44'/0'/account'/external ? 0 : 1/index
electrum   m/external ? 0 : 1/index

usage:
    $ java com.aqoleg.keys.HdKeyPair passphrase mnemonic m/1/1h
    $ java com.aqoleg.keys.HdKeyPair
     xprv9vqn8EWFNT8jkGZsM51WSqWPYjRARtMZBEUMGT4vs9BaEVvvyahcgZND4wome9gN2rnoxjuNw48Po7w27pkw9PyFEL5Q5WrERpmjJzZU6jm

    HdKeyPair hdKeyPair = HdKeyPair.deserialize(serialized);
    HdKeyPair hdKeyPair = HdKeyPair.createMaster(seedBytes);
    HdKeyPair hdKeyPair = hdKeyPair.generateChild("0'1/2h/2'/10000000");
    HdKeyPair hdKeyPair = hdKeyPair.generateChild(keyNumber, isHardened);
    String serialized = hdKeyPair.serialize(isPrivate);
    String path = hdKeyPair.path;
    int depth = hdKeyPair.depth;
    int keyNumber = hdKeyPair.keyNumber;
    boolean isHardened = hdKeyPair.isHardened;
    BigInteger privateKey = hdKeyPair.privateKey;
    PublicKey publicKey = hdKeyPair.publicKey;

serialized bytes:
    intBE, version, private - 0x0488ADE4, public - 0x0488B21E
    1 byte, depth
    byte[4], fingerprint of the parent key, ripemd160(sha256(compressed public key))
    intBE, key number, += 2^31 for the hardened keys
    byte[32], chain code
    byte[33], key, for private first byte is zero, for public use compressed form
    byte[4], checksum, first 4 bytes of sha256(sha256(bytes without checksum))
*/

package com.aqoleg.keys;

import com.aqoleg.crypto.Ecc;
import com.aqoleg.crypto.HmacSha512;
import com.aqoleg.crypto.Ripemd160;
import com.aqoleg.crypto.Sha256;
import com.aqoleg.utils.Base58;
import com.aqoleg.utils.BytesInput;
import com.aqoleg.utils.BytesOutput;
import com.aqoleg.utils.Converter;

import java.math.BigInteger;
import java.util.Arrays;

public class HdKeyPair extends KeyPair {
    public final String path; // path of this key as m/1'/55
    public final int depth; // depth of the derivation path, 0 for the master key
    public final int keyNumber; // key number, 0 for the master key
    public final boolean isHardened; // false for the master key
    private final byte[] fingerprint; // 4-bytes fingerprint of the parent key
    private final byte[] chainCode; // 32-bytes chain code

    private HdKeyPair(
            BigInteger privateKey,
            String path,
            int depth,
            int keyNumber,
            boolean isHardened,
            byte[] fingerprint,
            byte[] chainCode
    ) {
        super(privateKey, true); // compressed, throws exception if private key is not correct
        this.path = path;
        this.depth = depth;
        this.keyNumber = keyNumber;
        this.isHardened = isHardened;
        this.fingerprint = fingerprint;
        this.chainCode = chainCode;
    }

    /**
     * @param seed array containing seed
     * @return master HdKeyPair created from the seed
     * @throws NullPointerException if seed = null
     * @throws KeyPair.Exception    if created private key is not correct
     */
    public static HdKeyPair createMaster(byte[] seed) {
        // data = hmac-sha512(key = "Bitcoin seed", data = seed);
        byte[] data = HmacSha512.getMac(seed, "Bitcoin seed".getBytes());
        // the first 32 bytes is the private key, the last 32 bytes is the chain code
        BigInteger privateKey = new BigInteger(1, Arrays.copyOfRange(data, 0, 32));
        byte[] chainCode = Arrays.copyOfRange(data, 32, 64);
        return new HdKeyPair(privateKey, "m", 0, 0, false, new byte[4], chainCode);
    }

    /**
     * @param serialized serialized String
     * @return HdKeyPair deserialized from this string
     * @throws NullPointerException if string == null
     * @throws HdKeyPair.Exception  if string is incorrect
     * @throws KeyPair.Exception    if key is incorrect
     */
    public static HdKeyPair deserialize(String serialized) {
        byte[] bytes;
        try {
            bytes = Base58.decode(serialized);
        } catch (Base58.Exception exception) {
            throw new Exception(exception.getMessage());
        }
        if (bytes.length != 82) {
            throw new Exception("incorrect length");
        }
        BytesInput bytesInput = new BytesInput(bytes);
        if (bytesInput.readIntBE() != 0x0488ADE4) {
            throw new Exception("incorrect version");
        }
        int depth = bytesInput.read();
        byte[] fingerprint = bytesInput.readBytes(new byte[4]);
        if (depth == 0) {
            for (byte aByte : fingerprint) {
                if (aByte != 0) {
                    throw new Exception("non-zero fingerprint byte");
                }
            }
        }
        int keyNumber = bytesInput.readIntBE();
        if (depth == 0 && keyNumber != 0) {
            throw new Exception("non-zero key number");
        }
        boolean isHardened = (keyNumber >>> 31) == 1;
        if (isHardened) {
            keyNumber = keyNumber << 1 >>> 1;
        }
        byte[] chainCode = bytesInput.readBytes(new byte[32]);
        if (bytesInput.read() != 0) {
            throw new Exception("incorrect first byte of the key, requires 0x00");
        }
        byte[] key = bytesInput.readBytes(new byte[32]);
        byte[] checksum = Sha256.getHash(Sha256.getHash(bytes, 0, 78));
        for (int i = 0; i < 4; i++) {
            if ((checksum[i] & 0xFF) != bytesInput.read()) {
                throw new Exception("incorrect checksum");
            }
        }
        String path = "m";
        if (depth != 0) {
            for (int i = 0; i < depth; i++) {
                path = path.concat("/");
            }
            path = path.concat(String.valueOf(keyNumber));
            if (isHardened) {
                path = path.concat("\'");
            }
        }
        return new HdKeyPair(new BigInteger(1, key), path, depth, keyNumber, isHardened, fingerprint, chainCode);
    }

    /**
     * prints info about serialized or created hd key pair
     *
     * @param args serialized hd key; passphrase, then mnemonic, then path; or empty
     */
    public static void main(String[] args) {
        if (args.length == 1) {
            System.out.println(serializedToString(args[0]));
        } else if (args.length > 1) {
            StringBuilder mnemonic = new StringBuilder();
            for (int i = 1; i < args.length - 1; i++) {
                if (mnemonic.length() > 0) {
                    mnemonic.append(' ');
                }
                mnemonic.append(args[i]);
            }
            System.out.println(toString(args[0], mnemonic.toString(), args[args.length - 1]));
        } else if (System.console() == null) {
            System.out.println(serializedToString(""));
        } else {
            while (true) {
                String input = System.console().readLine("enter serialized hd key, 'hd' or 'exit': ");
                switch (input) {
                    case "hd":
                        String passphrase = System.console().readLine("enter passphrase: ");
                        String mnemonic = System.console().readLine("enter mnemonic: ");
                        String path = System.console().readLine("enter path: ");
                        System.out.println(toString(passphrase, mnemonic, path));
                        break;
                    case "exit":
                        System.exit(0);
                    default:
                        System.out.println(serializedToString(input));
                        break;
                }
            }
        }
    }

    /**
     * @param keyNumber  number of the child
     * @param isHardened true if the child is hardened
     * @return child HdKeyPair created from this HdKeyPair
     * @throws HdKeyPair.Exception if keyNumber < 0
     * @throws KeyPair.Exception   if private key is not correct
     */
    public HdKeyPair generateChild(int keyNumber, boolean isHardened) {
        if (keyNumber < 0) {
            throw new Exception("negative keyNumber");
        }
        byte[] data = new byte[37];
        // byte[33], key, for hardened first byte is zero, for non-hardened public use compressed public key
        // intBE, key number, += 2^31 for the hardened keys
        if (isHardened) {
            byte[] key = privateKey.toByteArray();
            // BigInteger byte array is the signed two's-complement representation, so the first byte can be 0
            int keyStart = key[0] == 0 ? 1 : 0;
            System.arraycopy(key, keyStart, data, 33 - (key.length - keyStart), key.length - keyStart);
        } else {
            byte[] key = publicKey.toByteArray();
            System.arraycopy(key, 0, data, 0, 33);
        }
        data[33] = (byte) (keyNumber >>> 24);
        data[34] = (byte) (keyNumber >>> 16);
        data[35] = (byte) (keyNumber >>> 8);
        data[36] = (byte) keyNumber;
        if (isHardened) {
            data[33] |= 0b10000000;
        }
        // data = hmac-sha512(key = parent chain code, data);
        data = HmacSha512.getMac(data, chainCode);
        // child chain code is the last 32 bytes of the data
        byte[] chainCode = Arrays.copyOfRange(data, 32, 64);
        // child private key = (the first 32 bytes of the data + parent private key) % n
        BigInteger privateKey = new BigInteger(1, Arrays.copyOfRange(data, 0, 32));
        privateKey = Ecc.modN(this.privateKey.add(privateKey));
        byte[] fingerprint = Arrays.copyOfRange(Ripemd160.getHash(Sha256.getHash(publicKey.toByteArray())), 0, 4);
        String path = this.path + "/" + keyNumber;
        if (isHardened) {
            path += "'";
        }
        return new HdKeyPair(privateKey, path, depth + 1, keyNumber, isHardened, fingerprint, chainCode);
    }

    /**
     * @param path String containing relative path of this child as 11/3'/0h/55
     * @return child HdKeyPair created from this HdKeyPair
     * @throws NullPointerException if path == null
     * @throws HdKeyPair.Exception  if path is incorrect
     * @throws KeyPair.Exception    if private key is not correct
     */
    public HdKeyPair generateChild(String path) {
        HdKeyPair child = this;
        int start, stop;
        try {
            start = path.charAt(0) == '/' ? 1 : 0;
            do {
                stop = path.indexOf('/', start);
                if (stop == -1) {
                    stop = path.length();
                    if (start >= stop) {
                        return child;
                    }
                }
                char lastChar = path.charAt(stop - 1);
                boolean hardened = lastChar == '\'' || lastChar == 'h';
                int keyNumber = Integer.parseInt(path.substring(start, hardened ? stop - 1 : stop));
                child = child.generateChild(keyNumber, hardened);
                start = stop + 1;
            } while (true);
        } catch (NumberFormatException | IndexOutOfBoundsException exception) {
            throw new Exception("path is incorrect " + exception.getMessage());
        }
    }

    /**
     * @param isKeyPrivate true if key is private, false if key is public
     * @return serialized String
     */
    public String serialize(boolean isKeyPrivate) {
        BytesOutput bytes = new BytesOutput();
        if (isKeyPrivate) {
            bytes.writeIntBE(0x0488ADE4);
        } else {
            bytes.writeIntBE(0x0488B21E);
        }
        bytes.write(depth);
        bytes.writeBytes(fingerprint);
        int n = keyNumber;
        if (isHardened) {
            // noinspection NumericOverflow
            n |= 1 << 31;
        }
        bytes.writeIntBE(n);
        bytes.writeBytes(chainCode);
        if (isKeyPrivate) {
            byte[] d = this.privateKey.toByteArray();
            // BigInteger byte array is the signed two's-complement representation, so the first byte can be 0
            int dStart = d[0] == 0 ? 1 : 0;
            byte[] key = new byte[33];
            System.arraycopy(d, dStart, key, 33 - (d.length - dStart), d.length - dStart);
            bytes.writeBytes(key, 0, 33);
        } else {
            bytes.writeBytes(publicKey.toByteArray());
        }
        byte[] checksum = Sha256.getHash(Sha256.getHash(bytes.toByteArray()));
        bytes.writeBytes(checksum, 0, 4);
        return Base58.encode(bytes.toByteArray());
    }

    private static String toString(String passphrase, String mnemonic, String path) {
        if (path.charAt(0) == 'm') {
            path = path.substring(1);
        }
        try {
            HdKeyPair keyPair = HdKeyPair.createMaster(Mnemonic.createSeed(mnemonic, passphrase));
            keyPair = keyPair.generateChild(path);
            String out = "passphrase: '" + passphrase + "', mnemonic: '" + mnemonic + "', path: '" + path;
            out += "', xprv: " + keyPair.serialize(true);
            out += ", xpub: " + keyPair.serialize(false);
            out += ", depth: " + keyPair.depth;
            out += ", key " + keyPair.keyNumber;
            if (keyPair.isHardened) {
                out += "'";
            }
            out += ", d: " + keyPair.privateKey.toString(16);
            out += keyPair.publicKey.compressed ? ", compressed" : ", uncompressed";
            out += ", x: " + keyPair.publicKey.point.x.toString(16);
            out += ", y: " + keyPair.publicKey.point.y.toString(16);
            out += ", hash: " + Converter.bytesToHex(keyPair.publicKey.getHash(), false, false);
            out += ", address: " + keyPair.getAddress();
            return out;
        } catch (HdKeyPair.Exception exception) {
            return "incorrect path '" + path + "': " + exception.getMessage();
        }
    }

    private static String serializedToString(String serialized) {
        try {
            HdKeyPair keyPair = HdKeyPair.deserialize(serialized);
            String out = "serialized hd key '" + serialized;
            out += "', xpub: " + keyPair.serialize(false);
            out += ", depth: " + keyPair.depth;
            out += ", key " + keyPair.keyNumber;
            if (keyPair.isHardened) {
                out += "'";
            }
            out += ", d: " + keyPair.privateKey.toString(16);
            out += keyPair.publicKey.compressed ? ", compressed" : ", uncompressed";
            out += ", x: " + keyPair.publicKey.point.x.toString(16);
            out += ", y: " + keyPair.publicKey.point.y.toString(16);
            out += ", hash: " + Converter.bytesToHex(keyPair.publicKey.getHash(), false, false);
            out += ", address: " + keyPair.getAddress();
            return out;
        } catch (HdKeyPair.Exception exception) {
            return "incorrect serialized hd key '" + serialized + "': " + exception.getMessage();
        }
    }

    public static class Exception extends KeyPair.Exception {
        private Exception(String message) {
            super(message);
        }
    }
}