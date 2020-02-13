package space.aqoleg.utils.test;

import org.junit.jupiter.api.Test;
import space.aqoleg.utils.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

class BigIntegerTest {

    @Test
    void constants() {
        assertEquals(0, BigInteger.zero.toInteger());
        assertEquals(1, BigInteger.one.toInteger());
        assertEquals(2, BigInteger.two.toInteger());
    }

    @Test
    void valueOf() {
        assertEquals(0, BigInteger.valueOf(0).signum());
        assertEquals("1000110101", BigInteger.valueOf(565).toBinaryString(false));
        assertEquals("10101110111011101", BigInteger.valueOf(89565).toBinaryString(false));
        assertEquals(-1, BigInteger.valueOf(-5899).signum());
        assertEquals("1110011001110111", BigInteger.valueOf(-58999).toBinaryString(false));
        assertEquals("1", BigInteger.valueOf(1).toBinaryString(false));
        assertEquals("10", BigInteger.valueOf(2).toBinaryString(false));
        assertEquals("1010110111011101101110101111010", BigInteger.valueOf(1458494842).toBinaryString(false));
        assertEquals("1111111111111111111111111111111", BigInteger.valueOf(2147483647).toBinaryString(false));
        assertEquals("1001111011010010111010001011101", BigInteger.valueOf(-1332311133).toBinaryString(false));
    }

    @Test
    void createFromBigEndian() {
        assertThrows(NullPointerException.class, () -> BigInteger.createFromBigEndian(null));
        assertThrows(NullPointerException.class, () -> BigInteger.createFromBigEndian(null, 9, 9));
        assertThrows(
                ArrayIndexOutOfBoundsException.class,
                () -> BigInteger.createFromBigEndian(new byte[]{9, 8}, 1, 3)
        );

        assertEquals(
                "100011101010110",
                BigInteger.createFromBigEndian(new byte[]{0x9, 0x47, 0x56, 0x0, 0x8}, 1, 2).toBinaryString(false)
        );
        assertEquals(
                "10101010100011100011010",
                BigInteger.createFromBigEndian(new byte[]{0, 0, 0x55, 0x47, 0x1A}, 2, 3).toBinaryString(false)
        );
        assertEquals(
                "101100000101",
                BigInteger.createFromBigEndian(new byte[]{0x0, 0x0, 0xB, 0x5}).toBinaryString(false)
        );
        assertEquals("11111111", BigInteger.createFromBigEndian(new byte[]{(byte) 0xFF}).toBinaryString(false));
        assertEquals("0b0", BigInteger.createFromBigEndian(new byte[]{0x0}).toBinaryString(true));
        assertEquals("0b0", BigInteger.createFromBigEndian(new byte[]{}).toBinaryString(true));
    }

    @Test
    void createFromLittleEndian() {
        assertThrows(NullPointerException.class, () -> BigInteger.createFromLittleEndian(null));
        assertThrows(NullPointerException.class, () -> BigInteger.createFromLittleEndian(null, 9, 9));
        assertThrows(
                ArrayIndexOutOfBoundsException.class,
                () -> BigInteger.createFromLittleEndian(new byte[]{9, 8}, 1, 3)
        );

        assertEquals(
                "101011001000111",
                BigInteger.createFromLittleEndian(new byte[]{0x9, 0x47, 0x56, 0x0, 0x8}, 1, 2).toBinaryString(false)
        );
        assertEquals(
                "110100100011101010101",
                BigInteger.createFromLittleEndian(new byte[]{0, 0, 0x55, 0x47, 0x1A}, 2, 3).toBinaryString(false)
        );
        assertEquals(
                "101100000101",
                BigInteger.createFromLittleEndian(new byte[]{0x05, 0x0B, 0x00, 0x00}).toBinaryString(false)
        );
        assertEquals("11111111", BigInteger.createFromLittleEndian(new byte[]{(byte) 0xFF}).toBinaryString(false));
        assertEquals("0b0", BigInteger.createFromLittleEndian(new byte[]{0x0}).toBinaryString(true));
        assertEquals("0b0", BigInteger.createFromLittleEndian(new byte[]{}).toBinaryString(true));
    }

    @Test
    void createFromVariableLength() {
        assertThrows(NullPointerException.class, () -> BigInteger.createFromVariableLength(null));
        assertThrows(
                ArrayIndexOutOfBoundsException.class,
                () -> BigInteger.createFromVariableLength(new byte[]{(byte) 0xFD, 8})
        );
        assertThrows(NullPointerException.class, () -> BigInteger.createFromVariableLength(null, 9));
        assertThrows(
                ArrayIndexOutOfBoundsException.class,
                () -> BigInteger.createFromVariableLength(new byte[]{0, (byte) 0xFF, 9, 8}, 1)
        );

        assertEquals(
                "0203",
                BigInteger.createFromVariableLength(new byte[]{(byte) 0xFD, 0x03, 0x02}).toHexString(false)
        );
        assertEquals("00", BigInteger.createFromVariableLength(new byte[]{0x0}).toHexString(false));
        assertEquals("01", BigInteger.createFromVariableLength(new byte[]{0, 1, 11}, 1).toHexString(false));
        assertEquals(
                "FC",
                BigInteger.createFromVariableLength(new byte[]{9, 8, 33, (byte) 0xFC, 0, 0}, 3).toHexString(false)
        );
        assertEquals(
                "FF",
                BigInteger.createFromVariableLength(new byte[]{(byte) 0xFD, (byte) 0xFF, 0, 0}).toHexString(false)
        );
        assertEquals(
                "FCDFD0",
                BigInteger.createFromVariableLength(new byte[]{(byte) 0xFE, (byte) 0xD0, (byte) 0xDF, (byte) 0xFC, 0})
                        .toHexString(false)
        );
        assertEquals(
                "01FCDFD000",
                BigInteger.createFromVariableLength(new byte[]{(byte) 0xFF, 0, (byte) 0xD0, (byte) 0xDF, (byte) 0xFC,
                        0x01, 0, 0, 0, 0}).toHexString(false)
        );
    }

