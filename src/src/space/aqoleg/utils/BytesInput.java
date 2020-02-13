// byte array with cursor, can read byte, integer, long, variable length integer, variable length string and byte array
package space.aqoleg.utils;

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
     * @param bytes array to be filled with read bytes, can be null or empty
     * @throws NullPointerException      if bytes == null
     * @throws IndexOutOfBoundsException if there is not enough bytes to read
     */
    public void readBytes(byte[] bytes) {
        readBytes(bytes, 0, bytes.length);
    }

    /**
     * @param bytes       array to be filled with read bytes, can be null or empty
     * @param bytesStart  starting position in the bytes array
     * @param bytesLength the number of the bytes to read, can be zero
     * @throws NullPointerException      if bytes == null
     * @throws IndexOutOfBoundsException if there is not enough bytes to read, or bytesStart or bytesLength is incorrect
     */
    public void readBytes(byte[] bytes, int bytesStart, int bytesLength) {
        if (bytesLength == 0) {
            return;
        }
        int bytesRead = read(bytes, bytesStart, bytesLength);
        if (bytesRead != bytesLength) {
            throw new IndexOutOfBoundsException("can not read " + (bytesLength - bytesRead) + " bytes");
        }
    }

    /**
     * 4 bytes, big endian
     *
     * @return integer, read from bytes
     * @throws IndexOutOfBoundsException if bytes less then 4
     */
    public int readIntBE() {
        if (available() < 4) {
            throw new IndexOutOfBoundsException("can not read " + (4 - available()) + " bytes");
        }
        return read() << 24 | read() << 16 | read() << 8 | read();
    }

    /**
     * 4 bytes, little endian
     *
     * @return integer, read from bytes
     * @throws IndexOutOfBoundsException if bytes less then 4
     */
    public int readIntLE() {
        if (available() < 4) {
            throw new IndexOutOfBoundsException("can not read " + (4 - available()) + " bytes");
        }
        return read() | read() << 8 | read() << 16 | read() << 24;
    }

    /**
     * 8 bytes, big endian
     *
     * @return long integer, read from bytes
     * @throws IndexOutOfBoundsException if bytes less then 8
     */
    public long readLongBE() {
        if (available() < 8) {
            throw new IndexOutOfBoundsException("can not read " + (8 - available()) + " bytes");
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
     * @throws IndexOutOfBoundsException if bytes less then 8
     */
    public long readLongLE() {
        if (available() < 8) {
            throw new IndexOutOfBoundsException("can not read " + (8 - available()) + " bytes");
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
     * @throws IndexOutOfBoundsException if bytes less then expected
     */
    public long readVariableLengthInt() {
        if (available() < 1) {
            throw new IndexOutOfBoundsException("can not read first byte");
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
            throw new IndexOutOfBoundsException("can not read " + (bytes - available()) + " bytes");
        }
        long out = 0;
        for (int i = 0; i < bytes; i++) {
            out |= (long) read() << (8 * i);
        }
        return out;
    }

    /**
     * @return variable length String, read from bytes
     * @throws UnsupportedOperationException if string is too big
     * @throws IndexOutOfBoundsException     if bytes less then expected
     */
    public String readVariableLengthString() {
        long bytesN = readVariableLengthInt();
        if (bytesN > Integer.MAX_VALUE) {
            throw new UnsupportedOperationException("too big string");
        }
        byte[] bytes = new byte[(int) bytesN];
        readBytes(bytes);
        return new String(bytes);
    }
}