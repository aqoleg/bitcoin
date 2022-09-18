package com.aqoleg.utils.test;

import com.aqoleg.Test;
import com.aqoleg.utils.BytesOutput;

@SuppressWarnings("unused")
public class BytesOutputTest extends Test {

    public static void main(String[] args) {
        new BytesOutputTest().testAll();
    }

    public void array() {
        BytesOutput bytes = new BytesOutput().writeBytes(new byte[]{});
        assertEquals(new byte[]{}, bytes.toByteArray());
        assertThrows(NullPointerException.class, () -> bytes.writeBytes(null));
        bytes.writeBytes(new byte[0]).writeBytes(new byte[]{10, 20, 0});
        assertEquals(new byte[]{10, 20, 0}, bytes.toByteArray());

        bytes.reset();
        assertThrows(NullPointerException.class, () -> bytes.writeBytes(null, 2, 2));
        bytes.writeBytes(new byte[]{0, 10, 20, 0}, 1, 0).writeBytes(new byte[]{0, 10, 20, 0}, 1, 2);
        assertEquals(new byte[]{10, 20}, bytes.toByteArray());
        assertThrows(IndexOutOfBoundsException.class, () -> bytes.writeBytes(new byte[5], 2, 5));
        assertThrows(IndexOutOfBoundsException.class, () -> bytes.writeBytes(new byte[5], 2, -1));
    }