    @Test
    void parse() {
        assertThrows(NullPointerException.class, () -> BigInteger.parse(null));
        assertThrows(UnsupportedOperationException.class, () -> BigInteger.parse(""));
        assertThrows(UnsupportedOperationException.class, () -> BigInteger.parse("0x"));
        assertThrows(UnsupportedOperationException.class, () -> BigInteger.parse("0xy"));
        assertThrows(UnsupportedOperationException.class, () -> BigInteger.parse("0b9"));

        assertEquals(0, BigInteger.parse("0").signum());
        assertEquals(0, BigInteger.parse("0x00").signum());
        assertEquals("0x02E51E13", BigInteger.parse("0b010111001010001111000010011").toHexString(true));
        assertEquals("0x4A200F", BigInteger.parse("0x4a200F").toHexString(true));
        assertEquals("5ED7A81F5F5FFFE3FFFF83", BigInteger.parse("10111101101011110101000000111110101111101011111111"
                + "1111111100011111111111111111110000011").toHexString(false));
        assertEquals("0A0123456789ABCDEFABCDEF0F", BigInteger.parse("a0123456789abcdefABCDEF0F").toHexString(false));
        assertEquals("0EAA999400CD95A5", BigInteger.parse("0X000eAA999400cd95A5").toHexString(false));
        assertEquals("07554CCA00CD95A5", BigInteger.parse("0B0000000111010101010100110011001010000000001100110110010"
                + "10110100101").toHexString(false));
    }

    @Test
    void getRandom() {
        assertThrows(UnsupportedOperationException.class, () -> BigInteger.getRandom(1, 1));
        assertThrows(UnsupportedOperationException.class, () -> BigInteger.getRandom(8, 6));

        assertEquals(2, BigInteger.getRandom(2, 2).bitLength());
        BigInteger a = BigInteger.getRandom(12, 12);
        BigInteger b = BigInteger.getRandom(12, 12);
        assertNotEquals(-1, a.signum());
        assertEquals(12, a.bitLength());
        assertEquals(12, b.bitLength());
        assertNotEquals(0, a.compareTo(b));
        a = BigInteger.getRandom(11, 12);
        assertTrue(a.bitLength() >= 11);
        assertTrue(a.bitLength() <= 12);
        a = BigInteger.getRandom(2, 23);
        assertTrue(a.bitLength() >= 2);
        assertTrue(a.bitLength() <= 23);
        assertNotEquals(0, a.compareTo(b));
    }

    @Test
    void getRandomPrime() {
        assertThrows(UnsupportedOperationException.class, () -> BigInteger.getRandom(-1, 1));
        assertThrows(UnsupportedOperationException.class, () -> BigInteger.getRandom(80, 6));

        BigInteger a = BigInteger.getRandomPrime(12, 12);
        BigInteger b = BigInteger.getRandomPrime(12, 12);
        assertEquals(12, a.bitLength());
        assertEquals(12, b.bitLength());
        assertNotEquals(0, a.compareTo(b));
        assertTrue(a.isProbablyPrime());
        assertTrue(b.isProbablyPrime());
        assertTrue(BigInteger.getRandomPrime(2, 2).isProbablyPrime());
        assertTrue(BigInteger.getRandomPrime(2, 4).isProbablyPrime());
    }

    @Test
    void testBit() {
        BigInteger a = BigInteger.parse("10100100");
        assertThrows(IndexOutOfBoundsException.class, () -> a.testBit(-2));
        assertThrows(IndexOutOfBoundsException.class, () -> a.testBit(8));
        assertTrue(a.testBit(2));
        assertTrue(a.testBit(7));
        assertFalse(a.testBit(0));
        assertFalse(a.testBit(3));
        assertFalse(a.testBit(6));
    }

    @Test
    void bitLength() {
        assertEquals(0, BigInteger.parse("0").bitLength());
        assertEquals(8, BigInteger.parse("10100100").bitLength());
        assertEquals(9, BigInteger.parse("110100100").bitLength());
        assertEquals(6, BigInteger.parse("0b101001").bitLength());
        assertEquals(2, BigInteger.parse("0b10").bitLength());
    }

    @Test
    void signum() {
        assertEquals(0, BigInteger.parse("0").signum());
        assertEquals(0, BigInteger.one.subtract(BigInteger.one).negate().signum());
        assertEquals(1, BigInteger.parse("6").signum());
        assertEquals(0, BigInteger.zero.signum());
        assertEquals(1, BigInteger.parse("10100100").signum());
        assertEquals(-1, BigInteger.parse("110100100").negate().signum());
        assertEquals(-1, BigInteger.zero.subtract(BigInteger.one).signum());
    }

