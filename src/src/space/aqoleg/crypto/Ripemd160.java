// cryptographic hash function
package space.aqoleg.crypto;

import space.aqoleg.utils.Converter;

public class Ripemd160 {
    private static final int kLeft[] = {0x00000000, 0x5A827999, 0x6ED9EBA1, 0x8F1BBCDC, 0xA953FD4E};
    private static final int kRight[] = {0x50A28BE6, 0x5C4DD124, 0x6D703EF3, 0x7A6D76E9, 0x00000000};
    private static final int rLeft[][] = {
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15},
            {7, 4, 13, 1, 10, 6, 15, 3, 12, 0, 9, 5, 2, 14, 11, 8},
            {3, 10, 14, 4, 9, 15, 8, 1, 2, 7, 0, 6, 13, 11, 5, 12},
            {1, 9, 11, 10, 0, 8, 12, 4, 13, 3, 7, 15, 14, 5, 6, 2},
            {4, 0, 5, 9, 7, 12, 2, 10, 14, 1, 3, 8, 11, 6, 15, 13}
    };
    private static final int rRight[][] = {
            {5, 14, 7, 0, 9, 2, 11, 4, 13, 6, 15, 8, 1, 10, 3, 12},
            {6, 11, 3, 7, 0, 13, 5, 10, 14, 15, 8, 12, 4, 9, 1, 2},
            {15, 5, 1, 3, 7, 14, 6, 9, 11, 8, 12, 2, 10, 0, 4, 13},
            {8, 6, 4, 1, 3, 11, 15, 0, 5, 12, 2, 13, 9, 7, 10, 14},
            {12, 15, 10, 4, 1, 5, 8, 7, 6, 2, 13, 14, 0, 3, 9, 11}
    };
    private static final int sLeft[][] = {
            {11, 14, 15, 12, 5, 8, 7, 9, 11, 13, 14, 15, 6, 7, 9, 8},
            {7, 6, 8, 13, 11, 9, 7, 15, 7, 12, 15, 9, 11, 7, 13, 12},
            {11, 13, 6, 7, 14, 9, 13, 15, 14, 8, 13, 6, 5, 12, 7, 5},
            {11, 12, 14, 15, 14, 15, 9, 8, 9, 14, 5, 6, 8, 6, 5, 12},
            {9, 15, 5, 11, 6, 8, 13, 12, 5, 12, 13, 14, 11, 8, 5, 6}
    };
    private static final int sRight[][] = {
            {8, 9, 9, 11, 13, 15, 15, 5, 7, 7, 8, 11, 14, 14, 12, 6},
            {9, 13, 15, 7, 12, 8, 9, 11, 7, 7, 12, 7, 6, 15, 13, 11},
            {9, 7, 15, 11, 8, 6, 6, 14, 12, 13, 5, 14, 13, 13, 7, 5},
            {15, 5, 8, 11, 14, 14, 6, 14, 6, 9, 12, 9, 12, 5, 15, 8},
            {8, 5, 12, 9, 12, 5, 14, 6, 8, 13, 6, 5, 15, 13, 11, 11}
    };

    /**
     * prints hash of the input string
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
        byte[] hash = getHash(input.toString().getBytes());
        System.out.println(Converter.bytesToHex(hash, false, false));
    }

    /**
     * @param message array containing message to be hashed
     * @return 20-bytes hash
     * @throws NullPointerException if message == null
     */
    public static byte[] getHash(byte[] message) {
        return getHash(message, 0, message.length);
    }

    /**
     * @param message       array containing message to be hashed
     * @param messageStart  starting position in the message array
     * @param messageLength the number of the bytes containing message
     * @return 20-bytes hash
     * @throws NullPointerException           if message == null
     * @throws ArrayIndexOutOfBoundsException if incorrect messageStart or messageLength
     */
    public static byte[] getHash(byte[] message, int messageStart, int messageLength) {
        int[] hash = {0x67452301, 0xEFCDAB89, 0x98BADCFE, 0x10325476, 0xC3D2E1F0}; // 160-bit hash
        int[] block = new int[16]; // 512-bit block, contain 16 words * 32 bits
        int blockPos = 0; // current position in the block
        int messagePos = messageStart; // current position in the message
        // process 32-bits words from message
        int stop = messageStart + messageLength - 3;
        while (messagePos < stop) {
            block[blockPos++] = getWord(message, messagePos);
            messagePos += 4;
            if (blockPos == 16) {
                blockPos = 0;
                hashBlock(block, hash);
            }
        }
        // create buffer word and fill it with the rest of the message
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
        // put length in the end of the last block, little-endian
        long bitLength = messageLength << 3;
        block[14] = (int) (bitLength);
        block[15] = (int) (bitLength >>> 32);
        hashBlock(block, hash);
        // return hash, 32-bit words are little-endian
        byte[] out = new byte[20];
        int outPos = 0;
        for (int word : hash) {
            out[outPos++] = (byte) word;
            out[outPos++] = (byte) (word >>> 8);
            out[outPos++] = (byte) (word >>> 16);
            out[outPos++] = (byte) (word >>> 24);
        }
        return out;
    }

    // returns 32-bit word, little-endian
    private static int getWord(byte[] bytes, int bytesStart) {
        return (bytes[bytesStart] & 0xFF) |
                (bytes[++bytesStart] & 0xFF) << 8 |
                (bytes[++bytesStart] & 0xFF) << 16 |
                bytes[++bytesStart] << 24;
    }

    private static void hashBlock(int[] block, int[] hash) {
        int aLeft = hash[0];
        int bLeft = hash[1];
        int cLeft = hash[2];
        int dLeft = hash[3];
        int eLeft = hash[4];
        int aRight = hash[0];
        int bRight = hash[1];
        int cRight = hash[2];
        int dRight = hash[3];
        int eRight = hash[4];
        int temp;
        // 5 rounds * 16 times
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 16; j++) {
                // left
                temp = rol(
                        (aLeft + f(i, bLeft, cLeft, dLeft) + block[rLeft[i][j]] + kLeft[i]),
                        sLeft[i][j]
                ) + eLeft;
                aLeft = eLeft;
                eLeft = dLeft;
                dLeft = rol(cLeft, 10);
                cLeft = bLeft;
                bLeft = temp;
                // right
                temp = rol(
                        (aRight + f(4 - i, bRight, cRight, dRight) + block[rRight[i][j]] + kRight[i]),
                        sRight[i][j]
                ) + eRight;
                aRight = eRight;
                eRight = dRight;
                dRight = rol(cRight, 10);
                cRight = bRight;
                bRight = temp;
            }
        }
        temp = hash[1] + cLeft + dRight;
        hash[1] = hash[2] + dLeft + eRight;
        hash[2] = hash[3] + eLeft + aRight;
        hash[3] = hash[4] + aLeft + bRight;
        hash[4] = hash[0] + bLeft + cRight;
        hash[0] = temp;
    }


    private static int f(int i, int b, int c, int d) {
        switch (i) {
            case 0:
                return b ^ c ^ d;
            case 1:
                return (b & c) | (~b & d);
            case 2:
                return (b | ~c) ^ d;
            case 3:
                return (b & d) | (c & ~d);
            case 4:
                return b ^ (c | ~d);
        }
        return 0;
    }

    // cyclic shift x << s
    private static int rol(int x, int s) {
        return (x << s) | (x >>> 32 - s);
    }
}