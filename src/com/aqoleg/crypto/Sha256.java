/*
cryptographic hash function

usage:
    byte[] hash = Sha256.getHash(bytes);
    byte[] hash = Sha256.getHash(bytes, start, length);
*/

package com.aqoleg.crypto;

public class Sha256 {
    private static final int k[] = {
            0x428a2f98, 0x71374491, 0xb5c0fbcf, 0xe9b5dba5, 0x3956c25b, 0x59f111f1, 0x923f82a4, 0xab1c5ed5,
            0xd807aa98, 0x12835b01, 0x243185be, 0x550c7dc3, 0x72be5d74, 0x80deb1fe, 0x9bdc06a7, 0xc19bf174,
            0xe49b69c1, 0xefbe4786, 0x0fc19dc6, 0x240ca1cc, 0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da,
            0x983e5152, 0xa831c66d, 0xb00327c8, 0xbf597fc7, 0xc6e00bf3, 0xd5a79147, 0x06ca6351, 0x14292967,
            0x27b70a85, 0x2e1b2138, 0x4d2c6dfc, 0x53380d13, 0x650a7354, 0x766a0abb, 0x81c2c92e, 0x92722c85,
            0xa2bfe8a1, 0xa81a664b, 0xc24b8b70, 0xc76c51a3, 0xd192e819, 0xd6990624, 0xf40e3585, 0x106aa070,
            0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5, 0x391c0cb3, 0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3,
            0x748f82ee, 0x78a5636f, 0x84c87814, 0x8cc70208, 0x90befffa, 0xa4506ceb, 0xbef9a3f7, 0xc67178f2
    };

    /**
     * @param message array containing message to be hashed
     * @return 32-bytes hash
     * @throws NullPointerException if message == null
     */
    public static byte[] getHash(byte[] message) {
        return getHash(message, 0, message.length);
    }

    /**
     * @param message       array containing message to be hashed
     * @param messageStart  starting position in the message array
     * @param messageLength the number of bytes containing the message
     * @return 32-bytes hash
     * @throws NullPointerException           if message == null
     * @throws ArrayIndexOutOfBoundsException if messageStart or messageLength is incorrect
     */
    public static byte[] getHash(byte[] message, int messageStart, int messageLength) {
        int[] hash = {0x6a09e667, 0xbb67ae85, 0x3c6ef372, 0xa54ff53a,
                0x510e527f, 0x9b05688c, 0x1f83d9ab, 0x5be0cd19}; // 256-bit hash
        int[] block = new int[64]; // 512-bit block, contains 16 words * 32 bits and extended 48 words
        int blockPos = 0; // current position in the block
        int messagePos = messageStart; // current position in the message
        // process 32-bits words from the message
        int stop = messageStart + messageLength - 3;
        while (messagePos < stop) {
            block[blockPos++] = getWord(message, messagePos);
            messagePos += 4;
            if (blockPos == 16) {
                blockPos = 0;
                hashBlock(block, hash);
            }
        }
        // create the buffer word and fill it with the rest of the message
        byte[] buffer = new byte[4];
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
        block[14] = (int) (bitLength >>> 32);
        block[15] = (int) (bitLength);
        hashBlock(block, hash);
        // return hash, 32-bit words are big-endian
        byte[] out = new byte[32];
        int outPos = 0;
        for (int word : hash) {
            out[outPos++] = (byte) (word >>> 24);
            out[outPos++] = (byte) (word >>> 16);
            out[outPos++] = (byte) (word >>> 8);
            out[outPos++] = (byte) word;
        }
        return out;
    }

    // returns 32-bit word, big-endian
    private static int getWord(byte[] bytes, int bytesStart) {
        return bytes[bytesStart] << 24 |
                (bytes[++bytesStart] & 0xFF) << 16 |
                (bytes[++bytesStart] & 0xFF) << 8 |
                (bytes[++bytesStart] & 0xFF);
    }

    private static void hashBlock(int[] block, int[] hash) {
        // extend the first 16 words into the remaining 48 words
        for (int i = 16; i < 64; i++) {
            block[i] = block[i - 16] + sum0(block[i - 15]) + block[i - 7] + sum1(block[i - 2]);
        }
        int a = hash[0];
        int b = hash[1];
        int c = hash[2];
        int d = hash[3];
        int e = hash[4];
        int f = hash[5];
        int g = hash[6];
        int h = hash[7];
        // 64 rounds
        for (int i = 0; i < 64; i++) {
            int temp1 = h + ch(e, f, g) + block[i] + k[i] + s1(e);
            int temp2 = maj(a, b, c) + s0(a);
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

    private static int sum0(int x) {
        return ((x >>> 7) | (x << 25)) ^ ((x >>> 18) | (x << 14)) ^ (x >>> 3);
    }

    private static int sum1(int x) {
        return ((x >>> 17) | (x << 15)) ^ ((x >>> 19) | (x << 13)) ^ (x >>> 10);
    }

    private static int s0(int a) {
        return ((a >>> 2) | (a << 30)) ^ ((a >>> 13) | (a << 19)) ^ ((a >>> 22) | (a << 10));
    }

    private static int s1(int e) {
        return ((e >>> 6) | (e << 26)) ^ ((e >>> 11) | (e << 21)) ^ ((e >>> 25) | (e << 7));
    }

    private static int ch(int e, int f, int g) {
        return (e & f) ^ ((~e) & g);
    }

    private static int maj(int a, int b, int c) {
        return (a & b) ^ (a & c) ^ (b & c);
    }
}