    @Test
    void shiftLeft() {
        assertEquals(0, BigInteger.zero.shiftLeft(34).toInteger());
        assertEquals(0b1101, BigInteger.parse("0b1101").shiftLeft(0).toInteger());
        assertEquals(-0b10100, BigInteger.parse("0b101").negate().shiftLeft(2).toInteger());
        assertEquals(0b1101011000, BigInteger.parse("0b1101011").shiftLeft(3).toInteger());
        assertEquals(0b101111011010110000000000000, BigInteger.parse("0b10111101101011").shiftLeft(13).toInteger());
        assertEquals(0b10, BigInteger.parse("0b1").shiftLeft(1).toInteger());
        assertEquals(0b110, BigInteger.parse("0b11010").shiftLeft(-2).toInteger());
    }

    @Test
    void shiftRight() {
        assertEquals(0, BigInteger.zero.shiftRight(14).toInteger());
        assertEquals(0b110, BigInteger.parse("0b110").shiftRight(0).toInteger());
        assertEquals(0, BigInteger.parse("0b1101").negate().shiftRight(15).toInteger());
        assertEquals(0, BigInteger.parse("0b11101").shiftRight(5).toInteger());
        assertEquals(0b1101, BigInteger.parse("0b1101011").shiftRight(3).toInteger());
        assertEquals(-0b10111100, BigInteger.parse("0b10111100101110").negate().shiftRight(6).toInteger());
        assertEquals(0b101111000000000, BigInteger.parse("0b101111").shiftRight(-9).toInteger());
        assertEquals(0b10, BigInteger.parse("0b100").shiftRight(1).toInteger());
    }

    @Test
    void compareTo() {
        assertThrows(NullPointerException.class, () -> BigInteger.zero.compareTo(null));
        assertEquals(0, BigInteger.zero.compareTo(BigInteger.zero));
        BigInteger b = BigInteger.parse("110");
        assertEquals(-1, BigInteger.one.compareTo(b));
        BigInteger a = b.negate();
        assertEquals(-1, a.compareTo(b));
        assertEquals(0, a.compareTo(b.negate()));
        a = BigInteger.parse("1000100");
        assertEquals(1, a.compareTo(b));
        b = BigInteger.parse("1001100");
        assertEquals(-1, a.compareTo(b));
        assertEquals(1, b.compareTo(a));
        assertEquals(0, a.compareTo(a));
        a = BigInteger.parse("110");
        b = BigInteger.parse("1000100");
        assertEquals(1, b.compareTo(a));
        assertEquals(1, a.negate().compareTo(b.negate()));
    }

    @Test
    void negate() {
        BigInteger b = BigInteger.parse("110");
        assertEquals(0, b.negate().add(b).signum());
        assertNotEquals(0, b.negate().compareTo(b));
        assertEquals(0, BigInteger.zero.negate().signum());
    }

    @Test
    void add() {
        assertThrows(NullPointerException.class, () -> BigInteger.zero.add(null));

        BigInteger a, b, c;
        for (int i = 0; i < 2000; i++) {
            int intA = (int) (Math.random() * 12565495);
            int intB = (int) (Math.random() * 1288895);
            a = BigInteger.parse(Integer.toBinaryString(intA));
            b = BigInteger.parse(Integer.toBinaryString(intB));
            assertEquals(Integer.toBinaryString(intA + intB), a.add(b).toBinaryString(false));
            assertEquals(Integer.toBinaryString(intA * 2 + intB), a.add(b).add(a).toBinaryString(false));
        }
        a = BigInteger.parse("0b01010100010100011111111111010101001101010010010111111110101011111111000111110101001");
        b = BigInteger.parse("0b00101111010001010000100000100011111111111100000000011110010101111101111000000111110");
        c = BigInteger.parse("0b10000011100101110000011111111001001101001110011000011101000001111100111111111100111");
        assertEquals(0, c.compareTo(a.add(b)));
        assertEquals(0, c.add(a.negate().add(b.negate())).signum());
        a = BigInteger.parse("100");
        b = BigInteger.parse("10");
        assertEquals(1, a.add(b).signum());
        assertEquals("110", a.add(b).toBinaryString(false));
        assertEquals(-1, a.negate().add(b).signum());
        assertEquals("10", a.negate().add(b).toBinaryString(false));
        assertEquals(1, a.add(b.negate()).signum());
        assertEquals("10", a.add(b.negate()).toBinaryString(false));
        assertEquals(-1, a.negate().add(b.negate()).signum());
        assertEquals("110", a.negate().add(b.negate()).toBinaryString(false));
        assertEquals(0, b.add(b.negate()).signum());
        a = BigInteger.parse("f4562ACD77fffad");
        b = BigInteger.parse("1233384999bbbb");
        c = BigInteger.parse("F5795E52119BB68");
        assertEquals(0, a.compareTo(a.add(BigInteger.zero)));
        assertEquals(0, c.compareTo(a.add(b)));
        assertEquals(0, c.add(a.negate().add(b.negate())).signum());
    }

