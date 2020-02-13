package space.aqoleg.utils.test;

import org.junit.jupiter.api.Test;
import space.aqoleg.utils.BytesOutput;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BytesOutputTest {

    @Test
    void array() {
        BytesOutput bytes = new BytesOutput();
        bytes.writeBytes(new byte[]{});
        assertArrayEquals(new byte[]{}, bytes.toByteArray());
        assertThrows(NullPointerException.class, () -> bytes.writeBytes(null));
        bytes.writeBytes(new byte[0]);
        bytes.writeBytes(new byte[]{10, 20, 0});
        assertArrayEquals(new byte[]{10, 20, 0}, bytes.toByteArray());

        bytes.reset();
        assertThrows(NullPointerException.class, () -> bytes.writeBytes(null, 2, 2));
        bytes.writeBytes(new byte[]{0, 10, 20, 0}, 1, 0);
        bytes.writeBytes(new byte[]{0, 10, 20, 0}, 1, 2);
        assertArrayEquals(new byte[]{10, 20}, bytes.toByteArray());
        assertThrows(IndexOutOfBoundsException.class, () -> bytes.writeBytes(new byte[5], 2, 5));
        assertThrows(IndexOutOfBoundsException.class, () -> bytes.writeBytes(new byte[5], 2, -1));
    }

    @Test
    void writeInt() {
        BytesOutput bytes = new BytesOutput();
        bytes.writeIntBE(0);
        assertArrayEquals(new byte[]{0, 0, 0, 0}, bytes.toByteArray());
        bytes.reset();
        bytes.writeIntLE(0);
        assertArrayEquals(new byte[]{0, 0, 0, 0}, bytes.toByteArray());

        bytes.reset();
        bytes.writeIntBE(1);
        assertArrayEquals(new byte[]{0, 0, 0, 1}, bytes.toByteArray());
        bytes.reset();
        bytes.writeIntLE(1);
        assertArrayEquals(new byte[]{1, 0, 0, 0}, bytes.toByteArray());

        bytes.reset();
        bytes.writeIntBE(0xFF0000FF);
        assertArrayEquals(new byte[]{(byte) 0xFF, 0, 0, (byte) 0xFF}, bytes.toByteArray());
        bytes.reset();
        bytes.writeIntLE(0xFF0000FF);
        assertArrayEquals(new byte[]{(byte) 0xFF, 0, 0, (byte) 0xFF}, bytes.toByteArray());

        bytes.reset();
        bytes.writeIntBE(0xFF0001FF);
        assertArrayEquals(new byte[]{(byte) 0xFF, 0x00, 0x01, (byte) 0xFF}, bytes.toByteArray());
        bytes.reset();
        bytes.writeIntLE(0xFF0001FF);
        assertArrayEquals(new byte[]{(byte) 0xFF, 0x01, 0x00, (byte) 0xFF}, bytes.toByteArray());

        bytes.reset();
        bytes.writeIntBE(0xFFFFFFFF);
        bytes.writeIntBE(0x88);
        assertArrayEquals(
                new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 0, 0, 0, (byte) 0x88},
                bytes.toByteArray()
        );
        bytes.reset();
        bytes.writeIntLE(0xFFFFFFFF);
        bytes.writeIntLE(0x88);
        assertArrayEquals(
                new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x88, 0, 0, 0},
                bytes.toByteArray()
        );
    }

    @Test
    void writeLong() {
        BytesOutput bytes = new BytesOutput();
        bytes.writeLongBE(0);
        assertArrayEquals(new byte[]{0, 0, 0, 0, 0, 0, 0, 0}, bytes.toByteArray());
        bytes.reset();
        bytes.writeLongLE(0);
        assertArrayEquals(new byte[]{0, 0, 0, 0, 0, 0, 0, 0}, bytes.toByteArray());

        bytes.reset();
        bytes.writeLongBE(1);
        assertArrayEquals(new byte[]{0, 0, 0, 0, 0, 0, 0, 1}, bytes.toByteArray());
        bytes.reset();
        bytes.writeLongLE(1);
        assertArrayEquals(new byte[]{1, 0, 0, 0, 0, 0, 0, 0}, bytes.toByteArray());

        bytes.reset();
        bytes.writeLongBE(0xFF0001FF000001FFL);
        assertArrayEquals(new byte[]{(byte) 0xFF, 0, 1, (byte) 0xFF, 0, 0, 1, (byte) 0xFF}, bytes.toByteArray());
        bytes.reset();
        bytes.writeLongLE(0xFF0001FF000001FFL);
        assertArrayEquals(new byte[]{(byte) 0xFF, 1, 0, 0, (byte) 0xFF, 1, 0, (byte) 0xFF}, bytes.toByteArray());

        bytes.reset();
        bytes.writeLongBE(0xFFFFFFFF00000000L);
        bytes.writeLongBE(9);
        assertArrayEquals(
                new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9},
                bytes.toByteArray()
        );
        bytes.reset();
        bytes.writeLongLE(0xFFFFFFFF00000000L);
        bytes.writeLongLE(9);
        assertArrayEquals(
                new byte[]{0, 0, 0, 0, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 9, 0, 0, 0, 0, 0, 0, 0},
                bytes.toByteArray()
        );
    }

    @Test
    void writeVariableLength() {
        BytesOutput bytes = new BytesOutput();
        bytes.writeVariableLength(0);
        assertArrayEquals(new byte[]{0}, bytes.toByteArray());

        bytes.reset();
        bytes.writeVariableLength(1);
        assertArrayEquals(new byte[]{1}, bytes.toByteArray());

        bytes.reset();
        bytes.writeVariableLength(0xFC);
        assertArrayEquals(new byte[]{(byte) 0xFC}, bytes.toByteArray());

        bytes.reset();
        bytes.writeVariableLength(0xFD);
        assertArrayEquals(new byte[]{(byte) 0xFD, (byte) 0xFD, 0}, bytes.toByteArray());

        bytes.reset();
        bytes.writeVariableLength(0x203);
        assertArrayEquals(new byte[]{(byte) 0xFD, 0x03, 0x02}, bytes.toByteArray());

        bytes.reset();
        bytes.writeVariableLength(0xFFFF);
        assertArrayEquals(new byte[]{(byte) 0xFD, (byte) 0xFF, (byte) 0xFF}, bytes.toByteArray());

        bytes.reset();
        bytes.writeVariableLength(0x0100FFFF);
        assertArrayEquals(new byte[]{(byte) 0xFE, (byte) 0xFF, (byte) 0xFF, 0, 1}, bytes.toByteArray());

        bytes.reset();
        bytes.writeVariableLength(0x010000000009L);
        assertArrayEquals(new byte[]{(byte) 0xFF, 9, 0, 0, 0, 0, 1, 0, 0}, bytes.toByteArray());

        bytes.reset();
        bytes.writeVariableLength(0xFFFFFFFFFFFFFFFFL);
        assertArrayEquals(new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF}, bytes.toByteArray());
    }

    @Test
    void writeVariableLengthString() {
        BytesOutput bytes = new BytesOutput();
        assertThrows(NullPointerException.class, () -> bytes.writeVariableLength(null));

        bytes.reset();
        bytes.writeVariableLength("");
        assertArrayEquals(new byte[]{0}, bytes.toByteArray());

        bytes.reset();
        bytes.writeVariableLength("aa");
        assertArrayEquals(new byte[]{2, 97, 97}, bytes.toByteArray());

        bytes.reset();
        bytes.writeVariableLength("          ");
        assertArrayEquals(new byte[]{10, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32}, bytes.toByteArray());

        bytes.reset();
        bytes.writeVariableLength("/Satoshi:0.7.2/");
        assertArrayEquals(new byte[]{0x0F, 0x2F, 0x53, 0x61, 0x74, 0x6F, 0x73, 0x68, 0x69, 0x3A, 0x30, 0x2E, 0x37,
                0x2E, 0x32, 0x2F}, bytes.toByteArray());
    }
}