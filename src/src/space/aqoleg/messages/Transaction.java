// tx message, create, parse, sign and verify transactions
//
// payload bytes:
//    intLE, version
//    varInt, #vin
//    vin[]:
//       byte[32], previousTransactionHash
//       intLE, previousOutIndex
//       varInt, scriptSigLen
//       byte[scriptSigLen], scriptSig
//       intLE, sequence
//    varInt, #vout
//    vout[]:
//       longLE, value
//       varInt, scriptPubKeyLen
//       byte[scriptPubKeyLen], scriptPubKey
//    intLE, lockTime

package space.aqoleg.messages;

import space.aqoleg.crypto.Sha256;
import space.aqoleg.keys.Address;
import space.aqoleg.keys.KeyPair;
import space.aqoleg.utils.BytesInput;
import space.aqoleg.utils.BytesOutput;

import java.util.ArrayList;
import java.util.Arrays;

public class Transaction {
    public static final String command = "tx";
    private int version = 1;
    private final ArrayList<Input> inputs = new ArrayList<>();
    private final ArrayList<Output> outputs = new ArrayList<>();
    private int lockTime = 0;

    /**
     * @param payload byte array with payload
     * @return instance of Transaction created from payload
     * @throws NullPointerException          if payload == null
     * @throws IndexOutOfBoundsException     if payload is incorrect
     * @throws UnsupportedOperationException if payload is incorrect
     */
    public static Transaction parse(byte[] payload) {
        Transaction transaction = new Transaction();
        BytesInput bytes = new BytesInput(payload);
        transaction.version = bytes.readIntLE();
        int count = (int) bytes.readVariableLengthInt();
        for (int i = 0; i < count; i++) {
            Input input = transaction.new Input();
            bytes.readBytes(input.previousTransactionHash); // final 32 byte array
            input.previousOutIndex = bytes.readIntLE();
            input.scriptSig = new byte[(int) bytes.readVariableLengthInt()];
            bytes.readBytes(input.scriptSig);
            input.sequence = bytes.readIntLE();
        }
        count = (int) bytes.readVariableLengthInt();
        for (int i = 0; i < count; i++) {
            Output output = transaction.new Output();
            output.value = bytes.readLongLE();
            output.scriptPubKey = new byte[(int) bytes.readVariableLengthInt()];
            bytes.readBytes(output.scriptPubKey);
        }
        transaction.lockTime = bytes.readIntLE();
        if (bytes.available() != 0) {
            throw new UnsupportedOperationException("payload length is incorrect");
        }
        return transaction;
    }

    /**
     * @return version
     */
    public int getVersion() {
        return version;
    }

    /**
     * @return number of the Inputs
     */
    public int getInputsN() {
        return inputs.size();
    }

    /**
     * @param inputN number of the Input to be returned
     * @return Input with specified inputN
     * @throws IndexOutOfBoundsException if inputN is incorrect
     */
    public Input getInput(int inputN) {
        return inputs.get(inputN);
    }

    /**
     * @return number of the Outputs
     */
    public int getOutputsN() {
        return outputs.size();
    }

    /**
     * @param outputN number of the Output to be returned
     * @return Output with specified outputN
     * @throws IndexOutOfBoundsException if outputN is incorrect
     */
    public Output getOutput(int outputN) {
        return outputs.get(outputN);
    }

    /**
     * @return lock time
     */
    public int getLockTime() {
        return lockTime;
    }