    public void writeInt() {
        BytesOutput bytes = new BytesOutput().writeIntBE(0);
        assertEquals(new byte[]{0, 0, 0, 0}, bytes.toByteArray());
        bytes.reset();
        bytes.writeIntLE(0);
        assertEquals(new byte[]{0, 0, 0, 0}, bytes.toByteArray());

        bytes.reset();
        bytes.writeIntBE(1);
        assertEquals(new byte[]{0, 0, 0, 1}, bytes.toByteArray());
        bytes.reset();
        bytes.writeIntLE(1);
        assertEquals(new byte[]{1, 0, 0, 0}, bytes.toByteArray());

        bytes.reset();
        bytes.writeIntBE(0xFF0000FF);
        assertEquals(new byte[]{(byte) 0xFF, 0, 0, (byte) 0xFF}, bytes.toByteArray());
        bytes.reset();
        bytes.writeIntLE(0xFF0000FF);
        assertEquals(new byte[]{(byte) 0xFF, 0, 0, (byte) 0xFF}, bytes.toByteArray());

        bytes.reset();
        bytes.writeIntBE(0xFF0001FF);
        assertEquals(new byte[]{(byte) 0xFF, 0x00, 0x01, (byte) 0xFF}, bytes.toByteArray());
        bytes.reset();
        bytes.writeIntLE(0xFF0001FF);
        assertEquals(new byte[]{(byte) 0xFF, 0x01, 0x00, (byte) 0xFF}, bytes.toByteArray());

        bytes.reset();
        bytes.writeIntBE(0xFFFFFFFF).writeIntBE(0x88);
        assertEquals(
                new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 0, 0, 0, (byte) 0x88},
                bytes.toByteArray()
        );
        bytes.reset();
        bytes.writeIntLE(0xFFFFFFFF).writeIntLE(0x88);
        assertEquals(
                new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x88, 0, 0, 0},
                bytes.toByteArray()
        );
    }

    public void writeLong() {
        BytesOutput bytes = new BytesOutput().writeLongBE(0);
        assertEquals(new byte[]{0, 0, 0, 0, 0, 0, 0, 0}, bytes.toByteArray());
        bytes.reset();
        bytes.writeLongLE(0);
        assertEquals(new byte[]{0, 0, 0, 0, 0, 0, 0, 0}, bytes.toByteArray());

        bytes.reset();
        bytes.writeLongBE(1);
        assertEquals(new byte[]{0, 0, 0, 0, 0, 0, 0, 1}, bytes.toByteArray());
        bytes.reset();
        bytes.writeLongLE(1);
        assertEquals(new byte[]{1, 0, 0, 0, 0, 0, 0, 0}, bytes.toByteArray());

        bytes.reset();
        bytes.writeLongBE(0xFF0001FF000001FFL);
        assertEquals(new byte[]{(byte) 0xFF, 0, 1, (byte) 0xFF, 0, 0, 1, (byte) 0xFF}, bytes.toByteArray());
        bytes.reset();
        bytes.writeLongLE(0xFF0001FF000001FFL);
        assertEquals(new byte[]{(byte) 0xFF, 1, 0, 0, (byte) 0xFF, 1, 0, (byte) 0xFF}, bytes.toByteArray());

        bytes.reset();
        bytes.writeLongBE(0xFFFFFFFF00000000L).writeLongBE(9);
        assertEquals(
                new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9},
                bytes.toByteArray()
        );
        bytes.reset();
        bytes.writeLongLE(0xFFFFFFFF00000000L).writeLongLE(9);
        assertEquals(
                new byte[]{0, 0, 0, 0, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 9, 0, 0, 0, 0, 0, 0, 0},
                bytes.toByteArray()
        );
    }

    public void writeVariableLength() {
        BytesOutput bytes = new BytesOutput().writeVariableLength(0);
        assertEquals(new byte[]{0}, bytes.toByteArray());

        bytes.reset();
        bytes.writeVariableLength(1);
        assertEquals(new byte[]{1}, bytes.toByteArray());

        bytes.reset();
        bytes.writeVariableLength(0xFC);
        assertEquals(new byte[]{(byte) 0xFC}, bytes.toByteArray());

        bytes.reset();
        bytes.writeVariableLength(0xFD);
        assertEquals(new byte[]{(byte) 0xFD, (byte) 0xFD, 0}, bytes.toByteArray());

        bytes.reset();
        bytes.writeVariableLength(0x203);
        assertEquals(new byte[]{(byte) 0xFD, 0x03, 0x02}, bytes.toByteArray());

        bytes.reset();
        bytes.writeVariableLength(0xFFFF);
        assertEquals(new byte[]{(byte) 0xFD, (byte) 0xFF, (byte) 0xFF}, bytes.toByteArray());

        bytes.reset();
        bytes.writeVariableLength(0x0100FFFF);
        assertEquals(new byte[]{(byte) 0xFE, (byte) 0xFF, (byte) 0xFF, 0, 1}, bytes.toByteArray());

        bytes.reset();
        bytes.writeVariableLength(0x010000000009L);
        assertEquals(new byte[]{(byte) 0xFF, 9, 0, 0, 0, 0, 1, 0, 0}, bytes.toByteArray());

        bytes.reset();
        bytes.writeVariableLength(0xFFFFFFFFFFFFFFFFL);
        assertEquals(new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF}, bytes.toByteArray());
    }

    public void writeVariableLengthString() {
        BytesOutput bytes = new BytesOutput();
        assertThrows(NullPointerException.class, () -> bytes.writeVariableLength(null));

        bytes.reset();
        bytes.writeVariableLength("");
        assertEquals(new byte[]{0}, bytes.toByteArray());

        bytes.reset();
        bytes.writeVariableLength("aa");
        assertEquals(new byte[]{2, 97, 97}, bytes.toByteArray());

        bytes.reset();
        bytes.writeVariableLength("          ");
        assertEquals(new byte[]{10, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32}, bytes.toByteArray());

        BytesOutput bytes1 = new BytesOutput().writeVariableLength("/Satoshi:0.7.2/");
        assertEquals(new byte[]{0x0F, 0x2F, 0x53, 0x61, 0x74, 0x6F, 0x73, 0x68, 0x69, 0x3A, 0x30, 0x2E, 0x37,
                0x2E, 0x32, 0x2F}, bytes1.toByteArray());
    }
}