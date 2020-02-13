package space.aqoleg.utils.test;

import org.junit.jupiter.api.Test;
import space.aqoleg.utils.Converter;

import static org.junit.jupiter.api.Assertions.*;

class ConverterTest {

    @Test
    void bytesToHex() {
        assertThrows(NullPointerException.class, () -> Converter.bytesToHex(null, false, false));
        assertEquals("", Converter.bytesToHex(new byte[]{}, true, false));
        assertEquals("", Converter.bytesToHex(new byte[]{}, false, false));
        assertEquals("0x00", Converter.bytesToHex(new byte[]{0}, true, true));
        assertEquals("00", Converter.bytesToHex(new byte[]{0}, false, true));
        assertEquals("02", Converter.bytesToHex(new byte[]{2}, false, true));
        assertEquals("0x89", Converter.bytesToHex(new byte[]{(byte) 0x89}, true, true));
        assertEquals("0x00FF", Converter.bytesToHex(new byte[]{0, (byte) 0xFF}, true, true));
        assertEquals("00ff", Converter.bytesToHex(new byte[]{0, (byte) 0xFF}, false, false));
        assertEquals("0x0000910189", Converter.bytesToHex(new byte[]{0, 0, (byte) 0x91, 1, (byte) 0x89}, true, false));
        assertEquals(
                "0xFFFF0000000000000000FF0100",
                Converter.bytesToHex(
                        new byte[]{(byte) 0xFF, (byte) 0xFF, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xFF, 1, 0},
                        true,
                        true
                )
        );
    }

    @Test
    void hexToBytes() {
        assertThrows(UnsupportedOperationException.class, () -> Converter.hexToBytes("0xdq"));
        assertThrows(UnsupportedOperationException.class, () -> Converter.hexToBytes("0xr4"));
        assertThrows(NullPointerException.class, () -> Converter.hexToBytes(null));
        assertArrayEquals(new byte[]{}, Converter.hexToBytes(""));
        assertArrayEquals(new byte[]{0}, Converter.hexToBytes("0x00"));
        assertArrayEquals(new byte[]{0}, Converter.hexToBytes("00"));
        assertArrayEquals(new byte[]{0}, Converter.hexToBytes("0"));
        assertArrayEquals(new byte[]{0}, Converter.hexToBytes("0x00"));
        assertArrayEquals(new byte[]{4}, Converter.hexToBytes("0x4"));
        assertArrayEquals(new byte[]{(byte) 0x89}, Converter.hexToBytes("0x89"));
        assertArrayEquals(new byte[]{0, (byte) 0xFF}, Converter.hexToBytes("0ff"));
        assertArrayEquals(new byte[]{0, (byte) 0xFF}, Converter.hexToBytes("0x00Ff"));
        assertArrayEquals(new byte[]{0, 0, (byte) 0x91, 1, (byte) 0x89}, Converter.hexToBytes("000910189"));
        assertArrayEquals(
                new byte[]{(byte) 0xFF, (byte) 0xFF, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xFF, 1, 0},
                Converter.hexToBytes("ffFF0000000000000000FF0100")
        );
    }
}