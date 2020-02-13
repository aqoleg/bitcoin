// it is like java BigInteger, but with bit array
// objects are immutable
package space.aqoleg.utils;

import java.security.SecureRandom;
import java.util.Random;

public class BigInteger {
    public static final BigInteger zero;
    public static final BigInteger one;
    public static final BigInteger two;
    private final boolean[] bits; // little-endian
    private final boolean positive; // zero is positive
    private final int bitLength; // 0 if zero

    static {
        zero = new BigInteger(new boolean[]{false}, true);
        one = new BigInteger(new boolean[]{true}, true);
        two = new BigInteger(new boolean[]{false, true}, true);
    }

    private BigInteger(boolean[] bits, boolean positive) {
        this.bits = bits;
        int bitLength = bits.length;
        while (!bits[bitLength - 1]) { // find first non-zero bit
            if (--bitLength == 0) {
                break;
            }
        }
        this.bitLength = bitLength;
        this.positive = bitLength == 0 || positive; // positive zero
    }

    /**
     * @param value value of the BigInteger
     * @return a BigInteger with the specified value
     */
    public static BigInteger valueOf(int value) {
        if (value == 0) {
            return zero;
        }
        boolean positive = value > 0;
        if (!positive) {
            value = -value;
        }
        boolean[] bits = new boolean[31]; // 32 bit, first bit is zero for the positive integers
        int bitsPos = 0;
        while (value != 0) {
            bits[bitsPos++] = (value & 0b1) != 0;
            value >>>= 1;
        }
        return new BigInteger(bits, positive);
    }

    /**
     * @param bytes big-endian binary representation of the positive number
     * @return a positive BigInteger created from bytes
     * @throws NullPointerException if bytes == null
     */
    public static BigInteger createFromBigEndian(byte[] bytes) {
        return BigInteger.createFromBigEndian(bytes, 0, bytes.length);
    }

    /**
     * @param bytes       big-endian binary representation of the positive number
     * @param bytesStart  starting position in the bytes array
     * @param bytesLength the number of the bytes
     * @return a positive BigInteger created from bytes
     * @throws NullPointerException           if bytes == null
     * @throws ArrayIndexOutOfBoundsException if incorrect bytesStart or bytesLength
     */
    public static BigInteger createFromBigEndian(byte[] bytes, int bytesStart, int bytesLength) {
        if (bytesLength == 0) {
            return zero;
        }
        boolean[] bits = new boolean[bytesLength << 3];
        int bitsPos = 0;
        for (int bytesPos = bytesStart + bytesLength - 1; bytesPos >= bytesStart; bytesPos--) {
            int value = bytes[bytesPos];
            for (int i = 0; i < 8; i++) {
                bits[bitsPos++] = (value & 0b1) != 0;
                value >>>= 1;
            }
        }
        return new BigInteger(bits, true);
    }

    /**
     * @param bytes little-endian binary representation of the positive number
     * @return a positive BigInteger created from bytes
     * @throws NullPointerException if bytes == null
     */
    public static BigInteger createFromLittleEndian(byte[] bytes) {
        return BigInteger.createFromLittleEndian(bytes, 0, bytes.length);
    }

    /**
     * @param bytes       little-endian binary representation of the positive number
     * @param bytesStart  starting position in the bytes array
     * @param bytesLength the number of the bytes
     * @return a positive BigInteger created from bytes
     * @throws NullPointerException           if bytes == null
     * @throws ArrayIndexOutOfBoundsException if incorrect bytesStart or bytesLength
     */
    public static BigInteger createFromLittleEndian(byte[] bytes, int bytesStart, int bytesLength) {
        if (bytesLength == 0) {
            return zero;
        }
        boolean[] bits = new boolean[bytesLength << 3];
        int bitsPos = 0;
        int stop = bytesStart + bytesLength;
        for (int bytesPos = bytesStart; bytesPos < stop; bytesPos++) {
            int value = bytes[bytesPos];
            for (int i = 0; i < 8; i++) {
                bits[bitsPos++] = (value & 0b1) != 0;
                value >>>= 1;
            }
        }
        return new BigInteger(bits, true);
    }

    /**
     * @param bytes variable length representation of the positive number
     * @return a positive BigInteger created from bytes
     * @throws NullPointerException           if bytes == null
     * @throws ArrayIndexOutOfBoundsException if incorrect bytes.length
     */
    public static BigInteger createFromVariableLength(byte[] bytes) {
        return BigInteger.createFromVariableLength(bytes, 0);
    }

