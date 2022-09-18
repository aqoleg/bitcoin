/*
cryptographic hash function

usage:
    byte[] hash = Sha512.getHash(bytes);
    byte[] hash = Sha512.getHash(bytes, start, length);
*/

package com.aqoleg.crypto;

public class Sha512 {
    private static final long k[] = {
            0x428a2f98d728ae22L, 0x7137449123ef65cdL, 0xb5c0fbcfec4d3b2fL, 0xe9b5dba58189dbbcL, 0x3956c25bf348b538L,
            0x59f111f1b605d019L, 0x923f82a4af194f9bL, 0xab1c5ed5da6d8118L, 0xd807aa98a3030242L, 0x12835b0145706fbeL,
            0x243185be4ee4b28cL, 0x550c7dc3d5ffb4e2L, 0x72be5d74f27b896fL, 0x80deb1fe3b1696b1L, 0x9bdc06a725c71235L,
            0xc19bf174cf692694L, 0xe49b69c19ef14ad2L, 0xefbe4786384f25e3L, 0x0fc19dc68b8cd5b5L, 0x240ca1cc77ac9c65L,
            0x2de92c6f592b0275L, 0x4a7484aa6ea6e483L, 0x5cb0a9dcbd41fbd4L, 0x76f988da831153b5L, 0x983e5152ee66dfabL,
            0xa831c66d2db43210L, 0xb00327c898fb213fL, 0xbf597fc7beef0ee4L, 0xc6e00bf33da88fc2L, 0xd5a79147930aa725L,
            0x06ca6351e003826fL, 0x142929670a0e6e70L, 0x27b70a8546d22ffcL, 0x2e1b21385c26c926L, 0x4d2c6dfc5ac42aedL,
            0x53380d139d95b3dfL, 0x650a73548baf63deL, 0x766a0abb3c77b2a8L, 0x81c2c92e47edaee6L, 0x92722c851482353bL,
            0xa2bfe8a14cf10364L, 0xa81a664bbc423001L, 0xc24b8b70d0f89791L, 0xc76c51a30654be30L, 0xd192e819d6ef5218L,
            0xd69906245565a910L, 0xf40e35855771202aL, 0x106aa07032bbd1b8L, 0x19a4c116b8d2d0c8L, 0x1e376c085141ab53L,
            0x2748774cdf8eeb99L, 0x34b0bcb5e19b48a8L, 0x391c0cb3c5c95a63L, 0x4ed8aa4ae3418acbL, 0x5b9cca4f7763e373L,
            0x682e6ff3d6b2b8a3L, 0x748f82ee5defb2fcL, 0x78a5636f43172f60L, 0x84c87814a1f0ab72L, 0x8cc702081a6439ecL,
            0x90befffa23631e28L, 0xa4506cebde82bde9L, 0xbef9a3f7b2c67915L, 0xc67178f2e372532bL, 0xca273eceea26619cL,
            0xd186b8c721c0c207L, 0xeada7dd6cde0eb1eL, 0xf57d4f7fee6ed178L, 0x06f067aa72176fbaL, 0x0a637dc5a2c898a6L,
            0x113f9804bef90daeL, 0x1b710b35131c471bL, 0x28db77f523047d84L, 0x32caab7b40c72493L, 0x3c9ebe0a15c9bebcL,
            0x431d67c49c100d4cL, 0x4cc5d4becb3e42b6L, 0x597f299cfc657e2aL, 0x5fcb6fab3ad6faecL, 0x6c44198c4a475817L
    };

    /**
     * @param message array containing message to be hashed
     * @return 64-bytes hash
     * @throws NullPointerException if message == null
     */
    public static byte[] getHash(byte[] message) {
        return getHash(message, 0, message.length);
    }

