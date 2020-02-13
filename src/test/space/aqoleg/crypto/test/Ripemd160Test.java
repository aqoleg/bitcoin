package space.aqoleg.crypto.test;

import org.junit.jupiter.api.Test;
import space.aqoleg.crypto.Ripemd160;
import space.aqoleg.utils.Converter;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Ripemd160Test {

    @Test
    void main() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(stream));
        Ripemd160.main(new String[]{});
        assertEquals("9c1185a5c5e9fc54612808977ee8f548b2258d31\n", stream.toString());
        stream.reset();
        Ripemd160.main(new String[]{""});
        assertEquals("9c1185a5c5e9fc54612808977ee8f548b2258d31\n", stream.toString());
        stream.reset();
        Ripemd160.main(new String[]{"abc"});
        assertEquals("8eb208f7e05d987a9b044a8e98c6b087f15a0bfc\n", stream.toString());
        stream.reset();
        Ripemd160.main(new String[]{"The quick", "brown fox jumps", "over the lazy", "dog"});
        assertEquals("37f332f68db77bd9d7edd4969571ad671cf9dd3b\n", stream.toString());
    }

    @Test
    void test() {
        assertThrows(NullPointerException.class, () -> Ripemd160.getHash(null));
        assertThrows(NullPointerException.class, () -> Ripemd160.getHash(null, 9, 9));
        assertThrows(IndexOutOfBoundsException.class, () -> Ripemd160.getHash(new byte[]{1, 1, 1, 1}, 2, 9));

        areEquals(
                "9c1185a5c5e9fc54612808977ee8f548b2258d31",
                Ripemd160.getHash(new byte[]{})
        );

        areEquals(
                "9c1185a5c5e9fc54612808977ee8f548b2258d31",
                Ripemd160.getHash("".getBytes())
        );

        areEquals(
                "0bdc9d2d256b3ee9daae347be6f4dc835a467ffe",
                Ripemd160.getHash("a".getBytes())
        );

        areEquals(
                "8eb208f7e05d987a9b044a8e98c6b087f15a0bfc",
                Ripemd160.getHash("abc".getBytes())
        );

        byte[] bytes = new byte[100];
        bytes[55] = 'a';
        bytes[56] = 'b';
        bytes[57] = 'c';
        areEquals(
                "8eb208f7e05d987a9b044a8e98c6b087f15a0bfc",
                Ripemd160.getHash(bytes, 55, 3)
        );

        areEquals(
                "5d0689ef49d2fae572b881b123a85ffa21595f36",
                Ripemd160.getHash("message digest".getBytes())
        );

        areEquals(
                "f71c27109c692c1b56bbdceb5b9d2865b3708dbc",
                Ripemd160.getHash("abcdefghijklmnopqrstuvwxyz".getBytes())
        );

        areEquals(
                "12a053384a9c0c88e405a06c27dcf49ada62eb2b",
                Ripemd160.getHash("abcdbcdecdefdefgefghfghighijhijkijkljklmklmnlmnomnopnopq".getBytes())
        );

        areEquals(
                "b0e20b6e3116640286ed3a87a5713079b21f5189",
                Ripemd160.getHash("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".getBytes())
        );

        areEquals(
                "9b752e45573d4b39f4dbd3323cab82bf63326bfb",
                Ripemd160.getHash(("1234567890123456789012345678901234567890"
                        + "1234567890123456789012345678901234567890").getBytes())
        );

        byte[] millionA = new byte[1000000];
        Arrays.fill(millionA, (byte) 'a');
        areEquals(
                "52783243c1697bdbe16d37f97f68f08325dc1528",
                Ripemd160.getHash(millionA)
        );

        areEquals(
                "37f332f68db77bd9d7edd4969571ad671cf9dd3b",
                Ripemd160.getHash("The quick brown fox jumps over the lazy dog".getBytes())
        );

        areEquals(
                "132072df690933835eb8b6ad0b77e7b6f14acad7",
                Ripemd160.getHash("The quick brown fox jumps over the lazy cog".getBytes())
        );
        areEquals(
                "9da839c2525d161aba0384a5efe2ee0669c51c51",
                Ripemd160.getHash("62-byte string------123456789012345678901234567890123456789012".getBytes())
        );

        areEquals(
                "ede9ecf9119451b361611f26ed609545a2922d69",
                Ripemd160.getHash("63-byte string------1234567890123456789012345678901234567890123".getBytes())
        );
        areEquals(
                "7d0e0292b0901905fa05c03e92b37fefb4a6efbf",
                Ripemd160.getHash("64-byte string------12345678901234567890123456789012345678901234".getBytes())
        );
        areEquals(
                "a93375968701f8306374734dde4ade4047247297",
                Ripemd160.getHash("65-byte string------123456789012345678901234567890123456789012345".getBytes())
        );
        areEquals(
                "a7084536bd0765650c392f7b299257a01585ef6e",
                Ripemd160.getHash("something stuff to be hashed".getBytes())
        );
    }

    private static void areEquals(String correctHash, byte[] hash) {
        assertEquals(correctHash, Converter.bytesToHex(hash, false, false));
    }
}