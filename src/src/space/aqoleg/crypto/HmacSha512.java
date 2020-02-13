// hash-based message authentication code using sha512
package space.aqoleg.crypto;

import space.aqoleg.utils.Converter;

import java.util.Arrays;

public class HmacSha512 {

    /**
     * prints hmac of the input Strings [message, key]
     *
     * @param args [message, key]
     */
    public static void main(String args[]) {
        String message = args.length == 0 ? "" : args[0];
        String key = args.length <= 1 ? "" : args[1];
        byte[] mac = getMac(message.getBytes(), key.getBytes());
        System.out.println(Converter.bytesToHex(mac, false, false));
    }

    /**
     * @param message array containing message to be authenticate
     * @param key     array containing key
     * @return 64-bytes mac
     * @throws NullPointerException if message == null or key == null
     */
    public static byte[] getMac(byte[] message, byte[] key) {
        // hmac(key, message) = hash( (K' xor opad) || hash((K' xor ipad) || message) )
        // padding
        // key longer than block size of the hash is shortened by hashing it
        if (key.length > 128) {
            key = Sha512.getHash(key);
        }
        // K' = block-sized key, key shorter than block size is padded with zeros on the right
        key = Arrays.copyOf(key, 128);

        byte[] inner = new byte[128 + message.length]; // inner.length = K'.length + message.length
        byte[] outer = new byte[192]; // outer.length = K'.length + hash.length
        // inner = K' xor ipad
        // outer = K' xor opad
        for (int i = 0; i < 128; i++) {
            inner[i] = (byte) (key[i] ^ 0x36);
            outer[i] = (byte) (key[i] ^ 0x5c);
        }
        // inner = inner || message
        System.arraycopy(message, 0, inner, 128, message.length);
        // inner = hash(inner)
        inner = Sha512.getHash(inner);
        // outer = outer || inner
        System.arraycopy(inner, 0, outer, 128, 64);
        // return hash(outer)
        return Sha512.getHash(outer);
    }
}