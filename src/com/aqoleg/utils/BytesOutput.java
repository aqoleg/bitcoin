/*
writes different data to the byte array

usage:
    BytesOutput bytesOutput = new BytesOutput();
    bytesOutput.reset();
    bytesOutput = bytesOutput.writeBytes(fromBytes);
    bytesOutput = bytesOutput.writeBytes(fromBytes, start, length);
    bytesOutput = bytesOutput.writeIntLE(intLE);
    bytesOutput = bytesOutput.writeIntBE(intBE);
    bytesOutput = bytesOutput.writeLongLE(longLE);
    bytesOutput = bytesOutput.writeLongBE(longBE);
    bytesOutput = bytesOutput.writeVariableLength(longVar);
    bytesOutput = bytesOutput.writeVariableLength(stringVar);
    byte[] bytes = bytesOutput.toByteArray();
*/

package com.aqoleg.utils;

import java.io.ByteArrayOutputStream;

public class BytesOutput extends ByteArrayOutputStream {

    /**
     * @param bytes array to be read, can be empty
     * @return this
     * @throws NullPointerException if bytes == null
     */
    public BytesOutput writeBytes(byte[] bytes) {
        write(bytes, 0, bytes.length);
        return this;
    }

    /**
     * @param bytes       array to be filled with read bytes, can be empty
     * @param bytesStart  starting position in the bytes array
     * @param bytesLength the number of the bytes to read, can be zero
     * @return this
     * @throws NullPointerException      if bytes == null
     * @throws IndexOutOfBoundsException if bytesStart or bytesLength is incorrect
     */
    public BytesOutput writeBytes(byte[] bytes, int bytesStart, int bytesLength) {
        write(bytes, bytesStart, bytesLength);
        return this;
    }

    /**
     * 4 bytes, big endian
     *
     * @param n the integer to be written
     * @return this
     */
    public BytesOutput writeIntBE(int n) {
        for (int i = 24; i >= 0; i -= 8) {
            write(n >> i);
        }
        return this;
    }

    /**
     * 4 bytes, little endian
     *
     * @param n the integer to be written
     * @return this
     */
    public BytesOutput writeIntLE(int n) {
        for (int i = 0; i < 4; i++) {
            write(n);
            n >>= 8;
        }
        return this;
    }

    /**
     * 8 bytes, big endian
     *
     * @param n the long integer to be written
     * @return this
     */
    public BytesOutput writeLongBE(long n) {
        for (int i = 56; i >= 0; i -= 8) {
            write((int) (n >> i));
        }
        return this;
    }

    /**
     * 8 bytes, little endian
     *
     * @param n the long integer to be written
     * @return this
     */
    public BytesOutput writeLongLE(long n) {
        for (int i = 0; i < 8; i++) {
            write((int) n);
            n >>= 8;
        }
        return this;
    }

    /**
     * n < 0xFD, 1 byte
     * 0xFD <= n < 0xFFFF, 0xFD || (2 bytes, little-endian)
     * 0xFFFF <= n < 0xFFFFFFFF, 0xFE || (4 byes, little-endian)
     * 0xFFFFFFFF <= n <= 0xFFFFFFFFFFFFFFFF, 0xFF || (8 bytes, little-endian)
     *
     * @param n the integer to be written
     * @return this
     */
    public BytesOutput writeVariableLength(long n) {
        int bytes;
        if (n < 0) {
            write(0xFF);
            bytes = 8;
        } else if (n < 0xFD) {
            bytes = 1;
        } else if (n <= 0xFFFF) {
            write(0xFD);
            bytes = 2;
        } else if (n <= 0xFFFFFFFFL) {
            write(0xFE);
            bytes = 4;
        } else {
            write(0xFF);
            bytes = 8;
        }
        for (int i = 0; i < bytes; i++) {
            write((int) n);
            n >>= 8;
        }
        return this;
    }

    /**
     * @param s String to be written, can be empty
     * @return this
     * @throws NullPointerException if s == null
     */
    public BytesOutput writeVariableLength(String s) {
        byte[] bytes = s.getBytes();
        writeVariableLength(bytes.length);
        write(bytes, 0, bytes.length);
        return this;
    }
}