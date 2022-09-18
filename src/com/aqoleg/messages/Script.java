/*
creates, prints, verifies scriptSig and scriptPubKey

usage:
    $ java com.aqoleg.messages.Script 473044022062a50c4373f0cad58787a4449e0a63a1cd19abe36a0e002a54487ff27d813668
     022026401ea091ac26bd09a2947acbe7a2b638cb2b98da19dc6c0e36de80a96430e90121039f843adf502df5c13c5f3a851a76bd9de
     980fbc63a84864b8d70a471030f7520

    Script script = new Script(byteArray);
    Script script = Script.fromBytesInput(bytesInput);
    Script scriptPubKey = Script.createScriptPubKey(address);
    Script scriptSig = previousScriptPublicKey.createScriptSig(messageHashBytes, keyPair);
    int type = script.type;
    Address address = scriptPubKey.address;
    String string = script.toString();
    int byteSize = script.getSize();
    int scriptSigByteSize = scriptPubKey.getScriptSigSize(isCompressedPublicKey);
    scriptSig.verify(prevScriptPublicKey, messageHashBytes);
    script.write(bytesOutput);

pay to public key, p2pk:
    scriptSig: sig
    scriptPubKey: pubKey checksig
pay to public key hash, p2pkh:
    scriptSig: sig pubKey
    scriptPubKey: dup hash160 pubKeyHash equalverify checksig
pay to script hash, p2sh:
    scriptSig: sig script
    scriptPubKey: hash160 scriptHash equal
*/

package com.aqoleg.messages;

import com.aqoleg.crypto.Ecc;
import com.aqoleg.crypto.Ripemd160;
import com.aqoleg.crypto.Sha256;
import com.aqoleg.keys.Address;
import com.aqoleg.keys.KeyPair;
import com.aqoleg.keys.PublicKey;
import com.aqoleg.utils.BytesInput;
import com.aqoleg.utils.BytesOutput;
import com.aqoleg.utils.Converter;

import java.math.BigInteger;
import java.util.HashMap;

public class Script {
    public static final int undefined = -1;
    public static final int p2pk = 0;
    public static final int p2pkh = 2;
    public static final int p2sh = 4;
    private static final HashMap<Integer, String> ops = new HashMap<>(); // all op codes

    public final int type;
    private final byte[] bytes;
    public final Address address; // null if scriptSig or undefined scriptPubKey

