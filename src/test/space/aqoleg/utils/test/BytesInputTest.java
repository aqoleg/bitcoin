package space.aqoleg.utils.test;

import org.junit.jupiter.api.Test;
import space.aqoleg.utils.BytesInput;

import static org.junit.jupiter.api.Assertions.*;

class BytesInputTest {

    @Test
    void array() {
        assertThrows(NullPointerException.class, () -> new BytesInput(null));

        BytesInput bytes = new BytesInput(new byte[]{10, 20});
        assertThrows(NullPointerException.class, () -> bytes.readBytes(null));
        bytes.readBytes(new byte[0]);
        byte[] read = new byte[2];
        bytes.readBytes(read);
        assertArrayEquals(new byte[]{10, 20}, read);
        bytes.readBytes(new byte[0]);
        assertEquals(0, bytes.available());
        assertThrows(IndexOutOfBoundsException.class, () -> bytes.readBytes(new byte[2]));

        BytesInput bytes1 = new BytesInput(new byte[]{10, 20, 11});
        assertThrows(NullPointerException.class, () -> bytes1.readBytes(null, 8, 8));
        bytes1.readBytes(new byte[]{}, 0, 0);
        assertThrows(IndexOutOfBoundsException.class, () -> bytes1.readBytes(new byte[2], 1, 3));
        bytes1.readBytes(new byte[]{}, 0, 0);
        read = new byte[5];
        bytes1.readBytes(read, 2, 2);
        assertArrayEquals(new byte[]{0, 0, 10, 20, 0}, read);
        assertEquals(1, bytes1.available());
        assertThrows(IndexOutOfBoundsException.class, () -> bytes1.readBytes(new byte[12], 1, -1));
        assertThrows(IndexOutOfBoundsException.class, () -> bytes1.readBytes(new byte[12], 1, 2));
    }

