package com.aqoleg.keys.test;

import com.aqoleg.Test;
import com.aqoleg.keys.Address;
import com.aqoleg.keys.KeyPair;
import com.aqoleg.keys.PublicKey;

import java.math.BigInteger;

@SuppressWarnings("unused")
public class AddressTest extends Test {

    public static void main(String[] args) {
        new AddressTest().testAll();
    }

    public void create() {
        assertThrows(NullPointerException.class, () -> new Address(null));
        assertThrows(
                Address.Exception.class,
                () -> new Address("4Ii")
        );
        assertThrows(
                Address.Exception.class,
                () -> new Address("14Wu4nRjjTNggBfoZ4Md8t1gZv788cdvqTk")
        );
        assertThrows(
                Address.Exception.class,
                () -> new Address("14Wu4nRjjTNggBfoZ4Md8t1gZv788cdvqj")
        );
        assertThrows(
                Address.Exception.class,
                () -> new Address("84Wu4nRjjTNggBfoZ4Md8t1gZv788cdvqT")
        );

        Address address = new Address("14Wu4nRjjTNggBfoZ4Md8t1gZv788cdvqT");
        assertTrue(address.p2pkh);
        assertEquals("14Wu4nRjjTNggBfoZ4Md8t1gZv788cdvqT", address.toString());
        //noinspection ObjectEqualsNull
        assertTrue(!address.equals(null));
        //noinspection EqualsBetweenInconvertibleTypes
        assertTrue(!address.equals("14Wu4nRjjTNggBfoZ4Md8t1gZv788cdvqT"));
        assertTrue(!address.equals(new Address("34qkc2iac6RsyxZVfyE2S5U5WcRsbg2dpK")));
        assertTrue(address.equals(new Address("14Wu4nRjjTNggBfoZ4Md8t1gZv788cdvqT")));
        //noinspection EqualsWithItself
        assertTrue(address.equals(address));

        address = new Address("34qkc2iac6RsyxZVfyE2S5U5WcRsbg2dpK");
        assertTrue(!address.p2pkh);
        assertEquals("34qkc2iac6RsyxZVfyE2S5U5WcRsbg2dpK", address.toString());
    }

    public void publicKey() {
        assertThrows(NullPointerException.class, () -> Address.createFromPublicKey(null));

        BigInteger d = new BigInteger("18e14a7b6a307f426a94f8114701e7c8e774e7f9a47e2c2035db29a206321725", 16);
        Address address = Address.createFromPublicKey(new KeyPair(d, true).publicKey);
        assertEquals("1PMycacnJaSqwwJqjawXBErnLsZ7RkXUAs", address.toString());
        assertTrue(address.p2pkh);
        assertEquals("f54a5851e9372b87810a8e60cdd2e7cfd80b6e31", address.getHash());

        PublicKey publicKey = PublicKey.createFromBytes(hexToBytes("04"
                + "5AE7B6463ACD4BA68265F15C93661D30BD03810C489EFA49BE85E857FE5A068A"
                + "E478E5752E92E619C16CC607C3649598DCA2000239F00C7CF921CE3B01285D0A"));
        address = Address.createFromPublicKey(publicKey);
        assertEquals("14gYn5TdLthFADfagKhyZfYXHyuMLtxB6t", address.toString());
        assertEquals("286320497f1c5892efe9253073225e86e8fc3a32", address.getHash());
    }

    public void hash() {
        assertThrows(NullPointerException.class, () -> Address.createFromHash(null, true));
        assertThrows(Address.Exception.class, () -> Address.createFromHash(new byte[33], true));

        Address address = Address.createFromHash(hexToBytes("286320497F1C5892EFE9253073225E86E8FC3A32"), true);
        assertTrue(address.p2pkh);
        assertEquals("14gYn5TdLthFADfagKhyZfYXHyuMLtxB6t", address.toString());

        address = Address.createFromHash(hexToBytes("19a7d869032368fd1f1e26e5e73a4ad0e474960e"), false);
        assertTrue(!address.p2pkh);
        assertEquals("342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey", address.toString());
        assertEquals("19a7d869032368fd1f1e26e5e73a4ad0e474960e", address.getHash());
    }
}