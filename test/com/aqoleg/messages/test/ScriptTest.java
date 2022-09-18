package com.aqoleg.messages.test;

import com.aqoleg.Test;
import com.aqoleg.crypto.Ripemd160;
import com.aqoleg.crypto.Sha256;
import com.aqoleg.keys.Address;
import com.aqoleg.keys.KeyPair;
import com.aqoleg.keys.PublicKey;
import com.aqoleg.messages.Message;
import com.aqoleg.messages.Script;
import com.aqoleg.utils.BytesInput;
import com.aqoleg.utils.BytesOutput;

import java.math.BigInteger;
import java.util.Arrays;

@SuppressWarnings("unused")
public class ScriptTest extends Test {

    public static void main(String[] args) {
        new ScriptTest().testAll();
    }

    public void constructor() {
        assertThrows(NullPointerException.class, () -> new Script(null));

        String input = "a9" + "14" + "c9e543738d1781bba80058ec693596660d9a25c8" + "87";
        Script script = new Script(hexToBytes(input));
        assertEquals(Script.p2sh, script.type);
        assertTrue(new Address("3L6YPcCQSMQPaak5s9zTSgaKusaNqSN939").equals(script.address));
        assertEquals("hash160 scriptHash(3L6YPcCQSMQPaak5s9zTSgaKusaNqSN939) equal", script.toString());
        assertEquals(23, script.getSize());
        input = "a7" + "14" + "c9e543738d1781bba80058ec693596660d9a25c8" + "87";
        script = new Script(hexToBytes(input));
        assertEquals("sha1 data(c9e5 Cs 8d1781bba800 X ec i5 96 f 0d9a % c8) equal", script.toString());
        assertEquals(Script.undefined, script.type);
        assertNull(script.address);
        input = "a9" + "15" + "11c9e543738d1781bba80058ec693596660d9a25c8" + "87";
        script = new Script(hexToBytes(input));
        assertEquals("hash160 data(11c9e5 Cs 8d1781bba800 X ec i5 96 f 0d9a % c8) equal", script.toString());
        assertEquals(Script.undefined, script.type);
        assertNull(script.address);
        input = "a9" + "14" + "c9e543738d1781bba80058ec693596660d9a25c8" + "88";
        script = new Script(hexToBytes(input));
        assertEquals("hash160 data(c9e5 Cs 8d1781bba800 X ec i5 96 f 0d9a % c8) equalverify", script.toString());
        assertEquals(Script.undefined, script.type);
        assertNull(script.address);
        input = "a9" + "14" + "c9e543738d1781bba80058ec693596660d9a25c8" + "87" + "01";
        script = new Script(hexToBytes(input));
        assertEquals(
                "a914c9e5 Cs 8d1781bba800 X ec i5 96 f 0d9a % c88701 (incorrect push: not enough bytes for byte array)",
                script.toString()
        );
        assertEquals(Script.undefined, script.type);
        assertNull(script.address);
        assertEquals(Script.undefined, new Script(new byte[]{2}).type);

        input = "76" + "a9" + "14" + "3564a74f9ddb4372301c49154605573d7d1a88fe" + "88" + "ac";
        script = new Script(hexToBytes(input));
        assertEquals(
                "dup hash160 pubKeyHash(15sKPhXzhXbTRDHby15b45AeodmCWXzj8G) equalverify checksig",
                script.toString()
        );
        assertEquals(Script.p2pkh, script.type);
        assertTrue(new Address("15sKPhXzhXbTRDHby15b45AeodmCWXzj8G").equals(script.address));
        assertEquals(25, script.getSize());
        assertEquals(106, script.getScriptSigSize(true));
        assertEquals(138, script.getScriptSigSize(false));
        input = "72" + "a9" + "14" + "3564a74f9ddb4372301c49154605573d7d1a88fe" + "88" + "ac";
        script = new Script(hexToBytes(input));
        assertEquals(
                "2swap hash160 data(5d a7 O 9ddb Cr0 1c I 15 F 05 W=} 1a88fe) equalverify checksig",
                script.toString()
        );
        assertEquals(Script.undefined, script.type);
        assertNull(script.address);
        input = "76" + "a9" + "13" + "64a74f9ddb4372301c49154605573d7d1a88fe" + "88" + "ac";
        script = new Script(hexToBytes(input));
        assertEquals(
                "dup hash160 data(d a7 O 9ddb Cr0 1c I 15 F 05 W=} 1a88fe) equalverify checksig",
                script.toString()
        );
        assertEquals(Script.undefined, script.type);
        assertNull(script.address);
        input = "76" + "a9" + "14" + "3564a74f9ddb4372301c49154605573d7d1a88fe" + "84" + "ac";
        script = new Script(hexToBytes(input));
        assertEquals("dup hash160 data(5d a7 O 9ddb Cr0 1c I 15 F 05 W=} 1a88fe) and checksig", script.toString());
        assertEquals(Script.undefined, script.type);
        assertNull(script.address);
        input = "76" + "a9" + "14" + "3564a74f9ddb4372301c49154605573d7d1a88fe" + "88" + "ac" + "11";
        script = new Script(hexToBytes(input));
        assertEquals(
                "v a914 5d a7 O 9ddb Cr0 1c I 15 F 05 W=} 1a88fe88ac11 " +
                        "(incorrect push: not enough bytes for byte array)",
                script.toString()
        );
        assertEquals(Script.undefined, script.type);
        assertNull(script.address);

        input = "41" + "04" + "11db93e1dcdb8a016b49840f8c53bc1eb68a382e97b1482ecad7b148a6909a5c"
                + "b2e0eaddfb84ccf9744464f82e160bfa9b8b64f9d4c03f999b8643f656b412a3" + "ac";
        script = new Script(hexToBytes(input));
        assertEquals("pubKey(12cbQLTFMXRnSzktFkuoG3eHoMeFtpTu3S) checksig", script.toString());
        assertEquals(Script.p2pk, script.type);
        assertTrue(new Address("12cbQLTFMXRnSzktFkuoG3eHoMeFtpTu3S").equals(script.address));
        assertEquals(67, script.getSize());
        assertEquals(72, script.getScriptSigSize(true));
        assertEquals(72, script.getScriptSigSize(false));
        BytesOutput bytesOutput = new BytesOutput();
        script.write(bytesOutput);
        assertEquals(input, bytesToHex(bytesOutput.toByteArray()).substring(2));
        input = "21" + "03" + "11db93e1dcdb8a016b49840f8c53bc1eb68a382e97b1482ecad7b148a6909a5c" + "ac";
        script = new Script(hexToBytes(input));
        assertEquals("pubKey(13KiMqUJ7xD6MhUD2k7mKEoZMHDP9HdWwW) checksig", script.toString());
        assertEquals(Script.p2pk, script.type);
        assertTrue(new Address("13KiMqUJ7xD6MhUD2k7mKEoZMHDP9HdWwW").equals(script.address));
        assertEquals(35, script.getSize());
        assertEquals(72, script.getScriptSigSize(true));
        assertEquals(72, script.getScriptSigSize(false));
        bytesOutput = new BytesOutput();
        script.write(bytesOutput);
        assertEquals(input, bytesToHex(bytesOutput.toByteArray()).substring(2));
        input = "22" + "03" + "11db93e1dcdb8a016b49840f8c53bc1eb68a382e97b1482ecad7b148a6909a5c11" + "ac";
        Script scriptE = new Script(hexToBytes(input));
        assertEquals(Script.undefined, scriptE.type);
        assertNull(scriptE.address);
        assertEquals(36, scriptE.getSize());
        assertThrows(Message.Exception.class, () -> scriptE.getScriptSigSize(true));
        input = "21" + "03" + "11db93e1dcdb8a016b49840f8c53bc1eb68a382e97b1482ecad7b148a6909a5c" + "a1";
        script = new Script(hexToBytes(input));
        assertEquals("pubKey(13KiMqUJ7xD6MhUD2k7mKEoZMHDP9HdWwW) lessthanorequal", script.toString());
        assertEquals(Script.undefined, script.type);
        assertNull(script.address);
        input = "21" + "03" + "11db93e1dcdb8a016b49840f8c53bc1eb68a382e97b1482ecad7b148a6909a5c" + "ac" + "ac";
        script = new Script(hexToBytes(input));
        assertEquals("pubKey(13KiMqUJ7xD6MhUD2k7mKEoZMHDP9HdWwW) checksig checksig", script.toString());
        assertEquals(Script.undefined, script.type);
        assertNull(script.address);
    }

