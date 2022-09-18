/*
reads and verifies transactions

usage:
    $ java com.aqoleg.messages.Transaction 01000000010000000000000000000000000000000000000000000000000000000000000000
     ffffffff0704ffff001d0104ffffffff0100f2052a0100000043410496b538e853519c726a2c91e61ec11600ae1390813a627c66fb8be794
     7be63c52da7589379515d4e0a604f8141781e62294721166bf621e73a82cbf2342c858eeac00000000

    Transaction transaction = (Transaction) Message.read(inputStream);
    Transaction transaction = Transaction.fromBytes(bytes);
    Transaction transaction = Transaction.fromBytesInput(bytesInput);
    String string = transaction.toString();
    String hash = transaction.getHash();
    String hexBytes = transaction.getHex();
    int size = transaction.getSize();
    transaction.verifyInput(inputIndex, previousTransaction);
    transaction.verifyInput(inputIndex, blockWithPreviousTransaction);
    Transaction.Output output = transaction.getOutput(outputIndex);
    int outputIndex = output.index;
    long valueSatoshi = output.value;
    byte[] transactionHash = output.getTxHash();
    Script scriptPubKey = output.scriptPubKey;
    boolean found = transaction.searchOutput(address, outputsList);
    byte[] message = transaction.toByteArray();

payload bytes:
    intLE, version
    0 or 2 bytes, flag, 0001 if has witnesses
    varInt, #vin
    vin[]:
        byte[32], previousTransactionHash
        intLE, previousOutIndex
        varInt, scriptSigLen
        byte[scriptSigLen], scriptSig
        intLE, sequence
    varInt, #vout
    vout[]:
        longLE, value
        varInt, scriptPubKeyLen
        byte[scriptPubKeyLen], scriptPubKey
    varInt, #witnesses, if flag == 0001
        varInt, witnessLength
        byte[witnessLength], witness
    intLE, lockTime
*/

package com.aqoleg.messages;

import com.aqoleg.crypto.Sha256;
import com.aqoleg.keys.Address;
import com.aqoleg.utils.BytesInput;
import com.aqoleg.utils.BytesOutput;
import com.aqoleg.utils.Converter;

import java.math.BigDecimal;
import java.util.ArrayList;

public class Transaction extends Message {
    public static final String command = "tx";
    private byte[] bytes;
    private Input[] inputs;
    private Output[] outputs;
    private int witnessesStartIndex; // -1 if there is no
    private String hash;

    private Transaction() {
    }

    /**
     * @param bytes byte array with payload
     * @return Transaction created from bytes
     * @throws NullPointerException if bytes == null
     * @throws Message.Exception    if bytes are incorrect
     */
    public static Transaction fromBytes(byte[] bytes) {
        BytesInput bytesInput = new BytesInput(bytes);
        Transaction transaction = fromBytesInput(bytesInput);
        if (bytesInput.available() != 0) {
            throw new Message.Exception("big length");
        }
        return transaction;
    }

    /**
     * @param bytesInput BytesInput with transaction
     * @return Transaction created from bytesInput
     * @throws NullPointerException if bytesInput == null
     * @throws Message.Exception    if bytesInput is incorrect
     */
    public static Transaction fromBytesInput(BytesInput bytesInput) {
        Transaction transaction = new Transaction();
        bytesInput.mark(0);
        int available = bytesInput.available();
        try {
            bytesInput.readIntLE(); // version
            int count = (int) bytesInput.readVariableLengthInt(); // flag or #vin
            boolean witness = count == 0;
            if (witness) {
                if (bytesInput.read() != 1) {
                    throw new Message.Exception("incorrect witness flag");
                }
                count = (int) bytesInput.readVariableLengthInt();
            }
            if (count < 0) {
                throw new Message.Exception("negative number of inputs");
            }
            transaction.inputs = new Input[count];
            for (int i = 0; i < count; i++) {
                transaction.inputs[i] = transaction.new Input(
                        bytesInput.readBytes(new byte[32]),
                        bytesInput.readIntLE(),
                        Script.fromBytesInput(bytesInput),
                        bytesInput.readIntLE()
                );
            }
            count = (int) bytesInput.readVariableLengthInt(); // #vout
            if (count < 0) {
                throw new Message.Exception("negative number of outputs");
            }
            transaction.outputs = new Output[count];
            for (int i = 0; i < count; i++) {
                transaction.outputs[i] = transaction.new Output(
                        i,
                        bytesInput.readLongLE(),
                        Script.fromBytesInput(bytesInput)
                );
            }
            transaction.witnessesStartIndex = -1;
            if (witness) {
                transaction.witnessesStartIndex = available - bytesInput.available();
                count = (int) bytesInput.readVariableLengthInt(); // #witnesses
                if (count < 0) {
                    throw new Message.Exception("negative number of witnesses");
                }
                for (int i = 0; i < count; i++) {
                    int witnessLength = (int) bytesInput.readVariableLengthInt();
                    if (bytesInput.skip(witnessLength) != witnessLength) { // witness
                        throw new Message.Exception("short witness");
                    }
                }
            }
            bytesInput.readIntLE(); // lockTime
            transaction.bytes = new byte[available - bytesInput.available()];
            bytesInput.reset();
            bytesInput.readBytes(transaction.bytes);
        } catch (IndexOutOfBoundsException exception) {
            throw new Message.Exception(exception.getMessage());
        }
        return transaction;
    }