    @Test
    void subtract() {
        assertThrows(NullPointerException.class, () -> BigInteger.zero.subtract(null));

        BigInteger a, b, c;
        for (int i = 0; i < 2000; i++) {
            int intA = (int) (Math.random() * 125654895);
            int intB = (int) (Math.random() * 128888895);
            a = BigInteger.parse(Integer.toBinaryString(intA));
            b = BigInteger.parse(Integer.toBinaryString(intB));
            c = a.subtract(b);
            assertEquals(Integer.toBinaryString(Math.abs(intA - intB)), c.toBinaryString(false));
            if (intA == intB) {
                assertEquals(0, c.signum());
            } else {
                assertEquals(intA > intB ? 1 : -1, c.signum());
            }
        }

        a = BigInteger.parse("0b01010100010100011111111111010101001101010010010111111110101011111111000111110101001");
        b = BigInteger.parse("0b00101111010001010000100000100011111111111100000000011110010101111101111000000111110");
        c = BigInteger.parse("0b10000011100101110000011111111001001101001110011000011101000001111100111111111100111");
        assertEquals(0, a.compareTo(c.subtract(b)));
        assertEquals(0, b.compareTo(c.subtract(a)));
        a = BigInteger.parse("100");
        b = BigInteger.parse("10");
        assertEquals(1, a.subtract(b).signum());
        assertEquals("10", a.subtract(b).toBinaryString(false));
        assertEquals(-1, b.subtract(a).signum());
        assertEquals("0b10", b.subtract(a).toBinaryString(true));
        assertEquals(-1, a.negate().subtract(b).signum());
        assertEquals("0b110", a.negate().subtract(b).toBinaryString(true));
        assertEquals(-1, b.negate().subtract(a).signum());
        assertEquals("110", b.negate().subtract(a).toBinaryString(false));
        assertEquals(1, a.subtract(b.negate()).signum());
        assertEquals("110", a.subtract(b.negate()).toBinaryString(false));
        assertEquals(1, b.subtract(a.negate()).signum());
        assertEquals("110", b.subtract(a.negate()).toBinaryString(false));
        assertEquals(-1, a.negate().subtract(b.negate()).signum());
        assertEquals("10", a.negate().subtract(b.negate()).toBinaryString(false));
        assertEquals(1, b.negate().subtract(a.negate()).signum());
        assertEquals("10", b.negate().subtract(a.negate()).toBinaryString(false));
        assertEquals(0, b.subtract(b).signum());
        a = BigInteger.parse("f4562ACD77fffad");
        b = BigInteger.parse("1233384999bbbb");
        c = BigInteger.parse("F5795E52119BB68");
        assertEquals(0, c.compareTo(c.subtract(BigInteger.zero)));
        assertEquals(0, c.compareTo(BigInteger.zero.subtract(c).negate()));
        assertEquals(0, a.compareTo(c.subtract(b)));
        assertEquals(0, b.compareTo(c.subtract(a)));
    }

    @Test
    void multiply() {
        assertThrows(NullPointerException.class, () -> BigInteger.one.multiply(null));

        BigInteger a, b, c;
        for (int i = 0; i < 2000; i++) {
            int intA = (int) (Math.random() * 125654895);
            int intB = (int) (Math.random() * 12888595);
            long multiply = (long) intA * intB;
            a = BigInteger.parse(Integer.toBinaryString(intA));
            b = BigInteger.parse(Integer.toBinaryString(intB));
            assertEquals(Long.toBinaryString(multiply), a.multiply(b).toBinaryString(false));
            assertEquals(Long.toBinaryString(multiply), b.multiply(a).toBinaryString(false));
        }

        a = BigInteger.parse("101");
        b = BigInteger.parse("10");
        assertEquals(1, a.multiply(b).signum());
        assertEquals("1010", a.multiply(b).toBinaryString(false));
        assertEquals(-1, a.negate().multiply(b).signum());
        assertEquals(-1, b.negate().multiply(a).signum());
        assertEquals("1010", b.negate().multiply(a).toBinaryString(false));
        assertEquals(1, a.negate().multiply(b.negate()).signum());
        assertEquals(0, b.multiply(BigInteger.zero).signum());
        a = BigInteger.parse("f4567fad");
        b = BigInteger.parse("2399bb");
        c = BigInteger.parse("21FA8DA224A85F");
        assertEquals(0, c.compareTo(a.multiply(b)));
        assertEquals(0, c.compareTo(b.multiply(a)));
        assertEquals(0, c.compareTo(c.multiply(BigInteger.one)));
    }

