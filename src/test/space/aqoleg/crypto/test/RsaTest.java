package space.aqoleg.crypto.test;

import org.junit.jupiter.api.Test;
import space.aqoleg.crypto.Rsa;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RsaTest {

    @Test
    void test() {
        assertThrows(UnsupportedOperationException.class, () -> Rsa.createKeyPair(6));
        Rsa.KeyPair keyPair = Rsa.createKeyPair(34);
        Rsa.PublicKey publicKey = keyPair.getPublicKey();
        byte[] msg = {4, 120, 8, -9, 7};
        assertThrows(NullPointerException.class, () -> keyPair.encrypt(null));
        assertThrows(NullPointerException.class, () -> publicKey.encrypt(null));
        assertThrows(NullPointerException.class, () -> keyPair.decrypt(null));
        assertThrows(UnsupportedOperationException.class, () -> keyPair.encrypt(msg));
        msg[0] = 1;
        byte[] cypher = publicKey.encrypt(msg);
        assertArrayEquals(cypher, keyPair.encrypt(msg));
        byte[] decrypt = keyPair.decrypt(cypher);
        assertArrayEquals(msg, decrypt);

        testWithKeyLength(20);
        testWithKeyLength(21);
        testWithKeyLength(82);
        testWithKeyLength(258);
        testWithKeyLength(514);
        testWithKeyLength(1002);
        testWithKeyLength(2007);
    }

    private void testWithKeyLength(int keyLength) {
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            Rsa.KeyPair keyPair = Rsa.createKeyPair(keyLength);
            byte[] msg = new byte[keyLength >>> 3];
            for (int j = 0; j < msg.length; j++) {
                msg[j] = (byte) random.nextInt();
                if (j == 0 && msg[0] == 0) {
                    msg[0] = 1;
                }
            }
            assertArrayEquals(msg, keyPair.decrypt(keyPair.encrypt(msg)));
        }
    }
}