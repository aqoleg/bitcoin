package com.aqoleg.utils.test;

import com.aqoleg.Test;
import com.aqoleg.utils.Converter;

@SuppressWarnings("unused")
public class ConverterTest extends Test {
    private static final String allHex = "0x000102030405060708090A0B0C0D0E0F101112131415161718191A1B1C1D1E1F"
            + "202122232425262728292A2B2C2D2E2F303132333435363738393A3B3C3D3E3F"
            + "404142434445464748494A4B4C4D4E4F505152535455565758595A5B5C5D5E5F"
            + "606162636465666768696A6B6C6D6E6F707172737475767778797A7B7C7D7E7F"
            + "808182838485868788898A8B8C8D8E8F909192939495969798999A9B9C9D9E9F"
            + "A0A1A2A3A4A5A6A7A8A9AAABACADAEAFB0B1B2B3B4B5B6B7B8B9BABBBCBDBEBF"
            + "C0C1C2C3C4C5C6C7C8C9CACBCCCDCECFD0D1D2D3D4D5D6D7D8D9DADBDCDDDEDF"
            + "E0E1E2E3E4E5E6E7E8E9EAEBECEDEEEFF0F1F2F3F4F5F6F7F8F9FAFBFCFDFEFF";

    public static void main(String[] args) {
        new ConverterTest().testAll();
    }

    public void bytesToString() {
        assertNull(Converter.bytesToString(null));
        assertEquals("", Converter.bytesToString(new byte[]{}));
        assertEquals("00", Converter.bytesToString(new byte[]{0}));
        assertEquals("0203 fk 7fc8", Converter.bytesToString(new byte[]{2, 3, 102, 107, 127, (byte) 200}));
        assertEquals("YX 0203 UU 1f  !\"", Converter.bytesToString(new byte[]{89, 88, 2, 3, 85, 85, 31, 32, 33, 34}));
        assertEquals("0000910189", Converter.bytesToString(new byte[]{0, 0, (byte) 0x91, 1, (byte) 0x89}));
        assertEquals(
                "ffff0000000000000000ff0100",
                Converter.bytesToString(new byte[]{(byte) 0xFF, (byte) 0xFF, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xFF, 1, 0})
        );

        byte[] bytes = new byte[256];
        for (int i = 0; i < 256; i++) {
            bytes[i] = (byte) i;
        }
        assertEquals(
                "000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f  !\"#$%&'()*+,-./0123456789:" +
                        ";<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~ 7f808182838485868788" +
                        "898a8b8c8d8e8f909192939495969798999a9b9c9d9e9fa0a1a2a3a4a5a6a7a8a9aaabacadaeafb0b1b2b3b4b5" +
                        "b6b7b8b9babbbcbdbebfc0c1c2c3c4c5c6c7c8c9cacbcccdcecfd0d1d2d3d4d5d6d7d8d9dadbdcdddedfe0e1e2" +
                        "e3e4e5e6e7e8e9eaebecedeeeff0f1f2f3f4f5f6f7f8f9fafbfcfdfeff",
                Converter.bytesToString(bytes)
        );
    }

    public void bytesToHex() {
        assertNull(Converter.bytesToHex(null, false, false));
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

        byte[] bytes = new byte[256];
        for (int i = 0; i < 256; i++) {
            bytes[i] = (byte) i;
        }
        assertEquals(allHex, Converter.bytesToHex(bytes, true, true));
        assertEquals(allHex.toLowerCase().substring(2), Converter.bytesToHex(bytes, false, false));
    }

    public void bytesToHexReverse() {
        assertNull(Converter.bytesToHexReverse(null, false, false));
        assertEquals("", Converter.bytesToHexReverse(new byte[]{}, true, false));
        assertEquals("", Converter.bytesToHexReverse(new byte[]{}, false, false));
        assertEquals("0x00", Converter.bytesToHexReverse(new byte[]{0}, true, true));
        assertEquals("00", Converter.bytesToHexReverse(new byte[]{0}, false, true));
        assertEquals("02", Converter.bytesToHexReverse(new byte[]{2}, false, true));
        assertEquals("0x89", Converter.bytesToHexReverse(new byte[]{(byte) 0x89}, true, true));
        assertEquals("0xFF00", Converter.bytesToHexReverse(new byte[]{0, (byte) 0xFF}, true, true));
        assertEquals("ff00", Converter.bytesToHexReverse(new byte[]{0, (byte) 0xFF}, false, false));
        assertEquals("0x8901910000", Converter.bytesToHexReverse(new byte[]{0, 0, (byte) 0x91, 1, (byte) 0x89}, true, false));
        assertEquals(
                "0x0001FF0000000000000000FFFF",
                Converter.bytesToHexReverse(
                        new byte[]{(byte) 0xFF, (byte) 0xFF, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xFF, 1, 0},
                        true,
                        true
                )
        );

        byte[] bytes = new byte[256];
        for (int i = 0; i < 256; i++) {
            bytes[255 - i] = (byte) i;
        }
        assertEquals(allHex, Converter.bytesToHexReverse(bytes, true, true));
        assertEquals(allHex.toLowerCase().substring(2), Converter.bytesToHexReverse(bytes, false, false));
    }