    @Test
    void divideAndRemainder() {
        assertThrows(NullPointerException.class, () -> BigInteger.one.divideAndRemainder(null));
        assertThrows(ArithmeticException.class, () -> BigInteger.one.divideAndRemainder(BigInteger.zero));

        BigInteger a, b, c;
        for (int i = 0; i < 2000; i++) {
            int intA = (int) (Math.random() * 1256554895);
            int intB = (int) (Math.random() * 12888595);
            a = BigInteger.parse(Integer.toBinaryString(intA));
            b = BigInteger.parse(Integer.toBinaryString(intB));
            BigInteger[] ab = a.divideAndRemainder(b);
            BigInteger[] ba = b.divideAndRemainder(a);
            assertEquals(Integer.toBinaryString(intA / intB), ab[0].toBinaryString(false));
            assertEquals(Integer.toBinaryString(intB / intA), ba[0].toBinaryString(false));
            assertEquals(Integer.toBinaryString(intA % intB), ab[1].toBinaryString(false));
            assertEquals(Integer.toBinaryString(intB % intA), ba[1].toBinaryString(false));
        }

        a = BigInteger.parse("110");
        b = BigInteger.parse("10");
        assertEquals(1, a.divideAndRemainder(b)[0].signum());
        assertEquals("11", a.divideAndRemainder(b)[0].toBinaryString(false));
        assertEquals(-1, a.negate().divideAndRemainder(b)[0].signum());
        assertEquals("0b11", a.negate().divideAndRemainder(b)[0].toBinaryString(true));
        assertEquals(0, b.negate().divideAndRemainder(a)[0].signum());
        assertEquals(0, b.divideAndRemainder(b)[0].compareTo(BigInteger.one));
        assertEquals(1, a.negate().divideAndRemainder(b.negate())[0].signum());
        assertEquals(0, BigInteger.zero.divideAndRemainder(a)[0].signum());
        a = BigInteger.parse("9adffadc17155514");
        b = BigInteger.parse("FBBAAAADD");
        assertEquals("09D80A59", a.divideAndRemainder(b)[0].toHexString(false));
        assertEquals("1", a.divideAndRemainder(a)[0].toBinaryString(false));
        a = BigInteger.parse("10");
        b = BigInteger.parse("101");
        c = a.divideAndRemainder(b)[1];
        assertEquals(1, c.signum());
        assertEquals("10", c.toBinaryString(false));
        c = a.negate().divideAndRemainder(b)[1];
        assertEquals(1, c.signum());
        assertEquals("11", c.toBinaryString(false));
        c = a.divideAndRemainder(b.negate())[1];
        assertEquals(-1, c.signum());
        assertEquals("11", c.toBinaryString(false));
        c = a.negate().divideAndRemainder(b.negate())[1];
        assertEquals(-1, c.signum());
        assertEquals("10", c.toBinaryString(false));
        a = BigInteger.parse("9fdca49765567655fffcd");
        b = BigInteger.parse("1452222255fffe");
        assertEquals("0x0CD2CCAAC19CE1", a.divideAndRemainder(b)[1].toHexString(true));
        assertEquals(0, a.divideAndRemainder(a)[1].signum());
    }

    @Test
    void divide() {
        assertThrows(NullPointerException.class, () -> BigInteger.one.divide(null));
        assertThrows(ArithmeticException.class, () -> BigInteger.one.divide(BigInteger.zero));

        BigInteger a, b;
        for (int i = 0; i < 2000; i++) {
            int intA = (int) (Math.random() * 1256554895);
            int intB = (int) (Math.random() * 12888595);
            a = BigInteger.parse(Integer.toBinaryString(intA));
            b = BigInteger.parse(Integer.toBinaryString(intB));
            assertEquals(Integer.toBinaryString(intA / intB), a.divide(b).toBinaryString(false));
            assertEquals(Integer.toBinaryString(intB / intA), b.divide(a).toBinaryString(false));
        }

        a = BigInteger.parse("110");
        b = BigInteger.parse("10");
        assertEquals(1, a.divide(b).signum());
        assertEquals("11", a.divide(b).toBinaryString(false));
        assertEquals(-1, a.negate().divide(b).signum());
        assertEquals("0b11", a.negate().divide(b).toBinaryString(true));
        assertEquals(0, b.negate().divide(a).signum());
        assertEquals(0, b.divide(b).compareTo(BigInteger.one));
        assertEquals(1, a.negate().divide(b.negate()).signum());
        assertEquals(0, BigInteger.zero.divide(a).signum());
        a = BigInteger.parse("9adffadc17155514");
        b = BigInteger.parse("FBBAAAADD");
        assertEquals("09D80A59", a.divide(b).toHexString(false));
        assertEquals("1", a.divide(a).toBinaryString(false));
    }

    @Test
    void remainder() {
        assertThrows(NullPointerException.class, () -> BigInteger.one.remainder(null));
        assertThrows(ArithmeticException.class, () -> BigInteger.one.remainder(BigInteger.zero));

        BigInteger a, b, c;
        for (int i = 0; i < 2000; i++) {
            int intA = (int) (Math.random() * 1256554895);
            int intB = (int) (Math.random() * 128885958);
            a = BigInteger.parse(Integer.toBinaryString(intA));
            b = BigInteger.parse(Integer.toBinaryString(intB));
            assertEquals(Integer.toBinaryString(intA % intB), a.remainder(b).toBinaryString(false));
            assertEquals(Integer.toBinaryString(intB % intA), b.remainder(a).toBinaryString(false));
        }

        a = BigInteger.parse("10");
        b = BigInteger.parse("101");
        c = a.remainder(b);
        assertEquals(1, c.signum());
        assertEquals("10", c.toBinaryString(false));
        c = a.negate().remainder(b);
        assertEquals(1, c.signum());
        assertEquals("11", c.toBinaryString(false));
        c = a.remainder(b.negate());
        assertEquals(-1, c.signum());
        assertEquals("11", c.toBinaryString(false));
        c = a.negate().remainder(b.negate());
        assertEquals(-1, c.signum());
        assertEquals("10", c.toBinaryString(false));
        a = BigInteger.parse("9fdca49765567655fffcd");
        b = BigInteger.parse("1452222255fffe");
        assertEquals("0x0CD2CCAAC19CE1", a.remainder(b).toHexString(true));
        assertEquals(0, a.remainder(a).signum());
    }