    static {
        ops.put(0x00, "0"); // pushes []
        // 0x01 - 0x4b, "", number of bytes (1 - 75) to push
        ops.put(0x4c, "pushdata1"); // next byte contains number of bytes to push
        ops.put(0x4d, "pushdata2"); // next 2 bytes (le) contains number of bytes to push
        ops.put(0x4e, "pushdata4"); // next 4 bytes (le) contains number of bytes to push
        ops.put(0x4f, "1negate"); // push -1
        ops.put(0x50, "reserved"); // fails
        // 0x51 - 0x60, "1 - 16", number (1 - 16) to push
        ops.put(0x61, "nop"); // does nothing
        ops.put(0x62, "ver"); // fails
        ops.put(0x63, "if");
        ops.put(0x64, "notif");
        ops.put(0x65, "verif"); // fails
        ops.put(0x66, "vernotif"); // fails
        ops.put(0x67, "else");
        ops.put(0x68, "endif");
        ops.put(0x69, "verify"); // removes top item if true, else fails
        ops.put(0x6a, "return"); // fails
        ops.put(0x6b, "toaltstack"); // moves top item from main stack to alt stack
        ops.put(0x6c, "fromaltstack"); // moves top item from alt stack to main stack
        ops.put(0x6d, "2drop"); // a b c -> a
        ops.put(0x6e, "2dup"); // a b -> a b a b
        ops.put(0x6f, "3dup"); // a b c -> a b c a b c
        ops.put(0x70, "2over"); // a b c d -> a b c d a b
        ops.put(0x71, "2rot"); // a b c d e f -> c d e f a b
        ops.put(0x72, "2swap"); // a b c d -> c d a b
        ops.put(0x73, "ifdup"); // duplicates if not zero
        ops.put(0x74, "depth"); // puts number of stack items
        ops.put(0x75, "drop"); // a b -> a
        ops.put(0x76, "dup"); // a -> a a
        ops.put(0x77, "nip"); // a b -> b
        ops.put(0x78, "over"); // a b -> a b a
        ops.put(0x79, "pick"); // a b c 1 -> a b c b
        ops.put(0x7a, "roll"); // a b c 1 -> a c b
        ops.put(0x7b, "rot"); // a b c -> b c a
        ops.put(0x7c, "swap"); // a b -> b a
        ops.put(0x7d, "tuck"); // a b -> b a b
        ops.put(0x7e, "cat"); // disabled; stringA stringB -> stringA + stringB
        ops.put(0x7f, "substr"); // disabled; string begin size -> string.substring(begin, begin + size)
        ops.put(0x80, "left"); // disabled; string size -> string.substring(0, size)
        ops.put(0x81, "right"); // disabled, string size -> string.substring(length - size, length)
        ops.put(0x82, "size"); // string -> string string.length
        ops.put(0x83, "invert"); // disabled; a -> ~a
        ops.put(0x84, "and"); // disabled; a b -> a & b
        ops.put(0x85, "or"); // disabled; a b -> a | b
        ops.put(0x86, "xor"); // disabled; a b -> a ^ b
        ops.put(0x87, "equal"); // a b -> a == b
        ops.put(0x88, "equalverify"); // removes 2 top items if they are equals, else fails
        ops.put(0x89, "reserved1"); // fails
        ops.put(0x8a, "reserved2"); // fails
        ops.put(0x8b, "1add"); // a -> a + 1
        ops.put(0x8c, "1sub"); // a -> a - 1
        ops.put(0x8d, "2mul"); // disabled; a -> a * 2
        ops.put(0x8e, "2div"); // disabled; a -> a / 2
        ops.put(0x8f, "negate"); // a -> -a
        ops.put(0x90, "abs"); // a -> |a|
        ops.put(0x91, "not"); // a -> a == 0
        ops.put(0x92, "0notequal"); // a -> a != 0
        ops.put(0x93, "add"); // a b -> a + b
        ops.put(0x94, "sub"); // a b -> a - b
        ops.put(0x95, "mul"); // disabled; a b -> a * b
        ops.put(0x96, "div"); // disabled; a b -> a / b
        ops.put(0x97, "mod"); // disabled; a b -> a % b
        ops.put(0x98, "lshift"); // disabled; a b -> a << b
        ops.put(0x99, "rshift"); // disabled; a b -> a >> b
        ops.put(0x9a, "booland"); // a b -> a && b
        ops.put(0x9b, "boolor"); // a b -> a || b
        ops.put(0x9c, "numequal"); // a b -> a == b
        ops.put(0x9d, "numequalverify"); // removes 2 top items if they are equals, else fails
        ops.put(0x9e, "numnotequal"); // a b -> a != b
        ops.put(0x9f, "lessthan"); // a b -> a < b
        ops.put(0xa0, "greaterthan"); // a b -> a > b
        ops.put(0xa1, "lessthanorequal"); // a b -> a <= b
        ops.put(0xa2, "greaterthanorequal"); // a b -> a >= b
        ops.put(0xa3, "min"); // a b -> Min(a, b)
        ops.put(0xa4, "max"); // a b -> Max(a, b)
        ops.put(0xa5, "within"); // a min max -> a >= min && a < max
        ops.put(0xa6, "ripemd160"); // a -> ripemd160(a)
        ops.put(0xa7, "sha1"); // a -> sha1(a)
        ops.put(0xa8, "sha256"); // a -> sha256(a)
        ops.put(0xa9, "hash160"); // a -> ripemd160(sha256(a))
        ops.put(0xaa, "hash256"); // a -> sha256(sha256(a))
        ops.put(0xab, "codeseparator"); // do not check signatures before this
        ops.put(0xac, "checksig"); // sig pubkey -> Boolean(verify())
        ops.put(0xad, "checksigverify"); // removes 2 top items if verifies, else fails
        ops.put(0xae, "checkmultisig"); // a sig0 ... sigN pubkey0 ... pubkeyM -> Boolean(verify())
        ops.put(0xaf, "checkmultisigverify"); // checkmultisig, then verify
        // 0xb0 - 0xb9, "nop", does nothing
    }

