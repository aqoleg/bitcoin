package space.aqoleg.keys.test;

import org.junit.jupiter.api.Test;
import space.aqoleg.keys.Address;
import space.aqoleg.keys.KeyPair;
import space.aqoleg.keys.PublicKey;
import space.aqoleg.utils.Converter;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

class AddressTest {

    @Test
    void test1() {
        assertThrows(NullPointerException.class, () -> new Address(null));
        assertThrows(
                UnsupportedOperationException.class,
                () -> new Address("14Wu4nRjjTNggBfoZ4Md8t1gZv788cdvqTk")
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> new Address("14Wu4nRjjTNggBfoZ4Md8t1gZv788cdvqj")
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> new Address("84Wu4nRjjTNggBfoZ4Md8t1gZv788cdvqT")
        );

        Address address = new Address("14Wu4nRjjTNggBfoZ4Md8t1gZv788cdvqT");
        assertTrue(address.p2pkh);
        assertEquals("14Wu4nRjjTNggBfoZ4Md8t1gZv788cdvqT", address.toString());

        address = new Address("34qkc2iac6RsyxZVfyE2S5U5WcRsbg2dpK");
        assertFalse(address.p2pkh);
        assertEquals("34qkc2iac6RsyxZVfyE2S5U5WcRsbg2dpK", address.toString());
    }

    @Test
    void test2() {
        assertThrows(NullPointerException.class, () -> Address.createFromPublicKey(null));

        BigInteger d = new BigInteger("18e14a7b6a307f426a94f8114701e7c8e774e7f9a47e2c2035db29a206321725", 16);
        Address address = Address.createFromPublicKey(new KeyPair(d, true).publicKey);
        assertEquals("1PMycacnJaSqwwJqjawXBErnLsZ7RkXUAs", address.toString());
        assertTrue(address.p2pkh);
        assertEquals("f54a5851e9372b87810a8e60cdd2e7cfd80b6e31", Converter.bytesToHex(address.getHash(), false, false));

        PublicKey publicKey = PublicKey.createFromBytes(Converter.hexToBytes("04"
                + "5AE7B6463ACD4BA68265F15C93661D30BD03810C489EFA49BE85E857FE5A068A"
                + "E478E5752E92E619C16CC607C3649598DCA2000239F00C7CF921CE3B01285D0A"));
        address = Address.createFromPublicKey(publicKey);
        assertEquals("14gYn5TdLthFADfagKhyZfYXHyuMLtxB6t", address.toString());
        assertEquals("286320497F1C5892EFE9253073225E86E8FC3A32", Converter.bytesToHex(address.getHash(), false, true));
    }

    @Test
    void test3() {
        assertThrows(NullPointerException.class, () -> Address.createFromHash(null, true));
        assertThrows(UnsupportedOperationException.class, () -> Address.createFromHash(new byte[33], true));

        Address address = Address.createFromHash(Converter.hexToBytes("286320497F1C5892EFE9253073225E86E8FC3A32"), true);
        assertTrue(address.p2pkh);
        assertEquals("14gYn5TdLthFADfagKhyZfYXHyuMLtxB6t", address.toString());

        address = Address.createFromHash(Converter.hexToBytes("19a7d869032368fd1f1e26e5e73a4ad0e474960e"), false);
        assertFalse(address.p2pkh);
        assertEquals("342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey", address.toString());
        assertEquals("19a7d869032368fd1f1e26e5e73a4ad0e474960e", Converter.bytesToHex(address.getHash(), false, false));
    }
}