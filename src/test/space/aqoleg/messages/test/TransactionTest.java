package space.aqoleg.messages.test;

import org.junit.jupiter.api.Test;
import space.aqoleg.keys.Address;
import space.aqoleg.keys.KeyPair;
import space.aqoleg.messages.Script;
import space.aqoleg.messages.Transaction;
import space.aqoleg.utils.Converter;

import java.math.BigInteger;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {

    @Test
    void test1() {
        String previousTransactionHash = "26c07ece0bce7cda0ccd14d99e205f118cde27e83dd75da7b141fe487b5528fb";
        String scriptSig = "48" + "30" + "45" + "02" + "20"
                + "2b7e37831273d74c8b5b1956c23e79acd660635a8d1063d413c50b218eb6bc8a" + "02" + "21"
                + "00a10a3a7b5aaa0f07827207daf81f718f51eeac96695cf1ef9f2020f21a0de02f" + "01" + "41" + "04"
                + "52684bce6797a0a50d028e9632be0c2a7e5031b710972c2a3285520fb29fcd4e"
                + "cfb5fc2bf86a1e7578e4f8a305eeb341d1c6fc0173e5837e2d3c7b178aade078";
        String scriptPubKey0 = "76" + "a9" + "14" + "3564a74f9ddb4372301c49154605573d7d1a88fe" + "88" + "ac";
        String scriptPubKey1 = "76" + "a9" + "14" + "010966776006953d5567439e5e39f86a0d273bee" + "88" + "ac";
        String input = "01000000" + "01" + previousTransactionHash + "00000000" + "8b" + scriptSig + "ffffffff" + "02"
                + "b06c191e01000000" + "19" + scriptPubKey0 + "00e1f50500000000" + "19" + scriptPubKey1 + "00000000";
        assertThrows(NullPointerException.class, () -> Transaction.parse(null));
        assertThrows(IndexOutOfBoundsException.class, () -> Transaction.parse(Converter.hexToBytes(scriptSig)));
        String string = "03000000";
        assertThrows(IndexOutOfBoundsException.class, () -> Transaction.parse(Converter.hexToBytes(string)));
        String string1 = "0100000000000000000000";
        assertThrows(UnsupportedOperationException.class, () -> Transaction.parse(Converter.hexToBytes(string1)));
        String string2 = "0100000009000000";
        assertThrows(IndexOutOfBoundsException.class, () -> Transaction.parse(Converter.hexToBytes(string2)));
        String string3 = string + "00";
        assertThrows(IndexOutOfBoundsException.class, () -> Transaction.parse(Converter.hexToBytes(string3)));

        Transaction transaction = Transaction.parse(Converter.hexToBytes(input));
        assertArrayEquals(Converter.hexToBytes(input), transaction.getPayload());
        assertEquals(1, transaction.getVersion());
        assertEquals(1, transaction.getInputsN());
        assertThrows(IndexOutOfBoundsException.class, () -> transaction.getInput(-9));
        assertEquals(0, transaction.getInput(0).index);
        assertEquals(
                previousTransactionHash,
                Converter.bytesToHex(transaction.getInput(0).getPreviousTransactionHash(true), false, false)
        );
        assertEquals(0, transaction.getInput(0).getPreviousOutIndex());
        assertEquals(scriptSig, Converter.bytesToHex(transaction.getInput(0).getScriptSig(), false, false));
        assertEquals(0xffffffff, transaction.getInput(0).getSequence());
        assertEquals(2, transaction.getOutputsN());
        assertThrows(IndexOutOfBoundsException.class, () -> transaction.getOutput(2));
        assertEquals(0, transaction.getOutput(0).index);
        assertEquals(1, transaction.getOutput(1).index);
        assertEquals("11e196cb0", Long.toHexString(transaction.getOutput(0).getValue()));
        assertEquals("5f5e100", Long.toHexString(transaction.getOutput(1).getValue()));
        assertEquals(scriptPubKey0, Converter.bytesToHex(transaction.getOutput(0).getScriptPubKey(), false, false));
        assertEquals(scriptPubKey1, Converter.bytesToHex(transaction.getOutput(1).getScriptPubKey(), false, false));
        assertEquals(0, transaction.getLockTime());

        assertThrows(NullPointerException.class, () -> transaction.getInput(0).verify(null));
        assertFalse(transaction.getInput(0).verify(Converter.hexToBytes(scriptPubKey0)));
        transaction.getInput(0).setScriptSig(null);
        assertThrows(UnsupportedOperationException.class, () -> transaction.getInput(0).verify(new byte[3]));
        assertThrows(UnsupportedOperationException.class, transaction::getPayload);
    }

    @Test
    void test2() {
        String previousScriptPubKey = "76a914010966776006953d5567439e5e39f86a0d273bee88ac";
        String previousTransactionHash = "eccf7e3034189b851985d871f91384b8ee357cd47c3024736e5676eb2debb3f2";
        String scriptSig = "49" + "30" + "46" + "02" + "21"
                + "009e0339f72c793a89e664a8a932df073962a3f84eda0bd9e02084a6a9567f75aa" + "02" + "21"
                + "00bd9cbaca2e5ec195751efdfac164b76250b1e21302e51ca86dd7ebd7020cdc06" + "01" + "41" + "04"
                + "50863ad64a87ae8a2fe83c1af1a8403cb53f53e486d8511dad8a04887e5b2352"
                + "2cd470243453a299fa9e77237716103abc11a1df38855ed6f2ee187e9c582ba6";
        String pubKeyHash = "097072524438d003d23a2f23edb65aae1bb3e469";
        String scriptPubKey = "76" + "a9" + "14" + pubKeyHash + "88" + "ac";
        String input = "01000000" + "01" + previousTransactionHash + "01000000" + "8c" + scriptSig + "ffffffff" + "01"
                + "605af40500000000" + "19" + scriptPubKey + "00000000";
        KeyPair keyPair = new KeyPair(new BigInteger("18E14A7B6A307F426A94F8114701E7C8E774E7F9A47E2C2035DB"
                + "29A206321725", 16), false);

        Transaction transaction = Transaction.parse(Converter.hexToBytes(input));
        assertArrayEquals(Converter.hexToBytes(input), transaction.getPayload());
        assertThrows(NullPointerException.class, () -> transaction.getInput(0).verify(null));
        assertFalse(transaction.getInput(0).verify(Converter.hexToBytes(scriptPubKey)));
        assertFalse(transaction.getInput(0).verify(null));
        assertTrue(transaction.getInput(0).verify(Converter.hexToBytes(previousScriptPubKey)));
        assertTrue(transaction.getInput(0).verify(null));

        assertThrows(
                NullPointerException.class,
                () -> transaction.addInput(null, true, 0, Converter.hexToBytes(previousScriptPubKey), keyPair)
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> transaction.addInput(new byte[34], true, 0, Converter.hexToBytes(previousScriptPubKey), keyPair)
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> transaction.addInput(new byte[32], true, -90, Converter.hexToBytes(previousScriptPubKey), keyPair)
        );
        assertEquals(1, transaction.addInput(Converter.hexToBytes(previousTransactionHash), true, 3,
                Converter.hexToBytes(previousScriptPubKey), keyPair));
        assertEquals(2, transaction.getInputsN());
        assertEquals(3, transaction.getInput(1).getPreviousOutIndex());
        assertEquals(
                previousTransactionHash,
                Converter.bytesToHex(transaction.getInput(1).getPreviousTransactionHash(true), false, false)
        );
        assertEquals(scriptSig, Converter.bytesToHex(transaction.getInput(0).getScriptSig(), false, false));

        assertThrows(UnsupportedOperationException.class, () -> transaction.addOutput(-10, new byte[19]));
        assertEquals(1, transaction.addOutput(99900000, Converter.hexToBytes(scriptPubKey)));
        assertEquals(2, transaction.getOutputsN());
        assertEquals(99900000, transaction.getOutput(1).getValue());
        assertEquals(scriptPubKey, Converter.bytesToHex(transaction.getOutput(1).getScriptPubKey(), false, false));

        assertThrows(
                UnsupportedOperationException.class,
                () -> transaction.addOutput(-10, Address.createFromPublicKey(keyPair.publicKey))
        );
        assertEquals(2, transaction.addOutput(9000, Address.createFromPublicKey(keyPair.publicKey)));
        assertEquals(3, transaction.getOutputsN());
        assertEquals(9000, transaction.getOutput(2).getValue());
        assertEquals(scriptPubKey, Converter.bytesToHex(transaction.getOutput(1).getScriptPubKey(), false, false));

        assertFalse(transaction.getInput(0).verify(null));
        assertThrows(UnsupportedOperationException.class, () -> transaction.getInput(1).verify(null));
        transaction.getPayload();
        assertFalse(transaction.getInput(0).verify(null));
        transaction.getInput(0).setKeyPair(keyPair);
        transaction.getPayload();
        assertTrue(transaction.getInput(0).verify(null));
        assertTrue(transaction.getInput(1).verify(null));
        assertFalse(transaction.getInput(0).verify(Converter.hexToBytes(scriptPubKey)));
        assertFalse(transaction.getInput(1).verify(Converter.hexToBytes(scriptPubKey)));
    }

    // ab750975b023bc6aff0db899c188015c09e6255edeaa00a9674961a87964c365
    @Test
    void test3() {
        String hex = "01000000" + "01" + "b5bdf03533a8861c9d6c81106573a3298e56f0b16eb42ca2c3da209f28819ed0"
                + "00000000" + "8c" + "49" + "30" + "46" + "02" + "21"
                + "00c841d4ccaaf879251c809dc683615ec55ff475769fe6d8c2297d4f0b094dd96b" + "02" + "21"
                + "00e071d73aa964740c8123f54b95b12a1f52b9185c1d91a56c9f86eea8584c9cc4" + "01" + "41" + "04"
                + "5ab78884ec84e8efb6d5ce4d5c9a4458d00b87cd02b8c1cf108439ca0c192e5c"
                + "6eca9d0deaef6c7e79b7144d26327a5c09b806496689b554f0b6e31284d2ca9e" + "ffffffff" + "01"
                + "80f0fa0200000000" + "19" + "76" + "a9" + "14" + "6f74fc0e7d0cd8fb6930a82a7914471dcee38d78"
                + "88" + "ac" + "00000000";
        byte[] previousScriptPublicKey = Converter.hexToBytes("76a914006c82c93dce51c6e5ffb4e425e13feea50d14e288ac");
        byte[] previousTransactionHash = Converter.hexToBytes(
                "d09e81289f20dac3a22cb46eb1f0568e29a3736510816c9d1c86a83335f0bdb5");

        Transaction transaction = Transaction.parse(Converter.hexToBytes(hex));
        assertArrayEquals(
                Converter.hexToBytes("ab750975b023bc6aff0db899c188015c09e6255edeaa00a9674961a87964c365"),
                Transaction.getHash(transaction.getPayload(), false)
        );
        assertArrayEquals(
                previousTransactionHash,
                transaction.getInput(0).getPreviousTransactionHash(false)
        );
        assertTrue(transaction.getInput(0).verify(previousScriptPublicKey));
        KeyPair keyPair = new KeyPair(BigInteger.ONE, false);
        transaction.getInput(0)
                .setPreviousScriptPubKey(Script.createScriptPubKey(Address.createFromPublicKey(keyPair.publicKey)));
        transaction.getInput(0).setKeyPair(keyPair);
        assertFalse(transaction.getInput(0).verify(null));
        transaction.getPayload();
        assertTrue(transaction.getInput(0).verify(null));
        transaction.getInput(0).setPreviousScriptPubKey(null);
        assertThrows(NullPointerException.class, () -> transaction.getInput(0).verify(null));
        transaction.getInput(0).setScriptSig(Converter.hexToBytes("49" + "30" + "46" + "02" + "21"
                + "00c841d4ccaaf879251c809dc683615ec55ff475769fe6d8c2297d4f0b094dd96b" + "02" + "21"
                + "00e071d73aa964740c8123f54b95b12a1f52b9185c1d91a56c9f86eea8584c9cc4" + "01" + "41" + "04"
                + "5ab78884ec84e8efb6d5ce4d5c9a4458d00b87cd02b8c1cf108439ca0c192e5c"
                + "6eca9d0deaef6c7e79b7144d26327a5c09b806496689b554f0b6e31284d2ca9e"));
        assertTrue(transaction.getInput(0).verify(previousScriptPublicKey));
    }

    // f4566c830badbc622a26ff29faf5bcde01078e1b4c6a900f470409f92196030e
    @Test
    void test4() {
        byte[] previousScriptPublicKey = Converter.hexToBytes("76a914d655b65519dca2553aade471971ddbfc7f531ad188ac");
        byte[] previousTransactionHash =
                Converter.hexToBytes("47ef237ab882546d32216b9b4a5e92b8969d5c7e73cd281fb0da95bbac881daf");
        String hex = "01000000" + "01" + "af1d88acbb95dab01f28cd737e5c9d96b8925e4a9b6b21326d5482b87a23ef47"
                + "00000000" + "8a" + "47" + "30" + "44" + "02" + "20"
                + "243419ecb20f8b9a23ba107d66199637f1aeaadb6a13a7582132dd8b8dd26884" + "02" + "20"
                + "2a8168e44da4b3e272ff22bda065d9fb2f06df5fb581af3c00e6c7247f751f59" + "01" + "41" + "04"
                + "9da5a1a2c2e415d9fe2b1cc449b79ff41b47cbbe9ee66e82857b913bf7632828"
                + "dce4dfe0cee38d258cdd60a19b22e9a3a4769c4300ba837a4abbf34251c2b5b2" + "ffffffff" + "01"
                + "00e8764817000000" + "19" + "76" + "a9" + "14" + "c066f19518e5b6aac18899b4004fb62e0cd8bc7d"
                + "88" + "ac" + "00000000";

        Transaction transaction = Transaction.parse(Converter.hexToBytes(hex));
        assertEquals(hex, Converter.bytesToHex(transaction.getPayload(), false, false));
        assertEquals(
                "f4566c830badbc622a26ff29faf5bcde01078e1b4c6a900f470409f92196030e",
                Converter.bytesToHex(Transaction.getHash(transaction.getPayload(), false), false, false)
        );
        assertEquals(1, transaction.getInputsN());
        assertArrayEquals(previousTransactionHash, transaction.getInput(0).getPreviousTransactionHash(false));
        assertEquals(0, transaction.getInput(0).getPreviousOutIndex());
        assertEquals(100000000000L, transaction.getOutput(0).getValue());
        assertArrayEquals(
                Converter.hexToBytes("76a914c066f19518e5b6aac18899b4004fb62e0cd8bc7d88ac"),
                transaction.getOutput(0).getScriptPubKey()
        );
        assertTrue(transaction.getInput(0).verify(previousScriptPublicKey));
    }

    // f4184fc596403b9d638783cf57adfe4c75c605f6356fbc91338530e9831e9e16
    @Test
    void test5() {
        String hex = "01000000" + "01" + "c997a5e56e104102fa209c6a852dd90660a20b2d9c352423edce25857fcd3704"
                + "00000000" + "48" + "47" + "30" + "44" + "02" + "20"
                + "4e45e16932b8af514961a1d3a1a25fdf3f4f7732e9d624c6c61548ab5fb8cd41" + "02" + "20"
                + "181522ec8eca07de4860a4acdd12909d831cc56cbbac4622082221a8768d1d09" + "01" + "ffffffff" + "02"
                + "00ca9a3b00000000" + "43" + "41" + "04"
                + "ae1a62fe09c5f51b13905f07f06b99a2f7159b2225f374cd378d71302fa28414"
                + "e7aab37397f554a7df5f142c21c1b7303b8a0626f1baded5c72a704f7e6cd84c" + "ac"
                + "00286bee00000000" + "43" + "41" + "04"
                + "11db93e1dcdb8a016b49840f8c53bc1eb68a382e97b1482ecad7b148a6909a5c"
                + "b2e0eaddfb84ccf9744464f82e160bfa9b8b64f9d4c03f999b8643f656b412a3" + "ac" + "00000000";
        String previousScriptPublicKey = "41" + "04"
                + "11db93e1dcdb8a016b49840f8c53bc1eb68a382e97b1482ecad7b148a6909a5c"
                + "b2e0eaddfb84ccf9744464f82e160bfa9b8b64f9d4c03f999b8643f656b412a3" + "ac";
        Transaction transaction = Transaction.parse(Converter.hexToBytes(hex));
        assertEquals(1, transaction.getInputsN());
        assertEquals(0, transaction.getInput(0).getPreviousOutIndex());
        assertEquals(
                "0437cd7f8525ceed2324359c2d0ba26006d92d856a9c20fa0241106ee5a597c9",
                Converter.bytesToHex(transaction.getInput(0).getPreviousTransactionHash(false), false, false)
        );
        assertEquals(2, transaction.getOutputsN());
        assertEquals(1000000000, transaction.getOutput(0).getValue());
        assertEquals(4000000000L, transaction.getOutput(1).getValue());
        assertEquals(
                "41" + "04" + "ae1a62fe09c5f51b13905f07f06b99a2f7159b2225f374cd378d71302fa28414"
                        + "e7aab37397f554a7df5f142c21c1b7303b8a0626f1baded5c72a704f7e6cd84c" + "ac",
                Converter.bytesToHex(transaction.getOutput(0).getScriptPubKey(), false, false)
        );
        assertEquals(
                "41" + "04" + "11db93e1dcdb8a016b49840f8c53bc1eb68a382e97b1482ecad7b148a6909a5c"
                        + "b2e0eaddfb84ccf9744464f82e160bfa9b8b64f9d4c03f999b8643f656b412a3" + "ac",
                Converter.bytesToHex(transaction.getOutput(1).getScriptPubKey(), false, false)
        );
        assertEquals(
                "f4184fc596403b9d638783cf57adfe4c75c605f6356fbc91338530e9831e9e16",
                Converter.bytesToHex(Transaction.getHash(transaction.getPayload(), false), false, false)
        );
        assertEquals(
                "169e1e83e930853391bc6f35f605c6754cfead57cf8387639d3b4096c54f18f4",
                Converter.bytesToHex(Transaction.getHash(transaction.getPayload(), true), false, false)
        );

        assertTrue(transaction.getInput(0).verify(Converter.hexToBytes(previousScriptPublicKey)));
        KeyPair keyPair = new KeyPair(BigInteger.TEN, false);
        transaction.getInput(0).setKeyPair(keyPair);
        transaction.getInput(0)
                .setPreviousScriptPubKey(Script.createScriptPubKey(Address.createFromPublicKey(keyPair.publicKey)));
        transaction.getPayload();
        assertTrue(transaction.getInput(0).verify(null));
    }

    @Test
    void test6() {
        KeyPair key0 = new KeyPair(new BigInteger(250, new Random()), false);
        KeyPair key1 = new KeyPair(new BigInteger(250, new Random()), false);
        KeyPair key2 = new KeyPair(new BigInteger(250, new Random()), false);

        Transaction transaction0 = new Transaction();
        assertEquals(0, transaction0.addOutput(800000, Address.createFromPublicKey(key0.publicKey)));
        assertEquals(1, transaction0.addOutput(1800000, Address.createFromPublicKey(key1.publicKey)));
        assertEquals(2, transaction0.addOutput(1800000, Address.createFromPublicKey(key2.publicKey)));
        assertArrayEquals(
                Script.createScriptPubKey(Address.createFromPublicKey(key1.publicKey)),
                transaction0.getOutput(1).getScriptPubKey()
        );
        byte[] previousTransactionHash = Transaction.getHash(transaction0.getPayload(), true);

        Transaction transaction = new Transaction();
        transaction.addInput(previousTransactionHash, true, 0, transaction0.getOutput(0).getScriptPubKey(), key0);
        transaction.addInput(previousTransactionHash, true, 1, transaction0.getOutput(1).getScriptPubKey(), key1);
        transaction.addInput(previousTransactionHash, true, 2, transaction0.getOutput(2).getScriptPubKey(), key2);
        transaction.addOutput(8, key1.publicKey.getHash());
        assertEquals(3, transaction.getInputsN());
        assertNull(transaction.getInput(0).getScriptSig());
        transaction.getInput(0).setScriptSig(previousTransactionHash);
        assertArrayEquals(previousTransactionHash, transaction.getInput(0).getScriptSig());
        assertArrayEquals(previousTransactionHash, transaction.getInput(0).getPreviousTransactionHash(true));

        transaction.getPayload();
        assertTrue(transaction.getInput(0).verify(null));
        assertTrue(transaction.getInput(1).verify(null));
        assertTrue(transaction.getInput(2).verify(null));
        assertFalse(transaction.getInput(0).verify(transaction0.getOutput(1).getScriptPubKey()));
        assertFalse(transaction.getInput(1).verify(transaction0.getOutput(0).getScriptPubKey()));
    }

    // 0ba584aee9d010c2ac0e9b2928a2a7f8daaaf5a7cf1bde7109608078ef4a5e82
    @Test
    void test7() {
        String input = "01000000" + "01" + "c1784fc223bddc405fe4cab0f964e8c38dab94ef147d7cada05701075578661e"
                + "01000000" + "db" + "00" + "48" + "30" + "45" + "02" + "21"
                + "00d6919cc764f7b7678ce8c464fb59eefe24a2fa52542ffb0392e4135e1277f0a1" + "02" + "20"
                + "56ae99079ef3389c053c4b9002c0c89524a611fb3e8bc52fbb41b2fe0e3db424" + "01" + "483045" + "02" + "21"
                + "00a4915c1023dc58efd8b73f43367d464aa2fd7e19782c08fc1d504e79519acb36" + "02" + "20"
                + "25a7c02f0c3e51260c1b4acc7526bcd3a218b3ddb1fff164458e8f5a8a53d248" + "01" + "47" + "52" + "21"
                + "030c8e9b96d5eeb8388138b8e7ee8365cb7a1c018f20f44aaec9b61d0bfc2c92e2" + "21"
                + "02484e57100db300868a2f219b0da97402a7147de8d10edf0e41b80ec4b197a227" + "52" + "ae" + "ffffffff"
                + "02" + "9048090000000000" + "19" + "76" + "a9" + "14"
                + "1b6a4a5cd55089df835f436d6c91bbe852984317" + "88" + "ac" + "6be9da0d00000000" + "17" + "a9" + "14"
                + "c9e543738d1781bba80058ec693596660d9a25c8" + "87" + "00000000";

        Transaction transaction = Transaction.parse(Converter.hexToBytes(input));
        assertEquals(
                "0ba584aee9d010c2ac0e9b2928a2a7f8daaaf5a7cf1bde7109608078ef4a5e82",
                Converter.bytesToHex(Transaction.getHash(transaction.getPayload(), false), false, false)
        );
        assertTrue(transaction.getInput(0).verify(Converter.hexToBytes("a914c9e543738d1781bba80058ec693596660d9a25c887")));
        assertFalse(transaction.getInput(0).verify(Converter.hexToBytes("a914c9e543738d1781bba40058ec693596660d9a25c887")));
    }

    // 8d2dd519e57969e8c8b2a10fc3fb7ef6777d21e4219944700ee28e05512e0780
    @Test
    void test8() {
        String input = "01000000" + "02" + "12224fedc9ba531702804e8aa5145a85aa182ef4a62d530a790b85433001d45e"
                + "01000000" + "da" + "00" + "48" + "30" + "45" + "02" + "21"
                + "00849be6cdb19c4dd33b1097d02462985bfad039e71c3923e6c302c27a4c069660" + "02" + "20"
                + "411a4ece7b2dc5c8e3123836492b24fd50eb170e7bb2c9107f961ebbe7c23e59" + "01" + "47" + "30" + "440220"
                + "246fca1e4946f7968ef596640047d4bbf9861d3010864a0105a6459c9648f20b" + "02" + "20"
                + "55ec46872947e60796c630935d5fcaf917bf91c6a3cdbcfe721739c09eae9ad2" + "01" + "4752" + "21" + "03"
                + "5e9808c74b7b812d9d3cc2db1787a42ab46869c2de5c1d99696c1f3f85deff49" + "21" + "03"
                + "ec106f147e75c0effb610cd42d4d229ac5cf24c0b55ae569404dbab880f7f1f9" + "52" + "ae" + "ffffffff"
                + "fcc5369251abe83799154ac8f881d4bc903f5ef8d53ace340d6ba147ad0c923c" + "01000000" + "da" + "00"
                + "47" + "30" + "44" + "02" + "20"
                + "7c7110f7f9ff2548b56328018ae6c522758a243ae609e330a41b8cc3e06de6f6" + "02" + "20"
                + "0f1f250882164ad640522b4b13dd07476ec5bcb4aebdd553d645b3a52ccfe3e7" + "01" + "48" + "30" + "450221"
                + "00c22eb278471556dc853c6bf1112d7fd41a56e0c5711595297d3cb6ee4fd88ad1" + "02" + "20"
                + "19e55b0436b62c90b9d84fdc20ae6f0275942d9d9101118b6d4e4d0e29ecce0a" + "01" + "4752" + "21" + "03"
                + "d095e25caf99eac040eb89313a67c73388682d5aa6f21e15cdc56726841568dd" + "21" + "03"
                + "ec106f147e75c0effb610cd42d4d229ac5cf24c0b55ae569404dbab880f7f1f9" + "52" + "ae" + "ffffffff" + "02"
                + "80841e0000000000" + "17" + "a9" + "14" + "8ff083bd257ce6dda22b387caf67acd8a5739cce" + "87"
                + "55181c0000000000" + "17" + "a9" + "14" + "c641cece9f569f48f2dc98b8bee2665cb7b85065" + "87"
                + "00000000";
        Transaction transaction = Transaction.parse(Converter.hexToBytes(input));
        assertEquals(
                "8d2dd519e57969e8c8b2a10fc3fb7ef6777d21e4219944700ee28e05512e0780",
                Converter.bytesToHex(Transaction.getHash(transaction.getPayload(), false), false, false)
        );
        assertEquals(input, Converter.bytesToHex(transaction.getPayload(), false, false));
        assertEquals(2, transaction.getInputsN());
        assertEquals(0, transaction.getInput(0).index);
        assertEquals(
                "12224fedc9ba531702804e8aa5145a85aa182ef4a62d530a790b85433001d45e",
                Converter.bytesToHex(transaction.getInput(0).getPreviousTransactionHash(true), false, false)
        );
        assertEquals(1, transaction.getInput(0).getPreviousOutIndex());
        assertEquals(1, transaction.getInput(1).index);
        assertEquals(
                "fcc5369251abe83799154ac8f881d4bc903f5ef8d53ace340d6ba147ad0c923c",
                Converter.bytesToHex(transaction.getInput(1).getPreviousTransactionHash(true), false, false)
        );
        assertEquals(1, transaction.getInput(1).getPreviousOutIndex());
        assertEquals(2, transaction.getOutputsN());
        assertEquals(0, transaction.getOutput(0).index);
        assertEquals(2000000, transaction.getOutput(0).getValue());
        assertEquals(
                "a9" + "14" + "8ff083bd257ce6dda22b387caf67acd8a5739cce" + "87",
                Converter.bytesToHex(transaction.getOutput(0).getScriptPubKey(), false, false)
        );
        assertEquals(1, transaction.getOutput(1).index);
        assertEquals(1841237, transaction.getOutput(1).getValue());
        assertEquals(
                "a9" + "14" + "c641cece9f569f48f2dc98b8bee2665cb7b85065" + "87",
                Converter.bytesToHex(transaction.getOutput(1).getScriptPubKey(), false, false)
        );

        assertTrue(transaction.getInput(0).verify(Converter.hexToBytes("a914974faf9268d0d0bb2901d28843d3a1b9d12ab0ba87")));
        assertFalse(transaction.getInput(1).verify(Converter.hexToBytes("a914974faf9268d0d0bb2901d28843d3a1b9d12ab0ba87")));
        assertTrue(transaction.getInput(1).verify(Converter.hexToBytes("a914c641cece9f569f48f2dc98b8bee2665cb7b8506587")));
    }

    // 92f034f515e1eba78eb745de1da4d8536908b14ad076370175d47eefe143ac6e
    @Test
    void test9() {
        String input = "01000000" + "09"
                + "0de329b2a25d72fde27013d2718c6cb078e57ab7749efe1abf0a6912d5c80180" + "00000000" + "494830450221"
                + "0097d5d337f0df407ef70356c9f12b23365f6ce922fbeadfe6aa14088aa9216d13" + "0220"
                + "30aac4e96ab8417329c35dadc992b7e53099b293fe4002b5b35916620f6e8b5a" + "01" + "ffffffff"
                + "5db6a0ea031e880e85a4b46571934ff972d252c13d08801ae3359405084ebf82" + "00000000" + "4a4930460221"
                + "00aa842a89059b1661be82c821cb2c0aea31aa03f1212b7627e33cc6df1523e407" + "0221"
                + "00b54e3a5bb6ab72c1312a873da49ecb729a2f6ca0856ba1c288f8e65e40069da3" + "01" + "ffffffff"
                + "42f76e4c0f3afe68226f15030bb5111fd9ce0e3f8c59a6a85f50ddc62a60b0f3" + "00000000" + "494830450221"
                + "00cd00b05beabefb0194fe35f212d63208955fd27b3ae4c0e7370f055ee30a262d" + "0220"
                + "5a30022e545c66af9bb9a2f628a6c0174e135ee55ae591492fc30b60027db536" + "01" + "ffffffff"
                + "0d286ba530dc03190a736402cdb0e5c48082c96b0a1f8b4fe3e1f9f2480d9a30" + "00000000" + "4a4930460221"
                + "00f5908bb5787e65daf15de64fcd6e530ae1896bf6aef7df81a2a8f684e6f343d6" + "0221"
                + "009d703f753af931ce6ac498ce84ad7d210afbdc052bc3bcce05d80251d1b2500d" + "01" + "ffffffff"
                + "087385a829612541abe4ba4f01ef57b1a0be08599e783b1a4e1164ca70a77da4" + "00000000" + "484730440220"
                + "1b037a08dbc929f325690f44e27841629611f94da6cf92748a79d3ec23be1868" + "0220"
                + "2a98a56715fb1085c5909630491cae80e42f3e5ffbbd9e6f89b69193c0bf3b50" + "01" + "ffffffff"
                + "953800e0dc8418a7b05964bd3c276c69679e8ebbd8e5f2605a248817a293d02e" + "00000000" + "484730440220"
                + "3c3347c1562dd1ab221f817201dc138350f98fd164998a5f5f2c6071931004dd" + "0220"
                + "28402f37e1da385783bb34531b71ae7db09f425ed299b23048cc2bd44d1ecf27" + "01" + "ffffffff"
                + "a30c3703546b3e669780a505df2b27b0694003cbe50ffd8da56802026fc1111b" + "00000000" + "494830450220"
                + "41e8d8d756975321e906ae638329b68fbc75edfcc168878aa70d93a5d66dc776" + "0221"
                + "0086fefbd68772c1821b29bb151e7040404ac0a71875b6ed3b3011f0a6842863f9" + "01" + "ffffffff"
                + "2b0637d1b59e8d5577be38d8f2229557043cb1dca809de88029d3c4e2185dbc8" + "00000000" + "494830450221"
                + "00c786967b1b78bd6e5bf2b01a422cc0f5e5d26f7e2b38239a69dcc03ec56a2e81" + "0220"
                + "37ea0b7cb4fb7f0cd2694050430a634c17c49de2a4bede2099cca37e832b262e" + "01" + "ffffffff"
                + "04239c3b0fd6f20acffb561fe03682c8b6e511fca6bacb57fdad4c28e8cdb688" + "00000000" + "4a4930460221"
                + "008f4283327d8f8e55ed6b179291315806be5d53f177d0024a926093e169d19c4e" + "0221"
                + "00dc594240c70c7f38195462f622864948cdf415f7e0b04adb413bf01e71df73b1" + "01" + "ffffffff"
                + "01" + "0082357a0a000000" + "1976a9145fe963e6d08693fb8adf5813e9788a865aa60f5a88ac" + "00000000";
        Transaction transaction = Transaction.parse(Converter.hexToBytes(input));
        assertArrayEquals(
                Converter.hexToBytes("92f034f515e1eba78eb745de1da4d8536908b14ad076370175d47eefe143ac6e"),
                Transaction.getHash(transaction.getPayload(), false)
        );

        assertTrue(transaction.getInput(0).verify(Converter.hexToBytes("4104"
                + "b3efd845332bdb71b62e16ae6110095a27f603fbf153a29840a12c8c5afe675c"
                + "cf3d07ff18b9c7a8489eb0f7c0a060d67bbdb035cd34a801cba83c7cdcb23799" + "ac")));
        assertTrue(transaction.getInput(1).verify(Converter.hexToBytes("4104"
                + "e75abcdae3bc7074253791c13b4fa142149c3bcd93ed1dd923bef74c88ebbe0f"
                + "da614a90fad028a0ab28c55527f33806a9de41338e727b2bbdbb4ef867a880ac" + "ac")));
        assertTrue(transaction.getInput(2).verify(Converter.hexToBytes("4104"
                + "42a2d4418e71b34fd3a6b6a7c992c2c29ae2eed3c71ca36963cf16ff0b429c3d"
                + "a4ddf0ca13af3d067be9e3462819a4f9028b1c30c2b3a0931d905827a80a49fa" + "ac")));
        assertTrue(transaction.getInput(3).verify(Converter.hexToBytes("4104"
                + "c508f9ea397e4163fbceb3b76904efbf30c0a00e08cfff684ce8e01b8c95952e"
                + "d93fb906f8e55a0f5d0ef1d61e260d8c51bf93c85e042a5fd0a1116ab39e0d8d" + "ac")));
        assertTrue(transaction.getInput(4).verify(Converter.hexToBytes("4104"
                + "32e0a28b95c6562e042bce5f5dce266871d426b2d8ea396c0d1ec44d988ff9ea"
                + "e3b1e64b46974bb3c5495eddb9f81bbef1b79aa3270d5c0e29a087d606589e5d" + "ac")));
        assertTrue(transaction.getInput(5).verify(Converter.hexToBytes("4104"
                + "b61fb4c35c8f6506b6e46def9d7a8bcd1d32f864364c2dff7ffe93d5f26db5c1"
                + "5230b9d99f541cf815ce3181f4a1e5a7d76be2f9c12de01fcf6ce7b11e7f8def" + "ac")));
        assertTrue(transaction.getInput(6).verify(Converter.hexToBytes("4104"
                + "8bdaeae45b729d9e966410967d4cbdf59c13de6e10327973c53e66e599f688ba"
                + "6bfdaaa62c3c5360d853cac4b63a8d5a6c677a7ea250f74b26abb0163e1f2ee1" + "ac")));
        assertTrue(transaction.getInput(7).verify(Converter.hexToBytes("4104"
                + "7a548d5e626231f2b208d629a7d4ed2f6a9a130b73aa53f90b682b34cb1834bc"
                + "f9b77e90223273843fb9bd0520dd2f2b015c98414f3e9233d7deba962e32ab1e" + "ac")));
        assertTrue(transaction.getInput(8).verify(Converter.hexToBytes("4104"
                + "e95fb10b0294dfc0a3330bf570ab3eb716965060cc337d87e0e5f3d7ada279f3"
                + "b348209e996ed2c20eb4f8582b8d04297dd5fdce79e4d2d459cc730359ec0640" + "ac")));
    }

    // ccd6e2dc6a4cd1dd2c49cac57df35efb42e90001c957e9f7dccb62be82f84b85
    @Test
    void test10() {
        String input = "02000000017d2ef88379332a28db8d193a70dc578409315cab616817b26a820beaf4ea9ff3"
                + "010000006a473044022032b5f03c85b36c4572658f949f6e0424be73e8c58ad65597223660c55257cc87"
                + "02204e66610ae09e7a8169e788e40984455218cd838960b05941e7ed60785cede18b0121"
                + "0351c4409af20383348424a49ad5d802852d312db93a66f7409f8ccf877b7dc6a9feffffff"
                + "02af576401000000001976a9143bd7927356c4e91977b24f2ae1533cc30f3552ef88ac"
                + "cc16eb02000000001976a9144d4fb017923df75bd9e92affdf83e2d4e3a18bf188ac55fb0700";
        Transaction transaction = Transaction.parse(Converter.hexToBytes(input));
        assertArrayEquals(
                Converter.hexToBytes("ccd6e2dc6a4cd1dd2c49cac57df35efb42e90001c957e9f7dccb62be82f84b85"),
                Transaction.getHash(transaction.getPayload(), false)
        );
        assertTrue(transaction.getInput(0).verify(Converter.hexToBytes("76a914"
                + "3734035ccca1ea38fd502688c01336571b285c7388ac")));
        assertFalse(transaction.getInput(0).verify(Converter.hexToBytes("76a914"
                + "3734035dcca1ea38fd502688c01336571b285c7388ac")));
        transaction.getInput(0).setScriptSig(Converter.hexToBytes("4730440220" +
                "32b5f03c85b36c4572658f949f6e7424be73e8c58ad65597223660c55257cc87" + // different r
                "02204e66610ae09e7a8169e788e40984455218cd838960b05941e7ed60785cede18b0121" +
                "0351c4409af20383348424a49ad5d802852d312db93a66f7409f8ccf877b7dc6a9"));
        assertFalse(transaction.getInput(0).verify(Converter.hexToBytes("76a914"
                + "3734035ccca1ea38fd502688c01336571b285c7388ac")));
    }

    // fc2a67befa5e18c0a4555b0c46f1fec90efe497860cc4e227a1f94ad682fc093
    @Test
    void test11() {
        String input = "01000000015003ed57f7c134809f489c9c32bb4dc5f5030cdbe8988c9792c3335e977f548c"
                + "010000008a473044022037bc90f4a4122af7b0884f5420d71183c0a03ab677b3d0942c645da94c967ed6"
                + "02205c8d658ff03d2fa3d3c215e1507cd73962db0389eeca747dde5ac374f7fe5afe0141"
                + "044762f413e4d1d46ebf3d66218e69ae8f24f5c840f0ccb1418c1f00cacb71e0a2"
                + "f3d07446f1a6e137f9d876c897fc539652a54aea90a1a4ae6f57b41ef02a4084ffffffff02"
                + "80d4d925010000001976a914f5407f390cedf7eb1b47dfa829b4896bbd851a1e88ac"
                + "00751903000000001976a914f1aac41c8717fab2f907958b1fd7bc390870fe4a88ac00000000";
        Transaction transaction = Transaction.parse(Converter.hexToBytes(input));
        assertArrayEquals(
                Converter.hexToBytes("fc2a67befa5e18c0a4555b0c46f1fec90efe497860cc4e227a1f94ad682fc093"),
                Transaction.getHash(transaction.getPayload(), false)
        );
        assertTrue(transaction.getInput(0).verify(Converter.hexToBytes("76a914"
                + "31c8e63a5f47fc4d9c02bdb1fd99c0a21b6c135688ac")));
        assertFalse(transaction.getInput(0).verify(Converter.hexToBytes("76a914"
                + "3734035dcca1ea38fd502688c01336571b285c7388ac")));
    }

    // 3d0126be7eef6aa33430349e5a265e14e96f6b81e2a04afeb39cfb4cf89de7b2
    @Test
    void test12() {
        String input = "0100000003081ca23eb293a8e2b21ca90151700eb33aeb73111874ff606b427074f2c6dbca"
                + "010000006b483045022100961cc74cd7f8c35fc68c953ee6d560ffb1d4924038d358d3615365963a4d72dd"
                + "02204041b9c8c39f0ba1b39f83cc59d0bac25050fcc8da5588509192c7ab1b028c080121"
                + "0278176b0c473f632e2d7a26364102de073229558bc6d263f7015b79c6faa59826ffffffff"
                + "617d552a23ca76b23a88bb924afcb145fa2ac50cce4349e3b03b8291c15d1d55"
                + "020000006b483045022100bcecb73afb03b8dee21f308008bcb0ad1fcb8401ed04b787048965f4888eb3cc"
                + "02200bb32b6a7c3515ae59a2e5b6f6ab96e3611dbe3c6adbf63ddaf87e698473d37e0121"
                + "03ec639464f4eb494cd6174475c6c9e66ad9dc1818f0be6a21b4d4bb08b128ec1effffffff"
                + "0cac4622b828c5d3a9ea52e875549e7dd501c11ae3116ab92d4f8241cb87528b"
                + "040000006b48304502210088e443275573a92a858e79e710ae89fc18b7769f5817a0021c4bd8a38aeba600"
                + "022024608f27ca9dbbbc40616f50f9e566274a2e74e760327bcb9d2b9f3eaa920e4a0121"
                + "02b3282d55a82fb99cc4dd3ff5dfd14b5922f4435ba5320ad7dedd80d2b38e86f6ffffffff"
                + "02002f6859000000001976a9140dc9274e11fee401824a8afb7ea20886dc62cff388ac"
                + "20aa4400000000001976a914d1db06308213dae36794a8bc189b1f067217906f88ac00000000";
        Transaction transaction = Transaction.parse(Converter.hexToBytes(input));
        assertArrayEquals(
                Converter.hexToBytes("3d0126be7eef6aa33430349e5a265e14e96f6b81e2a04afeb39cfb4cf89de7b2"),
                Transaction.getHash(transaction.getPayload(), false)
        );
        String prevScriptPubKey0 = "76a914a03a50186db031b04b9ffdc7fa164093af0a22c588ac";
        String prevScriptPubKey1 = "76a914722e11430b858b985da4e6eba74777ab41abbcd888ac";
        String prevScriptPubKey2 = "76a91406b67532a580222668640f06aca0f4ac5d34e44888ac";
        assertTrue(transaction.getInput(0).verify(Converter.hexToBytes(prevScriptPubKey0)));
        assertFalse(transaction.getInput(1).verify(Converter.hexToBytes(prevScriptPubKey0)));
        assertFalse(transaction.getInput(2).verify(Converter.hexToBytes(prevScriptPubKey0)));
        assertTrue(transaction.getInput(1).verify(Converter.hexToBytes(prevScriptPubKey1)));
        assertFalse(transaction.getInput(0).verify(Converter.hexToBytes(prevScriptPubKey1)));
        assertFalse(transaction.getInput(2).verify(Converter.hexToBytes(prevScriptPubKey1)));
        assertTrue(transaction.getInput(2).verify(Converter.hexToBytes(prevScriptPubKey2)));
        assertFalse(transaction.getInput(0).verify(Converter.hexToBytes(prevScriptPubKey2)));
        assertFalse(transaction.getInput(1).verify(Converter.hexToBytes(prevScriptPubKey2)));
    }
}