    /**
     * @param previousTransactionHash 32 byte array with the hash of the previous transaction
     * @param bigEndian               endianness of the previousTransactionHash
     *                                big-endian in the raw transactions, little-endian in the explorers
     * @param previousOutIndex        index of the output in the previous transaction
     * @param previousScriptPubKey    scriptPubKey of the previous output or null
     * @param keyPair                 KeyPair to sign this Input or null
     * @return number of the new Input
     * @throws NullPointerException          if previousTransactionHash == null
     * @throws UnsupportedOperationException if previousTransactionHash.length != 32 or previousOutIndex < 0
     */
    public int addInput(
            byte[] previousTransactionHash,
            boolean bigEndian,
            int previousOutIndex,
            byte[] previousScriptPubKey,
            KeyPair keyPair
    ) {
        if (previousTransactionHash.length != 32) {
            throw new UnsupportedOperationException("the length of the previousTransactionHash is incorrect, requires 32");
        }
        if (previousOutIndex < 0) {
            throw new UnsupportedOperationException("the previousOutIndex is negative");
        }
        Input input = new Input();
        if (bigEndian) {
            System.arraycopy(previousTransactionHash, 0, input.previousTransactionHash, 0, 32);
        } else {
            for (int i = 0; i < 32; i++) {
                input.previousTransactionHash[i] = previousTransactionHash[31 - i];
            }
        }
        input.previousOutIndex = previousOutIndex;
        if (previousScriptPubKey != null) {
            input.previousScriptPubKey = Arrays.copyOf(previousScriptPubKey, previousScriptPubKey.length);
        }
        input.keyPair = keyPair;
        return input.index;
    }

    /**
     * @param value        satoshi
     * @param scriptPubKey bytes containing scriptPubKey
     * @return number of the new Output
     * @throws NullPointerException          if scriptPubKey == null
     * @throws UnsupportedOperationException if value < 0
     */
    public int addOutput(long value, byte[] scriptPubKey) {
        if (value < 0) {
            throw new UnsupportedOperationException("the value is negative");
        }
        Output output = new Output();
        output.value = value;
        output.scriptPubKey = Arrays.copyOf(scriptPubKey, scriptPubKey.length);
        return output.index;
    }

    /**
     * @param value   satoshi
     * @param address to which will be transferred satoshi
     * @return number of the new Output
     * @throws NullPointerException          if address == null
     * @throws UnsupportedOperationException if value < 0
     */
    public int addOutput(long value, Address address) {
        if (value < 0) {
            throw new UnsupportedOperationException("the value is negative");
        }
        Output output = new Output();
        output.value = value;
        output.scriptPubKey = Script.createScriptPubKey(address);
        return output.index;
    }

    /**
     * sign each input with not null keyPair and previousScriptPubKey
     *
     * @return byte array containing this transaction
     * @throws UnsupportedOperationException if transaction does not signed or transaction type does not support
     */
    public byte[] getPayload() {
        for (Input input : inputs) {
            input.sign();
        }
        return toByteArray(-1, null);
    }

    /**
     * @param transaction byte array containing the transaction
     * @param bigEndian   endianness of the hash
     *                    big-endian in the raw transactions, little-endian in the explorers
     * @return 32-byte hash of the transaction
     * @throws NullPointerException if transaction == null
     */
    public static byte[] getHash(byte[] transaction, boolean bigEndian) {
        byte[] hash = Sha256.getHash(Sha256.getHash(transaction));
        if (bigEndian) {
            return hash;
        } else {
            byte[] out = new byte[32];
            for (int i = 0; i < 32; i++) {
                out[i] = hash[31 - i];
            }
            return out;
        }
    }

    // if signedInputN < 0 create final transaction, else create transaction for signing this input
    private byte[] toByteArray(int signedInputN, byte[] previousScriptPubKey) {
        BytesOutput bytes = new BytesOutput();
        bytes.writeIntLE(version);
        int count = inputs.size();
        bytes.writeVariableLength(count);
        for (int i = 0; i < count; i++) {
            Input input = getInput(i);
            bytes.writeBytes(input.previousTransactionHash);
            bytes.writeIntLE(input.previousOutIndex);
            if (signedInputN < 0) {
                // not signing, put current scriptSig
                if (input.scriptSig == null) {
                    throw new UnsupportedOperationException("there is no signature " + i + ", sign it first");
                }
                bytes.writeVariableLength(input.scriptSig.length);
                bytes.writeBytes(input.scriptSig);
            } else if (i == signedInputN) {
                // signing this input, put previousScriptPubKey
                bytes.writeVariableLength(previousScriptPubKey.length);
                bytes.writeBytes(previousScriptPubKey);
            } else {
                // signing other input, put zero array
                bytes.write(0x00);
            }
            bytes.writeIntLE(input.sequence);
        }
        count = outputs.size();
        bytes.writeVariableLength(count);
        for (int i = 0; i < count; i++) {
            Output output = getOutput(i);
            bytes.writeLongLE(output.value);
            bytes.writeVariableLength(output.scriptPubKey.length);
            bytes.writeBytes(output.scriptPubKey);
        }
        bytes.writeIntLE(lockTime);
        // hash code type
        if (signedInputN >= 0) {
            bytes.writeIntLE(Script.signHashAll);
        }
        return bytes.toByteArray();
    }

