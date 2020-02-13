// scriptSig and scriptPubKey
//
// pay to public key, p2pk:
//    scriptSig: <sig>
//    scriptPubKey: <pubKey> checksig
// pay to public key hash, p2pkh:
//    scriptSig: <sig> <pubKey>
//    scriptPubKey: dup hash160 <pubKeyHash> equalverify checksig
// pay to script hash, p2sh:
//    scriptSig: <sig> <script>
//    scriptPubKey: hash160 <scriptHash> equal
// p2sh multiSig:
//    scriptSig: 0 <sig_1>...<sig_m> <script>
//    script: m <pubKey_1>...<pubKey_n> n checkmultisig
//
// signature bytes:
//    der encoded signature:
//       1 byte, type, object
//       1 byte, length of the object
//       1 byte, type, integer
//       1 byte, length of the integer
//       byte[length], r, big-endian, signed, two's complement
//       1 byte, type, integer
//       1 byte, length of the integer
//       byte[length], s, big-endian, signed, two's complement
//    1 byte, signHash
package space.aqoleg.messages;

import space.aqoleg.crypto.Ecc;
import space.aqoleg.crypto.Ripemd160;
import space.aqoleg.crypto.Sha256;
import space.aqoleg.keys.Address;
import space.aqoleg.keys.KeyPair;
import space.aqoleg.keys.PublicKey;
import space.aqoleg.utils.BytesInput;
import space.aqoleg.utils.BytesOutput;

import java.math.BigInteger;
import java.util.Arrays;

public class Script {
    static final int signHashAll = 1;
    // types
    public static final int undefined = 0;
    public static final int p2pk = 1;
    public static final int p2pkh = 2;
    public static final int p2sh = 3;
    // op codes
    private static final int zero = 0x00; // push zero array
    private static final int pushMax = 0x4b; // the next 0x01...0x4b bytes is data to be pushed
    private static final int pushData1 = 0x4c; // the next 1 byte contains the number of bytes to be pushed
    private static final int pushData2 = 0x4d; // the next 2 bytes, little-endian, contains the number of bytes to be pushed
    private static final int dup = 0x76; // <a> --> <a> <a>
    private static final int equal = 0x87; // <a> <b> --> <a> == <b> ? 1 : 0
    private static final int equalverify = 0x88; // <a> <b> --> <a> == <b> ? null : exception
    private static final int hash160 = 0xA9; // <a> --> ripemd160(sha256(<a>))
    private static final int checksig = 0xAC; // <sig> <pubKey> --> verify ? 1 : 0
    // der codes
    private static final int object = 0x30;
    private static final int integer = 0x02;

    /**
     * @param scriptPubKey array containing scriptPubKey
     * @return type of the scriptPubKey: undefined, p2pk, p2pkh or p2sh
     * @throws NullPointerException if scriptPubKey == null
     */
    public static int determineType(byte[] scriptPubKey) {
        BytesInput bytes = new BytesInput(scriptPubKey);
        int firstByte = bytes.read();
        if (firstByte == hash160) {
            // p2sh: hash160 <20 bytes> equal
            if (bytes.read() == 20 && bytes.skip(20) == 20 && bytes.read() == equal && bytes.available() == 0) {
                return p2sh;
            }
        } else if (firstByte == dup) {
            // p2pkh: dup hash160 <20 bytes> equalverify checksig
            if (bytes.read() == hash160 && bytes.read() == 20 && bytes.skip(20) == 20 &&
                    bytes.read() == equalverify && bytes.read() == checksig && bytes.available() == 0) {
                return p2pkh;
            }
        } else if (firstByte == 33 || firstByte == 65) {
            // p2pk: <33 or 65 bytes> checksig
            if (bytes.skip(firstByte) == firstByte && bytes.read() == checksig && bytes.available() == 0) {
                return p2pk;
            }
        }
        return undefined;
    }

    /**
     * @param address Address for which will be created scriptPubKey
     * @return byte array containing p2pkh or p2sh scriptPubKey
     * @throws NullPointerException if address == null
     */
    public static byte[] createScriptPubKey(Address address) {
        BytesOutput bytes = new BytesOutput();
        byte[] hash = address.getHash();
        if (address.p2pkh) {
            // p2pkh: dup hash160 <pubKeyHash> equalverify checksig
            bytes.write(dup);
            bytes.write(hash160);
            bytes.write(hash.length);
            bytes.writeBytes(hash);
            bytes.write(equalverify);
            bytes.write(checksig);
        } else {
            // p2sh: hash160 <scriptHash> equal
            bytes.write(hash160);
            bytes.write(hash.length);
            bytes.writeBytes(hash);
            bytes.write(equal);
        }
        return bytes.toByteArray();
    }

    /**
     * @param previousTxType type of the redeemed scriptPubKey
     * @param messageHash    hash of the message to sign
     * @param keyPair        KeyPair to sign with
     * @return byte array with p2pk or p2pkh scriptSig
     * @throws NullPointerException          if messageHash == null or previousScriptPubKey == null or keyPair == null
     * @throws UnsupportedOperationException if transaction type does not support or messageHash is too big
     */
    public static byte[] createScriptSig(int previousTxType, byte[] messageHash, KeyPair keyPair) {
        if (previousTxType != p2pk && previousTxType != p2pkh) {
            throw new UnsupportedOperationException("unsupported transaction type");
        }
        byte[] signature = encodeSignature(Ecc.secp256k1.sign(messageHash, keyPair.d));
        BytesOutput bytes = new BytesOutput();
        // scriptSig: <sig>
        bytes.write(signature.length);
        bytes.writeBytes(signature);
        if (previousTxType == p2pkh) {
            // scriptSig: <sig> <pubKey>
            byte[] pubKey = keyPair.publicKey.toByteArray();
            bytes.write(pubKey.length);
            bytes.writeBytes(pubKey);
        }
        return bytes.toByteArray();
    }