    @Test
    void isqrt() {
        assertThrows(ArithmeticException.class, () -> BigInteger.two.negate().isqrt());

        assertEquals(0, BigInteger.zero.isqrt().toInteger());
        assertEquals(1, BigInteger.one.isqrt().toInteger());
        assertEquals(1, BigInteger.two.isqrt().toInteger());
        assertEquals(1, BigInteger.valueOf(3).isqrt().toInteger());
        assertEquals(2, BigInteger.valueOf(4).isqrt().toInteger());
        assertEquals(2, BigInteger.valueOf(5).isqrt().toInteger());
        assertEquals(7, BigInteger.valueOf(49).isqrt().toInteger());
        assertEquals(7, BigInteger.valueOf(50).isqrt().toInteger());
        assertEquals(0b10000, BigInteger.valueOf(0b100000000).isqrt().toInteger());
        BigInteger a = BigInteger.parse("883756433473856376573");
        assertEquals(0, a.compareTo(a.multiply(a).isqrt()));
        assertEquals(0, a.compareTo(a.multiply(a).add(BigInteger.one).isqrt()));
        a = BigInteger.parse("883756433473856376573");
        assertEquals(0, a.compareTo(a.multiply(a).isqrt()));
        a = BigInteger.parse("36984088649026141674860566025160085729845143049675518936248606642207362762409");
        assertEquals(0, a.compareTo(a.multiply(a).isqrt()));
        a = BigInteger.parse("87348657637467364828798273883756433473856376573");
        assertEquals(0, a.compareTo(a.multiply(a).add(BigInteger.two).isqrt()));
    }

    @Test
    void lcm() {
        assertThrows(NullPointerException.class, () -> BigInteger.one.lcm(null));

        BigInteger a = BigInteger.parse("548");
        assertEquals(0, a.negate().lcm(a).compareTo(a));
        assertEquals(0, BigInteger.zero.lcm(a).signum());
        assertEquals(0, BigInteger.one.lcm(a).compareTo(a));
        assertEquals(0, BigInteger.one.lcm(BigInteger.one).compareTo(BigInteger.one));
        assertEquals(0, a.lcm(a).compareTo(a));
        a = BigInteger.parse("2D2A").lcm(BigInteger.parse("465"));
        assertEquals(1, a.signum());
        assertEquals("0x422886", a.toHexString(true));
        assertEquals(
                "18AE68F66353C062",
                BigInteger.parse("4250191a").negate().lcm(BigInteger.parse("5f482335")).toHexString(false)
        );
        assertEquals("0xC6", BigInteger.parse("16").lcm(BigInteger.parse("12").negate()).toHexString(true));
        assertEquals("0x065941DF", BigInteger.parse("ddb").lcm(BigInteger.parse("754d")).toHexString(true));
        assertEquals("1F5AB414A9", BigInteger.parse("3eb57").lcm(BigInteger.parse("7ffff")).toHexString(false));
        assertEquals("0x0400", BigInteger.parse("20").lcm(BigInteger.parse("400")).toHexString(true));
        assertEquals("31AD0DE47F", BigInteger.parse("3e71").lcm(BigInteger.parse("cba9ef")).toHexString(false));
        assertEquals("04D06D765B", BigInteger.parse("20923").lcm(BigInteger.parse("25d69")).toHexString(false));
        assertEquals("0x2163", BigInteger.parse("4d").lcm(BigInteger.parse("309")).toHexString(true));
        assertEquals("0x0325", BigInteger.parse("73").lcm(BigInteger.parse("23")).toHexString(true));
    }

