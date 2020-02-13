package space.aqoleg.messages.test;

import org.junit.jupiter.api.Test;
import space.aqoleg.crypto.Ripemd160;
import space.aqoleg.crypto.Sha256;
import space.aqoleg.keys.Address;
import space.aqoleg.keys.KeyPair;
import space.aqoleg.keys.PublicKey;
import space.aqoleg.messages.Script;
import space.aqoleg.utils.Converter;

import java.math.BigInteger;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class ScriptTest {

    @Test
    void determineType() {
        assertThrows(NullPointerException.class, () -> Script.determineType(null));

        String input = "41" + "04" + "11db93e1dcdb8a016b49840f8c53bc1eb68a382e97b1482ecad7b148a6909a5c"
                + "b2e0eaddfb84ccf9744464f82e160bfa9b8b64f9d4c03f999b8643f656b412a3" + "ac";
        assertEquals(Script.p2pk, Script.determineType(Converter.hexToBytes(input)));
        input = "21" + "03" + "11db93e1dcdb8a016b49840f8c53bc1eb68a382e97b1482ecad7b148a6909a5c" + "ac";
        assertEquals(Script.p2pk, Script.determineType(Converter.hexToBytes(input)));
        input = "22" + "03" + "11db93e1dcdb8a016b49840f8c53bc1eb68a382e97b1482ecad7b148a6909a5c11" + "ac";
        assertEquals(Script.undefined, Script.determineType(Converter.hexToBytes(input)));
        input = "21" + "03" + "11db93e1dcdb8a016b49840f8c53bc1eb68a382e97b1482ecad7b148a6909a5c" + "a1";
        assertEquals(Script.undefined, Script.determineType(Converter.hexToBytes(input)));
        input = "21" + "03" + "11db93e1dcdb8a016b49840f8c53bc1eb68a382e97b1482ecad7b148a6909a5c" + "ac" + "ac";
        assertEquals(Script.undefined, Script.determineType(Converter.hexToBytes(input)));

        input = "76" + "a9" + "14" + "3564a74f9ddb4372301c49154605573d7d1a88fe" + "88" + "ac";
        assertEquals(Script.p2pkh, Script.determineType(Converter.hexToBytes(input)));
        input = "72" + "a9" + "14" + "3564a74f9ddb4372301c49154605573d7d1a88fe" + "88" + "ac";
        assertEquals(Script.undefined, Script.determineType(Converter.hexToBytes(input)));
        input = "76" + "a9" + "13" + "64a74f9ddb4372301c49154605573d7d1a88fe" + "88" + "ac";
        assertEquals(Script.undefined, Script.determineType(Converter.hexToBytes(input)));
        input = "76" + "a9" + "14" + "3564a74f9ddb4372301c49154605573d7d1a88fe" + "84" + "ac";
        assertEquals(Script.undefined, Script.determineType(Converter.hexToBytes(input)));
        input = "76" + "a9" + "14" + "3564a74f9ddb4372301c49154605573d7d1a88fe" + "88" + "ac" + "11";
        assertEquals(Script.undefined, Script.determineType(Converter.hexToBytes(input)));

        input = "a9" + "14" + "c9e543738d1781bba80058ec693596660d9a25c8" + "87";
        assertEquals(Script.p2sh, Script.determineType(Converter.hexToBytes(input)));
        input = "a1" + "14" + "c9e543738d1781bba80058ec693596660d9a25c8" + "87";
        assertEquals(Script.undefined, Script.determineType(Converter.hexToBytes(input)));
        input = "a9" + "15" + "11c9e543738d1781bba80058ec693596660d9a25c8" + "87";
        assertEquals(Script.undefined, Script.determineType(Converter.hexToBytes(input)));
        input = "a9" + "14" + "c9e543738d1781bba80058ec693596660d9a25c8" + "88";
        assertEquals(Script.undefined, Script.determineType(Converter.hexToBytes(input)));
        input = "a9" + "14" + "c9e543738d1781bba80058ec693596660d9a25c8" + "87" + "01";
        assertEquals(Script.undefined, Script.determineType(Converter.hexToBytes(input)));

        assertEquals(Script.undefined, Script.determineType(new byte[]{2}));
    }

    @Test
    void createScriptPubKey() {
        assertThrows(NullPointerException.class, () -> Script.createScriptPubKey(null));

        byte[] scriptHash = Ripemd160.getHash(new byte[]{2});
        assertEquals(
                "a914" + Converter.bytesToHex(scriptHash, false, false) + "87",
                Converter.bytesToHex(Script.createScriptPubKey(Address.createFromHash(scriptHash, false)), false, false)
        );

        PublicKey publicKey = new KeyPair(BigInteger.valueOf(1313), true).publicKey;
        assertEquals(
                "76a914" + Converter.bytesToHex(publicKey.getHash(), false, false) + "88ac",
                Converter.bytesToHex(Script.createScriptPubKey(Address.createFromPublicKey(publicKey)), false, false)
        );
    }

    @Test
    void signVerify() {
        // p2pk scriptPubKey: <pubKey> checksig
        // uncompressed public key
        byte[] messageHash = Sha256.getHash(new byte[]{29, 9, 8});
        byte[] messageHash1 = Sha256.getHash(new byte[]{28, 0, 0, 8});
        KeyPair keyPair = new KeyPair(BigInteger.valueOf(1313), false);
        KeyPair keyPair1 = new KeyPair(BigInteger.valueOf(1314), false);
        byte[] previousScriptPubKey = Converter.hexToBytes("41"
                + Converter.bytesToHex(keyPair.publicKey.toByteArray(), false, false) + "ac");
        byte[] previousScriptPubKey1 = Converter.hexToBytes("41"
                + Converter.bytesToHex(keyPair1.publicKey.toByteArray(), false, false) + "ac");
        assertThrows(NullPointerException.class, () -> Script.createScriptSig(Script.p2pk, null, keyPair));
        assertThrows(NullPointerException.class, () -> Script.createScriptSig(Script.p2pk, messageHash, null));
        assertThrows(
                UnsupportedOperationException.class,
                () -> Script.createScriptSig(Script.p2pk, Arrays.copyOf(messageHash, 44), keyPair)
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> Script.createScriptSig(Script.undefined, messageHash, keyPair)
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> Script.createScriptSig(Script.p2sh, messageHash, keyPair)
        );
        byte[] scriptSig = Script.createScriptSig(Script.p2pk, messageHash, keyPair);
        byte[] scriptSig1 = Script.createScriptSig(Script.p2pk, messageHash1, keyPair1);
        assertThrows(NullPointerException.class, () -> Script.verify(null, previousScriptPubKey, scriptSig));
        assertThrows(NullPointerException.class, () -> Script.verify(messageHash, null, scriptSig));
        assertThrows(NullPointerException.class, () -> Script.verify(messageHash, previousScriptPubKey, null));
        assertTrue(Script.verify(messageHash, previousScriptPubKey, scriptSig));
        assertTrue(Script.verify(messageHash1, previousScriptPubKey1, scriptSig1));
        assertFalse(Script.verify(messageHash1, previousScriptPubKey, scriptSig));
        assertFalse(Script.verify(messageHash, previousScriptPubKey1, scriptSig));
        assertFalse(Script.verify(messageHash, previousScriptPubKey, scriptSig1));
        // compressed public key
        KeyPair keyPair2 = new KeyPair(BigInteger.valueOf(131344), true);
        KeyPair keyPair3 = new KeyPair(BigInteger.valueOf(131456), true);
        byte[] previousScriptPubKey2 = Converter.hexToBytes("21"
                + Converter.bytesToHex(keyPair2.publicKey.toByteArray(), false, false) + "ac");
        byte[] previousScriptPubKey3 = Converter.hexToBytes("21"
                + Converter.bytesToHex(keyPair3.publicKey.toByteArray(), false, false) + "ac");
        byte[] scriptSig2 = Script.createScriptSig(Script.p2pk, messageHash, keyPair2);
        byte[] scriptSig3 = Script.createScriptSig(Script.p2pk, messageHash1, keyPair3);
        assertTrue(Script.verify(messageHash, previousScriptPubKey2, scriptSig2));
        assertTrue(Script.verify(messageHash1, previousScriptPubKey3, scriptSig3));
        assertFalse(Script.verify(messageHash1, previousScriptPubKey2, scriptSig2));
        assertFalse(Script.verify(messageHash, previousScriptPubKey2, scriptSig3));
        assertFalse(Script.verify(messageHash1, previousScriptPubKey3, scriptSig2));
        // p2pkh scriptPubKey: dup hash160 <pubKeyHash> equalverify checksig
        // uncompressed public key
        byte[] previousScriptPubKey4 = Script.createScriptPubKey(Address.createFromPublicKey(keyPair.publicKey));
        byte[] previousScriptPubKey5 = Script.createScriptPubKey(Address.createFromPublicKey(keyPair1.publicKey));
        byte[] scriptSig4 = Script.createScriptSig(Script.p2pkh, messageHash, keyPair);
        byte[] scriptSig5 = Script.createScriptSig(Script.p2pkh, messageHash1, keyPair1);
        assertTrue(Script.verify(messageHash, previousScriptPubKey4, scriptSig4));
        assertTrue(Script.verify(messageHash1, previousScriptPubKey5, scriptSig5));
        assertFalse(Script.verify(messageHash, previousScriptPubKey5, scriptSig5));
        assertFalse(Script.verify(messageHash, previousScriptPubKey4, scriptSig5));
        assertFalse(Script.verify(messageHash1, previousScriptPubKey5, scriptSig4));
        assertThrows(
                UnsupportedOperationException.class,
                () -> Script.verify(messageHash, previousScriptPubKey, scriptSig5)
        );
        assertThrows(
                NegativeArraySizeException.class,
                () -> Script.verify(messageHash, previousScriptPubKey4, scriptSig)
        );
        // compressed public key
        byte[] previousScriptPubKey6 = Script.createScriptPubKey(Address.createFromPublicKey(keyPair2.publicKey));
        byte[] previousScriptPubKey7 = Script.createScriptPubKey(Address.createFromPublicKey(keyPair3.publicKey));
        byte[] scriptSig6 = Script.createScriptSig(Script.p2pkh, messageHash, keyPair2);
        byte[] scriptSig7 = Script.createScriptSig(Script.p2pkh, messageHash1, keyPair3);
        assertTrue(Script.verify(messageHash, previousScriptPubKey6, scriptSig6));
        assertTrue(Script.verify(messageHash1, previousScriptPubKey7, scriptSig7));
        assertFalse(Script.verify(messageHash, previousScriptPubKey7, scriptSig7));
        assertFalse(Script.verify(messageHash, previousScriptPubKey6, scriptSig7));
        assertFalse(Script.verify(messageHash1, previousScriptPubKey7, scriptSig6));
        // p2sh   scriptSig: <sig> <script>   scriptPubKey: hash160 <scriptHash> equal
        byte[] script = {22, 33, 44, 55, 66, 77};
        byte[] script1 = {11, 22, 12, 12, 32, 34, 45, 65, 34, 13, 43, 53, 23};
        byte[] previousScriptPubKey8 = Converter.hexToBytes("a914"
                + Converter.bytesToHex(Ripemd160.getHash(Sha256.getHash(script)), false, false) + "87");
        byte[] previousScriptPubKey9 = Converter.hexToBytes("a914"
                + Converter.bytesToHex(Ripemd160.getHash(Sha256.getHash(script1)), false, false) + "87");
        byte[] scriptSig8 = Converter.hexToBytes("06" + Converter.bytesToHex(script, false, false));
        byte[] scriptSig9 = Converter.hexToBytes("00020000030000000d" + Converter.bytesToHex(script1, false, false));
        assertTrue(Script.verify(messageHash, previousScriptPubKey8, scriptSig8));
        assertTrue(Script.verify(messageHash, previousScriptPubKey9, scriptSig9));
        assertFalse(Script.verify(messageHash, previousScriptPubKey8, scriptSig9));
        assertFalse(Script.verify(messageHash, previousScriptPubKey9, scriptSig8));
        // p2pkh tx
        byte[] messageHash0 = Sha256.getHash(Sha256.getHash(Converter.hexToBytes("01000000" + "01"
                + "b5bdf03533a8861c9d6c81106573a3298e56f0b16eb42ca2c3da209f28819ed0" + "00000000"
                + "19" + "76" + "a9" + "14" + "006c82c93dce51c6e5ffb4e425e13feea50d14e2" + "88" + "ac" + "ffffffff"
                + "01" + "80f0fa0200000000" + "19" + "76" + "a9" + "14" + "6f74fc0e7d0cd8fb6930a82a7914471dcee38d78"
                + "88" + "ac" + "00000000" + "01000000")));
        byte[] previousScriptPubKey0 = Converter.hexToBytes("76a914006c82c93dce51c6e5ffb4e425e13feea50d14e288ac");
        byte[] scriptSig0 = Converter.hexToBytes("49" + "30" + "46" + "02" + "21"
                + "00c841d4ccaaf879251c809dc683615ec55ff475769fe6d8c2297d4f0b094dd96b" + "02" + "21"
                + "00e071d73aa964740c8123f54b95b12a1f52b9185c1d91a56c9f86eea8584c9cc4" + "01" + "41" + "04"
                + "5ab78884ec84e8efb6d5ce4d5c9a4458d00b87cd02b8c1cf108439ca0c192e5c"
                + "6eca9d0deaef6c7e79b7144d26327a5c09b806496689b554f0b6e31284d2ca9e");
        assertTrue(Script.verify(messageHash0, previousScriptPubKey0, scriptSig0));
        assertFalse(Script.verify(messageHash0, previousScriptPubKey0, scriptSig4));
        assertFalse(Script.verify(messageHash0, previousScriptPubKey0, scriptSig5));
    }
}