    /**
     * @param bytes      variable length representation of the positive number
     * @param bytesStart starting position in the bytes array
     * @return a positive BigInteger created from bytes
     * @throws NullPointerException           if bytes == null
     * @throws ArrayIndexOutOfBoundsException if incorrect bytesStart or bytes.length
     */
    public static BigInteger createFromVariableLength(byte[] bytes, int bytesStart) {
        if (bytes[bytesStart] == (byte) 0xFF) {
            return createFromLittleEndian(bytes, bytesStart + 1, 8);
        } else if (bytes[bytesStart] == (byte) 0xFE) {
            return createFromLittleEndian(bytes, bytesStart + 1, 4);
        } else if (bytes[bytesStart] == (byte) 0xFD) {
            return createFromLittleEndian(bytes, bytesStart + 1, 2);
        } else {
            return createFromLittleEndian(bytes, bytesStart, 1);
        }
    }

    /**
     * parses binary or hex String, can start with 0x, 0X, 0b, 0B
     *
     * @param string String representation of the number
     * @return a positive BigInteger created from string
     * @throws NullPointerException          if string == null
     * @throws UnsupportedOperationException if string is empty or contain not accepted symbols
     */
    public static BigInteger parse(String string) {
        if (string.length() > 2 && string.charAt(0) == '0') {
            char c = string.charAt(1);
            if (c == 'b' || c == 'B') {
                return parseBinaryString(string, 2);
            } else if (c == 'x' || c == 'X') {
                return parseHexString(string, 2);
            }
        }
        try {
            return parseBinaryString(string, 0);
        } catch (Exception e) {
            return parseHexString(string, 0);
        }
    }

    /**
     * @param minBitLength minimum bit length of the number
     * @param maxBitLength maximum bit length of the number
     * @return a positive random BigInteger, minBitLength >= bitLength >= maxBitLength
     * @throws UnsupportedOperationException if minBitLength < 2 or maxBitLength < minBitLength
     */
    public static BigInteger getRandom(int minBitLength, int maxBitLength) {
        if (minBitLength < 2) {
            throw new UnsupportedOperationException("require minBitLength > 1");
        }
        if (maxBitLength < minBitLength) {
            throw new UnsupportedOperationException("require maxBitLength >= minBitLength");
        }
        Random random = new SecureRandom();
        boolean[] bits = new boolean[maxBitLength];
        for (int i = minBitLength - 2; i >= 0; i--) {
            bits[i] = random.nextBoolean();
        }
        if (minBitLength == maxBitLength) {
            bits[minBitLength - 1] = true;
        } else {
            // at least one 1 bit
            boolean bit = false;
            do {
                for (int i = minBitLength - 1; i < maxBitLength; i++) {
                    if (random.nextBoolean()) {
                        bits[i] = true;
                        bit = true;
                    } else {
                        bits[i] = false;
                    }
                }
            } while (!bit);
        }
        return new BigInteger(bits, true);
    }

    /**
     * @param minBitLength minimum bit length of the number
     * @param maxBitLength maximum bit length of the number
     * @return a positive random prime BigInteger, minBitLength >= bitLength >= maxBitLength
     * @throws UnsupportedOperationException if minBitLength < 2 or maxBitLength < minBitLength
     */
    public static BigInteger getRandomPrime(int minBitLength, int maxBitLength) {
        BigInteger random;
        do {
            random = getRandom(minBitLength, maxBitLength);
        } while (!random.isProbablyPrime());
        return random;
    }

    /**
     * @param position little-endian position of the bit to test
     * @return true if bit at position is true
     * @throws IndexOutOfBoundsException if incorrect position
     */
    public boolean testBit(int position) {
        return bits[position];
    }

    /**
     * @return bit length of this BigInteger, 0 for zero
     */
    public int bitLength() {
        return bitLength;
    }

    /**
     * @return -1 if this < 0, 0 if this == 0, 1 if this > 0
     */
    public int signum() {
        if (bitLength == 0) {
            return 0;
        } else if (positive) {
            return 1;
        } else {
            return -1;
        }
    }

