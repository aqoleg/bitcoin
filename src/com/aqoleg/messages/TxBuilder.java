/*
builds and signs transaction; not synchronized, synchronize it outside!

usage:
    TxBuilder txBuilder = new TxBuilder();
    String string = txBuilder.toString();
    long feeSatoshi = txBuilder.calculateFee();
    int sizeBytes = txBuilder.getSize();
    txBuilder = txBuilder.addInput(previousTransactionOutput, keyPair);
    txBuilder = txBuilder.addOutput(satoshi, toAddress);
    txBuilder = txBuilder.setChange(satoshiPerByte, changeAddress);
    Transaction transaction = txBuilder.build();
*/

package com.aqoleg.messages;

import com.aqoleg.crypto.Sha256;
import com.aqoleg.keys.Address;
import com.aqoleg.keys.KeyPair;
import com.aqoleg.utils.BytesOutput;

import java.math.BigDecimal;
import java.util.ArrayList;

public class TxBuilder {
    private final ArrayList<Input> inputs = new ArrayList<>();
    private final ArrayList<Output> outputs = new ArrayList<>();
    private Output change = null; // can be negative if there are not enough inputs
    private float targetFeePerByte; // satoshiPerByte = (sum(inputs) - sum(outputs) - change) / size

    /**
     * @return "inputs: 1, 0: (Input.toString()), outputs: 1, 0: (Output.toString()), fee: 0.00020000, size: 10"
     */
    @Override
    public String toString() {
        long fee = calculateFee();
        StringBuilder out = new StringBuilder();
        out.append("inputs: ").append(inputs.size());
        for (int i = 0; i < inputs.size(); i++) {
            out.append(", ").append(i).append(": (").append(inputs.get(i)).append(")");
        }
        out.append(", outputs: ").append(outputs.size() + (change == null ? 0 : 1));
        for (int i = 0; i < outputs.size(); i++) {
            out.append(", ").append(i).append(": (").append(outputs.get(i)).append(")");
        }
        if (change != null) {
            out.append(", ").append(outputs.size()).append("(change): (").append(change).append(")");
        }
        out.append(", fee: ").append(new BigDecimal(fee).movePointLeft(8).toPlainString());
        out.append(", size: ").append(getSize());
        return out.toString();
    }

    /**
     * calculates the change value, so that the fee matches the target fee
     *
     * @return fee in satoshi, negative if there are not enough inputs and no change set
     */
    public long calculateFee() {
        long sum = 0;
        for (Input input : inputs) {
            sum += input.previousOutput.value;
        }
        for (Output output : outputs) {
            sum -= output.value;
        }
        if (change != null) {
            change.value = sum - (long) (targetFeePerByte * getSize());
            sum -= change.value;
        }
        return sum;
    }

    /**
     * @return calculated tx size in bytes; actual tx size can vary on few bytes
     */
    public int getSize() {
        int size = 10; // version, #vin, #vout, lockTime
        for (Input input : inputs) {
            size += input.getSize();
        }
        for (Output output : outputs) {
            size += output.getSize();
        }
        if (change != null) {
            size += change.getSize();
        }
        return size;
    }

    /**
     * @param previousTransactionOutput unspent output to be added
     * @param keyPair                   KeyPair to sign input with
     * @return this txBuilder with input added
     * @throws NullPointerException if previousTransactionOutput == null or keyPair == null
     * @throws Message.Exception    if txBuilder already contains input, it has unsupported type or key is not match
     */
    public TxBuilder addInput(Transaction.Output previousTransactionOutput, KeyPair keyPair) {
        if (previousTransactionOutput == null) {
            throw new NullPointerException();
        }
        Input input = new Input(previousTransactionOutput, keyPair);
        if (inputs.contains(input)) {
            throw new Message.Exception("already contains");
        }
        inputs.add(input);
        return this;
    }

    /**
     * @param value   satoshi to transfer
     * @param address Address to transfer on
     * @return this txBuilder with output added
     * @throws NullPointerException if address == null
     * @throws Message.Exception    if value is negative
     */
    public TxBuilder addOutput(long value, Address address) {
        Output output = new Output(value, address);
        outputs.add(output);
        return this;
    }

    /**
     * set or change the target fee per byte and change address
     *
     * @param targetFeePerByte target fee, satoshi per byte
     * @param address          address for change, null to remove
     * @return this txBuilder with change and fee added/changed/removed
     * @throws Message.Exception if targetFeePerByte is negative
     */
    public TxBuilder setChange(float targetFeePerByte, Address address) {
        if (targetFeePerByte < 0) {
            throw new Message.Exception("negative targetFeePerByte");
        }
        this.targetFeePerByte = targetFeePerByte;
        change = address == null ? null : new Output(0, address);
        return this;
    }