    public void hexToBytes() {
        assertThrows(Converter.Exception.class, () -> Converter.hexToBytes("0xdq"));
        assertThrows(Converter.Exception.class, () -> Converter.hexToBytes(":"));
        assertThrows(Converter.Exception.class, () -> Converter.hexToBytes("="));
        assertThrows(Converter.Exception.class, () -> Converter.hexToBytes("G"));
        assertThrows(Converter.Exception.class, () -> Converter.hexToBytes("g"));
        assertNull(Converter.hexToBytes(null));
        assertEquals(new byte[]{}, Converter.hexToBytes(""));
        assertEquals(new byte[]{}, Converter.hexToBytes("0x"));
        assertEquals(new byte[]{0}, Converter.hexToBytes("0x00"));
        assertEquals(new byte[]{0}, Converter.hexToBytes("00"));
        assertEquals(new byte[]{0}, Converter.hexToBytes("0"));
        assertEquals(new byte[]{4}, Converter.hexToBytes("0x4"));
        assertEquals(new byte[]{10}, Converter.hexToBytes("A"));
        assertEquals(new byte[]{16}, Converter.hexToBytes("10"));
        assertEquals(new byte[]{(byte) 0x89}, Converter.hexToBytes("0x89"));
        assertEquals(new byte[]{0, (byte) 0xFF}, Converter.hexToBytes("0ff"));
        assertEquals(new byte[]{0, (byte) 0xFF}, Converter.hexToBytes("0x00Ff"));
        assertEquals(new byte[]{0, 0, (byte) 0x91, 1, (byte) 0x89}, Converter.hexToBytes("000910189"));
        assertEquals(
                new byte[]{(byte) 0xFF, (byte) 0xFF, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xFF, 1, 0},
                Converter.hexToBytes("ffFF0000000000000000FF0100")
        );
        byte[] bytes = new byte[256];
        for (int i = 0; i < 256; i++) {
            bytes[i] = (byte) i;
        }
        assertEquals(bytes, Converter.hexToBytes(allHex));
        assertEquals(bytes, Converter.hexToBytes(allHex.toLowerCase().substring(2)));
    }

    public void hexToBytesReverse() {
        assertThrows(Converter.Exception.class, () -> Converter.hexToBytesReverse("0xdq"));
        assertThrows(Converter.Exception.class, () -> Converter.hexToBytesReverse(":"));
        assertThrows(Converter.Exception.class, () -> Converter.hexToBytesReverse("="));
        assertThrows(Converter.Exception.class, () -> Converter.hexToBytesReverse("G"));
        assertThrows(Converter.Exception.class, () -> Converter.hexToBytesReverse("g"));
        assertNull(Converter.hexToBytesReverse(null));
        assertEquals(new byte[]{}, Converter.hexToBytesReverse(""));
        assertEquals(new byte[]{}, Converter.hexToBytesReverse("0x"));
        assertEquals(new byte[]{0}, Converter.hexToBytesReverse("0x00"));
        assertEquals(new byte[]{0}, Converter.hexToBytesReverse("00"));
        assertEquals(new byte[]{0}, Converter.hexToBytesReverse("0"));
        assertEquals(new byte[]{4}, Converter.hexToBytesReverse("0x4"));
        assertEquals(new byte[]{10}, Converter.hexToBytesReverse("A"));
        assertEquals(new byte[]{16}, Converter.hexToBytesReverse("10"));
        assertEquals(new byte[]{(byte) 0x89}, Converter.hexToBytesReverse("0x89"));
        assertEquals(new byte[]{(byte) 0xFF, 0}, Converter.hexToBytesReverse("0ff"));
        assertEquals(new byte[]{(byte) 0xFF, 0}, Converter.hexToBytesReverse("0x00Ff"));
        assertEquals(new byte[]{(byte) 0x89, 1, (byte) 0x91, 0, 0}, Converter.hexToBytesReverse("000910189"));
        assertEquals(
                new byte[]{(byte) 0xFF, (byte) 0xFF, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xFF, 1, 0},
                Converter.hexToBytesReverse("0001FF0000000000000000ffFF")
        );
        byte[] bytes = new byte[256];
        for (int i = 0; i < 256; i++) {
            bytes[255 - i] = (byte) i;
        }
        assertEquals(bytes, Converter.hexToBytesReverse(allHex));
        assertEquals(bytes, Converter.hexToBytesReverse(allHex.toLowerCase().substring(2)));
    }
}