    /**
     * @param bytes byte array with script
     * @throws NullPointerException if bytes == null
     */
    public Script(byte[] bytes) {
        BytesInput bytesInput = new BytesInput(bytes);
        int type = undefined;
        Address address = null;
        try {
            int firstByte = bytesInput.read();
            if (firstByte == 0xa9) { // p2sh: hash160 scriptHash equal
                if (bytesInput.read() == 20) {
                    byte[] scriptHash = bytesInput.readBytes(new byte[20]);
                    if (bytesInput.read() == 0x87 && bytesInput.available() == 0) {
                        type = p2sh;
                        address = Address.createFromHash(scriptHash, false);
                    }
                }
            } else if (firstByte == 0x76) { // p2pkh: dup hash160 pubKeyHash equalverify checksig
                if (bytesInput.read() == 0xa9 && bytesInput.read() == 20) {
                    byte[] pubKeyHash = bytesInput.readBytes(new byte[20]);
                    if (bytesInput.read() == 0x88 && bytesInput.read() == 0xac && bytesInput.available() == 0) {
                        type = p2pkh;
                        address = Address.createFromHash(pubKeyHash, true);
                    }
                }
            } else if (firstByte == 33 || firstByte == 65) { // p2pk: pubKey checksig
                PublicKey pubKey = PublicKey.createFromBytes(bytesInput.readBytes(new byte[firstByte]));
                if (bytesInput.read() == 0xac && bytesInput.available() == 0) {
                    type = p2pk;
                    address = Address.createFromPublicKey(pubKey);
                }
            }
        } catch (IndexOutOfBoundsException | PublicKey.Exception ignored) {
        }
        this.type = type;
        this.bytes = bytes;
        this.address = address;
    }

    /**
     * @param bytesInput BytesInput to read script from, including script length
     * @return new created Script
     * @throws NullPointerException if bytesInput == null
     * @throws Message.Exception    if script is incorrect
     */
    public static Script fromBytesInput(BytesInput bytesInput) {
        try {
            byte[] bytes = bytesInput.readBytes(new byte[(int) bytesInput.readVariableLengthInt()]);
            return new Script(bytes);
        } catch (IndexOutOfBoundsException | NegativeArraySizeException exception) {
            throw new Message.Exception("short script " + exception.getMessage());
        }
    }

    /**
     * @param address Address for which will be created scriptPubKey
     * @return p2pkh or p2sh scriptPubKey
     * @throws NullPointerException if address == null
     */
    public static Script createScriptPubKey(Address address) {
        BytesOutput bytesOutput = new BytesOutput();
        byte[] hash = address.getHash();
        if (address.p2pkh) {
            // p2pkh: dup hash160 pubKeyHash equalverify checksig
            bytesOutput.write(0x76);
            bytesOutput.write(0xa9);
            bytesOutput.write(hash.length);
            bytesOutput.writeBytes(hash);
            bytesOutput.write(0x88);
            bytesOutput.write(0xac);
        } else {
            // p2sh: hash160 scriptHash equal
            bytesOutput.write(0xa9);
            bytesOutput.write(hash.length);
            bytesOutput.writeBytes(hash);
            bytesOutput.write(0x87);
        }
        return new Script(bytesOutput.toByteArray());
    }

    /**
     * prints info about entered script
     *
     * @param args String raw hex script or empty
     */
    public static void main(String[] args) {
        if (args.length == 1) {
            System.out.println(toString(args[0]));
        } else if (System.console() == null) {
            System.out.println(toString(""));
        } else {
            while (true) {
                String input = System.console().readLine("enter raw hex script or 'exit': ");
                if (input.equals("exit")) {
                    System.exit(0);
                }
                System.out.println(toString(input));
            }
        }
    }

    /**
     * @return script op codes and data
     */
    @Override
    public String toString() {
        try {
            return read();
        } catch (Message.Exception exception) {
            return Converter.bytesToString(bytes) + " (" + exception.getMessage() + ')';
        }
    }

    /**
     * @return length in bytes
     */
    public int getSize() {
        return bytes.length;
    }