    /**
     * prints info about entered transaction
     *
     * @param args String raw hex tx or empty
     */
    public static void main(String[] args) {
        if (args.length == 1) {
            System.out.println(toString(args[0]));
        } else if (System.console() == null) {
            System.out.println(toString(""));
        } else {
            while (true) {
                String input = System.console().readLine("enter raw hex tx or 'exit': ");
                if (input.equals("exit")) {
                    System.exit(0);
                }
                System.out.println(toString(input));
            }
        }
    }

    /**
     * @return "txHash: 11..ee, size: 12, version: 1, inputs: 1,
     * 0: (prevTxHash: 00..23, prevOut: 0, scriptSig: (Script.toString()), sequence: 0), outputs: 1,
     * 0: (value: 1.00000000, scriptPubKey: (Script.toString())), lockTime: 0"
     * hash is lower case, hex, reversed
     */
    @Override
    public String toString() {
        BytesInput bytesInput = new BytesInput(bytes);
        StringBuilder out = new StringBuilder();
        out.append("txHash: ").append(getHash());
        out.append(", size: ").append(bytes.length);
        out.append(", version: ").append(bytesInput.readIntLE());
        out.append(", inputs: ").append(inputs.length);
        for (int i = 0; i < inputs.length; i++) {
            out.append(", ").append(i).append(": (").append(inputs[i]).append(')');
        }
        out.append(", outputs: ").append(outputs.length);
        for (int i = 0; i < outputs.length; i++) {
            out.append(", ").append(i).append(": (").append(outputs[i]).append(')');
        }
        if (witnessesStartIndex > 0) {
            bytesInput.reset();
            //noinspection ResultOfMethodCallIgnored
            bytesInput.skip(witnessesStartIndex);
            int count = (int) bytesInput.readVariableLengthInt();
            out.append(", witnesses: ").append(count);
            for (int i = 0; i < count; i++) {
                //noinspection ResultOfMethodCallIgnored
                bytesInput.skip(bytesInput.readVariableLengthInt());
            }
        } else {
            //noinspection ResultOfMethodCallIgnored
            bytesInput.skip(bytesInput.available() - 4);
        }
        out.append(", lockTime: ").append(bytesInput.readIntLE());
        return out.toString();
    }

    /**
     * @return reversed lower case 32-bytes hex hash of this transaction
     */
    public String getHash() {
        if (hash == null) {
            hash = Converter.bytesToHexReverse(Sha256.getHash(Sha256.getHash(bytes)), false, false);
        }
        return hash;
    }

    /**
     * @return lower case hex bytes of this transaction
     */
    public String getHex() {
        return Converter.bytesToHex(bytes, false, false);
    }

    /**
     * @return transaction size in bytes
     */
    public int getSize() {
        return bytes.length;
    }

    /**
     * @param inputIndex          index of input to verify
     * @param previousTransaction Transaction with output of input to verify
     * @throws NullPointerException      if previousTransaction == null
     * @throws IndexOutOfBoundsException if inputIndex is incorrect
     * @throws Message.Exception         if input is not verified
     */
    public void verifyInput(int inputIndex, Transaction previousTransaction) {
        Input input = inputs[inputIndex];
        if (!input.getPreviousTxHash().equals(previousTransaction.getHash())) {
            throw new Message.Exception("incorrect hash");
        }

        Output output;
        try {
            output = previousTransaction.outputs[input.previousOutIndex];
        } catch (IndexOutOfBoundsException exception) {
            throw new Message.Exception("incorrect prevOutIndex " + exception.getMessage());
        }

        input.scriptSig.verify(output.scriptPubKey, getSignHash(inputIndex, output.scriptPubKey));
    }

    /**
     * @param inputIndex                   index of input to verify
     * @param blockWithPreviousTransaction Block with Transaction with output of input to verify
     * @throws NullPointerException      if blockWithPreviousTransaction == null
     * @throws IndexOutOfBoundsException if inputIndex is incorrect
     * @throws Message.Exception         if block does not contain tx or input is not verified
     */
    public void verifyInput(int inputIndex, Block blockWithPreviousTransaction) {
        Input input = inputs[inputIndex];
        Transaction previousTransaction = blockWithPreviousTransaction.getTx(input.getPreviousTxHash());
        if (previousTransaction == null) {
            throw new Message.Exception("block does not contains tx " + input.getPreviousTxHash());
        }

        Output output;
        try {
            output = previousTransaction.outputs[input.previousOutIndex];
        } catch (IndexOutOfBoundsException exception) {
            throw new Message.Exception("incorrect prevOutIndex " + exception.getMessage());
        }

        input.scriptSig.verify(output.scriptPubKey, getSignHash(inputIndex, output.scriptPubKey));
    }