    public void fromBytesInput() {
        assertThrows(NullPointerException.class, () -> Script.fromBytesInput(null));
        byte[] bytes = new byte[]{1};
        assertThrows(Message.Exception.class, () -> Script.fromBytesInput(new BytesInput(bytes)));
        byte[] bytes1 = hexToBytes("17" + "a9" + "14" + "c9e543738d1781bba80058ec693596660d9a25c8" + "87");
        Script script = Script.fromBytesInput(new BytesInput(bytes1));
        assertTrue(new Address("3L6YPcCQSMQPaak5s9zTSgaKusaNqSN939").equals(script.address));
        BytesOutput bytesOutput = new BytesOutput();
        script.write(bytesOutput);
        assertEquals(bytes1, bytesOutput.toByteArray());
    }

    public void createScriptPubKey() {
        assertThrows(NullPointerException.class, () -> Script.createScriptPubKey(null));

        byte[] scriptHash = Ripemd160.getHash(new byte[]{2});
        Address address = Address.createFromHash(scriptHash, false);
        BytesOutput bytesOutput = new BytesOutput();
        Script script = Script.createScriptPubKey(address);
        assertEquals("hash160 scriptHash(34UotCpzqF4oEYqheotxosVZVnpQxmgsge) equal", script.toString());
        assertEquals(Script.p2sh, script.type);
        assertTrue(address.equals(script.address));
        assertEquals(23, script.getSize());
        script.write(bytesOutput);
        assertEquals("17a914" + bytesToHex(scriptHash) + "87", bytesOutput.toByteArray());

        PublicKey publicKey = new KeyPair(BigInteger.valueOf(1313), true).publicKey;
        address = Address.createFromPublicKey(publicKey);
        bytesOutput.reset();
        script = Script.createScriptPubKey(address);
        assertEquals(
                "dup hash160 pubKeyHash(1A2mLvZxnD31N3B5Pox1XW8D9PgjSg8Bnw) equalverify checksig",
                script.toString()
        );
        assertEquals(Script.p2pkh, script.type);
        assertTrue(address.equals(script.address));
        assertEquals(25, script.getSize());
        script.write(bytesOutput);
        assertEquals("1976a914" + bytesToHex(publicKey.getHash()) + "88ac", bytesOutput.toByteArray());
    }

