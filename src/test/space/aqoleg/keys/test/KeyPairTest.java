package space.aqoleg.keys.test;

import org.junit.jupiter.api.Test;
import space.aqoleg.keys.KeyPair;
import space.aqoleg.crypto.Ecc;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

class KeyPairTest {

    @Test
    void d() {
        assertThrows(NullPointerException.class, () -> new KeyPair(null, true));
        assertThrows(UnsupportedOperationException.class, () -> new KeyPair(BigInteger.valueOf(-1), true));
        assertThrows(UnsupportedOperationException.class, () -> new KeyPair(Ecc.secp256k1.getN(), false));
        assertThrows(UnsupportedOperationException.class, () -> new KeyPair(new BigInteger(new byte[32]), true));

        byte[] bytes = new byte[32];
        bytes[30] = 2;
        bytes[29] = 1;
        KeyPair keyPair = new KeyPair(new BigInteger(1, bytes), false);
        assertEquals("10200", keyPair.d.toString(16));
        assertFalse(keyPair.publicKey.compressed);
    }

    @Test
    void wif() {
        assertThrows(NullPointerException.class, () -> KeyPair.decode(null));
        assertThrows(
                UnsupportedOperationException.class,
                () -> KeyPair.decode("5q1Z1yeeVAZaPfFzuB94SMMHiBbkEv3zDHmGv8w3Myx8VQzqy6R")
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> KeyPair.decode("5HueCGU8rMjxEXxiPuD5BDku4MkFqeZyd4dZ1jvhTVqvbTNQxje")
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> KeyPair.decode("5HueCGU8rMjxEXxiPuD5BDku4MkFqeZyd4dZ1jvhTVqvbTLvyTK")
        );

        hexWifTest(
                "C28FCA386C7A227600B2FE50B7CAE11EC86D3BF1FBE471BE89827E19D72AA1D",
                "5HueCGU8rMjxEXxiPuD5BDku4MkFqeZyd4dZ1jvhTVqvbTLvyTJ"
        );
        hexWifTest(
                "85B7DFAB8D2C695C0EBF013AD50A758FDBC62775391B7CE33282634F7DB47480",
                "5JqBEaUuYJFA4dQsd62ys13RumxCGu97BRu3LzwaVduD8MQnFwc"
        );
        hexWifTest(
                "85B7DFAB8D2C695C0EBF013AD50A758FDBC62775391B7CE33282634F7DB47480",
                "L1heAvnAu97V11iNSHDjyhBiwwQcmB8nH2thMGjtqPsSXKaZ9VwY"
        );
        hexWifTest(
                "6190289CEB09ED41776AE27AE81F46C6540C56CF8F3900BCAADE96B4470F23D6",
                "5JZFhabXLayCMCehpyhdCPZ5LXwvgvu3gX4Png4xYzCWYCXJ3jx"
        );
        hexWifTest(
                "6190289CEB09ED41776AE27AE81F46C6540C56CF8F3900BCAADE96B4470F23D6",
                "KzVMt7HNuSw36ARRCmGJmGYa9DbRyEtiqRfyabSUCBToNRwMrAog"
        );
        hexWifTest(
                "763B1998BF7282A99CA21EB821EB9A204589FFA189E290EE64528854FDA91080",
                "5JiMdW1Czd3tTuy33wiC5wmrkhZxx2PNcC7NVZoFc7AixTVgmf5"
        );
        hexWifTest(
                "763B1998BF7282A99CA21EB821EB9A204589FFA189E290EE64528854FDA91080",
                "L1BY471uwL7k9nnL3zUovpkzbt4Vws7kNL37s5m9TxDvBjnadvMy"
        );
        hexWifTest(
                "102030405060708090A0B0C0D0E0F101112131415161718191A1B1C1D1E1F",
                "5HpHgWkLaovGWySEFpng1XQ6pdG1TzNWR7SrETvfTRVdKHNXZh8"
        );
        hexWifTest(
                "102030405060708090A0B0C0D0E0F101112131415161718191A1B1C1D1E1F",
                "KwDidQJHSE67VJ6MWRvbBKAxhD3F48DvqRT6JRqrjd7MHLBjGF7V"
        );
        hexWifTest(
                "1",
                "5HpHagT65TZzG1PH3CSu63k8DbpvD8s5ip4nEB3kEsreAnchuDf"
        );
        hexWifTest(
                "7512",
                "5HpHagT65TZzG1PH3CSu63k8DbpvD8s5ip4nEB3kEsseTvR8u51"
        );
        hexWifTest(
                "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEBAAEDCE6AF48A03BBFD25E8CD036413B",
                "5Km2kuu7vtFDPpxywn4u3NLpbr5jKpTB3jsuDU2KYEqetEoeLmv"
        );
        hexWifTest(
                "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEBAAEDCE6AF48A03BBFD25E8CD036413F",
                "5Km2kuu7vtFDPpxywn4u3NLpbr5jKpTB3jsuDU2KYEqetd9ZKJ4"
        );
        hexWifTest(
                "A56A6EBB7982FDD1FB7BF001BC9691C18C09E3B97561A7652B5789C8DAA5561E",
                "L2mFtSdGPpnA6FPR24W9Gnij6J9XF347bCDBFPBHQoQ2rtEn2HMh"
        );
        hexWifTest(
                "A56A6EBB7982FDD1FB7BF001BC9691C18C09E3B97561A7652B5789C8DAA5561E",
                "5K58uSHptqtTyvZGQoZRz3f12Cb6wqVoMSCNMUgVBq82nPrn8G5"
        );
        hexWifTest(
                "1E99423A4ED27608A15A2616A2B0E9E52CED330AC530EDCC32C8FFC6A526AEDD",
                "5J3mBbAH58CpQ3Y5RNJpUKPE62SQ5tfcvU2JpbnkeyhfsYB1Jcn"
        );
        hexWifTest(
                "1E99423A4ED27608A15A2616A2B0E9E52CED330AC530EDCC32C8FFC6A526AEDD",
                "KxFC1jmwwCoACiCAWZ3eXa96mBM6tb3TYzGmf6YwgdGWZgawvrtJ"
        );
    }

