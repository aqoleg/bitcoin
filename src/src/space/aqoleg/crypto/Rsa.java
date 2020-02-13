// public-key cryptosystem
package space.aqoleg.crypto;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;

public class Rsa {
    private final static Rsa rsa = new Rsa(); // single instance for keys

    private Rsa() {
    }

    /**
     * @param keyBitLength length of the key
     * @return randomly generated KeyPair
     * @throws UnsupportedOperationException if keyBitLength < 20
     */
    public static KeyPair createKeyPair(int keyBitLength) {
        if (keyBitLength < 20) {
            throw new UnsupportedOperationException("too small key bit length, required keyBitLength >= 20");
        }
        Random random = new SecureRandom();
        BigInteger n; // modulus
        BigInteger e; // private exponent
        BigInteger d; // public exponent
        // get random primes p and q, p != q, n = p * q, n bit length == keyBitLength
        BigInteger p, q;
        int pMinLength = (keyBitLength >> 1) - 4;
        int pMaxLength = (keyBitLength >> 1) + 3;
        do {
            do {
                int pLength = pMinLength + (int) (Math.random() * (pMaxLength - pMinLength + 1));
                // pLength + qLength = keyBitLength or keyBitLength + 1
                int qLength = keyBitLength - pLength + (int) (Math.random() * 1.4);
                p = new BigInteger(pLength, 8, random);
                q = new BigInteger(qLength, 8, random);
                n = p.multiply(q);
            } while (p.compareTo(q) == 0 || n.bitLength() != keyBitLength);
            // find lambda = lcm(p - 1, q - 1)
            // lcm(a, b) = a * b / gcd(a, b)
            p = p.subtract(BigInteger.ONE);
            q = q.subtract(BigInteger.ONE);
            BigInteger lambda = p.multiply(q).divide(p.gcd(q));
            // get random e, 1 < e < lambda, gcd(e, lambda) = 1
            // choose a prime e, so lambda % e != 0
            int eMaxLength = lambda.bitLength() > 32 ? lambda.bitLength() / 2 : lambda.bitLength() - 1;
            do {
                int eLength = 3 + (int) (Math.random() * (eMaxLength - 2));
                e = new BigInteger(eLength, 8, random);
            } while (lambda.remainder(e).signum() == 0);
            // find d = e**(-1) % lambda
            d = e.modInverse(lambda);
        } while (d.compareTo(e) == 0);
        return rsa.new KeyPair(n, e, d);
    }

    private static byte[] process(byte[] message, BigInteger modulus, BigInteger exponent) {
        // cypher = message**e % modulus
        // message = cypher**d % modulus = message**(e * d) % modulus
        BigInteger msg = new BigInteger(1, message);
        if (msg.compareTo(modulus) > -1) {
            throw new UnsupportedOperationException("too big message, required message < " + modulus.toString());
        }
        msg = msg.modPow(exponent, modulus);
        byte[] bytes = msg.toByteArray();
        // BigInteger byte array is the signed two's-complement representation, so the first byte can be 0
        if (bytes[0] == 0) {
            return Arrays.copyOfRange(bytes, 1, bytes.length);
        }
        return bytes;
    }

    public class KeyPair {
        private final BigInteger n;
        private final BigInteger e;
        private final BigInteger d;

        KeyPair(BigInteger n, BigInteger e, BigInteger d) {
            this.n = n;
            this.e = e;
            this.d = d;
        }

        /**
         * @return PublicKey of this KeyPair
         */
        public PublicKey getPublicKey() {
            return new PublicKey(n, d);
        }

        /**
         * @param message array containing message to be encrypted with this KeyPair, big-endian
         * @return array containing encrypted message
         * @throws NullPointerException     if message == null
         * @throws IllegalArgumentException if message is too big
         */
        public byte[] encrypt(byte[] message) {
            return process(message, n, d);
        }

        /**
         * @param message array containing message to be decrypted with this KeyPair, big-endian
         * @return array containing decrypted message
         * @throws NullPointerException     if message == null
         * @throws IllegalArgumentException if message is too big
         */
        public byte[] decrypt(byte[] message) {
            return process(message, n, e);
        }
    }

    public class PublicKey {
        private final BigInteger n;
        private final BigInteger d;

        PublicKey(BigInteger n, BigInteger d) {
            this.n = n;
            this.d = d;
        }

        /**
         * @param message array containing message to be encrypted with this PublicKey, big-endian
         * @return array containing encrypted message
         * @throws NullPointerException          if message == null
         * @throws UnsupportedOperationException if message is too big
         */
        public byte[] encrypt(byte[] message) {
            return process(message, n, d);
        }
    }
}