    public class Input {
        public final int index; // in the inputs array
        private final byte[] previousTransactionHash = new byte[32]; // big-endian, as output of the hash function
        private int previousOutIndex;
        private byte[] previousScriptPubKey; // can be null
        private KeyPair keyPair; // can be null
        private byte[] scriptSig; // can be null
        private int sequence = 0xFFFFFFFF;

        private Input() {
            index = inputs.size();
            inputs.add(this);
        }

        /**
         * @param bigEndian endianness of the previousTransactionHash
         *                  big-endian in the raw transactions, little-endian in the explorers
         * @return copy of the hash of the previous transaction
         */
        public byte[] getPreviousTransactionHash(boolean bigEndian) {
            if (bigEndian) {
                return Arrays.copyOf(previousTransactionHash, 32);
            } else {
                byte[] out = new byte[32];
                for (int i = 0; i < 32; i++) {
                    out[i] = previousTransactionHash[31 - i];
                }
                return out;
            }
        }

        /**
         * @return index of the output in the previous transaction
         */
        public int getPreviousOutIndex() {
            return previousOutIndex;
        }

        /**
         * @param previousScriptPubKey array to be set as scriptPubKey of the previous output, or null
         */
        public void setPreviousScriptPubKey(byte[] previousScriptPubKey) {
            if (previousScriptPubKey == null) {
                this.previousScriptPubKey = null;
            } else {
                this.previousScriptPubKey = Arrays.copyOf(previousScriptPubKey, previousScriptPubKey.length);
            }
        }

        /**
         * @param keyPair KeyPair for signing this input, or null
         */
        public void setKeyPair(KeyPair keyPair) {
            this.keyPair = keyPair;
        }

        /**
         * @param scriptSig array to be set as scripSig of this Input, or null
         */
        public void setScriptSig(byte[] scriptSig) {
            if (scriptSig == null) {
                this.scriptSig = null;
            } else {
                this.scriptSig = Arrays.copyOf(scriptSig, scriptSig.length);
            }
        }

        /**
         * @return copy of the scriptSig or null
         */
        public byte[] getScriptSig() {
            if (scriptSig == null) {
                return null;
            } else {
                return Arrays.copyOf(scriptSig, scriptSig.length);
            }
        }

        /**
         * @return sequence
         */
        public int getSequence() {
            return sequence;
        }

        /**
         * @param previousScriptPubKey scriptPubKey of the previous output or null
         * @return true if this input is verified
         * @throws NullPointerException          if previousScriptPubKey == null and this.previousScriptPubKey == null
         * @throws UnsupportedOperationException if can not verify or there is no signature
         */
        public boolean verify(byte[] previousScriptPubKey) {
            if (scriptSig == null) {
                throw new UnsupportedOperationException("there is no signature to verify");
            }
            if (previousScriptPubKey != null) {
                this.previousScriptPubKey = Arrays.copyOf(previousScriptPubKey, previousScriptPubKey.length);
            }
            return Script.verify(
                    getHash(toByteArray(index, this.previousScriptPubKey), true),
                    this.previousScriptPubKey,
                    scriptSig
            );
        }

        private void sign() {
            if (keyPair == null || previousScriptPubKey == null) {
                return;
            }
            scriptSig = Script.createScriptSig(
                    Script.determineType(previousScriptPubKey),
                    getHash(toByteArray(index, previousScriptPubKey), true),
                    keyPair
            );
        }
    }

    public class Output {
        public final int index; // in the outputs array
        private long value; // satoshi
        private byte[] scriptPubKey;

        private Output() {
            this.index = outputs.size();
            outputs.add(this);
        }

        /**
         * @return satoshi
         */
        public long getValue() {
            return value;
        }

        /**
         * @return copy of scriptPubKey
         */
        public byte[] getScriptPubKey() {
            return Arrays.copyOf(scriptPubKey, scriptPubKey.length);
        }
    }
}