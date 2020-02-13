// cryptographic hash function
// https://keccak.team/keccak_specs_summary.html
// keccak-f[1344, 256], bitrate r = 1344, capacity c = 256
// width of the permutation, b = r + c = 1600
// bit length of the lane w = b / 25 = 64
package space.aqoleg.crypto;

import space.aqoleg.utils.Converter;

public class Shake128 {
    private static final long[] rc = { // rc[i], round constants
            0x0000000000000001L, 0x0000000000008082L, 0x800000000000808aL, 0x8000000080008000L, 0x000000000000808bL,
            0x0000000080000001L, 0x8000000080008081L, 0x8000000000008009L, 0x000000000000008aL, 0x0000000000000088L,
            0x0000000080008009L, 0x000000008000000aL, 0x000000008000808bL, 0x800000000000008bL, 0x8000000000008089L,
            0x8000000000008003L, 0x8000000000008002L, 0x8000000000000080L, 0x000000000000800aL, 0x800000008000000aL,
            0x8000000080008081L, 0x8000000000008080L, 0x0000000080000001L, 0x8000000080008008L
    };
    private static final int[][] r = { // r[x, y], rotation offsets
            {0, 36, 3, 41, 18},
            {1, 44, 10, 45, 2},
            {62, 6, 43, 15, 61},
            {28, 55, 25, 21, 56},
            {27, 20, 39, 8, 14}
    };

    /**
     * prints 32-bytes hash of the input string
     *
     * @param args Strings to be hashed
     */
    public static void main(String args[]) {
        StringBuilder input = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i != 0) {
                input.append(" ");
            }
            input.append(args[i]);
        }
        byte[] hash = getHash(32, input.toString().getBytes());
        System.out.println(Converter.bytesToHex(hash, false, false));
    }

    /**
     * @param hashByteLength length of the hash in bytes
     * @param message        array containing message to be hashed
     * @return hash with the specified length
     * @throws NullPointerException          if message == null
     * @throws UnsupportedOperationException if hashByteLength < 2 or hashByteLength > 8192
     */
    public static byte[] getHash(int hashByteLength, byte[] message) {
        return getHash(hashByteLength, message, 0, message.length);
    }

    /**
     * @param hashByteLength length of the hash in bytes
     * @param message        array containing message to be hashed
     * @param messageStart   starting position in the message array
     * @param messageLength  the number of the bytes containing message
     * @return hash with the specified length
     * @throws NullPointerException           if message == null
     * @throws ArrayIndexOutOfBoundsException if incorrect messageStart or messageLength
     * @throws UnsupportedOperationException  if hashByteLength < 2 or hashByteLength > 8192
     */
    public static byte[] getHash(int hashByteLength, byte[] message, int messageStart, int messageLength) {
        // check hashByteLength
        if (hashByteLength < 2) {
            throw new UnsupportedOperationException("too small length of the hash, require hashByteLength >= 2");
        }
        if (hashByteLength > 8192) {
            throw new UnsupportedOperationException("too big length of the hash, require hashByteLength <= 8192");
        }
        // initialize
        long[][] a = new long[5][5]; // A[x, y], permutation state array
        long[][] b = new long[5][5]; // B[x, y], intermediate variable
        long[] c = new long[5]; // C[x], intermediate variable
        long[] d = new long[5]; // D[x], intermediate variable
        long[] block = new long[21]; // 1344-bit block Pi, contain 21 words * 64 bits
        int blockPos = 0; // current position in the block
        int messagePos = messageStart; // current position in the message
        // process 64-bits words from message
        int stop = messageStart + messageLength - 7;
        while (messagePos < stop) {
            block[blockPos++] = getWord(message, messagePos);
            messagePos += 8;
            if (blockPos == 21) {
                blockPos = 0;
                hashBlock(block, a, b, c, d);
            }
        }
        // create buffer word and fill it with the rest of the message
        byte[] buffer = new byte[8];
        int bufferPos = 0; // current position in the buffer
        while (messagePos < messageStart + messageLength) {
            buffer[bufferPos++] = message[messagePos++];
        }
        // add d = 0x1F in the end of the message
        buffer[bufferPos] = (byte) 0x1F;
        // put buffer word in the block
        // last byte xor 0x80
        if (blockPos == 20) {
            buffer[7] |= 0x80;
        } else {
            block[blockPos++] = getWord(buffer, 0);
            while (blockPos < 20) {
                block[blockPos++] = 0L;
            }
            buffer = new byte[8];
            buffer[7] |= 0x80;
        }
        block[blockPos] = getWord(buffer, 0);
        hashBlock(block, a, b, c, d);
        // squeezing
        byte[] hash = new byte[hashByteLength];
        int hashPos = 0;
        while (true) {
            for (int y = 0; y < 5; y++) {
                for (int x = 0; x < 5; x++) {
                    if (x + 5 * y < 21) {
                        long word = a[x][y];
                        // little-endian
                        for (int i = 0; i < 8; i++) {
                            hash[hashPos++] = (byte) word;
                            if (hashPos == hashByteLength) {
                                return hash;
                            }
                            word >>= 8;
                        }
                    }
                }
            }
            f(a, b, c, d);
        }
    }

    // returns 64-bit word, little-endian
    private static long getWord(byte[] bytes, int bytesStart) {
        return (bytes[bytesStart] & 0xFF) |
                (bytes[++bytesStart] & 0xFF) << 8 |
                (bytes[++bytesStart] & 0xFF) << 16 |
                (long) (bytes[++bytesStart] & 0xFF) << 24 |
                (long) (bytes[++bytesStart] & 0xFF) << 32 |
                (long) (bytes[++bytesStart] & 0xFF) << 40 |
                (long) (bytes[++bytesStart] & 0xFF) << 48 |
                (long) bytes[++bytesStart] << 56;
    }

    private static void hashBlock(long[] block, long[][] a, long[][] b, long[] c, long[] d) {
        // absorbing
        int index;
        for (int y = 0; y < 5; y++) {
            for (int x = 0; x < 5; x++) {
                index = x + 5 * y;
                if (index == 21) {
                    break;
                }
                a[x][y] ^= block[index];
            }
        }
        f(a, b, c, d);
    }

    // keccak-f
    private static void f(long[][] a, long[][] b, long[] c, long[] d) {
        // the number of rounds n = 12 + 2 * log2(w)) = 24
        for (int i = 0; i < 24; i++) {
            // theta step
            for (int x = 0; x < 5; x++) {
                c[x] = a[x][0] ^ a[x][1] ^ a[x][2] ^ a[x][3] ^ a[x][4];
            }
            for (int x = 0; x < 5; x++) {
                d[x] = c[(x + 4) % 5] ^ rot(c[((x + 1) % 5)], 1);
            }
            for (int y = 0; y < 5; y++) {
                for (int x = 0; x < 5; x++) {
                    a[x][y] = a[x][y] ^ d[x];
                }
            }
            // rho and pi steps
            for (int y = 0; y < 5; y++) {
                for (int x = 0; x < 5; x++) {
                    b[y][(2 * x + 3 * y) % 5] = rot(a[x][y], r[x][y]);
                }
            }
            // chi step
            for (int y = 0; y < 5; y++) {
                for (int x = 0; x < 5; x++) {
                    a[x][y] = b[x][y] ^ (~(b[(x + 1) % 5][y]) & b[(x + 2) % 5][y]);
                }
            }
            // iota step
            a[0][0] = a[0][0] ^ rc[i];
        }
    }

    // cyclic shift x << s
    private static long rot(long x, int s) {
        return (x << s) | (x >>> (64 - s));
    }
}