    public void string() {
        assertEquals("", new Script(new byte[0]).toString());
        assertEquals("0", new Script(new byte[1]).toString());
        assertEquals(
                "boolor ripemd160 abs reserved 1 2 15 16 nop 1negate 0 script(rot verify hash160)",
                new Script(hexToBytes("9ba6905051525f60614f00037b69a9")).toString()
        );
        assertEquals(
                "4U (incorrect push: not enough bytes for byte array)",
                new Script(hexToBytes("3455")).toString()
        );
        assertEquals(
                "L (incorrect pushdata1: null)",
                new Script(hexToBytes("4c")).toString()
        );
        assertEquals(
                "LLU (incorrect pushdata1: not enough bytes for byte array)",
                new Script(hexToBytes("4c4c55")).toString()
        );
        assertEquals(
                "aM (incorrect pushdata2: null)",
                new Script(hexToBytes("614d")).toString()
        );
        assertEquals(
                "aMaqa (incorrect pushdata2: not enough bytes for byte array)",
                new Script(hexToBytes("614d617161")).toString()
        );
        assertEquals(
                "aNb (incorrect pushdata4: not enough bytes for leInt)",
                new Script(hexToBytes("614e62")).toString()
        );
        assertEquals(
                "aNb 000000 fg (incorrect pushdata4: not enough bytes for byte array)",
                new Script(hexToBytes("614e620000006667")).toString()
        );
        assertEquals("f4 (incorrect opcode 0xf4)", new Script(hexToBytes("f4")).toString());
        assertEquals(
                "3dup script(and drop data(35V) verify script(3 add 6)) nop(0xb9)",
                new Script(hexToBytes("6f0b8475033335566903539356b9")).toString()
        );
    }

