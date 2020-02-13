package space.aqoleg.keys.test;

import org.junit.jupiter.api.Test;
import space.aqoleg.keys.KeyPair;
import space.aqoleg.keys.PublicKey;
import space.aqoleg.crypto.Ecc;
import space.aqoleg.utils.Converter;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

class PublicKeyTest {

    @Test
    void test1() {
        assertThrows(NullPointerException.class, () -> new PublicKey(null, true));

        BigInteger x = new BigInteger("352BBF4A4CDD12564F93FA332CE333301D9AD40271F8107181340AEF25BE59D5", 16);
        BigInteger y = new BigInteger("321EB4075348F534D59C18259DDA3E1F4A1B3B2E71B1039C67BD3D8BCF81998C", 16);
        PublicKey publicKey = new PublicKey(Ecc.secp256k1.createPoint(x, y), true);
        assertEquals(
                "2352BBF4A4CDD12564F93FA332CE333301D9AD40271F8107181340AEF25BE59D5",
                new BigInteger(publicKey.toByteArray()).toString(16).toUpperCase()
        );
        assertTrue(publicKey.compressed);

        publicKey = new KeyPair(BigInteger.valueOf(21), false).publicKey;
        assertEquals(
                "4352BBF4A4CDD12564F93FA332CE333301D9AD40271F8107181340AEF25BE59D5"
                        + "321EB4075348F534D59C18259DDA3E1F4A1B3B2E71B1039C67BD3D8BCF81998C",
                new BigInteger(publicKey.toByteArray()).toString(16).toUpperCase()
        );
        assertFalse(publicKey.compressed);

        publicKey = new KeyPair(BigInteger.valueOf(21), true).publicKey;
        assertEquals(
                "2352BBF4A4CDD12564F93FA332CE333301D9AD40271F8107181340AEF25BE59D5",
                new BigInteger(publicKey.toByteArray()).toString(16).toUpperCase()
        );
        assertTrue(publicKey.compressed);
    }

