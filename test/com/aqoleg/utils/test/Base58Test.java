package com.aqoleg.utils.test;

import com.aqoleg.Test;
import com.aqoleg.utils.Base58;

@SuppressWarnings("unused")
public class Base58Test extends Test {

    public static void main(String[] args) {
        new Base58Test().testAll();
    }

    public void inAndOutTest() {
        assertThrows(Base58.Exception.class, () -> Base58.decode("1I"));
        assertNull(Base58.encode(null));
        assertNull(Base58.decode(null));
        test("", new byte[]{});
        test("1", new byte[]{0});
        test("2", new byte[]{1});
        test("4", new byte[]{3});
        test("111", new byte[]{0, 0, 0});
        test("1112", new byte[]{0, 0, 0, 1});
        test("12", new byte[]{0, 1});
        test("23", new byte[]{60});
        test("3D", new byte[]{(byte) 128});
        test("113E", new byte[]{0, 0, (byte) 129});
        test("Jsof", "58B".getBytes());
        test("6KbQDUjRUyn8QJniSvxCFotG17GfRaL59qcH6jHv", "u3n9cuccn3ncinuer3iucjdcn 3je".getBytes());
        test(
                "3hUFUB9D81fnRDDJrs4Ftz8eDfnMTYFW7ZiWTocCxBJLU2RirhHTDky",
                "6KbQDUjRUyn8QJniSvxCFotG17GfRaL59qcH6jHv".getBytes()
        );
        test("9bid9cfGRZD", "3hUFUB9D".getBytes());
        test("1115S", new byte[]{0, 0, 0, 1, 1});
        test("11BwACo", new byte[]{0, 0, 0x07, 0x60, 0, 0});
        test("111187838473", new byte[]{0, 0, 0, 0, 0x0E, 0x44, (byte) 0xDC, 0x7E, 0x60, 0x62});
        test("Zz34111", new byte[]{0x01, 0x24, 0x59, (byte) 0x9A, 0x01, 0x38});
        test(
                "114ydZNFi1RGG1TxT",
                new byte[]{0, 0, 0x10, 0x09, 0x12, 0x08, 0x19, (byte) 0x83, (byte) 0x91, (byte) 0x83, (byte) 0xa3,
                        0x29, (byte) 0x88}
        );
        test(
                "111FoxesAreSiLentHunters1",
                new byte[]{0, 0, 0, 0x77, (byte) 0xEE, 0x73, (byte) 0xB2, (byte) 0x9B, 0x14, 0x6F, 0x21, 0x27, 0x33,
                        (byte) 0xA7, (byte) 0xD8, (byte) 0xD1, (byte) 0xA5, 0x2C, 0x50}
        );
    }

    private void test(String string, byte[] bytes) {
        assertEquals(string, Base58.encode(bytes));
        assertEquals(bytes, Base58.decode(string));
    }
}