// bytes-to-hex and hex-to-bytes converters
package space.aqoleg.utils;

public class Converter {

    /**
     * @param bytes     array to be converted, big endian
     * @param prefix    if true, starts with "0x"
     * @param uppercase if true, return uppercase hex
     * @return String containing hex value of bytes
     * @throws NullPointerException if bytes == null
     */
    public static String bytesToHex(byte[] bytes, boolean prefix, boolean uppercase) {
        if (bytes.length == 0) {
            return "";
        }
        StringBuilder stringBuilder = prefix ? new StringBuilder("0x") : new StringBuilder();
        for (byte b : bytes) {
            stringBuilder.append(String.format(uppercase ? "%02X" : "%02x", b)); // zero-padded, min width 2
        }
        return stringBuilder.toString();
    }

    /**
     * @param hex String in hex, can start with 0x
     * @return byte array creating from this hex String, big endian
     * @throws NullPointerException          if hex == null
     * @throws UnsupportedOperationException if string contains not accepted symbols
     */
    public static byte[] hexToBytes(String hex) {
        if (hex.startsWith("0x")) {
            hex = hex.substring(2);
        }
        if (hex.length() % 2 != 0) {
            hex = "0".concat(hex);
        }
        int length = hex.length();
        if (length == 0) {
            return new byte[]{};
        }
        byte[] bytes = new byte[length / 2];
        int firstChar;
        int secondChar;
        for (int i = 0; i < length; i += 2) {
            firstChar = Character.digit(hex.charAt(i), 16);
            if (firstChar < 0) {
                throw new UnsupportedOperationException("symbol '" + hex.charAt(i) + "' is not accepted");
            }
            secondChar = Character.digit(hex.charAt(i + 1), 16);
            if (secondChar < 0) {
                throw new UnsupportedOperationException("symbol '" + hex.charAt(i + 1) + "' is not accepted");
            }
            bytes[i / 2] = (byte) (firstChar << 4 | secondChar);
        }
        return bytes;
    }
}