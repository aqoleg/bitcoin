/*
requests Inv message with blocks, starting from the first found hash (not including this hash),
 stopping on the stopHash (not including stopHash) or on 500th block

usage:
    GetBlocks getBlocks = GetBlocks.create(intVersion, hashes, stopHash);
    String string = getBlocks.toString();
    byte[] message = getBlocks.toByteArray();

payload bytes:
    intLE, version
    varInt, hash#
    byte[32] * hash#, hashes
    byte[32], stopHash
*/

package com.aqoleg.messages;

import com.aqoleg.utils.BytesInput;
import com.aqoleg.utils.BytesOutput;
import com.aqoleg.utils.Converter;

public class GetBlocks extends Message {
    public static final String command = "getblocks";
    private final byte[] bytes;

    private GetBlocks(byte[] bytes) {
        this.bytes = bytes;
    }

    /**
     * @param version  version
     * @param hashes   array of reversed 32-bytes hex hashes of last known blocks
     * @param stopHash reversed 32-bytes hex hash or null
     * @return instance of GetBlocks
     * @throws NullPointerException if hash == null
     * @throws Message.Exception    if hash length is incorrect
     */
    public static GetBlocks create(int version, String[] hashes, String stopHash) {
        BytesOutput bytesOutput = new BytesOutput().writeIntLE(version).writeVariableLength(hashes.length);
        byte[] hashBytes;
        for (String hash : hashes) {
            try {
                hashBytes = Converter.hexToBytesReverse(hash);
            } catch (Converter.Exception exception) {
                throw new Message.Exception(exception.getMessage());
            }
            if (hashBytes.length != 32) {
                throw new Message.Exception("incorrect hash length");
            }
            bytesOutput.writeBytes(hashBytes);
        }
        if (stopHash == null) {
            bytesOutput.writeBytes(new byte[32]);
        } else {
            try {
                hashBytes = Converter.hexToBytesReverse(stopHash);
            } catch (Converter.Exception exception) {
                throw new Message.Exception(exception.getMessage());
            }
            if (hashBytes.length != 32) {
                throw new Message.Exception("incorrect stopHash length");
            }
            bytesOutput.writeBytes(hashBytes);
        }
        return new GetBlocks(bytesOutput.toByteArray());
    }

    /**
     * @return "version: 1, hashes: 1, 0: 00...d3, stopHash: 00..00"
     * hashes are lower case, hex, reversed
     */
    @Override
    public String toString() {
        BytesInput bytesInput = new BytesInput(bytes);
        StringBuilder out = new StringBuilder();
        out.append("version: ").append(bytesInput.readIntLE());
        int count = (int) bytesInput.readVariableLengthInt();
        out.append(", hashes: ").append(count);
        byte[] hash = new byte[32];
        for (int i = 0; i < count; i++) {
            bytesInput.readBytes(hash);
            out.append(", ").append(i).append(": ").append(Converter.bytesToHexReverse(hash, false, false));
        }
        bytesInput.readBytes(hash);
        out.append(", stopHash: ").append(Converter.bytesToHexReverse(hash, false, false));
        return out.toString();
    }

    /**
     * @return byte array with this GetBlocks message
     */
    public byte[] toByteArray() {
        return Message.toByteArray(command, bytes);
    }
}