    /**
     * @param isCompressed true if public key from redeemed scriptPubKey is compressed
     * @return length in bytes of the scriptSig for redeem this scriptPubKey
     * @throws Message.Exception if script type is unsupported
     */
    public int getScriptSigSize(boolean isCompressed) {
        if (type != p2pk && type != p2pkh) {
            throw new Message.Exception("unsupported type");
        }
        int sigLength = 8 + 32 + 32;
        if (type == p2pkh) {
            sigLength += isCompressed ? 34 : 66;
        }
        return sigLength;
    }

    /**
     * @param messageHash hash of the message to sign
     * @param keyPair     KeyPair to sign with
     * @return p2pk or p2pkh scriptSig
     * @throws NullPointerException if messageHash == null or keyPair == null
     * @throws Message.Exception    if this scriptPublicKey is unsupported or messageHash is too big
     */
    public Script createScriptSig(byte[] messageHash, KeyPair keyPair) {
        if (type != p2pk && type != p2pkh) {
            throw new Message.Exception("unsupported type");
        }
        BigInteger[] rs;
        try {
            rs = Ecc.sign(messageHash, keyPair.privateKey);
        } catch (Ecc.Exception exception) {
            throw new Message.Exception(exception.getMessage());
        }
        byte[] signature = new Signature(rs).encode();
        BytesOutput bytesOutput = new BytesOutput();
        // p2pk: sig
        bytesOutput.write(signature.length);
        bytesOutput.writeBytes(signature);
        if (type == p2pkh) {
            // p2pkh: sig pubKey
            byte[] pubKey = keyPair.publicKey.toByteArray();
            bytesOutput.write(pubKey.length);
            bytesOutput.writeBytes(pubKey);
        }
        return new Script(bytesOutput.toByteArray());
    }

    /**
     * verifies this scriptSig to redeem scriptPublicKey
     *
     * @param previousScriptPublicKey scriptPubKey of the previous transaction
     * @param messageHash             hash of the message to verify
     * @throws NullPointerException if previousScriptPubKey == null or messageHash == null
     * @throws Message.Exception    if script is not verified
     */
    public void verify(Script previousScriptPublicKey, byte[] messageHash) {
        if (previousScriptPublicKey.type == p2pk) {
            // scriptPubKey: pubKey checksig
            BytesInput bytesInput = new BytesInput(previousScriptPublicKey.bytes);
            PublicKey publicKey = PublicKey.createFromBytes(bytesInput.readBytes(new byte[bytesInput.read()]));
            // scriptSig: sig
            bytesInput = new BytesInput(bytes);
            Object next = readNext(bytesInput);
            Signature signature;
            try {
                signature = new Signature((byte[]) next);
            } catch (ClassCastException | NullPointerException exception) {
                throw new Message.Exception("has no signature: " + next);
            }
            if (signature.signHash != Signature.signHashAll) {
                throw new Message.Exception("unsupported signHash " + signature.signHash);
            }
            if (bytesInput.available() != 0) {
                throw new Message.Exception("big scriptSig length");
            }

            try {
                Ecc.verify(messageHash, publicKey.point, new BigInteger[]{signature.r, signature.s});
            } catch (Ecc.Exception exception) {
                throw new Message.Exception(exception.getMessage());
            }
        } else if (previousScriptPublicKey.type == p2pkh) {
            // scriptSig: sig pubKey
            BytesInput bytesInput = new BytesInput(bytes);
            Object next = readNext(bytesInput);
            Signature signature;
            try {
                signature = new Signature((byte[]) next);
            } catch (ClassCastException | NullPointerException ignored) {
                throw new Message.Exception("has no signature: " + next);
            }
            if (signature.signHash != Signature.signHashAll) {
                throw new Message.Exception("unsupported signHash " + signature.signHash);
            }
            next = readNext(bytesInput);
            PublicKey publicKey;
            try {
                publicKey = PublicKey.createFromBytes((byte[]) next);
            } catch (ClassCastException | NullPointerException ignored) {
                throw new Message.Exception("has no public key: " + next);
            } catch (PublicKey.Exception exception) {
                throw new Message.Exception("incorrect public key: " + exception.getMessage());
            }
            if (bytesInput.available() != 0) {
                throw new Message.Exception("big scriptSig length");
            }

            if (!Address.createFromPublicKey(publicKey).equals(previousScriptPublicKey.address)) {
                throw new Message.Exception("incorrect pubKeyHash");
            }
            try {
                Ecc.verify(messageHash, publicKey.point, new BigInteger[]{signature.r, signature.s});
            } catch (Ecc.Exception exception) {
                throw new Message.Exception(exception.getMessage());
            }
        } else if (previousScriptPublicKey.type == p2sh) {
            // scriptSig: sig script
            BytesInput bytesInput = new BytesInput(bytes);
            Object next = readNext(bytesInput);
            Signature signature;
            try {
                signature = new Signature((byte[]) next);
            } catch (ClassCastException | NullPointerException ignored) {
                throw new Message.Exception("has no signature: " + next);
            }
            if (signature.signHash != Signature.signHashAll) {
                throw new Message.Exception("unsupported signHash " + signature.signHash);
            }
            next = readNext(bytesInput);
            if (!(next instanceof byte[])) {
                throw new Message.Exception("has no script: " + next);
            }
            if (bytesInput.available() != 0) {
                throw new Message.Exception("big scriptSig length");
            }

            Address address = Address.createFromHash(Ripemd160.getHash(Sha256.getHash((byte[]) next)), false);
            if (!address.equals(previousScriptPublicKey.address)) {
                throw new Message.Exception("incorrect scriptHash");
            }
        } else {
            throw new Message.Exception("unsupported type");
        }
    }

