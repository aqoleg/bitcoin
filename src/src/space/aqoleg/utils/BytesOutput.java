// infinite byte array, can write byte, integer, long, variable length integer, variable length string and byte array
package space.aqoleg.utils;

import java.io.ByteArrayOutputStream;

public class BytesOutput extends ByteArrayOutputStream {

    /**
     * @param bytes array to be read, can be null or empty
     * @throws NullPointerException if bytes == null
     */
    public void writeBytes(byte[] bytes) {
        write(bytes, 0, bytes.length);
    }

    /**
     * @param bytes       array to be filled with read bytes, can be null or empty
     * @param bytesStart  starting position in the bytes array
     * @param bytesLength the number of the bytes to read, can be zero
     * @throws NullPointerException      if bytes == null
     * @throws IndexOutOfBoundsException if bytesStart or bytesLength is incorrect
     */
    public void writeBytes(byte[] bytes, int bytesStart, int bytesLength) {
        write(bytes, bytesStart, bytesLength);
    }

    /**
     * 4 bytes, big endian
     *
     * @param n the integer to be written
     */
    public void writeIntBE(int n) {
        for (int i = 24; i >= 0; i -= 8) {
            write(n >> i);
        }
    }

    /**
     * 4 bytes, little endian
     *
     * @param n the integer to be written
     */
    public void writeIntLE(int n) {
        for (int i = 0; i < 4; i++) {
            write(n);
            n >>= 8;
        }
    }

    /**
     * 8 bytes, big endian
     *
     * @param n the long integer to be written
     */
    public void writeLongBE(long n) {
        for (int i = 56; i >= 0; i -= 8) {
            write((int) (n >> i));
        }
    }

    /**
     * 8 bytes, little endian
     *
     * @param n the long integer to be written
     */
    public void writeLongLE(long n) {
        for (int i = 0; i < 8; i++) {
            write((int) n);
            n >>= 8;
        }
    }

    /**
     * n < 0xFD, 1 byte
     * 0xFD <= n < 0xFFFF, 0xFD || (2 bytes, little-endian)
     * 0xFFFF <= n < 0xFFFFFFFF, 0xFE || (4 byes, little-endian)
     * 0xFFFFFFFF <= n <= 0xFFFFFFFFFFFFFFFF, 0xFF || (8 bytes, little-endian)
     *
     * @param n the integer to be written
     */
    public void writeVariableLength(long n) {
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
    }

    /**
     * @param s String to be written, can be null or empty
     * @throws NullPointerException if s == null
     */
    public void writeVariableLength(String s) {
        byte[] bytes = s.getBytes();
        writeVariableLength(bytes.length);
        write(bytes, 0, bytes.length);
    }
}