    /**
     * @param a BigInteger to which this BigInteger to be compared
     * @return -1 if this < a, 0 if this == a, 1 if this > a
     * @throws NullPointerException if a == null
     */
    public int compareTo(BigInteger a) {
        // 0 == 0
        if (bitLength == 0 && a.bitLength == 0) {
            return 0;
        }
        // positive > negative
        if (positive != a.positive) {
            return positive ? 1 : -1;
        }
        return positive ? absCompareTo(a) : -absCompareTo(a);
    }

    /**
     * @param n shift distance, can be negative
     * @return this << n
     */
    public BigInteger shiftLeft(int n) {
        if (n < 0) {
            return shiftRight(-n);
        }
        boolean[] shiftedBits = new boolean[bitLength + n];
        System.arraycopy(bits, 0, shiftedBits, n, bitLength);
        return new BigInteger(shiftedBits, positive);
    }

    /**
     * @param n shift distance, can be negative
     * @return this >> n
     */
    public BigInteger shiftRight(int n) {
        if (n < 0) {
            return shiftLeft(-n);
        }
        if (n >= bitLength) {
            return zero;
        }
        boolean[] shiftedBits = new boolean[bitLength - n];
        System.arraycopy(bits, n, shiftedBits, 0, bitLength - n);
        return new BigInteger(shiftedBits, positive);
    }

    /**
     * @return a BigInteger whose value equals -this
     */
    public BigInteger negate() {
        if (bitLength == 0) {
            return zero;
        }
        boolean[] copy = new boolean[bitLength];
        System.arraycopy(bits, 0, copy, 0, bitLength);
        return new BigInteger(copy, !positive);
    }

    /**
     * @param a BigInteger to be added to this BigInteger
     * @return this + a
     * @throws NullPointerException if a == null
     */
    public BigInteger add(BigInteger a) {
        if (positive == a.positive) {
            if (absCompareTo(a) == 1) {
                return add(this, a, positive);
            } else {
                return add(a, this, positive);
            }
        } else {
            // this + (-a) = this - a
            // -this + a = -(this - a)
            int signum = absCompareTo(a);
            if (signum == 1) {
                return subtract(this, a, positive);
            } else if (signum == -1) {
                return subtract(a, this, a.positive);
            } else {
                return zero;
            }
        }
    }

    /**
     * @param a BigInteger to be subtracted from this BigInteger
     * @return this - a
     * @throws NullPointerException if a == null
     */
    public BigInteger subtract(BigInteger a) {
        if (positive == a.positive) {
            int signum = absCompareTo(a);
            if (signum == 1) {
                return subtract(this, a, positive);
            } else if (signum == -1) {
                return subtract(a, this, !a.positive); // this - a = -(a - this)
            } else {
                return zero;
            }
        } else {
            // this - (-a) = this + a
            if (absCompareTo(a) == 1) {
                return add(this, a, positive);
            } else {
                return add(a, this, positive);
            }
        }
    }

    /**
     * @param a BigInteger to be multiplied by this BigInteger
     * @return this * a
     * @throws NullPointerException if a == null
     */
    public BigInteger multiply(BigInteger a) {
        if (bitLength == 0 || a.bitLength == 0) {
            return zero;
        }
        // this            1 0 1 0 1
        // * a                 1 0 1
        // = result  0 0 0 0 0 0 0 0
        // +=                  1 0 1   i = 0
        // +=              1 0 1       i = 2
        // +=          1 0 1           i = 4
        // =         0 1 1 0 1 0 0 1
        boolean[] result = new boolean[bitLength + a.bitLength];
        for (int i = 0; i < bitLength; i++) {
            if (bits[i]) {
                multiply(result, i, a);
            }
        }
        return new BigInteger(result, positive == a.positive);
    }