    /**
     * write this Script including script length into the BytesOutput
     *
     * @param bytesOutput BytesOutput in which will be written this Script
     * @throws NullPointerException if bytesOutput == null
     */
    public void write(BytesOutput bytesOutput) {
        bytesOutput.writeVariableLength(bytes.length);
        bytesOutput.writeBytes(bytes);
    }

    // returns String or throws Message.Exception
    private String read() {
        StringBuilder out = new StringBuilder();
        BytesInput bytesInput = new BytesInput(bytes);
        Object next;
        do {
            next = readNext(bytesInput);
            if (next == null) {
                return out.toString();
            } else if (out.length() > 0) {
                out.append(' ');
            }
            if (next instanceof String) {
                out.append(next);
                continue;
            }
            byte[] data = (byte[]) next;
            if (type == p2pkh) {
                out.append("pubKeyHash(").append(address).append(')');
                continue;
            } else if (type == p2sh) {
                out.append("scriptHash(").append(address).append(')');
                continue;
            } else if (type == p2pk) {
                out.append("pubKey(").append(address).append(')');
                continue;
            }
            try {
                PublicKey publicKey = PublicKey.createFromBytes(data);
                out.append("pubKey(").append(Address.createFromPublicKey(publicKey)).append(')');
                continue;
            } catch (PublicKey.Exception ignored) {
            }
            try {
                out.append(new Signature(data));
                continue;
            } catch (Message.Exception ignored) {
            }
            try {
                String script = new Script(data).read();
                out.append("script(").append(script).append(')');
            } catch (Message.Exception ignored) {
                out.append("data(").append(Converter.bytesToString(data)).append(')');
            }
        } while (true);
    }

    // returns null, String, byte[] or throws Message.Exception
    private static Object readNext(BytesInput bytesInput) {
        if (bytesInput.available() == 0) {
            return null;
        }
        int aByte = bytesInput.read();
        byte[] pushData;
        if (aByte >= 0x01 && aByte <= 0x4b) { // number of bytes (1 - 75) to push
            try {
                pushData = bytesInput.readBytes(new byte[aByte]);
            } catch (IndexOutOfBoundsException exception) {
                throw new Message.Exception("incorrect push: " + exception.getMessage());
            }
        } else if (aByte == 0x4c) { // next byte contains number of bytes to push
            try {
                pushData = bytesInput.readBytes(new byte[bytesInput.read()]);
            } catch (NegativeArraySizeException | IndexOutOfBoundsException exception) {
                throw new Message.Exception("incorrect pushdata1: " + exception.getMessage());
            }
        } else if (aByte == 0x4d) { // next 2 bytes (le) contains number of bytes to push
            try {
                pushData = bytesInput.readBytes(new byte[bytesInput.read() | bytesInput.read() << 8]);
            } catch (NegativeArraySizeException | IndexOutOfBoundsException exception) {
                throw new Message.Exception("incorrect pushdata2: " + exception.getMessage());
            }
        } else if (aByte == 0x4e) { // next 4 bytes (le) contains number of bytes to push
            try {
                pushData = bytesInput.readBytes(new byte[bytesInput.readIntLE()]);
            } catch (NegativeArraySizeException | IndexOutOfBoundsException exception) {
                throw new Message.Exception("incorrect pushdata4: " + exception.getMessage());
            }
        } else if (aByte >= 0x51 && aByte <= 0x60) { // 0x51 - 0x60, "1 - 16", number (1 - 16) to push
            return String.valueOf(aByte - 0x50);
        } else if (aByte >= 0xb0 && aByte <= 0xb9) { // nop
            return "nop(0x" + Integer.toHexString(aByte) + ")";
        } else {
            String value = ops.get(aByte);
            if (value == null) {
                throw new Message.Exception("incorrect opcode 0x" + Integer.toHexString(aByte));
            }
            return value;
        }
        return pushData;
    }

