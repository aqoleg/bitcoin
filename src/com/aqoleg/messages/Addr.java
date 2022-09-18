/*
message with addresses of nodes, response to the GetAddr message

usage:
    Addr addr = (Addr) Message.read(inputStream);
    Addr addr = new Addr(bytes);
    String string = addr.toString();
    Iterator<NetAddress> iterator = addr.getIterator();

payload bytes:
    varInt, count
    count * byte[30], addrList, NetAddress
*/

package com.aqoleg.messages;

import com.aqoleg.utils.BytesInput;

import java.util.Iterator;

public class Addr extends Message {
    public static final String command = "addr";
    private final byte[] bytes;

    /**
     * @param bytes byte array with payload
     * @throws NullPointerException if bytes == null
     * @throws Message.Exception    if bytes are incorrect
     */
    public Addr(byte[] bytes) {
        BytesInput bytesInput = new BytesInput(bytes);
        int count;
        try {
            count = (int) bytesInput.readVariableLengthInt();
        } catch (IndexOutOfBoundsException exception) {
            throw new Message.Exception(exception.getMessage());
        }
        if (bytesInput.available() != count * 30) {
            throw new Message.Exception("incorrect length");
        }
        this.bytes = bytes;
    }

    /**
     * @return "count: 1, 0: (NetAddress.toString())"
     */
    @Override
    public String toString() {
        BytesInput bytesInput = new BytesInput(bytes);
        int count = (int) bytesInput.readVariableLengthInt();
        StringBuilder out = new StringBuilder();
        out.append("count: ").append(count);
        for (int i = 0; i < count; i++) {
            out.append(", ").append(i).append(": (").append(NetAddress.read(bytesInput, false)).append(')');
        }
        return out.toString();
    }

    /**
     * @return iterator through all addresses
     */
    public Iterator<NetAddress> getIterator() {
        return new Iterator<NetAddress>() {
            private final BytesInput bytesInput = new BytesInput(bytes);
            private int count = (int) bytesInput.readVariableLengthInt();

            @Override
            public boolean hasNext() {
                return count > 0;
            }

            @Override
            public NetAddress next() {
                if (count-- <= 0) {
                    return null;
                }
                return NetAddress.read(bytesInput, false);
            }
        };
    }
}