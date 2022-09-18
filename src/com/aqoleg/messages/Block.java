/*
bitcoin block, response to the GetData message; stored in the filesystem

usage:
    Block block = (Block) Message.read(inputStream);
    Block block = new Block(bytes);
    block.write(outputStream);
    String string = block.toString();
    String hash = block.getHash();
    int txNumber = block.txNumber();
    Transaction transaction = block.getTx(i);
    Transaction transaction = block.getTx(hash);
    boolean found = block.searchTxOutput(address, txOutputsList);

bytes:
    intLE, version
    byte[32], prevBlock, hash of the previous block
    byte[32], merkleRoot, root of the merkle tree of all transactions
    intLE, timestamp, unix timestamp
    intLE, bits, difficulty
    intLE, nonce
    varInt, count
    tx[]:
        Transaction
*/

package com.aqoleg.messages;

import com.aqoleg.crypto.Sha256;
import com.aqoleg.data.Storage;
import com.aqoleg.keys.Address;
import com.aqoleg.utils.BytesInput;
import com.aqoleg.utils.Converter;

import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.util.ArrayList;

public class Block extends Message implements Storage.Writable {
    public static final String command = "block";
    private final byte[] bytes;
    private final Transaction[] transactions;
    private String hash;

    /**
     * @param bytes byte array with payload
     * @throws NullPointerException if bytes == null
     * @throws Message.Exception    if bytes are incorrect
     */
    public Block(byte[] bytes) {
        BytesInput bytesInput = new BytesInput(bytes);
        if (bytesInput.skip(80) != 80) { // version, prevBlock, merkleRoot, timestamp, bits, nonce
            throw new Message.Exception("short header");
        }
        try {
            transactions = new Transaction[(int) bytesInput.readVariableLengthInt()]; // count
        } catch (IndexOutOfBoundsException | NegativeArraySizeException exception) {
            throw new Message.Exception(exception.getMessage());
        }
        for (int i = 0; i < transactions.length; i++) {
            transactions[i] = Transaction.fromBytesInput(bytesInput);
        }
        if (bytesInput.available() != 0) {
            throw new Message.Exception("big length");
        }
        this.bytes = bytes;
    }

    /**
     * write this Block into the outputStream
     *
     * @param outputStream OutputStream in which will be written this Block
     * @return number of bytes written
     * @throws NullPointerException if outputStream == null
     * @throws IOException          if cannot write
     */
    @Override
    public int write(OutputStream outputStream) throws IOException {
        outputStream.write(bytes);
        return bytes.length;
    }

    /**
     * @return "blockHash: 00..6f, version: 1, prevBlock: 00..34, merkleRoot: 4a..3b,
     * timestamp: 2009-01-03T18:15:05Z, bits: 486604799, nonce: 2083236893, count: 1, 0: (Transaction.toString())"
     * hashes are lower case, hex, reversed
     * @throws OutOfMemoryError if block is too big
     */
    @Override
    public String toString() {
        BytesInput bytesInput = new BytesInput(bytes);
        StringBuilder out = new StringBuilder();
        out.append("blockHash: ").append(getHash());
        out.append(", version: ").append(bytesInput.readIntLE());
        byte[] bytes = new byte[32];
        bytesInput.readBytes(bytes);
        out.append(", prevBlock: ").append(Converter.bytesToHexReverse(bytes, false, false));
        bytesInput.readBytes(bytes);
        out.append(", merkleRoot: ").append(Converter.bytesToHexReverse(bytes, false, false));
        out.append(", timestamp: ").append(Instant.ofEpochSecond(bytesInput.readIntLE()));
        out.append(", bits: ").append(bytesInput.readIntLE());
        out.append(", nonce: ").append(bytesInput.readIntLE());
        out.append(", count: ").append(transactions.length);
        for (int i = 0; i < transactions.length; i++) {
            out.append(", ").append(i).append(": (").append(transactions[i]).append(')');
        }
        return out.toString();
    }

    /**
     * @return reversed 32-bytes hex hash of this block
     */
    public String getHash() {
        if (hash == null) {
            hash = Converter.bytesToHexReverse(Sha256.getHash(Sha256.getHash(bytes, 0, 80)), false, false);
        }
        return hash;
    }

    /**
     * @return number of transactions in this block
     */
    public int txNumber() {
        return transactions.length;
    }

    /**
     * @param i transaction index
     * @return Transaction with this index
     * @throws IndexOutOfBoundsException if i is incorrect
     */
    public Transaction getTx(int i) {
        return transactions[i];
    }

    /**
     * @param hash reversed 32-bytes hex hash of the tx
     * @return Transaction with this hash or null
     */
    public Transaction getTx(String hash) {
        for (Transaction transaction : transactions) {
            if (transaction.getHash().equals(hash)) {
                return transaction;
            }
        }
        return null;
    }

    /**
     * add to the list all found outputs for this address
     *
     * @param address for searching outputs
     * @param outputs ArrayList to add found output into
     * @return true if found
     * @throws NullPointerException if address == null or outputs == null
     */
    public boolean searchTxOutput(Address address, ArrayList<Transaction.Output> outputs) {
        boolean found = false;
        for (Transaction transaction : transactions) {
            if (transaction.searchTxOutput(address, outputs)) {
                found = true;
            }
        }
        return found;
    }
}