/*
bytes-to-string, bytes-to-hex and hex-to-bytes converters

usage:
    String string = Converter.bytesToString(bytes);
    String hex = Converter.bytesToHex(bytes, withPrefix, uppercase);
    String reverseHex = Converter.bytesToHexReverse(bytes, withPrefix, uppercase);
    byte[] bytes = Converter.hexToBytes(string);
    byte[] reverseBytes = Converter.hexToBytesReverse(string);
*/

package com.aqoleg.utils;

public class Converter {

    /**
     * @param bytes array to be converted
     * @return String with chars or hex values of bytes (if not a char) in the same endianness
     */
    public static String bytesToString(byte[] bytes) {
        if (bytes == null) {
            return null;
        } else if (bytes.length == 0) {
            return "";
        }
        boolean prevHex = true;
        StringBuilder stringBuilder = new StringBuilder();
        for (byte b : bytes) {
            if (b >= 32 && b <= 126) {
                if (prevHex) {
                    prevHex = false;
                    if (stringBuilder.length() > 0) {
                        stringBuilder.append(' ');
                    }
                }
                stringBuilder.append((char) b);
            } else {
                if (!prevHex) {
                    prevHex = true;
                    if (stringBuilder.length() > 0) {
                        stringBuilder.append(' ');
                    }
                }
                stringBuilder.append(intToChar((b & 0b11110000) >> 4, false));
                stringBuilder.append(intToChar(b & 0b1111, false));
            }
        }
        return stringBuilder.toString();
    }

    /**
     * @param bytes     array to be converted
     * @param prefix    if true, starts with "0x"
     * @param uppercase if true, return uppercase hex
     * @return String with hex values of bytes in the same endianness
     */
    public static String bytesToHex(byte[] bytes, boolean prefix, boolean uppercase) {
        if (bytes == null) {
            return null;
        } else if (bytes.length == 0) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder(bytes.length * 2 + 2);
        if (prefix) {
            stringBuilder.append("0x");
        }
        for (byte b : bytes) {
            stringBuilder.append(intToChar((b & 0b11110000) >> 4, uppercase));
            stringBuilder.append(intToChar(b & 0b1111, uppercase));
        }
        return stringBuilder.toString();
    }

    /**
     * @param bytes     array to be converted
     * @param prefix    if true, starts with "0x"
     * @param uppercase if true, return uppercase hex
     * @return String with hex values of bytes in the opposite endianness
     */
    public static String bytesToHexReverse(byte[] bytes, boolean prefix, boolean uppercase) {
        if (bytes == null) {
            return null;
        } else if (bytes.length == 0) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder(bytes.length * 2 + 2);
        if (prefix) {
            stringBuilder.append("0x");
        }
        for (int i = bytes.length - 1; i >= 0; i--) {
            stringBuilder.append(intToChar((bytes[i] & 0b11110000) >> 4, uppercase));
            stringBuilder.append(intToChar(bytes[i] & 0b1111, uppercase));
        }
        return stringBuilder.toString();
    }

    /**
     * @param hex String in hex, can start with 0x
     * @return byte array created from this hex String in the same endianness
     * @throws Converter.Exception if string contains non-accepted symbols
     */
    public static byte[] hexToBytes(String hex) {
        if (hex == null) {
            return null;
        }
        int hexI = 0;
        if (hex.length() > 1 && hex.charAt(0) == '0' && hex.charAt(1) == 'x') {
            hexI += 2;
        }
        if (hex.length() % 2 != 0) {
            hexI--;
        }
        int length = (hex.length() - hexI) / 2;
        byte[] bytes = new byte[length];
        if (length == 0) {
            return bytes;
        }
        int aByte = 0;
        if (hexI >= 0 && hex.charAt(hexI) != 'x') {
            aByte = charToInt(hex.charAt(hexI)) << 4;
        }
        hexI++;
        aByte |= charToInt(hex.charAt(hexI++));
        bytes[0] = (byte) aByte;
        for (int i = 1; i < length; i++) {
            aByte = charToInt(hex.charAt(hexI++)) << 4;
            aByte |= charToInt(hex.charAt(hexI++));
            bytes[i] = (byte) aByte;
        }
        return bytes;
    }

    /**
     * @param hex String in hex, can start with 0x
     * @return byte array created from this hex String in the opposite endianness
     * @throws Converter.Exception if string contains non-accepted symbols
     */
    public static byte[] hexToBytesReverse(String hex) {
        if (hex == null) {
            return null;
        }
        int hexI = 0;
        if (hex.length() > 1 && hex.charAt(0) == '0' && hex.charAt(1) == 'x') {
            hexI += 2;
        }
        if (hex.length() % 2 != 0) {
            hexI--;
        }
        int length = (hex.length() - hexI) / 2;
        byte[] bytes = new byte[length];
        if (length == 0) {
            return bytes;
        }
        int aByte = 0;
        if (hexI >= 0 && hex.charAt(hexI) != 'x') {
            aByte = charToInt(hex.charAt(hexI)) << 4;
        }
        hexI++;
        aByte |= charToInt(hex.charAt(hexI++));
        bytes[length - 1] = (byte) aByte;
        for (int i = length - 2; i >= 0; i--) {
            aByte = charToInt(hex.charAt(hexI++)) << 4;
            aByte |= charToInt(hex.charAt(hexI++));
            bytes[i] = (byte) aByte;
        }
        return bytes;
    }

    // 0 <= i <= 35
    private static char intToChar(int i, boolean uppercase) {
        if (i < 10) {
            return (char) (i + 48);
        }
        if (uppercase) {
            return (char) (i + 55);
        } else {
            return (char) (i + 87);
        }
    }

    // 0 <= c <= 9 or A <= c <= F or a <= c <= f else throws exception
    private static int charToInt(char c) {
        if (c < 48) {
            throw new Exception("unaccepted symbol '" + c + "'");
        } else if (c < 58) {
            return c - 48;
        } else if (c < 65) {
            throw new Exception("unaccepted symbol '" + c + "'");
        } else if (c < 71) {
            return c - 55;
        } else if (c < 97) {
            throw new Exception("unaccepted symbol '" + c + "'");
        } else if (c < 103) {
            return c - 87;
        } else {
            throw new Exception("unaccepted symbol '" + c + "'");
        }
    }

    public static class Exception extends RuntimeException {
        private Exception(String message) {
            super(message);
        }
    }
}