    @Test
    void address() {
        wifToAddress("5Jfe7EAN8aZGWEkcmCrCDu1kDsGwjNtzXgCzx81W4tGzzZ1PuWt", "1D4cD5YUQXjv29Bgu6XxnSe9yzwp1NT1Qs");
        wifToAddress("5KeoJvVa3aAqKa2kJmF8UGo2byDiQDjwUi5eDVPHLbSZdJSZs24", "1Q1xGXjB3oRw9RmPQwNAjTcQSnQj6dRzPa");
        wifToAddress("5HpXKzZMc5LtScUUmDxiASkqCcoBUCrKhmdxSwzqY1LfHapjCkJ", "1FZo6CWpeVrYzpcpm1DS3we8f4CMFAYQ26");
        wifToAddress("5KQdoEbq5JucnxYb2LjvfvfctrNHbUBN8UhiUqyPjPyPo5ydjzm", "1BtmVmsspuUtxTputARmw5YG2pB89sLujU");
        wifToAddress("5JxppgnQLtVxScw37yWeesv7mA4yRw6TWEBY6RmLC6avXD2E1wg", "1vCRNpMq7BvYom17qeSC8YoqsrpLTneVx");
        wifToAddress("5HpHagT65TZzG1PH3CSu63k8DbpvD8s5ip4nEB3kEsreAnchuDf", "1EHNa6Q4Jz2uvNExL497mE43ikXhwF6kZm");
        wifToAddress("5HpHagT65TZzG1PH3CSu63k8DbpvD8s5ip4nEB3kEsreAvUcVfH", "1LagHJk2FyCV2VzrNHVqg3gYG4TSYwDV4m");
        wifToAddress("5HpHagT65TZzG1PH3CSu63k8DbpvD8s5ip4nEB3kEsreB1FQ8BZ", "1NZUP3JAc9JkmbvmoTv7nVgZGtyJjirKV1");
        wifToAddress("5HpHagT65TZzG1PH3CSu63k8DbpvD8s5ip4nEB3kEsreB4AD8Yi", "1MnyqgrXCmcWJHBYEsAWf7oMyqJAS81eC");
        wifToAddress("KySotfQDS8cdXL9zJLMVP7pyYFjzswJDgUfMd7GfUBqmveRWAhZg", "1JGfC5seNDxfG93yaJ6UKBor87fw27HAW8");
        wifToAddress("5JKY1q85BE466xzmhdZHQ8tejRdvtsN7xASTRVcGWisvgPtHtYF", "1GvQrNgcqivoBPx93ZHWBUsnVs64qvjrFi");
        wifToAddress("L559f8A9cqLBtwc5m9mwsRtzkyYxakb7d33fk5Yp8SzFqaCnyHvq", "15395ruxq3YVWfdbiHSyYebR6HJb5vtaPa");
        wifToAddress("5KbULWiVgqZSaX3WdHVJsPrqtCTGmMJFBtDPNYPcbmQPr6S21py", "1AsjwqTJM4GghvLSXjKhiiWTuEYf8ACyyZ");

        hexToAddress("75", "16NUWSmWh7K6e885kybvaDdBoFQPZeDUpy", false);
        hexToAddress("01", "1EHNa6Q4Jz2uvNExL497mE43ikXhwF6kZm", false);
        hexToAddress(
                "18E14A7B6A307F426A94F8114701E7C8E774E7F9A47E2C2035DB29A206321725",
                "16UwLL9Risc3QfPqBUvKofHmBQ7wMtjvM",
                false
        );
        hexToAddress(
                "18E14A7B6A307F426A94F8114701E7C8E774E7F9A47E2C2035DB29A206321725",
                "1PMycacnJaSqwwJqjawXBErnLsZ7RkXUAs",
                true
        );
        hexToAddress("58956666fff75", "148SKWVLaAo8vLrRuD12Ci1ziZpjyU64gw", false);
        hexToAddress(
                "92282c97d36d10603648e79447cc104858642c208c733e76d8f32e186ba43a6c",
                "1Mbb599RmVgag4TbcmnWcTtF4qyej6bLyW",
                true
        );
        hexToAddress(
                "92282c97d36d10603648e79447cc104858642c208c733e76d8f32e186ba43a6c",
                "1H9Wu8EL87Z86bKnGo6jgb7hX9kGvEGyhi",
                false
        );
        hexToAddress("3c", "1AXSyzMK4Ft3HTmgmUQP62UBFwwCqyWwjg", true);
        hexToAddress("3c", "132vDcKDe96wL2GLLznueKnGVP4RnAX5xH", false);
        hexToAddress("2b981aef8cb5", "1Acom2hVc6QuL7hsRUQcENkfmfHorRSuzU", true);
        hexToAddress("2b981aef8cb5", "15ni7aCZWuyP6f1CJEWzVMabi7EsgFrJkG", false);
        hexToAddress(
                "15683AF3B43079485F9B28F3E39DCF5E81DDD41806C0F011C6916094CDEDD7D3",
                "1MrxDF7VekuPQj3L7XtGGmJbqXPUhpqbor",
                true
        );
        hexToAddress(
                "15683AF3B43079485F9B28F3E39DCF5E81DDD41806C0F011C6916094CDEDD7D3",
                "19ZSJbwPFDHbs8m2ok6p8Dng3kh6umVk8K",
                false
        );
        hexToAddress("E171929F0C", "14Wu4nRjjTNggBfoZ4Md8t1gZv788cdvqT", true);
        hexToAddress("E171929F0C", "1zrx8U7a9qoiqqNHF2j8bqzTYVZL2Y56p", false);
    }

    private static void hexWifTest(String hex, String wif) {
        KeyPair key = KeyPair.decode(wif);
        assertEquals(hex, key.d.toString(16).toUpperCase());
        assertEquals(wif, (new KeyPair(new BigInteger(hex, 16), key.publicKey.compressed)).encode());
    }

    private static void wifToAddress(String wif, String address) {
        assertEquals(address, KeyPair.decode(wif).getAddress());
    }

    private static void hexToAddress(String hex, String address, boolean compressed) {
        assertEquals(address, new KeyPair(new BigInteger(hex, 16), compressed).getAddress());
    }
}