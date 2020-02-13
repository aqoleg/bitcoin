// hierarchical deterministic keys created from the seed, see bip32
// blockchain.com   m/44'/0'/account'/external ? 0 : 1/index
// electrum   m/external ? 0 : 1/index
//
// serialized bytes:
//    intBE, version, private - 0x0488ADE4, public - 0x0488B21E
//    1 byte, depth
//    byte[4], fingerprint of the parent key, ripemd160(sha256(compressed public key))
//    intBE, key number, += 2^31 for the hardened keys
//    byte[32], chain code
//    byte[33], key, for private first byte is zero, for public use compressed form
//    byte[4], checksum, first 4 bytes of sha256(sha256(bytes without checksum))
package space.aqoleg.keys;

import space.aqoleg.crypto.Ecc;
import space.aqoleg.crypto.HmacSha512;
import space.aqoleg.crypto.Ripemd160;
import space.aqoleg.crypto.Sha256;
import space.aqoleg.utils.Base58;
import space.aqoleg.utils.BytesInput;
import space.aqoleg.utils.BytesOutput;

import java.math.BigInteger;
import java.util.Arrays;

public class HdKeyPair {
    public final KeyPair keyPair; // compressed
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
        keyPair = new KeyPair(privateKey, true); // compressed, throws exception if private key is not valid
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
     * @throws NullPointerException          if seed = null
     * @throws UnsupportedOperationException if created private key is not valid
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
     * @param string Serialized string
     * @return HdKeyPair deserialized from this string
     * @throws NullPointerException          if string == null
     * @throws UnsupportedOperationException if string is incorrect or key is private
     */
    public static HdKeyPair deserialize(String string) {
        byte[] serialized = Base58.decode(string);
        if (serialized.length != 82) {
            throw new UnsupportedOperationException("incorrect length");
        }
        BytesInput bytes = new BytesInput(serialized);
        if (bytes.readIntBE() != 0x0488ADE4) {
            throw new UnsupportedOperationException("incorrect version");
        }
        int depth = bytes.read();
        byte[] fingerprint = new byte[4];
        bytes.readBytes(fingerprint);
        if (depth == 0) {
            for (byte aByte : fingerprint) {
                if (aByte != 0) {
                    throw new UnsupportedOperationException("non-zero fingerprint byte");
                }
            }
        }
        int keyNumber = bytes.readIntBE();
        if (depth == 0 && keyNumber != 0) {
            throw new UnsupportedOperationException("non-zero key number");
        }
        boolean isHardened = (keyNumber >>> 31) == 1;
        if (isHardened) {
            keyNumber = keyNumber << 1 >>> 1;
        }
        byte[] chainCode = new byte[32];
        bytes.readBytes(chainCode);
        if (bytes.read() != 0) {
            throw new UnsupportedOperationException("incorrect first byte of the key, requires 0x00");
        }
        byte[] key = new byte[32];
        bytes.readBytes(key);
        byte[] checksum = Sha256.getHash(Sha256.getHash(serialized, 0, 78));
        for (int i = 0; i < 4; i++) {
            if ((checksum[i] & 0xFF) != bytes.read()) {
                throw new UnsupportedOperationException("incorrect checksum");
            }
        }
        // path
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
     * @param keyNumber  number of the child
     * @param isHardened true if the child is hardened
     * @return child HdKeyPair created from this HdKeyPair
     * @throws UnsupportedOperationException if created private key is not valid or keyNumber < 0
     */
    public HdKeyPair generateChild(int keyNumber, boolean isHardened) {
        if (keyNumber < 0) {
            throw new UnsupportedOperationException("negative keyNumber");
        }
        byte[] data = new byte[37];
        // byte[33], key, for hardened first byte is zero, for non-hardened public use compressed public key
        // intBE, key number, += 2^31 for the hardened keys
        if (isHardened) {
            byte[] key = keyPair.d.toByteArray();
            // BigInteger byte array is the signed two's-complement representation, so the first byte can be 0
            int keyStart = key[0] == 0 ? 1 : 0;
            System.arraycopy(key, keyStart, data, 33 - (key.length - keyStart), key.length - keyStart);
        } else {
            byte[] key = keyPair.publicKey.toByteArray();
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
        if (privateKey.compareTo(Ecc.secp256k1.getN()) >= 0) {
            throw new UnsupportedOperationException("this key is not less than n");
        }
        privateKey = keyPair.d.add(privateKey).mod(Ecc.secp256k1.getN());
        if (privateKey.signum() <= 0) {
            throw new UnsupportedOperationException("this key is zero");
        }
        byte[] fingerprint = Arrays.copyOfRange(Ripemd160.getHash(Sha256.getHash(keyPair.publicKey.toByteArray())), 0, 4);
        String path = this.path + "/" + keyNumber;
        if (isHardened) {
            path += "'";
        }
        return new HdKeyPair(privateKey, path, depth + 1, keyNumber, isHardened, fingerprint, chainCode);
    }

    /**
     * @param path String containing relative path of this child as 11/3'/0h/55
     * @return child HdKeyPair created from this HdKeyPair
     * @throws NullPointerException          if path == null
     * @throws NumberFormatException         path is incorrect
     * @throws UnsupportedOperationException if created private key is not valid
     */
    public HdKeyPair generateChild(String path) {
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        HdKeyPair out = this;
        do {
            int stop = path.indexOf('/');
            if (stop == -1) {
                stop = path.length();
                if (stop == 0) {
                    return out;
                }
            }
            char lastChar = path.charAt(stop - 1);
            boolean hardened = lastChar == '\'' || lastChar == 'h';
            int keyNumber = Integer.parseInt(path.substring(0, hardened ? stop - 1 : stop));
            out = out.generateChild(keyNumber, hardened);
            if (stop == path.length()) {
                return out;
            }
            path = path.substring(stop + 1);
        } while (true);
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
            byte[] d = keyPair.d.toByteArray();
            // BigInteger byte array is the signed two's-complement representation, so the first byte can be 0
            int dStart = d[0] == 0 ? 1 : 0;
            byte[] key = new byte[33];
            System.arraycopy(d, dStart, key, 33 - (d.length - dStart), d.length - dStart);
            bytes.writeBytes(key, 0, 33);
        } else {
            bytes.writeBytes(keyPair.publicKey.toByteArray());
        }
        byte[] checksum = Sha256.getHash(Sha256.getHash(bytes.toByteArray()));
        bytes.writeBytes(checksum, 0, 4);
        return Base58.encode(bytes.toByteArray());
    }
}