    @Test
    void modPow() {
        BigInteger b = BigInteger.parse("f5848");
        assertThrows(NullPointerException.class, () -> b.modPow(b, null));
        assertThrows(NullPointerException.class, () -> b.modPow(null, b));
        assertThrows(ArithmeticException.class, () -> b.negate().modPow(b, b));
        assertThrows(ArithmeticException.class, () -> b.modPow(BigInteger.zero, b));
        assertThrows(ArithmeticException.class, () -> b.modPow(b.negate(), b));
        assertThrows(ArithmeticException.class, () -> b.modPow(b, BigInteger.zero));
        assertThrows(ArithmeticException.class, () -> b.modPow(b, b.negate()));

        BigInteger a = BigInteger.parse("f548");
        assertEquals(0, BigInteger.zero.modPow(a, a).signum());
        assertEquals(0, BigInteger.zero.modPow(BigInteger.one, a).signum());
        assertEquals(0, BigInteger.zero.modPow(a, BigInteger.one).signum());
        assertEquals(0, BigInteger.one.modPow(a, BigInteger.one).signum());
        assertEquals(0, BigInteger.one.modPow(a, a).compareTo(BigInteger.one));
        assertEquals(0, BigInteger.parse("1010").modPow(BigInteger.parse("1000"), BigInteger.parse("10")).signum());
        assertEquals(
                0,
                BigInteger.parse("40A5BA7")
                        .modPow(BigInteger.parse("10"), BigInteger.parse("11"))
                        .compareTo(BigInteger.one)
        );
        a = BigInteger.parse("2703").modPow(BigInteger.parse("63"), BigInteger.parse("7"));
        assertEquals(1, a.signum());
        assertEquals("0x06", a.toHexString(true));
        assertEquals(
                "012A",
                BigInteger.parse("d6d73")
                        .modPow(BigInteger.parse("3d3"), BigInteger.parse("29b"))
                        .toHexString(false)
        );
        assertEquals(
                "045C8F",
                BigInteger.parse("9dd4ca3c5b")
                        .modPow(BigInteger.parse("eefd3"), BigInteger.parse("a5aa7"))
                        .toHexString(false)
        );
        assertEquals(
                "0296C836",
                BigInteger.parse("9dd4ca3c5b")
                        .modPow(BigInteger.parse("3a5a5673"), BigInteger.parse("40bda6f"))
                        .toHexString(false)
        );
        assertEquals(
                "01",
                BigInteger.parse("28679b9f")
                        .modPow(BigInteger.parse("649b68"), BigInteger.parse("30f"))
                        .toHexString(false)
        );
        assertEquals(
                "04",
                BigInteger.parse("d").modPow(BigInteger.parse("c"), BigInteger.parse("b")).toHexString(false)
        );
        assertEquals(
                "033C",
                BigInteger.parse("246384").modPow(BigInteger.parse("3c9eb"), BigInteger.parse("954")).toHexString(false)
        );
        assertEquals(
                "6914F953",
                BigInteger.parse("25bdf56776")
                        .modPow(BigInteger.parse("aabdf4566"), BigInteger.parse("56678788d"))
                        .toHexString(false)
        );
        assertEquals(
                "031835F2",
                BigInteger.parse("25abfff456")
                        .modPow(BigInteger.parse("a12344566"), BigInteger.parse("ee8788d"))
                        .toHexString(false)
        );
        assertEquals(
                "05F1C6AA7A94D2",
                BigInteger.parse("2515894456")
                        .modPow(BigInteger.parse("a1234feaddef66"), BigInteger.parse("ee8244883788d"))
                        .toHexString(false)
        );
        assertEquals(
                "365BA016E2",
                BigInteger.parse("2515456")
                        .modPow(BigInteger.parse("a12addef66"), BigInteger.parse("e44883788d"))
                        .toHexString(false)
        );
        assertEquals(
                "D8E7F7",
                BigInteger.parse("2515456")
                        .modPow(BigInteger.parse("a12eef66"), BigInteger.parse("e4788d"))
                        .toHexString(false)
        );
        assertEquals(
                "07B13D",
                BigInteger.parse("2515453361")
                        .modPow(BigInteger.parse("a124f661"), BigInteger.parse("e472d"))
                        .toHexString(false)
        );
    }

    @Test
    void modInverse() {
        BigInteger b = BigInteger.parse("e5070");
        assertThrows(NullPointerException.class, () -> BigInteger.one.modInverse(null));
        assertThrows(ArithmeticException.class, () -> BigInteger.zero.modInverse(b));
        assertThrows(ArithmeticException.class, () -> b.modInverse(BigInteger.zero));
        assertThrows(ArithmeticException.class, () -> b.modInverse(b.negate()));

        assertEquals(0, BigInteger.parse("24a").modInverse(BigInteger.parse("8")).signum());
        assertEquals(0, BigInteger.parse("2c1fd5").modInverse(BigInteger.parse("c27ac")).signum());
        assertEquals(0, BigInteger.parse("12fd46").modInverse(BigInteger.parse("b")).compareTo(BigInteger.one));
        BigInteger a = BigInteger.parse("4b0a4322").modInverse(BigInteger.parse("bfc981"));
        assertEquals(1, a.signum());
        assertEquals("0x7336A1", a.toHexString(true));
        a = BigInteger.parse("5").negate().modInverse(BigInteger.parse("17"));
        assertEquals(1, a.signum());
        assertEquals("0x09", a.toHexString(true));
        a = BigInteger.parse("0x11").modInverse(BigInteger.parse("63"));
        assertEquals(1, a.signum());
        assertEquals("0x23", a.toHexString(true));
        a = BigInteger.parse("130a73").modInverse(BigInteger.parse("c6601"));
        assertEquals(1, a.signum());
        assertEquals("0x048575", a.toHexString(true));
        a = BigInteger.parse("1325b1").modInverse(BigInteger.parse("efbf1"));
        assertEquals(1, a.signum());
        assertEquals("0x058648", a.toHexString(true));
        a = BigInteger.parse("1f5").negate().modInverse(BigInteger.parse("13"));
        assertEquals(1, a.signum());
        assertEquals("0x08", a.toHexString(true));
        a = BigInteger.parse("600").modInverse(BigInteger.parse("2a5"));
        assertEquals(1, a.signum());
        assertEquals("0x5D", a.toHexString(true));
        a = BigInteger.parse("3172").modInverse(BigInteger.parse("479"));
        assertEquals(1, a.signum());
        assertEquals("0x02D7", a.toHexString(true));
        a = BigInteger.parse("4e7").modInverse(BigInteger.parse("2ba9"));
        assertEquals(1, a.signum());
        assertEquals("0x0412", a.toHexString(true));
        a = BigInteger.parse("218").modInverse(BigInteger.parse("2a5"));
        assertEquals(1, a.signum());
        assertEquals("0x18", a.toHexString(true));
        a = BigInteger.parse("e570").modInverse(BigInteger.parse("10d0d"));
        assertEquals(1, a.signum());
        assertEquals("0xA9B8", a.toHexString(true));
        a = BigInteger.valueOf(3).modInverse(BigInteger.valueOf(6));
        assertEquals(0, a.toInteger());
    }

