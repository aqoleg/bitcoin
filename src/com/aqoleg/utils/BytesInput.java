/*
reads different data from the byte array

usage:
    BytesInput bytesInput = new BytesInput(fromBytes);
    int availableToRead = bytesInput.available();
    bytesInput.mark(0);
    bytesInput.reset();
    long skipped = bytesInput.skip(number);
    byte[] toBytes = bytesInput.readBytes(toBytes);
    byte[] toBytes = bytesInput.readBytes(toBytes, start, length);
    int oneByte = bytesInput.read();
    int le = bytesInput.readIntLE();
    int be = bytesInput.readIntBE();
    long le = bytesInput.readLongLE();
    long be = bytesInput.readLongBE();
    long var = bytesInput.readVariableLengthInt();
    String var = bytesInput.readVariableLengthString();
*/

package com.aqoleg.utils;

import java.io.ByteArrayInputStream;

public class BytesInput extends ByteArrayInputStream {

    /**
     * @param bytes array from which this BytesInput will be created
     * @throws NullPointerException if bytes == null
     */
    public BytesInput(byte[] bytes) {
        super(bytes);
    }

    /**
     * @param bytes array to be filled with read bytes, can be empty
     * @return bytes filled with byte array read from bytes
     * @throws NullPointerException      if bytes == null
     * @throws IndexOutOfBoundsException if there is not enough bytes to read
     */
    public byte[] readBytes(byte[] bytes) {
        return readBytes(bytes, 0, bytes.length);
    }

    /**
     * @param bytes       array to be filled with read bytes, can be empty
     * @param bytesStart  starting position in the bytes array
     * @param bytesLength the number of the bytes to read, can be zero
     * @return bytes filled with byte array read from bytes
     * @throws NullPointerException      if bytes == null
     * @throws IndexOutOfBoundsException if there is not enough bytes to read, or bytesStart or bytesLength is incorrect
     */
    public byte[] readBytes(byte[] bytes, int bytesStart, int bytesLength) {
        if (bytesLength == 0) {
            return bytes;
        }
        int bytesRead = read(bytes, bytesStart, bytesLength);
        if (bytesRead != bytesLength) {
            throw new IndexOutOfBoundsException("not enough bytes for byte array");
        }
        return bytes;
    }

    /**
     * 4 bytes, big endian
     *
     * @return integer, read from bytes
     * @throws IndexOutOfBoundsException if less than 4 bytes available
     */
    public int readIntBE() {
        if (available() < 4) {
            throw new IndexOutOfBoundsException("not enough bytes for beInt");
        }
        return read() << 24 | read() << 16 | read() << 8 | read();
    }

    /**
     * 4 bytes, little endian
     *
     * @return integer, read from bytes
     * @throws IndexOutOfBoundsException if less than 4 bytes available
     */
    public int readIntLE() {
        if (available() < 4) {
            throw new IndexOutOfBoundsException("not enough bytes for leInt");
        }
        return read() | read() << 8 | read() << 16 | read() << 24;
    }

    /**
     * 8 bytes, big endian
     *
     * @return long integer, read from bytes
     * @throws IndexOutOfBoundsException if less than 8 bytes available
     */
    public long readLongBE() {
        if (available() < 8) {
            throw new IndexOutOfBoundsException("not enough bytes for beLong");
        }
        long out = 0;
        for (int i = 56; i >= 0; i -= 8) {
            out |= (long) read() << i;
        }
        return out;
    }

    /**
     * 8 bytes, little endian
     *
     * @return long integer, read from bytes
     * @throws IndexOutOfBoundsException if less than 8 bytes available
     */
    public long readLongLE() {
        if (available() < 8) {
            throw new IndexOutOfBoundsException("not enough bytes for leLong");
        }
        long out = 0;
        for (int i = 0; i <= 56; i += 8) {
            out |= (long) read() << i;
        }
        return out;
    }

    /**
     * n < 0xFD, 1 byte
     * 0xFD <= n < 0xFFFF, 0xFD || (2 bytes, little-endian)
     * 0xFFFF <= n < 0xFFFFFFFF, 0xFE || (4 byes, little-endian)
     * 0xFFFFFFFF <= n <= 0xFFFFFFFFFFFFFFFF, 0xFF || (8 bytes, little-endian)
     *
     * @return variable length integer, read from bytes
     * @throws IndexOutOfBoundsException if number of bytes is less than expected
     */
    public long readVariableLengthInt() {
        if (available() < 1) {
            throw new IndexOutOfBoundsException("not enough bytes for varInt");
        }
        int firstByte = read();
        int bytes;
        if (firstByte == 0xFD) {
            bytes = 2;
        } else if (firstByte == 0xFE) {
            bytes = 4;
        } else if (firstByte == 0xFF) {
            bytes = 8;
        } else {
            return firstByte;
        }
        if (available() < bytes) {
            throw new IndexOutOfBoundsException("not enough bytes for varInt");
        }
        long out = 0;
        for (int i = 0; i < bytes; i++) {
            out |= (long) read() << (8 * i);
        }
        return out;
    }

    /**
     * @return variable length String, read from bytes
     * @throws IndexOutOfBoundsException if string is too big or number of bytes is less than expected
     */
    public String readVariableLengthString() {
        long bytesN = readVariableLengthInt();
        if (bytesN > Integer.MAX_VALUE) {
            throw new IndexOutOfBoundsException("too big varString");
        }
        byte[] bytes = new byte[(int) bytesN];
        readBytes(bytes);
        return new String(bytes);
    }
}