    @Test
    void readInt() {
        BytesInput bytes1 = new BytesInput(new byte[]{0, 0, 0});
        assertThrows(IndexOutOfBoundsException.class, bytes1::readIntBE);
        bytes1.reset();
        assertThrows(IndexOutOfBoundsException.class, bytes1::readIntLE);

        BytesInput bytes = new BytesInput(new byte[]{0, 0, 0, 0});
        assertEquals(0, bytes.readIntBE());
        assertEquals(0, bytes.available());
        bytes.reset();
        assertEquals(0, bytes.readIntLE());
        assertEquals(0, bytes.available());

        bytes = new BytesInput(new byte[]{0, 0, 0, 1});
        assertEquals(1, bytes.readIntBE());
        bytes = new BytesInput(new byte[]{1, 0, 0, 0});
        assertEquals(1, bytes.readIntLE());

        bytes = new BytesInput(new byte[]{(byte) 0xFF, 0, 0, (byte) 0xFF});
        assertEquals(0xFF0000FF, bytes.readIntBE());
        bytes.reset();
        assertEquals(0xFF0000FF, bytes.readIntLE());

        bytes = new BytesInput(new byte[]{(byte) 0xFF, 0x01, 0x00, (byte) 0xFF});
        assertEquals(0xFF0100FF, bytes.readIntBE());
        bytes.reset();
        assertEquals(0xFF0001FF, bytes.readIntLE());

        bytes = new BytesInput(new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x88, 0, 0, 0});
        assertEquals(0xFFFFFFFF, bytes.readIntBE());
        assertEquals(0x88000000, bytes.readIntBE());
        assertEquals(0, bytes.available());
        assertThrows(IndexOutOfBoundsException.class, bytes::readIntBE);
        bytes.reset();
        assertEquals(0xFFFFFFFF, bytes.readIntLE());
        assertEquals(0x88, bytes.readIntLE());
        assertEquals(0, bytes.available());
        assertThrows(IndexOutOfBoundsException.class, bytes::readIntLE);
    }

    @Test
    void readLong() {
        BytesInput bytes1 = new BytesInput(new byte[]{0, 0, 0, 0});
        assertThrows(IndexOutOfBoundsException.class, bytes1::readLongBE);
        bytes1.reset();
        assertThrows(IndexOutOfBoundsException.class, bytes1::readLongLE);

        BytesInput bytes = new BytesInput(new byte[]{0, 0, 0, 0, 0, 0, 0, 0});
        assertEquals(0, bytes.readLongBE());
        assertEquals(0, bytes.available());
        bytes.reset();
        assertEquals(0, bytes.readLongLE());
        assertEquals(0, bytes.available());

        bytes = new BytesInput(new byte[]{1, 0, 0, 0, 0, 0, 0, 0});
        assertEquals(0x100000000000000L, bytes.readLongBE());
        bytes.reset();
        assertEquals(1, bytes.readLongLE());

        bytes = new BytesInput(new byte[]{(byte) 0xFF, 1, 0, 0, (byte) 0xFF, 1, 0, (byte) 0xFF});
        assertEquals(0xFF010000FF0100FFL, bytes.readLongBE());
        bytes.reset();
        assertEquals(0xFF0001FF000001FFL, bytes.readLongLE());

        bytes = new BytesInput(new byte[]{0, 0, 0, 0, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 9, 0, 0, 0, 0,
                0, 0, 0});
        assertEquals(0xFFFFFFFFL, bytes.readLongBE());
        assertEquals(0x900000000000000L, bytes.readLongBE());
        assertEquals(0, bytes.available());
        assertThrows(IndexOutOfBoundsException.class, bytes::readLongBE);
        bytes.reset();
        assertEquals(0xFFFFFFFF00000000L, bytes.readLongLE());
        assertEquals(9, bytes.readLongLE());
        assertEquals(0, bytes.available());
        assertThrows(IndexOutOfBoundsException.class, bytes::readLongLE);
    }

    @Test
    void readVariableLengthInt() {
        BytesInput bytes1 = new BytesInput(new byte[]{});
        assertThrows(IndexOutOfBoundsException.class, bytes1::readVariableLengthInt);
        BytesInput bytes2 = new BytesInput(new byte[]{(byte) 0xFD, 0x11});
        assertThrows(IndexOutOfBoundsException.class, bytes2::readVariableLengthInt);

        BytesInput bytes = new BytesInput(new byte[]{0});
        assertEquals(0, bytes.readVariableLengthInt());
        assertEquals(0, bytes.available());

        bytes = new BytesInput(new byte[]{1});
        assertEquals(1, bytes.readVariableLengthInt());
        assertEquals(0, bytes.available());

        bytes = new BytesInput(new byte[]{(byte) 0xFC});
        assertEquals(0xFC, bytes.readVariableLengthInt());
        assertEquals(0, bytes.available());

        bytes = new BytesInput(new byte[]{(byte) 0xFD, (byte) 0xFD, 0});
        assertEquals(0xFD, bytes.readVariableLengthInt());
        assertEquals(0, bytes.available());

        bytes = new BytesInput(new byte[]{(byte) 0xFD, 0x03, 0x02});
        assertEquals(0x203, bytes.readVariableLengthInt());
        assertEquals(0, bytes.available());

        bytes = new BytesInput(new byte[]{(byte) 0xFD, (byte) 0xFF, (byte) 0xFF});
        assertEquals(0xFFFF, bytes.readVariableLengthInt());
        assertEquals(0, bytes.available());

        bytes = new BytesInput(new byte[]{(byte) 0xFE, (byte) 0xFF, (byte) 0xFF, 0, 1});
        assertEquals(0x0100FFFF, bytes.readVariableLengthInt());
        assertEquals(0, bytes.available());

        bytes = new BytesInput(new byte[]{(byte) 0xFF, 9, 0, 0, 0, 0, 1, 0, 0});
        assertEquals(0x010000000009L, bytes.readVariableLengthInt());
        assertEquals(0, bytes.available());

        bytes = new BytesInput(new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF});
        assertEquals(0xFFFFFFFFFFFFFFFFL, bytes.readVariableLengthInt());
        assertEquals(0, bytes.available());
        assertThrows(IndexOutOfBoundsException.class, bytes::readVariableLengthInt);
    }

    @Test
    void readVariableLengthString() {
        BytesInput bytes1 = new BytesInput(new byte[]{});
        assertThrows(IndexOutOfBoundsException.class, bytes1::readVariableLengthString);
        BytesInput bytes2 = new BytesInput(new byte[]{3, 12, 1});
        assertThrows(IndexOutOfBoundsException.class, bytes2::readVariableLengthString);

        BytesInput bytes = new BytesInput(new byte[]{0});
        assertEquals("", bytes.readVariableLengthString());
        assertEquals(0, bytes.available());

        bytes = new BytesInput(new byte[]{2, 97, 97});
        assertEquals("aa", bytes.readVariableLengthString());

        bytes = new BytesInput(new byte[]{10, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32});
        assertEquals("          ", bytes.readVariableLengthString());

        bytes = new BytesInput(new byte[]{0x0F, 0x2F, 0x53, 0x61, 0x74, 0x6F, 0x73, 0x68, 0x69, 0x3A, 0x30, 0x2E, 0x37,
                0x2E, 0x32, 0x2F});
        assertEquals("/Satoshi:0.7.2/", bytes.readVariableLengthString());
    }
}