    /**
     * @param messageHash          hash of the message to verify
     * @param previousScriptPubKey scriptPubKey of the previous transaction
     * @param scriptSig            scriptSig to verify
     * @return true if message is verified
     * @throws NullPointerException          if messageHash == null or previousScriptPubKey == null or scriptSig == null
     * @throws NegativeArraySizeException    if scriptSig is incorrect
     * @throws UnsupportedOperationException if transaction type does not support or scriptSig is incorrect
     */
    public static boolean verify(byte[] messageHash, byte[] previousScriptPubKey, byte[] scriptSig) {
        int type = determineType(previousScriptPubKey);
        if (type == undefined) {
            throw new UnsupportedOperationException("unsupported transaction type");
        }
        if (type == p2pk) {
            // scriptPubKey: <pubKey> checksig
            byte[] publicKey = Arrays.copyOfRange(previousScriptPubKey, 1, 1 + previousScriptPubKey[0]);
            // scriptSig: <sig>
            BytesInput bytes = new BytesInput(scriptSig);
            byte[] signature = new byte[bytes.read()];
            bytes.readBytes(signature);
            if (bytes.available() != 0) {
                throw new UnsupportedOperationException("scriptSig length is incorrect");
            }
            return Ecc.secp256k1.verify(
                    messageHash,
                    PublicKey.createFromBytes(publicKey).point,
                    decodeSignature(signature)
            );
        } else if (type == p2pkh) {
            // scriptPubKey: dup hash160 <pubKeyHash> equalverify checksig
            byte[] publicKeyHash = Arrays.copyOfRange(previousScriptPubKey, 3, 23);
            // scriptSig: <sig> <pubKey>
            BytesInput bytes = new BytesInput(scriptSig);
            byte[] signature = new byte[bytes.read()];
            bytes.readBytes(signature);
            byte[] publicKey = new byte[bytes.read()];
            bytes.readBytes(publicKey);
            if (bytes.available() != 0) {
                throw new UnsupportedOperationException("scriptSig length is incorrect");
            }
            return Arrays.equals(publicKeyHash, Ripemd160.getHash(Sha256.getHash(publicKey))) &&
                    Ecc.secp256k1.verify(
                            messageHash,
                            PublicKey.createFromBytes(publicKey).point,
                            decodeSignature(signature)
                    );
        } else {
            // scriptPubKey: hash160 <scriptHash> equal
            byte[] scriptHash = Arrays.copyOfRange(previousScriptPubKey, 2, 22);
            // scriptSig: 0 <sig_1>...<sig_m> <script>
            byte[] script = {};
            BytesInput bytes = new BytesInput(scriptSig);
            do {
                int push = bytes.read();
                if (push == zero) {
                    continue;
                } else if (push == pushData1) {
                    push = bytes.read();
                } else if (push == pushData2) {
                    push = bytes.read() | bytes.read() << 8;
                } else if (push < zero || push > pushMax) {
                    throw new UnsupportedOperationException("push code is incorrect");
                }
                script = new byte[push];
                bytes.readBytes(script);
            } while (bytes.available() != 0);
            return Arrays.equals(scriptHash, Ripemd160.getHash(Sha256.getHash(script)));
        }
    }

    // returns [r, s]
    private static BigInteger[] decodeSignature(byte[] signature) {
        BytesInput bytes = new BytesInput(signature);
        if (bytes.read() != object) {
            throw new UnsupportedOperationException("der object type is incorrect");
        }
        if (bytes.read() != signature.length - 3) { // object, this and sign hash data
            throw new UnsupportedOperationException("der length is incorrect");
        }
        if (bytes.read() != integer) {
            throw new UnsupportedOperationException("der r type is incorrect");
        }
        byte[] rBytes = new byte[bytes.read()];
        bytes.readBytes(rBytes);
        if (bytes.read() != integer) {
            throw new UnsupportedOperationException("der s type is incorrect");
        }
        byte[] sBytes = new byte[bytes.read()];
        bytes.readBytes(sBytes);
        // sign hash data
        if (bytes.read() != signHashAll) {
            throw new UnsupportedOperationException("sign hash data is unaccepted, requires " + signHashAll);
        }
        if (bytes.available() != 0) {
            throw new UnsupportedOperationException("the length is incorrect");
        }
        return new BigInteger[]{new BigInteger(rBytes), new BigInteger(sBytes)};
    }

    // returns der-encoded signature and sign data
    private static byte[] encodeSignature(BigInteger[] rs) {
        byte[] rBytes = rs[0].toByteArray();
        byte[] sBytes = rs[1].toByteArray();
        BytesOutput bytes = new BytesOutput();
        bytes.write(object);
        bytes.write(rBytes.length + sBytes.length + 4); // r type, r length, s type, s length
        bytes.write(integer);
        bytes.write(rBytes.length);
        bytes.writeBytes(rBytes);
        bytes.write(integer);
        bytes.write(sBytes.length);
        bytes.writeBytes(sBytes);
        // sign hash data
        bytes.write(signHashAll);
        return bytes.toByteArray();
    }
}