    /**
     * @param outputIndex index of output
     * @return Output with outputIndex
     * @throws IndexOutOfBoundsException if outputIndex is incorrect
     */
    public Output getTxOutput(int outputIndex) {
        return outputs[outputIndex];
    }

    /**
     * add to the list all found outputs for this address
     *
     * @param address for searching outputs
     * @param outputs ArrayList to add found output into
     * @return true if found
     * @throws NullPointerException if address == null or outputs == null
     */
    public boolean searchTxOutput(Address address, ArrayList<Output> outputs) {
        boolean found = false;
        for (Output output : this.outputs) {
            if (output.hasTxOutput(address)) {
                outputs.add(output);
                found = true;
            }
        }
        return found;
    }

    /**
     * @return byte array with this Transaction message
     */
    public byte[] toByteArray() {
        return Message.toByteArray(command, bytes);
    }

    private static String toString(String input) {
        try {
            return Transaction.fromBytes(Converter.hexToBytes(input)).toString();
        } catch (Converter.Exception exception) {
            return "incorrect hex '" + input + "': " + exception.getMessage();
        } catch (Message.Exception exception) {
            return "incorrect transaction '" + input + "': " + exception.getMessage();
        }
    }

    private byte[] getSignHash(int inputIndex, Script previousScriptPubKey) {
        BytesInput bytesInput = new BytesInput(bytes);
        BytesOutput bytesOutput = new BytesOutput();
        bytesOutput.writeIntLE(bytesInput.readIntLE()); // version
        int count = (int) bytesInput.readVariableLengthInt(); // flag or #vin
        if (count == 0) {
            bytesOutput.write(0);
            bytesOutput.write(bytesInput.read());
            count = (int) bytesInput.readVariableLengthInt();
        }
        bytesOutput.writeVariableLength(count);
        byte[] input = new byte[36];
        for (int i = 0; i < count; i++) {
            bytesInput.readBytes(input); // previousTransactionHash, previousOutIndex
            bytesOutput.writeBytes(input);
            //noinspection ResultOfMethodCallIgnored
            bytesInput.skip(bytesInput.readVariableLengthInt()); // scriptSig
            if (i == inputIndex) {
                previousScriptPubKey.write(bytesOutput);
            } else {
                bytesOutput.write(0x00);
            }
            bytesOutput.writeIntLE(bytesInput.readIntLE()); // sequence
        }
        bytesOutput.writeBytes(bytesInput.readBytes(new byte[bytesInput.available()]));
        bytesOutput.writeIntLE(1); // signHashAll
        return Sha256.getHash(Sha256.getHash(bytesOutput.toByteArray()));
    }

    private class Input {
        private final byte[] previousTransactionHash;
        private final int previousOutIndex;
        private final Script scriptSig;
        private final int sequence;
        private String prevHash;

        private Input(byte[] previousTransactionHash, int previousOutIndex, Script scriptSig, int sequence) {
            this.previousTransactionHash = previousTransactionHash;
            this.previousOutIndex = previousOutIndex;
            this.scriptSig = scriptSig;
            this.sequence = sequence;
        }

        @Override
        public String toString() {
            return "prevTxHash: " + getPreviousTxHash() +
                    ", prevOut: " + previousOutIndex +
                    ", scriptSig: (" + scriptSig +
                    "), sequence: " + sequence;
        }

        private String getPreviousTxHash() {
            if (prevHash == null) {
                prevHash = Converter.bytesToHexReverse(previousTransactionHash, false, false);
            }
            return prevHash;
        }
    }

    public class Output {
        public final int index;
        public final long value;
        private final Script scriptPubKey;

        private Output(int index, long value, Script scriptPubKey) {
            this.index = index;
            this.value = value;
            this.scriptPubKey = scriptPubKey;
        }

        @Override
        public String toString() {
            return "value: " + new BigDecimal(value).movePointLeft(8).toPlainString() +
                    ", scriptPubKey: (" + scriptPubKey + ')';
        }

        /**
         * @return tx hash bytes
         */
        public byte[] getTxHash() {
            return Sha256.getHash(Sha256.getHash(bytes));
        }

        /**
         * @return scriptPubKey
         */
        public Script getScriptPubKey() {
            return scriptPubKey;
        }

        private boolean hasTxOutput(Address address) {
            return address.equals(scriptPubKey.address);
        }
    }
}