    /**
     * @param a BigInteger by which this BigInteger is to be divided
     * @return [quotient = this / a, remainder = this % a]
     * @throws NullPointerException if a == null
     * @throws ArithmeticException  if a == 0
     */
    public BigInteger[] divideAndRemainder(BigInteger a) {
        if (a.bitLength == 0) {
            throw new ArithmeticException("divide by zero");
        }
        // this / a     1 0 1 1 0 1 | 1 0 1 0   dividend 0 1 0 1 1 0 1 quotient 0 0 0
        // -            1 0 1 0       1         i = 2                  quotient 1 0 0
        // =                  1 0 1             dividend 0 0 0 0 1 0 1
        // -              1 0 1 0       0       i = 1                  quotient 1 0 0
        // -                1 0 1 0       0     i = 0                  quotient 1 0 0
        BigInteger q, r;
        boolean[] dividend = new boolean[bitLength + 1];
        System.arraycopy(bits, 0, dividend, 0, bitLength);
        if (absCompareTo(a) == -1) {
            q = zero;
        } else {
            boolean[] quotient = new boolean[bitLength - a.bitLength + 1];
            for (int i = bitLength - a.bitLength; i >= 0; i--) {
                quotient[i] = divide(dividend, i, a);
            }
            q = new BigInteger(quotient, positive == a.positive);
        }
        if (a.positive) {
            if (positive) {
                r = new BigInteger(dividend, true);
            } else {
                // -this % a = r
                // this % a = -r % a = a - r
                r = subtract(a, new BigInteger(dividend, true), true);
            }
        } else {
            if (positive) {
                // this % -a = r
                // this % a = -(-a - r) = -(|a| - r)
                r = subtract(a, new BigInteger(dividend, true), false);
            } else {
                // -this % -a = r
                // -this % a = -(-a - r) = a + r
                // this % a = a - (a + r) = -r
                r = new BigInteger(dividend, false);
            }
        }
        return new BigInteger[]{q, r};
    }

    /**
     * @param a BigInteger by which this BigInteger is to be divided
     * @return quotient = this / a
     * @throws NullPointerException if a == null
     * @throws ArithmeticException  if a == 0
     */
    public BigInteger divide(BigInteger a) {
        if (a.bitLength == 0) {
            throw new ArithmeticException("divide by zero");
        }
        if (absCompareTo(a) == -1) {
            return zero;
        }
        boolean[] dividend = new boolean[bitLength + 1];
        System.arraycopy(bits, 0, dividend, 0, bitLength);
        boolean[] quotient = new boolean[bitLength - a.bitLength + 1];
        for (int i = bitLength - a.bitLength; i >= 0; i--) {
            quotient[i] = divide(dividend, i, a);
        }
        return new BigInteger(quotient, positive == a.positive);
    }

    /**
     * @param a BigInteger by which this BigInteger is to be divided
     * @return remainder = this % a
     * @throws NullPointerException if a == null
     * @throws ArithmeticException  if a == 0
     */
    public BigInteger remainder(BigInteger a) {
        if (a.bitLength == 0) {
            throw new ArithmeticException("divide by zero");
        }
        boolean[] dividend = new boolean[bitLength + 1];
        System.arraycopy(bits, 0, dividend, 0, bitLength);
        if (absCompareTo(a) != -1) {
            for (int i = bitLength - a.bitLength; i >= 0; i--) {
                divide(dividend, i, a);
            }
        }
        if (a.positive) {
            if (positive) {
                return new BigInteger(dividend, true);
            } else {
                return subtract(a, new BigInteger(dividend, true), true);
            }
        } else {
            if (positive) {
                return subtract(a, new BigInteger(dividend, true), false);
            } else {
                return new BigInteger(dividend, false);
            }
        }
    }

    /**
     * @return the greatest BigInteger less than or equal to the square root of this
     * @throws ArithmeticException if this < 0
     */
    public BigInteger isqrt() {
        if (!positive) {
            throw new ArithmeticException("not positive, require this > 0");
        }
        // find the greatest square, square = 4 ^ n, square <= this
        int shift = bitLength;
        if (shift % 2 == 1) {
            shift--;
        }
        BigInteger square = one.shiftLeft(shift);
        BigInteger reminder = this;
        BigInteger result = zero;
        while (square.signum() != 0) {
            BigInteger sum = square.add(result);
            result = result.shiftRight(1);
            if (reminder.compareTo(sum) >= 0) {
                reminder = reminder.subtract(sum);
                result = result.add(square);
            }
            square = square.shiftRight(2);
        }
        return result;
    }