    private static String toString(String input) {
        try {
            return new Script(Converter.hexToBytes(input)).toString();
        } catch (Converter.Exception exception) {
            return "incorrect hex '" + input + "': " + exception.getMessage();
        } catch (Message.Exception exception) {
            return "incorrect script '" + input + "': " + exception.getMessage();
        }
    }

    /*
    signature bytes:
        der encoded signature:
            1 byte, type, object
            1 byte, length of the object
            1 byte, type, integer
            1 byte, length of the integer
            byte[length], r, big-endian, signed, two's complement
            1 byte, type, integer
            1 byte, length of the integer
            byte[length], s, big-endian, signed, two's complement
        1 byte, signHash
    */
    private static class Signature {
        private static final int typeObject = 0x30;
        private static final int typeInteger = 0x02;
        private static final int signHashAll = 1;

        private final BigInteger r;
        private final BigInteger s;
        private final int signHash;

        private Signature(BigInteger[] rs) {
            r = rs[0];
            s = rs[1];
            signHash = signHashAll;
        }

        // decode signature or throws Message.Exception
        private Signature(byte[] bytes) {
            BytesInput bytesInput = new BytesInput(bytes);
            if (bytesInput.read() != typeObject) {
                throw new Message.Exception("incorrect object type");
            }
            if (bytesInput.read() != bytes.length - 3) { // object, this and signHash
                throw new Message.Exception("incorrect object length");
            }
            if (bytesInput.read() != typeInteger) {
                throw new Message.Exception("incorrect r type");
            }
            try {
                r = new BigInteger(bytesInput.readBytes(new byte[bytesInput.read()]));
            } catch (NegativeArraySizeException | IndexOutOfBoundsException | NumberFormatException exception) {
                throw new Message.Exception("incorrect r: " + exception.getMessage());
            }
            if (bytesInput.read() != typeInteger) {
                throw new Message.Exception("incorrect s type");
            }
            try {
                s = new BigInteger(bytesInput.readBytes(new byte[bytesInput.read()]));
            } catch (NegativeArraySizeException | IndexOutOfBoundsException | NumberFormatException exception) {
                throw new Message.Exception("incorrect s: " + exception.getMessage());
            }
            signHash = bytesInput.read();
            if (bytesInput.available() != 0) {
                throw new Message.Exception("big length");
            }
        }

        @Override
        public String toString() {
            return "sig(r: " + r.toString(16) + ", s: " + s.toString(16) + ", signHash: " + signHash + ")";
        }

        private byte[] encode() {
            byte[] rBytes = r.toByteArray();
            byte[] sBytes = s.toByteArray();
            BytesOutput bytesOutput = new BytesOutput();
            bytesOutput.write(typeObject);
            bytesOutput.write(rBytes.length + sBytes.length + 4); // r type, r length, s type, s length
            bytesOutput.write(typeInteger);
            bytesOutput.write(rBytes.length);
            bytesOutput.writeBytes(rBytes);
            bytesOutput.write(typeInteger);
            bytesOutput.write(sBytes.length);
            bytesOutput.writeBytes(sBytes);
            bytesOutput.write(signHash);
            return bytesOutput.toByteArray();
        }
    }
}