    @Test
    void test2() {
        assertThrows(NullPointerException.class, () -> PublicKey.createFromBytes(null));
        assertThrows(
                UnsupportedOperationException.class,
                () -> PublicKey.createFromBytes(new byte[]{99, 99})
        );
        byte[] bytes0 = Converter.hexToBytes("016339cd653f9678e7ecda450054d51594b1e4655728a9cf2791b68cadb189f34c"
                + "b0cba21bff403282c82de5c17b24f5537757ede0a2342959b9a051f2fc950103");
        assertThrows(UnsupportedOperationException.class, () -> PublicKey.createFromBytes(bytes0));
        byte[] bytes1 = Converter.hexToBytes("046339cd653f9678e7ecda450054d51594b1e4655728a9cf2791b68cadb189f34c"
                + "b0cba21bff403282c82de5c17b24f5537757ede0a2342959b9a051f2fc950102");
        assertThrows(UnsupportedOperationException.class, () -> PublicKey.createFromBytes(bytes1));

        byte[] bytes = Converter.hexToBytes("046339cd653f9678e7ecda450054d51594b1e4655728a9cf2791b68cadb189f34c"
                + "b0cba21bff403282c82de5c17b24f5537757ede0a2342959b9a051f2fc950103");
        PublicKey publicKey = PublicKey.createFromBytes(bytes);
        assertEquals(
                "6339cd653f9678e7ecda450054d51594b1e4655728a9cf2791b68cadb189f34c",
                publicKey.point.x.toString(16)
        );
        assertEquals(
                "b0cba21bff403282c82de5c17b24f5537757ede0a2342959b9a051f2fc950103",
                publicKey.point.y.toString(16)
        );
        assertFalse(publicKey.compressed);

        byte[] bytes2 = Converter.hexToBytes("036339cd653f9678e7ecda450054d51594b1e4655728a9cf2791b68cadb189f3");
        assertThrows(UnsupportedOperationException.class, () -> PublicKey.createFromBytes(bytes2));

        byte[] bytes3 = Converter.hexToBytes("086339cd653f9678e7ecda450054d51594b1e4655728a9cf2791b68cadb189f34c");
        assertThrows(UnsupportedOperationException.class, () -> PublicKey.createFromBytes(bytes3));

        bytes = Converter.hexToBytes("036339cd653f9678e7ecda450054d51594b1e4655728a9cf2791b68cadb189f34c");
        publicKey = PublicKey.createFromBytes(bytes);
        assertEquals(
                "6339cd653f9678e7ecda450054d51594b1e4655728a9cf2791b68cadb189f34c",
                publicKey.point.x.toString(16)
        );
        assertEquals(
                "b0cba21bff403282c82de5c17b24f5537757ede0a2342959b9a051f2fc950103",
                publicKey.point.y.toString(16)
        );
        assertTrue(publicKey.compressed);

        bytes = Converter.hexToBytes("0450863ad64a87ae8a2fe83c1af1a8403cb53f53e486d8511dad8a04887e5b2352"
                + "2cd470243453a299fa9e77237716103abc11a1df38855ed6f2ee187e9c582ba6");
        publicKey = PublicKey.createFromBytes(bytes);
        assertEquals(
                "50863ad64a87ae8a2fe83c1af1a8403cb53f53e486d8511dad8a04887e5b2352",
                publicKey.point.x.toString(16)
        );
        assertEquals(
                "2cd470243453a299fa9e77237716103abc11a1df38855ed6f2ee187e9c582ba6",
                publicKey.point.y.toString(16)
        );
        assertFalse(publicKey.compressed);

        bytes = Converter.hexToBytes("0250863ad64a87ae8a2fe83c1af1a8403cb53f53e486d8511dad8a04887e5b2352");
        publicKey = PublicKey.createFromBytes(bytes);
        assertEquals(
                "50863ad64a87ae8a2fe83c1af1a8403cb53f53e486d8511dad8a04887e5b2352",
                publicKey.point.x.toString(16)
        );
        assertEquals(
                "2cd470243453a299fa9e77237716103abc11a1df38855ed6f2ee187e9c582ba6",
                publicKey.point.y.toString(16)
        );
        assertTrue(publicKey.compressed);
    }

    @Test
    void test3() {
        BigInteger d = new BigInteger("18e14a7b6a307f426a94f8114701e7c8e774e7f9a47e2c2035db29a206321725", 16);
        assertArrayEquals(
                Converter.hexToBytes("f54a5851e9372b87810a8e60cdd2e7cfd80b6e31"),
                new KeyPair(d, true).publicKey.getHash()
        );

        PublicKey publicKey = KeyPair.decode("L559f8A9cqLBtwc5m9mwsRtzkyYxakb7d33fk5Yp8SzFqaCnyHvq").publicKey;
        assertTrue(publicKey.compressed);
        publicKey = KeyPair.decode("5KbULWiVgqZSaX3WdHVJsPrqtCTGmMJFBtDPNYPcbmQPr6S21py").publicKey;
        assertFalse(publicKey.compressed);
    }

    @Test
    void test4() {
        // x less then 32 bit
        assertEquals(
                "1P6mxEa4JiKXy3vQzvQAAbNiawXzW6k1V6",
                new KeyPair(BigInteger.valueOf(4057), true).getAddress()
        );
        assertEquals(
                "1MYWFGXvCohiWNNbdizqP3K53ApL4QJvQ2",
                new KeyPair(BigInteger.valueOf(4057), false).getAddress()
        );
        assertEquals(
                "12hr9wNVaCuyq1eCzsk2DzAuUpCAnpvwrw",
                new KeyPair(BigInteger.valueOf(68831), true).getAddress()
        );
        assertEquals(
                "1DSqiFAPpmxPj3XD1M2pf5g573FB94FjyZ",
                new KeyPair(BigInteger.valueOf(68831), false).getAddress()
        );
    }
}