    /**
     * @param message       array containing message to be hashed
     * @param messageStart  starting position in the message array
     * @param messageLength the number of bytes containing the message
     * @return 64-bytes hash
     * @throws NullPointerException           if message == null
     * @throws ArrayIndexOutOfBoundsException if messageStart or messageLength is incorrect
     */
    public static byte[] getHash(byte[] message, int messageStart, int messageLength) {
        long[] hash = {0x6a09e667f3bcc908L, 0xbb67ae8584caa73bL, 0x3c6ef372fe94f82bL, 0xa54ff53a5f1d36f1L,
                0x510e527fade682d1L, 0x9b05688c2b3e6c1fL, 0x1f83d9abfb41bd6bL, 0x5be0cd19137e2179L}; // 512-bit hash
        long[] block = new long[80]; // 1024-bit block, contains 16 words * 64 bits and extended 64 words
        int blockPos = 0; // current position in the block
        int messagePos = messageStart; // current position in the message
        // process 64-bits words from the message
        int stop = messageStart + messageLength - 7;
        while (messagePos < stop) {
            block[blockPos++] = getWord(message, messagePos);
            messagePos += 8;
            if (blockPos == 16) {
                blockPos = 0;
                hashBlock(block, hash);
            }
        }
        // create the buffer word and fill it with the rest of the message
        byte[] buffer = new byte[8];
        int bufferPos = 0; // current position in the buffer
        while (messagePos < messageStart + messageLength) {
            buffer[bufferPos++] = message[messagePos++];
        }
        // add 0b10000000 in the end of the message
        buffer[bufferPos] = (byte) 0b10000000;
        // put buffer word in the block
        block[blockPos++] = getWord(buffer, 0);
        if (blockPos == 16) {
            blockPos = 0;
            hashBlock(block, hash);
        }
        // 2 last words in the block is for the length of the message
        // if they are not empty, process this block and create new one
        if (blockPos == 15) {
            block[15] = 0;
            blockPos = 0;
            hashBlock(block, hash);
        }
        for (int i = blockPos; i < 14; i++) {
            block[i] = 0;
        }
        // put length in the end of the last block, big-endian
        long bitLength = messageLength << 3;
        block[14] = 0; // this is for the bitLength > 64bit, it is impossible
        block[15] = bitLength;
        hashBlock(block, hash);
        // return hash, 64-bit words are big-endian
        byte[] out = new byte[64];
        int outPos = 0;
        for (long word : hash) {
            out[outPos++] = (byte) (word >>> 56);
            out[outPos++] = (byte) (word >>> 48);
            out[outPos++] = (byte) (word >>> 40);
            out[outPos++] = (byte) (word >>> 32);
            out[outPos++] = (byte) (word >>> 24);
            out[outPos++] = (byte) (word >>> 16);
            out[outPos++] = (byte) (word >>> 8);
            out[outPos++] = (byte) word;
        }
        return out;
    }

    // returns 64-bit word, big-endian
    private static long getWord(byte[] bytes, int bytesStart) {
        return (long) bytes[bytesStart] << 56 |
                (long) (bytes[++bytesStart] & 0xFF) << 48 |
                (long) (bytes[++bytesStart] & 0xFF) << 40 |
                (long) (bytes[++bytesStart] & 0xFF) << 32 |
                (long) (bytes[++bytesStart] & 0xFF) << 24 |
                (bytes[++bytesStart] & 0xFF) << 16 |
                (bytes[++bytesStart] & 0xFF) << 8 |
                (bytes[++bytesStart] & 0xFF);
    }

    private static void hashBlock(long[] block, long[] hash) {
        // extends the first 16 words into the remaining 64 words
        for (int i = 16; i < 80; i++) {
            block[i] = block[i - 16] + sum0(block[i - 15]) + block[i - 7] + sum1(block[i - 2]);
        }
        long a = hash[0];
        long b = hash[1];
        long c = hash[2];
        long d = hash[3];
        long e = hash[4];
        long f = hash[5];
        long g = hash[6];
        long h = hash[7];
        // 80 rounds
        for (int i = 0; i < 80; i++) {
            long temp1 = h + ch(e, f, g) + block[i] + k[i] + s1(e);
            long temp2 = maj(a, b, c) + s0(a);
            h = g;
            g = f;
            f = e;
            e = d + temp1;
            d = c;
            c = b;
            b = a;
            a = temp1 + temp2;
        }
        hash[0] += a;
        hash[1] += b;
        hash[2] += c;
        hash[3] += d;
        hash[4] += e;
        hash[5] += f;
        hash[6] += g;
        hash[7] += h;
    }

    private static long sum0(long x) {
        return ((x >>> 1) | (x << 63)) ^ ((x >>> 8) | (x << 56)) ^ (x >>> 7);
    }

    private static long sum1(long x) {
        return ((x >>> 19) | (x << 45)) ^ ((x >>> 61) | (x << 3)) ^ (x >>> 6);
    }

    private static long s0(long a) {
        return ((a >>> 28) | (a << 36)) ^ ((a >>> 34) | (a << 30)) ^ ((a >>> 39) | (a << 25));
    }

    private static long s1(long e) {
        return ((e >>> 14) | (e << 50)) ^ ((e >>> 18) | (e << 46)) ^ ((e >>> 41) | (e << 23));
    }

    private static long ch(long e, long f, long g) {
        return (e & f) ^ ((~e) & g);
    }

    private static long maj(long a, long b, long c) {
        return (a & b) ^ (a & c) ^ (b & c);
    }
}