    public void signVerify() {
        // p2pk scriptPubKey: pubKey checksig
        // uncompressed public key
        byte[] messageHash = Sha256.getHash(new byte[]{29, 9, 8});
        byte[] messageHash1 = Sha256.getHash(new byte[]{28, 0, 0, 8});
        KeyPair keyPair = new KeyPair(BigInteger.valueOf(1313), false);
        KeyPair keyPair1 = new KeyPair(BigInteger.valueOf(1314), false);
        Script prevScriptPubKey = new Script(hexToBytes("41" + bytesToHex(keyPair.publicKey.toByteArray()) + "ac"));
        Script prevScriptPubKey1 = new Script(hexToBytes("41" + bytesToHex(keyPair1.publicKey.toByteArray()) + "ac"));
        assertThrows(NullPointerException.class, () -> prevScriptPubKey.createScriptSig(null, keyPair));
        assertThrows(NullPointerException.class, () -> prevScriptPubKey.createScriptSig(messageHash, null));
        assertThrows(
                Message.Exception.class,
                () -> prevScriptPubKey.createScriptSig(Arrays.copyOf(messageHash, 44), keyPair)
        );
        assertThrows(
                Message.Exception.class,
                () -> new Script(messageHash).createScriptSig(messageHash, keyPair)
        );
        System.out.print(".");
        Script scriptSig = prevScriptPubKey.createScriptSig(messageHash, keyPair);
        Script scriptSig1 = prevScriptPubKey1.createScriptSig(messageHash1, keyPair1);
        assertThrows(NullPointerException.class, () -> scriptSig.verify(null, messageHash));
        assertThrows(NullPointerException.class, () -> scriptSig.verify(prevScriptPubKey, null));
        assertNotThrows(() -> scriptSig.verify(prevScriptPubKey, messageHash));
        assertNotThrows(() -> scriptSig1.verify(prevScriptPubKey1, messageHash1));
        assertThrows(Message.Exception.class, () -> scriptSig.verify(scriptSig, messageHash));
        assertThrows(Message.Exception.class, () -> scriptSig.verify(prevScriptPubKey, messageHash1));
        assertThrows(Message.Exception.class, () -> scriptSig.verify(prevScriptPubKey1, messageHash));
        assertThrows(Message.Exception.class, () -> scriptSig.verify(prevScriptPubKey1, messageHash1));
        assertThrows(Message.Exception.class, () -> scriptSig.verify(prevScriptPubKey1, messageHash1));
        assertThrows(Message.Exception.class, () -> scriptSig1.verify(prevScriptPubKey, messageHash));
        assertThrows(Message.Exception.class, () -> scriptSig1.verify(prevScriptPubKey1, messageHash));
        assertThrows(Message.Exception.class, () -> scriptSig1.verify(prevScriptPubKey, messageHash1));
        // compressed public key
        System.out.print(".");
        KeyPair keyPair2 = new KeyPair(BigInteger.valueOf(131344), true);
        KeyPair keyPair3 = new KeyPair(BigInteger.valueOf(131456), true);
        Script prevScriptPubKey2 = new Script(hexToBytes("21" + bytesToHex(keyPair2.publicKey.toByteArray()) + "ac"));
        Script prevScriptPubKey3 = new Script(hexToBytes("21" + bytesToHex(keyPair3.publicKey.toByteArray()) + "ac"));
        Script scriptSig2 = prevScriptPubKey2.createScriptSig(messageHash, keyPair2);
        Script scriptSig3 = prevScriptPubKey3.createScriptSig(messageHash1, keyPair3);
        assertNotThrows(() -> scriptSig2.verify(prevScriptPubKey2, messageHash));
        assertNotThrows(() -> scriptSig3.verify(prevScriptPubKey3, messageHash1));
        assertThrows(Message.Exception.class, () -> scriptSig2.verify(prevScriptPubKey1, messageHash));
        assertThrows(Message.Exception.class, () -> scriptSig2.verify(prevScriptPubKey2, messageHash1));
        assertThrows(Message.Exception.class, () -> scriptSig3.verify(prevScriptPubKey2, messageHash1));
        // p2pkh scriptPubKey: dup hash160 pubKeyHash equalverify checksig
        // uncompressed public key
        System.out.print(".");
        Script previousScriptPubKey4 = Script.createScriptPubKey(Address.createFromPublicKey(keyPair.publicKey));
        Script previousScriptPubKey5 = Script.createScriptPubKey(Address.createFromPublicKey(keyPair1.publicKey));
        Script scriptSig4 = previousScriptPubKey4.createScriptSig(messageHash, keyPair);
        Script scriptSig5 = previousScriptPubKey5.createScriptSig(messageHash1, keyPair1);
        assertNotThrows(() -> scriptSig4.verify(previousScriptPubKey4, messageHash));
        assertNotThrows(() -> scriptSig5.verify(previousScriptPubKey5, messageHash1));
        assertThrows(Message.Exception.class, () -> scriptSig4.verify(previousScriptPubKey4, messageHash1));
        assertThrows(Message.Exception.class, () -> scriptSig4.verify(previousScriptPubKey5, messageHash));
        assertThrows(Message.Exception.class, () -> scriptSig4.verify(previousScriptPubKey5, messageHash1));
        assertThrows(Message.Exception.class, () -> scriptSig4.verify(prevScriptPubKey1, messageHash));
        assertThrows(Message.Exception.class, () -> scriptSig5.verify(previousScriptPubKey5, messageHash));
        assertThrows(Message.Exception.class, () -> scriptSig5.verify(previousScriptPubKey4, messageHash1));
        assertThrows(Message.Exception.class, () -> scriptSig5.verify(previousScriptPubKey4, messageHash));
        // compressed public key
        System.out.print(".");
        Script previousScriptPubKey6 = Script.createScriptPubKey(Address.createFromPublicKey(keyPair2.publicKey));
        Script previousScriptPubKey7 = Script.createScriptPubKey(Address.createFromPublicKey(keyPair3.publicKey));
        Script scriptSig6 = previousScriptPubKey6.createScriptSig(messageHash, keyPair2);
        Script scriptSig7 = previousScriptPubKey7.createScriptSig(messageHash1, keyPair3);
        assertNotThrows(() -> scriptSig6.verify(previousScriptPubKey6, messageHash));
        assertNotThrows(() -> scriptSig7.verify(previousScriptPubKey7, messageHash1));
        assertThrows(Message.Exception.class, () -> scriptSig6.verify(previousScriptPubKey7, messageHash));
        assertThrows(Message.Exception.class, () -> scriptSig6.verify(previousScriptPubKey6, messageHash1));
        assertThrows(Message.Exception.class, () -> scriptSig7.verify(previousScriptPubKey7, messageHash));
    }
}