    /**
     * @return signed Transaction created from this txBuilder
     * @throws Message.Exception if there are not enough values in inputs
     */
    public Transaction build() {
        if (inputs.size() < 1) {
            throw new Message.Exception("zero inputs");
        }
        if (calculateFee() < 0) {
            throw new Message.Exception("not enough values in inputs");
        }
        if (change != null && change.value < 0) {
            throw new Message.Exception("not enough values in inputs");
        }
        for (int i = 0; i < inputs.size(); i++) {
            inputs.get(i).sign(Sha256.getHash(Sha256.getHash(getBytes(i))));
        }
        return Transaction.fromBytes(getBytes(-1));
    }

    // get tx bytes, inputToSign < 0 for final tx
    private byte[] getBytes(int inputToSign) {
        BytesOutput bytesOutput = new BytesOutput();
        bytesOutput.writeIntLE(1); // version
        int count = inputs.size();
        bytesOutput.writeVariableLength(count);
        for (int i = 0; i < count; i++) {
            inputs.get(i).write(bytesOutput, inputToSign >= 0, inputToSign == i);
        }
        count = outputs.size() + (change == null ? 0 : 1);
        bytesOutput.writeVariableLength(count);
        for (Output output : outputs) {
            output.write(bytesOutput);
        }
        if (change != null) {
            change.write(bytesOutput);
        }
        bytesOutput.writeIntLE(0);
        // hash code type
        if (inputToSign >= 0) {
            bytesOutput.writeIntLE(1); // signHashAll
        }
        return bytesOutput.toByteArray();
    }

    private static class Input {
        private final byte[] previousTransactionHash;
        private final Transaction.Output previousOutput;
        private final KeyPair keyPair;
        private Script scriptSig;

        // throws Message.Exception if unsupported type or incorrect key
        private Input(Transaction.Output previousOutput, KeyPair keyPair) {
            this.previousTransactionHash = previousOutput.getTxHash();
            this.previousOutput = previousOutput;
            this.keyPair = keyPair;
            if (previousOutput.getScriptPubKey().type != Script.p2pkh) {
                throw new Message.Exception("unsupported type");
            }
            if (!Address.createFromPublicKey(keyPair.publicKey).equals(previousOutput.getScriptPubKey().address)) {
                throw new Message.Exception("incorrect keyPair");
            }
        }

        /**
         * @return "13DaZ9nfmJLfzU6oBnD2sdCiDmf3M5fmLx: 1.00000000"
         */
        @Override
        public String toString() {
            return previousOutput.getScriptPubKey().address + ": " +
                    new BigDecimal(previousOutput.value).movePointLeft(8).toPlainString();
        }

        /**
         * @param object to be compared with
         * @return true if previousTransactionHash and previousOutIndex are the same
         */
        @Override
        public boolean equals(Object object) {
            if (!(object instanceof Input)) {
                return false;
            }
            Input input = (Input) object;
            for (int i = 0; i < previousTransactionHash.length; i++) {
                if (input.previousTransactionHash[i] != previousTransactionHash[i]) {
                    return false;
                }
            }
            return input.previousOutput.index == previousOutput.index;
        }

        // actual or calculated size
        private int getSize() {
            // previousTransactionHash, previousOutIndex, scriptSigLen, sequence
            if (scriptSig == null) {
                return 41 + previousOutput.getScriptPubKey().getScriptSigSize(keyPair.publicKey.compressed);
            } else {
                return 41 + scriptSig.getSize();
            }
        }

        private void sign(byte[] hash) {
            scriptSig = previousOutput.getScriptPubKey().createScriptSig(hash, keyPair);
        }

        // throws if write not for signature before creating signature
        private void write(BytesOutput bytesOutput, boolean forSignature, boolean forThisSignature) {
            bytesOutput.writeBytes(previousTransactionHash);
            bytesOutput.writeIntLE(previousOutput.index);
            if (!forSignature) {
                scriptSig.write(bytesOutput);
            } else if (forThisSignature) {
                previousOutput.getScriptPubKey().write(bytesOutput);
            } else {
                bytesOutput.write(0x00);
            }
            bytesOutput.writeIntLE(0xFFFFFFFF);
        }
    }

    private static class Output {
        private final Script scriptPubKey;
        private long value;

        // throws Message.Exception if value < 0
        private Output(long value, Address address) {
            if (value < 0) {
                throw new Message.Exception("negative value");
            }
            this.value = value;
            scriptPubKey = Script.createScriptPubKey(address);
        }

        /**
         * @return "13DaZ9nfmJLfzU6oBnD2sdCiDmf3M5fmLx: 1.00000000"
         */
        @Override
        public String toString() {
            return scriptPubKey.address + ": " + new BigDecimal(value).movePointLeft(8).toPlainString();
        }

        private int getSize() {
            // value, scriptPubKeyLen
            return 9 + scriptPubKey.getSize();
        }

        private void write(BytesOutput bytesOutput) {
            bytesOutput.writeLongLE(value);
            scriptPubKey.write(bytesOutput);
        }
    }
}