    /**
     * @param a BigInteger with which the least common multiple is to be computed
     * @return least common multiple of this and a, the smallest positive number, lcm % this = 0, lcm % a = 0
     * @throws NullPointerException if a == null
     */
    public BigInteger lcm(BigInteger a) {
        if (bitLength == 0 || a.bitLength == 0) {
            return zero;
        }
        if (!positive) {
            return negate().lcm(a);
        }
        if (!a.positive) {
            return lcm(a.negate());
        }
        // lcm(a, b) = a * b / gcd(a, b)
        BigInteger multiple = multiply(a);
        BigInteger b = this;
        do {
            // gcd(a, b) = gcd(b, a % b)
            if (a.absCompareTo(b) == 1) {
                a = a.remainder(b);
            } else {
                b = b.remainder(a);
            }
            // gcd(a, 0) = a
            if (a.bitLength == 0) {
                return multiple.divide(b);
            } else if (b.bitLength == 0) {
                return multiple.divide(a);
            }
        } while (true);
    }

    /**
     * @param exponent exponent
     * @param m        modulus
     * @return a BigInteger = (this**exponent) % m
     * @throws NullPointerException if exponent == null or m == null
     * @throws ArithmeticException  if this < 0 or exponent <= 0 or m <= 0
     */
    public BigInteger modPow(BigInteger exponent, BigInteger m) {
        if (!positive) {
            throw new ArithmeticException("negative, require this >= 0");
        }
        if (exponent.bitLength == 0 || !exponent.positive) {
            throw new ArithmeticException("non-positive exponent, require exponent > 0");
        }
        if (m.bitLength == 0 || !m.positive) {
            throw new ArithmeticException("non-positive m, require m > 0");
        }
        // (a**(b1 + b2)) % c = ( ((a**b1) % c) * ((a**b2) % c) ) % c
        // (a**2) % c = ((a % c)**2) % c
        BigInteger result = one;
        BigInteger twice = remainder(m);
        int bitsPos = 0;
        do {
            if (exponent.bits[bitsPos]) {
                // result = result * twice
                result = result.multiply(twice).remainder(m);
            }
            // twice = twice**2
            twice = twice.multiply(twice).remainder(m);
            bitsPos++;
        } while (bitsPos != exponent.bitLength);
        return result;
    }

    /**
     * @param m the modulus
     * @return positive number, modular multiplicative inverse (this**(-1)) % m, or 0 if there is no solution
     * @throws NullPointerException if m == null
     * @throws ArithmeticException  if this == 0 or m <= 0
     */
    public BigInteger modInverse(BigInteger m) {
        if (bitLength == 0) {
            throw new ArithmeticException("zero, require this != 0");
        }
        if (m.bitLength == 0 || !m.positive) {
            throw new ArithmeticException("non-positive m, require m > 0");
        }
        // x = (this**(-1)) % m
        // (this * x) % m = 1
        // 1 = m * y + this * x
        // extended Euclidean algorithm gcd(a, b) = r = a * s + b * t
        // r0 = a, r1 = b, s0 = 1, s1 = 0, t0 = 0, t1 = 1
        BigInteger r0 = m;
        BigInteger s0 = one;
        BigInteger t0 = zero;
        BigInteger r1 = this;
        BigInteger s1 = zero;
        BigInteger t1 = one;
        BigInteger q, r, s, t;
        // qI = rI-1 % rI
        // rI+1 = rI-1 - qI * rI
        // sI+1 = sI-1 - qI * sI
        // tI+1 = tI-1 - qI * tI
        do {
            q = r0.divide(r1);
            r = r0.subtract(q.multiply(r1));
            r0 = r1;
            r1 = r;
            s = s0.subtract(q.multiply(s1));
            s0 = s1;
            s1 = s;
            t = t0.subtract(q.multiply(t1));
            t0 = t1;
            t1 = t;
            if (r.compareTo(one) == 0) {
                if (t.signum() < 0) {
                    return t.add(m);
                }
                return t;
            }
        } while (r.bitLength != 0);
        return zero;
    }

