package com.aqoleg.keys.test;

import com.aqoleg.Test;
import com.aqoleg.crypto.Ecc;
import com.aqoleg.keys.KeyPair;
import com.aqoleg.keys.PublicKey;

import java.math.BigInteger;

@SuppressWarnings("unused")
public class PublicKeyTest extends Test {

    public static void main(String[] args) {
        new PublicKeyTest().testAll();
    }

    public void create() {
        assertThrows(NullPointerException.class, () -> new PublicKey(null, true));

        BigInteger x = new BigInteger("352BBF4A4CDD12564F93FA332CE333301D9AD40271F8107181340AEF25BE59D5", 16);
        BigInteger y = new BigInteger("321EB4075348F534D59C18259DDA3E1F4A1B3B2E71B1039C67BD3D8BCF81998C", 16);
        PublicKey publicKey = new PublicKey(Ecc.createPoint(x, y), true);
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
        assertTrue(!publicKey.compressed);

        publicKey = new KeyPair(BigInteger.valueOf(21), true).publicKey;
        assertEquals(
                "2352BBF4A4CDD12564F93FA332CE333301D9AD40271F8107181340AEF25BE59D5",
                new BigInteger(publicKey.toByteArray()).toString(16).toUpperCase()
        );
        assertTrue(publicKey.compressed);
    }

    public void fromBytes() {
        assertThrows(NullPointerException.class, () -> PublicKey.createFromBytes(null));
        assertThrows(
                PublicKey.Exception.class,
                () -> PublicKey.createFromBytes(new byte[]{99, 99})
        );
        byte[] bytes0 = hexToBytes("016339cd653f9678e7ecda450054d51594b1e4655728a9cf2791b68cadb189f34c"
                + "b0cba21bff403282c82de5c17b24f5537757ede0a2342959b9a051f2fc950103");
        assertThrows(PublicKey.Exception.class, () -> PublicKey.createFromBytes(bytes0));
        byte[] bytes1 = hexToBytes("046339cd653f9678e7ecda450054d51594b1e4655728a9cf2791b68cadb189f34c"
                + "b0cba21bff403282c82de5c17b24f5537757ede0a2342959b9a051f2fc950102");
        assertThrows(PublicKey.Exception.class, () -> PublicKey.createFromBytes(bytes1));

        byte[] bytes = hexToBytes("046339cd653f9678e7ecda450054d51594b1e4655728a9cf2791b68cadb189f34c"
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
        assertTrue(!publicKey.compressed);

        byte[] bytes2 = hexToBytes("036339cd653f9678e7ecda450054d51594b1e4655728a9cf2791b68cadb189f3");
        assertThrows(PublicKey.Exception.class, () -> PublicKey.createFromBytes(bytes2));

        byte[] bytes3 = hexToBytes("086339cd653f9678e7ecda450054d51594b1e4655728a9cf2791b68cadb189f34c");
        assertThrows(PublicKey.Exception.class, () -> PublicKey.createFromBytes(bytes3));

        bytes = hexToBytes("036339cd653f9678e7ecda450054d51594b1e4655728a9cf2791b68cadb189f34c");
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

        bytes = hexToBytes("0450863ad64a87ae8a2fe83c1af1a8403cb53f53e486d8511dad8a04887e5b2352"
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
        assertTrue(!publicKey.compressed);

        bytes = hexToBytes("0250863ad64a87ae8a2fe83c1af1a8403cb53f53e486d8511dad8a04887e5b2352");
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

    public void hash() {
        BigInteger d = new BigInteger("18e14a7b6a307f426a94f8114701e7c8e774e7f9a47e2c2035db29a206321725", 16);
        assertEquals(
                hexToBytes("f54a5851e9372b87810a8e60cdd2e7cfd80b6e31"),
                new KeyPair(d, true).publicKey.getHash()
        );

        PublicKey publicKey = KeyPair.decode("L559f8A9cqLBtwc5m9mwsRtzkyYxakb7d33fk5Yp8SzFqaCnyHvq").publicKey;
        assertTrue(publicKey.compressed);
        publicKey = KeyPair.decode("5KbULWiVgqZSaX3WdHVJsPrqtCTGmMJFBtDPNYPcbmQPr6S21py").publicKey;
        assertTrue(!publicKey.compressed);
    }
}