    @Test
    void isProbablyPrime() {
        assertFalse(BigInteger.parse("988").negate().isProbablyPrime());
        prime(false, "0");
        prime(true, "2");
        prime(false, "4");
        prime(true, "5");
        prime(false, "16");
        prime(true, "17");
        prime(false, "19");
        prime(true, "71");
        prime(true, "ddb");
        prime(true, "754d");
        prime(true, "3ffeffff");
        prime(true, "cba9ef");
        prime(true, "7ffff");
        prime(false, "231");
        prime(false, "7cb3");
        prime(false, "61dab");
        prime(false, "293b034");
        prime(false, "1fa4a");
        prime(false, "12e8e7");
        prime(false, "2874fc3");
        prime(false, "3de1");
        prime(false, "ed73");
        prime(false, "22cf");
    }

    @Test
    void toInteger() {
        assertThrows(UnsupportedOperationException.class, () -> BigInteger.parse("aaaa22221").toInteger());

        assertEquals(0, BigInteger.zero.toInteger());
        assertEquals(4557, BigInteger.valueOf(4557).toInteger());
        assertEquals(1, BigInteger.one.toInteger());
        assertEquals(56666892, BigInteger.valueOf(56666892).toInteger());
        assertEquals(-4949, BigInteger.parse("1355").negate().toInteger());
    }

    @Test
    void toBigEndian() {
        assertArrayEquals(new byte[]{0b1001001, 0b110}, BigInteger.parse("100100100000110").toBigEndian());
        assertArrayEquals(new byte[]{0}, BigInteger.zero.toBigEndian());
        assertArrayEquals(new byte[]{1}, BigInteger.one.toBigEndian());
        assertArrayEquals(new byte[]{(byte) 0b11111111, 0b1001}, BigInteger.parse("1111111100001001").toBigEndian());
        assertArrayEquals(new byte[]{0b1011001, 0b01100100}, BigInteger.parse("101100101100100").toBigEndian());
        assertArrayEquals(new byte[]{1, 0, (byte) 0b10000000}, BigInteger.parse("10000000010000000").toBigEndian());
    }

    @Test
    void toLittleEndian() {
        assertArrayEquals(new byte[]{0b110, 0b1001001}, BigInteger.parse("100100100000110").toLittleEndian());
        assertArrayEquals(new byte[]{0}, BigInteger.zero.toLittleEndian());
        assertArrayEquals(new byte[]{1}, BigInteger.one.toLittleEndian());
        assertArrayEquals(new byte[]{0b1001, (byte) 0b11111111}, BigInteger.parse("1111111100001001").toLittleEndian());
        assertArrayEquals(new byte[]{0b01100100, 0b1011001}, BigInteger.parse("101100101100100").toLittleEndian());
        assertArrayEquals(new byte[]{(byte) 0b10000000, 0, 1}, BigInteger.parse("10000000010000000").toLittleEndian());
    }

    @Test
    void toVariableLength() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> BigInteger.parse("0x010000000000000000").toVariableLength()
        );

        assertArrayEquals(new byte[]{(byte) 0xFD, 0x03, 0x02}, BigInteger.valueOf(515).toVariableLength());
        assertArrayEquals(new byte[]{0}, BigInteger.zero.toVariableLength());
        assertArrayEquals(new byte[]{1}, BigInteger.one.toVariableLength());
        assertArrayEquals(new byte[]{(byte) 0xFC}, BigInteger.parse("0xFC").toVariableLength());
        assertArrayEquals(new byte[]{(byte) 0xFD, (byte) 0xFF, 0}, BigInteger.parse("0xFF").toVariableLength());
        assertArrayEquals(
                new byte[]{(byte) 0xFE, (byte) 0xD0, (byte) 0xDF, (byte) 0xFC, 0},
                BigInteger.parse("0xFCDFD0").negate().toVariableLength()
        );
        assertArrayEquals(
                new byte[]{(byte) 0xFF, 0, (byte) 0xD0, (byte) 0xDF, (byte) 0xFC, 0x01, 0, 0, 0},
                BigInteger.parse("0x01FCDFD000").toVariableLength()
        );
    }

    @Test
    void toBinaryString() {
        assertEquals("0", BigInteger.zero.toBinaryString(false));
        assertEquals("0b0", BigInteger.zero.toBinaryString(true));
        assertEquals("1", BigInteger.one.toBinaryString(false));
        assertEquals("0b10111001010001111000010011", BigInteger.parse("2E51E13").negate().toBinaryString(true));
        assertEquals("10010100010000000001111", BigInteger.parse("4A200F").toBinaryString(false));
    }

    @Test
    void toHexString() {
        assertEquals("00", BigInteger.zero.toHexString(false));
        assertEquals("0x00", BigInteger.zero.toHexString(true));
        assertEquals("0x01", BigInteger.one.toHexString(true));
        assertEquals("0x10", BigInteger.valueOf(16).toHexString(true));
        assertEquals("1234567890ABCDEF", BigInteger.parse("1234567890ABCDEF").negate().toHexString(false));
        assertEquals("0x0494944A200F", BigInteger.parse("494944A200F").toHexString(true));
    }

    private void prime(boolean isPrime, String hex) {
        BigInteger a = BigInteger.parse(hex);
        for (int i = 0; i < 100; i++) {
            assertEquals(isPrime, a.isProbablyPrime());
        }
    }
}