    /**
     * @return true if this BigInteger is probably prime
     */
    public boolean isProbablyPrime() {
        if (!positive) {
            return false;
        } else if (bitLength < 5) {
            switch (toBinaryString(false)) {
                case ("10"):
                case ("11"):
                case ("101"):
                case ("1011"):
                case ("1101"):
                    return true;
                default:
                    return false;
            }
        }
        // fermat primality test
        // 3 < a < (p - 1)
        // if p is prime, then (a**(p - 1)) % p = 1
        BigInteger pow = subtract(one);
        for (int i = 0; i < 40; i++) {
            BigInteger a = getRandom(3, bitLength - 1);
            if (a.modPow(pow, this).compareTo(one) != 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return integer value of this BigInteger
     * @throws UnsupportedOperationException if this BigInteger is too big
     */
    public int toInteger() {
        if (bitLength > 31) {
            throw new UnsupportedOperationException("contain " + bitLength + " bits, require less then 32");
        }
        int n = 0;
        for (int i = 0; i < bitLength; i++) {
            if (bits[i]) {
                n |= 1 << i;
            }
        }
        return positive ? n : -n;
    }

    /**
     * @return a byte array containing big-endian binary representation of |this|
     */
    public byte[] toBigEndian() {
        if (bitLength == 0) {
            return new byte[]{0};
        }
        int byteLength = bitLength >> 3;
        if ((bitLength & 0b111) != 0) {
            byteLength++;
        }
        byte[] out = new byte[byteLength];
        int bitsPos = 0;
        for (int i = byteLength - 1; i >= 0; i--) {
            int aByte = 0;
            int shift = 0;
            while (shift < 8 && bitsPos < bitLength) {
                if (bits[bitsPos++]) {
                    aByte += 1 << shift;
                }
                shift++;
            }
            out[i] = (byte) aByte;
        }
        return out;
    }

    /**
     * @return a byte array containing little-endian binary representation of |this|
     */
    public byte[] toLittleEndian() {
        if (bitLength == 0) {
            return new byte[]{0};
        }
        int byteLength = bitLength >> 3;
        if ((bitLength & 0b111) != 0) {
            byteLength++;
        }
        byte[] out = new byte[byteLength];
        int bitsPos = 0;
        for (int i = 0; i < byteLength; i++) {
            int aByte = 0;
            int shift = 0;
            while (shift < 8 && bitsPos < bitLength) {
                if (bits[bitsPos++]) {
                    aByte += 1 << shift;
                }
                shift++;
            }
            out[i] = (byte) aByte;
        }
        return out;
    }

    /**
     * @return a byte array containing variable length representation of |this|
     * @throws UnsupportedOperationException if this BigInteger is too big
     */
    public byte[] toVariableLength() {
        byte[] out;
        if (absCompareTo(BigInteger.parse("0xFD")) == -1) {
            return toLittleEndian();
        } else if (bitLength <= 16) {
            out = new byte[3];
            out[0] = (byte) 0xFD;
        } else if (bitLength <= 32) {
            out = new byte[5];
            out[0] = (byte) 0xFE;
        } else if (bitLength <= 64) {
            out = new byte[9];
            out[0] = (byte) 0xFF;
        } else {
            throw new UnsupportedOperationException("contain " + bitLength + " bits, require less then 65");
        }
        byte[] number = toLittleEndian();
        System.arraycopy(number, 0, out, 1, number.length);
        return out;
    }

    /**
     * @param addPrefix if true starts with "0b"
     * @return binary string of |this|
     */
    public String toBinaryString(boolean addPrefix) {
        if (bitLength == 0) {
            return addPrefix ? "0b0" : "0";
        }
        StringBuilder builder = new StringBuilder();
        if (addPrefix) {
            builder.append("0b");
        }
        for (int i = bitLength - 1; i >= 0; i--) {
            if (bits[i]) {
                builder.append('1');
            } else {
                builder.append('0');
            }
        }
        return builder.toString();
    }

    /**
     * @param addPrefix if true starts with "0x"
     * @return hex string of |this|
     */
    public String toHexString(boolean addPrefix) {
        if (bitLength == 0) {
            return addPrefix ? "0x00" : "00";
        }
        StringBuilder builder = new StringBuilder();
        int bitsPos = 0;
        while (bitsPos < bitLength) {
            int hex = 0;
            int shift = 0;
            while (shift < 4 && bitsPos < bitLength) {
                if (bits[bitsPos]) {
                    hex += 1 << shift;
                }
                bitsPos++;
                shift++;
            }
            switch (hex) {
                case (10):
                    builder.append('A');
                    break;
                case (11):
                    builder.append('B');
                    break;
                case (12):
                    builder.append('C');
                    break;
                case (13):
                    builder.append('D');
                    break;
                case (14):
                    builder.append('E');
                    break;
                case (15):
                    builder.append('F');
                    break;
                default:
                    builder.append(hex);
            }
        }
        if (builder.length() % 2 != 0) {
            builder.append("0");
        }
        if (addPrefix) {
            builder.append("x0");
        }
        return builder.reverse().toString();
    }

    // parses binary string
    // throws exception if can not parse
    private static BigInteger parseBinaryString(String string, int trim) {
        if (string.length() - trim < 1) {
            throw new UnsupportedOperationException("empty string");
        }
        boolean[] bits = new boolean[string.length() - trim];
        int bitsPos = 0;
        for (int i = string.length() - 1; i >= trim; i--) {
            switch (string.charAt(i)) {
                case ('0'):
                    bits[bitsPos++] = false;
                    break;
                case ('1'):
                    bits[bitsPos++] = true;
                    break;
                default:
                    throw new UnsupportedOperationException("symbol '" + string.charAt(i) + "' is not accepted");
            }
        }
        return new BigInteger(bits, true);
    }

    // parses hex string
    // throws exception if can not parse
    private static BigInteger parseHexString(String string, int trim) {
        if (string.length() - trim < 1) {
            throw new UnsupportedOperationException("empty string");
        }
        boolean[] bits = new boolean[(string.length() - trim) << 4];
        int bitsPos = 0;
        for (int i = string.length() - 1; i >= trim; i--) {
            byte hex;
            switch (string.charAt(i)) {
                case ('0'):
                    hex = 0;
                    break;
                case ('1'):
                    hex = 1;
                    break;
                case ('2'):
                    hex = 2;
                    break;
                case ('3'):
                    hex = 3;
                    break;
                case ('4'):
                    hex = 4;
                    break;
                case ('5'):
                    hex = 5;
                    break;
                case ('6'):
                    hex = 6;
                    break;
                case ('7'):
                    hex = 7;
                    break;
                case ('8'):
                    hex = 8;
                    break;
                case ('9'):
                    hex = 9;
                    break;
                case ('a'):
                case ('A'):
                    hex = 10;
                    break;
                case ('b'):
                case ('B'):
                    hex = 11;
                    break;
                case ('c'):
                case ('C'):
                    hex = 12;
                    break;
                case ('d'):
                case ('D'):
                    hex = 13;
                    break;
                case ('e'):
                case ('E'):
                    hex = 14;
                    break;
                case ('f'):
                case ('F'):
                    hex = 15;
                    break;
                default:
                    throw new UnsupportedOperationException("symbol '" + string.charAt(i) + "' is not accepted");
            }
            bits[bitsPos++] = (hex & 0b1) != 0;
            bits[bitsPos++] = (hex & 0b10) != 0;
            bits[bitsPos++] = (hex & 0b100) != 0;
            bits[bitsPos++] = (hex & 0b1000) != 0;
        }
        return new BigInteger(bits, true);
    }

    // returns -1 if |this| < |a|, 0 if |this| == |a|, 1 if |this| > |a|
    private int absCompareTo(BigInteger a) {
        if (bitLength > a.bitLength) {
            return 1;
        } else if (bitLength < a.bitLength) {
            return -1;
        }
        for (int i = bitLength - 1; i >= 0; i--) {
            if (bits[i]) {
                if (!a.bits[i]) {
                    return 1;
                }
            } else {
                if (a.bits[i]) {
                    return -1;
                }
            }
        }
        return 0;
    }

    // returns |a| + |b| or -(|a| + |b|) if !positive
    // requires |a| > |b|
    private BigInteger add(BigInteger a, BigInteger b, boolean positive) {
        // nextBit         1
        // a         1 0 1 0 1
        // + b         1 0 0 1
        // = result          0
        boolean nextBit = false;
        boolean[] result = new boolean[a.bitLength + 1];
        boolean bit;
        for (int i = 0; i < a.bitLength; i++) {
            // bit = nextBit + a[i]
            bit = a.bits[i];
            if (nextBit) {
                if (bit) {
                    // 1 + 1 = 10, next bit 1, bit 1 -> 0
                    bit = false;
                } else {
                    // 1 + 0 = 1, next bit 1 -> 0, bit 0 -> 1
                    nextBit = false;
                    bit = true;
                }
            }
            // bit += b[i]
            if (i < b.bitLength && b.bits[i]) {
                if (bit) {
                    // 1 + 1 = 10, next bit 0 -> 1, bit 0 -> 1
                    nextBit = true;
                    bit = false;
                } else {
                    // 0 + 1 = 1, bit 0 -> 1
                    bit = true;
                }
            }
            result[i] = bit;
        }
        if (nextBit) {
            result[a.bitLength] = true;
        }
        return new BigInteger(result, positive);
    }

    // returns |a| - |b| or -(|a| - |b|) if !positive
    // requires |a| > |b|
    private BigInteger subtract(BigInteger a, BigInteger b, boolean positive) {
        // nextBit        -1
        // a         1 0 1 0 0
        // - b         1 0 0 1
        // = result          1
        boolean nextBit = false; // true for -1
        boolean[] result = new boolean[a.bitLength];
        boolean bit;
        for (int i = 0; i < a.bitLength; i++) {
            // bit = nextBit + a[i]
            bit = a.bits[i];
            if (nextBit) {
                if (bit) {
                    // -1 + 1 = 0, next bit -1 -> 0, bit 1 -> 0
                    nextBit = false;
                    bit = false;
                } else {
                    // -1 + 0 = -10 + 1, next bit -1, bit 0 -> 1
                    bit = true;
                }
            }
            // bit -= b[i]
            if (i < b.bitLength && b.bits[i]) {
                if (bit) {
                    // 1 - 1 = 0, bit 1 -> 0
                    bit = false;
                } else {
                    // 0 - 1 = -10 + 1, next bit 0 -> -1, bit 0 -> 1
                    nextBit = true;
                    bit = true;
                }
            }
            result[i] = bit;
        }
        return new BigInteger(result, positive);
    }

    // returns result += (multiplier << start)
    // requires result.length > (multiplier.bitLength + start)
    private void multiply(boolean[] result, int start, BigInteger multiplier) {
        // 10101 * 101
        // nextBit                          1
        // result                   0 0 0 0 0 1 0 1
        // + multiplier << start          1 0 1
        // = result                 0 0 0 0 0 0 0 1
        boolean nextBit = false;
        boolean bit;
        for (int i = 0; i <= multiplier.bitLength; i++) {
            // bit = nextBit + result[start + i]
            bit = result[start + i];
            if (nextBit) {
                if (bit) {
                    // 1 + 1 = 10, next bit 1, bit 1 -> 0
                    bit = false;
                } else {
                    // 1 + 0 = 1, next bit 1 -> 0, bit 0 -> 1
                    nextBit = false;
                    bit = true;
                }
            }
            // bit += multiplier[i]
            if (i != multiplier.bitLength && multiplier.bits[i]) {
                if (bit) {
                    // 1 + 1 = 10, next bit 0 -> 1, bit 0 -> 1
                    nextBit = true;
                    bit = false;
                } else {
                    // 0 + 1 = 1, bit 0 -> 1
                    bit = true;
                }
            }
            result[start + i] = bit;
        }
    }

    // if dividend < (divisor << start) returns false
    // else dividend -= (divisor << start), returns true
    // requires dividend.length > (divisor.bitLength + start)
    private boolean divide(boolean[] dividend, int start, BigInteger divisor) {
        // compare dividend and divisor << start
        if (!dividend[start + divisor.bitLength]) {
            for (int i = divisor.bitLength - 1; i >= 0; i--) {
                if (dividend[start + i]) {
                    if (!divisor.bits[i]) {
                        break;
                    }
                } else {
                    if (divisor.bits[i]) {
                        return false;
                    }
                }
            }
        }
        // 101101 / 1010
        // nextBit                    0
        // dividend             0 1 0 1 1 0 1
        // - divisor << start     1 0 1 0
        // = dividend           0 1 0 1 1 0 1
        boolean nextBit = false;
        for (int i = 0; i <= divisor.bitLength; i++) {
            // bit = nextBit + dividend[start + i]
            boolean bit = dividend[start + i];
            if (nextBit) {
                if (bit) {
                    // -1 + 1 = 0, next bit 1 -> 0, bit 1 -> 0
                    nextBit = false;
                    bit = false;
                } else {
                    // -1 + 0 = -10 + 1, next bit 1, bit 0 -> 1
                    bit = true;
                }
            }
            // bit -= divisor[i]
            if (i != divisor.bitLength && divisor.bits[i]) {
                if (bit) {
                    // 1 - 1 = 0, bit 1 -> 0
                    bit = false;
                } else {
                    // 0 - 1 = -10 + 1, next bit 0 -> 1, bit 0 -> 1
                    nextBit = true;
                    bit = true;
                }
            }
            dividend[start + i] = bit;
        }
        return true;
    }
}