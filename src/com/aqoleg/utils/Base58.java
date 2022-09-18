/*
base58 decoder and encoder

usage:
    String encoded = Base58.encode(bytes);
    byte[] decoded = Base58.decode(string);
*/

package com.aqoleg.utils;

import java.math.BigInteger;

public class Base58 {
    private static final String alphabet = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
    private static final BigInteger base = BigInteger.valueOf(58);

    /**
     * @param bytes array containing bytes to be encoded
     * @return bytes encoded in base58 String
     */
    public static String encode(byte[] bytes) {
        if (bytes == null) {
            return null;
        } else if (bytes.length == 0) {
            return "";
        }
        StringBuilder code = new StringBuilder();
        BigInteger x = new BigInteger(1, bytes);
        while (x.signum() > 0) {
            // code += alphabet[x % 58]
            // x = x / 58
            BigInteger[] divide = x.divideAndRemainder(base);
            x = divide[0];
            code.append(alphabet.charAt(divide[1].intValue()));
        }
        // add "1" for each leading zero byte
        int bytesPos = 0;
        while (bytes[bytesPos++] == 0) {
            code.append(alphabet.charAt(0));
            if (bytes.length == bytesPos) {
                break;
            }
        }
        return code.reverse().toString();
    }

    /**
     * @param string base58 String to be decoded
     * @return a byte array containing decoded string
     * @throws Base58.Exception if string contains non-base58 symbols
     */
    public static byte[] decode(String string) {
        if (string == null) {
            return null;
        } else if (string.isEmpty()) {
            return new byte[]{};
        }
        BigInteger x = BigInteger.ZERO;
        BigInteger base = BigInteger.ONE;
        for (int i = string.length() - 1; i >= 0; i--) {
            // x += base * alphabetPos
            // base *= 58
            char c = string.charAt(i);
            for (int j = 0; j < 58; j++) {
                if (c == alphabet.charAt(j)) {
                    x = x.add(base.multiply(BigInteger.valueOf(j)));
                    break;
                } else if (j == 57) {
                    throw new Exception("unaccepted symbol '" + c + "' at position " + i);
                }
            }
            base = base.multiply(Base58.base);
        }
        byte[] bytes = x.toByteArray();
        // BigInteger byte array is the signed two's-complement representation, so the first byte can be 0
        int bytesStart = (bytes.length > 1 && bytes[0] == 0) ? 1 : 0;
        // add zero byte in the left for each "1"
        int zeros = 0;
        for (int i = 0; i < string.length() - 1; i++) {
            if (string.charAt(i) == alphabet.charAt(0)) {
                zeros++;
            } else {
                break;
            }
        }
        byte[] out = new byte[zeros + bytes.length - bytesStart];
        System.arraycopy(bytes, bytesStart, out, zeros, bytes.length - bytesStart);
        return out;
    }

    public static class Exception extends RuntimeException {
        private Exception(String message) {
            super(message);
        }
    }
}