package com.aqoleg.messages.test;

import com.aqoleg.Test;
import com.aqoleg.keys.Address;
import com.aqoleg.messages.Message;
import com.aqoleg.messages.Transaction;
import com.aqoleg.utils.BytesInput;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;

@SuppressWarnings("unused")
public class TransactionTest extends Test {

    public static void main(String[] args) {
        new TransactionTest().testAll();
    }

    public void stream() {
        byte[] bytes = hexToBytes("F9BEB4D9747800000000000000000000"
                + "02010000E293CDBE01000000016DBDDB085B1D8AF75184F0BC01FAD58D1266E9B63B50881990E4B40D6AEE3629000000"
                + "008B483045022100F3581E1972AE8AC7C7367A7A253BC1135223ADB9A468BB3A59233F45BC578380022059AF01CA17D0"
                + "0E41837A1D58E97AA31BAE584EDEC28D35BD96923690913BAE9A0141049C02BFC97EF236CE6D8FE5D94013C721E91598"
                + "2ACD2B12B65D9B7D59E20A842005F8FC4E02532E873D37B96F09D6D4511ADA8F14042F46614A4C70C0F14BEFF5FFFFFF"
                + "FF02404B4C00000000001976A9141AA0CD1CBEA6E7458A7ABAD512A9D9EA1AFB225E88AC80FAE9C7000000001976A914"
                + "0EAB5BEA436A0484CFAB12485EFDA0B78B4ECC5288AC00000000");
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        final Transaction[] tx = new Transaction[1];
        assertNotThrows(() -> tx[0] = (Transaction) Message.read(stream));
        assertEquals(bytes, tx[0].toByteArray());
        assertEquals(
                "txHash: d4a73f51ab7ee7acb4cf0505d1fab34661666c461488e58ec30281e2becd93e2, " +
                        "size: 258, version: 1, inputs: 1, " +
                        "0: (prevTxHash: 2936ee6a0db4e4901988503bb6e966128dd5fa01bcf08451f78a1d5b08dbbd6d, " +
                        "prevOut: 0, " +
                        "scriptSig: (sig(r: f3581e1972ae8ac7c7367a7a253bc1135223adb9a468bb3a59233f45bc578380, " +
                        "s: 59af01ca17d00e41837a1d58e97aa31bae584edec28d35bd96923690913bae9a, signHash: 1) " +
                        "pubKey(1689LPUuixaxSchENLMNaNbS3hYVgdpaSS)), sequence: -1), outputs: 2, " +
                        "0: (value: 0.05000000, scriptPubKey: (dup hash160 " +
                        "pubKeyHash(13RoCeq4K8ddPW6ugcheFoXK4GC2BLVuET) equalverify checksig)), " +
                        "1: (value: 33.54000000, scriptPubKey: (dup hash160 " +
                        "pubKeyHash(12LZjvQBy31ABRpqvMZQbu7S9K5SxaifjW) equalverify checksig)), lockTime: 0",
                tx[0].toString()
        );
    }

    public void fromBytes() {
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
        assertThrows(NullPointerException.class, () -> Transaction.read(null));
        assertThrows(Message.Exception.class, () -> Transaction.fromBytes(hexToBytes(scriptSig)));
        String string = "03000000";
        assertThrows(Message.Exception.class, () -> Transaction.fromBytes(hexToBytes(string)));
        String string1 = "0100000000000000000000";
        assertThrows(Message.Exception.class, () -> Transaction.fromBytes(hexToBytes(string1)));
        String string2 = "0100000009000000";
        assertThrows(Message.Exception.class, () -> Transaction.fromBytes(hexToBytes(string2)));
        String string3 = string + "00";
        assertThrows(Message.Exception.class, () -> Transaction.fromBytes(hexToBytes(string3)));

        Transaction transaction = Transaction.fromBytes(hexToBytes(input));
        assertEquals(input, bytesToHex(transaction.toByteArray()).substring(48));
        assertEquals(
                "txHash: f2b3eb2deb76566e7324307cd47c35eeb88413f971d88519859b1834307ecfec, " +
                        "size: 258, version: 1, inputs: 1, " +
                        "0: (prevTxHash: fb28557b48fe41b1a75dd73de827de8c115f209ed914cd0cda7cce0bce7ec026, " +
                        "prevOut: 0, " +
                        "scriptSig: (sig(r: 2b7e37831273d74c8b5b1956c23e79acd660635a8d1063d413c50b218eb6bc8a, " +
                        "s: a10a3a7b5aaa0f07827207daf81f718f51eeac96695cf1ef9f2020f21a0de02f, signHash: 1) " +
                        "pubKey(13VPNS2zFGDA1vScbuySo4E7u7ySWN41L1)), sequence: -1), outputs: 2, " +
                        "0: (value: 47.99950000, scriptPubKey: (dup hash160 " +
                        "pubKeyHash(15sKPhXzhXbTRDHby15b45AeodmCWXzj8G) equalverify checksig)), " +
                        "1: (value: 1.00000000, scriptPubKey: (dup hash160 " +
                        "pubKeyHash(16UwLL9Risc3QfPqBUvKofHmBQ7wMtjvM) equalverify checksig)), lockTime: 0",
                transaction.toString()
        );
        assertEquals("f2b3eb2deb76566e7324307cd47c35eeb88413f971d88519859b1834307ecfec", transaction.getHash());
        assertEquals(input, transaction.getHex());
        assertEquals(input.length() / 2, transaction.getSize());

        Transaction.Output output = transaction.getTxOutput(0);
        assertEquals(
                "value: 47.99950000, scriptPubKey: (dup hash160 " +
                        "pubKeyHash(15sKPhXzhXbTRDHby15b45AeodmCWXzj8G) equalverify checksig)",
                output.toString()
        );
        assertEquals(0, output.index);
        assertEquals(4799950000L, output.value);
        assertEquals("eccf7e3034189b851985d871f91384b8ee357cd47c3024736e5676eb2debb3f2", output.getTxHash());
        assertEquals(
                "dup hash160 pubKeyHash(15sKPhXzhXbTRDHby15b45AeodmCWXzj8G) equalverify checksig",
                output.getScriptPubKey().toString()
        );
    }

    public void fromBytesInput() {
        String hex = "010000000165c36479a8614967a900aade5e25e6095c0188c199b80dff6abc23b0750975ab000000008b" +
                "483045022028b301b04aa8707985920c521661bef4412e6733a0dd958c181def2b0f6fd9ce022100beb2e0cbd" +
                "71ef9cc81c806a08ad732bccbca6d5e42b15323ecacdbeab96562520141046e5e612659824f8a09a2484c8247" +
                "fe6c405842009803f4c99c7da9279a4bd23207899266c833d1514be0003d8c80dba06668efba8574dd8e8b4c1" +
                "9c7e3d9332fffffffff0280c3c901000000001976a914c5f211477853b9e4ba7d4dcf8cdb6af57e4619d388ac" +
                "002d3101000000001976a9147d219b82b20dc8b6a5e733d065bd60ff95a5461b88ac00000000" + "112244556677";
        BytesInput bytesInput = new BytesInput(hexToBytes(hex));
        Transaction transaction = Transaction.fromBytesInput((bytesInput));
        assertEquals(6, bytesInput.available());
        assertThrows(Message.Exception.class, () -> Transaction.fromBytesInput(bytesInput));
        assertEquals(
                "txHash: da1b8ac5545110c3b9f69848e457e48e984d7b7d763ead768df6d9663cc36e52, " +
                        "size: 258, version: 1, inputs: 1, " +
                        "0: (prevTxHash: ab750975b023bc6aff0db899c188015c09e6255edeaa00a9674961a87964c365, " +
                        "prevOut: 0, " +
                        "scriptSig: (sig(r: 28b301b04aa8707985920c521661bef4412e6733a0dd958c181def2b0f6fd9ce, " +
                        "s: beb2e0cbd71ef9cc81c806a08ad732bccbca6d5e42b15323ecacdbeab9656252, signHash: 1) " +
                        "pubKey(1BAL9XUUh86tGXPiyJvG5bkDP1XY95MwAp)), sequence: -1), outputs: 2, " +
                        "0: (value: 0.30000000, scriptPubKey: (dup hash160 " +
                        "pubKeyHash(1K3e6wEmyueYkQbxUmNrXy8fheL22EbEyS) equalverify checksig)), " +
                        "1: (value: 0.20000000, scriptPubKey: (dup hash160 " +
                        "pubKeyHash(1CQdj4qvrnXfud3eUCTyzTZFVumw9b9z13) equalverify checksig)), lockTime: 0",
                transaction.toString()
        );

        hex = "01000000" + "01" + "b5bdf03533a8861c9d6c81106573a3298e56f0b16eb42ca2c3da209f28819ed0"
                + "00000000" + "8c" + "49" + "30" + "46" + "02" + "21"
                + "00c841d4ccaaf879251c809dc683615ec55ff475769fe6d8c2297d4f0b094dd96b" + "02" + "21"
                + "00e071d73aa964740c8123f54b95b12a1f52b9185c1d91a56c9f86eea8584c9cc4" + "01" + "41" + "04"
                + "5ab78884ec84e8efb6d5ce4d5c9a4458d00b87cd02b8c1cf108439ca0c192e5c"
                + "6eca9d0deaef6c7e79b7144d26327a5c09b806496689b554f0b6e31284d2ca9e" + "ffffffff" + "01"
                + "80f0fa0200000000" + "19" + "76" + "a9" + "14" + "6f74fc0e7d0cd8fb6930a82a7914471dcee38d78"
                + "88" + "ac" + "00000000";
        Transaction prevTx = Transaction.fromBytes(hexToBytes(hex));
        assertEquals(
                "ab750975b023bc6aff0db899c188015c09e6255edeaa00a9674961a87964c365",
                prevTx.getHash()
        );
        assertEquals(
                "txHash: ab750975b023bc6aff0db899c188015c09e6255edeaa00a9674961a87964c365, " +
                        "size: 225, version: 1, inputs: 1, " +
                        "0: (prevTxHash: d09e81289f20dac3a22cb46eb1f0568e29a3736510816c9d1c86a83335f0bdb5, " +
                        "prevOut: 0, scriptSig: " +
                        "(sig(r: c841d4ccaaf879251c809dc683615ec55ff475769fe6d8c2297d4f0b094dd96b, " +
                        "s: e071d73aa964740c8123f54b95b12a1f52b9185c1d91a56c9f86eea8584c9cc4, signHash: 1) " +
                        "pubKey(113EzUAVrkHmMFhzCtEY6xAv4VdKTShGWp)), sequence: -1), outputs: 1, " + "" +
                        "0: (value: 0.50000000, " +
                        "scriptPubKey: (dup hash160 pubKeyHash(1BAL9XUUh86tGXPiyJvG5bkDP1XY95MwAp) " +
                        "equalverify checksig)), lockTime: 0",
                prevTx.toString()
        );
        ArrayList<Transaction.Output> outputs = new ArrayList<>();
        assertTrue(!prevTx.searchTxOutput(new Address("113EzUAVrkHmMFhzCtEY6xAv4VdKTShGWp"), outputs));
        assertTrue(prevTx.searchTxOutput(new Address("1BAL9XUUh86tGXPiyJvG5bkDP1XY95MwAp"), outputs));
        assertEquals(
                "value: 0.50000000, scriptPubKey: (dup hash160 " +
                        "pubKeyHash(1BAL9XUUh86tGXPiyJvG5bkDP1XY95MwAp) equalverify checksig)",
                outputs.get(0).toString()
        );
        assertEquals(hex, prevTx.getHex());

        assertNotThrows(() -> transaction.verifyInput(0, prevTx));
        assertThrows(IndexOutOfBoundsException.class, () -> transaction.verifyInput(1, prevTx));
        assertThrows(Message.Exception.class, () -> transaction.verifyInput(0, transaction));
    }

    public void verify() {
        String hex = "01000000" + "01" + "af1d88acbb95dab01f28cd737e5c9d96b8925e4a9b6b21326d5482b87a23ef47"
                + "00000000" + "8a" + "47" + "30" + "44" + "02" + "20"
                + "243419ecb20f8b9a23ba107d66199637f1aeaadb6a13a7582132dd8b8dd26884" + "02" + "20"
                + "2a8168e44da4b3e272ff22bda065d9fb2f06df5fb581af3c00e6c7247f751f59" + "01" + "41" + "04"
                + "9da5a1a2c2e415d9fe2b1cc449b79ff41b47cbbe9ee66e82857b913bf7632828"
                + "dce4dfe0cee38d258cdd60a19b22e9a3a4769c4300ba837a4abbf34251c2b5b2" + "ffffffff" + "01"
                + "00e8764817000000" + "19" + "76" + "a9" + "14" + "c066f19518e5b6aac18899b4004fb62e0cd8bc7d"
                + "88" + "ac" + "00000000";
        Transaction txF44566 = Transaction.fromBytes(hexToBytes(hex));
        assertEquals(
                "txHash: f4566c830badbc622a26ff29faf5bcde01078e1b4c6a900f470409f92196030e, " +
                        "size: 223, version: 1, inputs: 1, " +
                        "0: (prevTxHash: 47ef237ab882546d32216b9b4a5e92b8969d5c7e73cd281fb0da95bbac881daf, " +
                        "prevOut: 0, " +
                        "scriptSig: (sig(r: 243419ecb20f8b9a23ba107d66199637f1aeaadb6a13a7582132dd8b8dd26884, " +
                        "s: 2a8168e44da4b3e272ff22bda065d9fb2f06df5fb581af3c00e6c7247f751f59, signHash: 1) " +
                        "pubKey(1LYJHSep7gAtvePMPVdsXvnKeNM7ifHkEK)), sequence: -1), " +
                        "outputs: 1, 0: (value: 1000.00000000, scriptPubKey: (dup hash160 " +
                        "pubKeyHash(1JYL4V4YPG5ku5tYURngRCwcWZjb2BEzGL) equalverify checksig)), lockTime: 0",
                txF44566.toString()
        );
        hex = "0100000001d14f3995168e8d3f9da3f3980bcd4d79ab04c9783d7fedcc20b1453b8ab77380000000008b4830450221" +
                "00f9444b348def806904ea441231320616f671db4bc3fb1ce90127a01566c3ae88022050cd43345913d05923bfa1" +
                "9e0d5e64bc3d1c221b917857776e4f441362b2aa9d014104157eb7e4ed5b595907a455213e01f8a41b2838e98cb4" +
                "721afb86b363f118f2e4059d6215fa8a964bd53861af1d9bd89374f2e4be0a423a6f4fd45ef71fb3ccd1ffffffff" +
                "0100e87648170000001976a914d655b65519dca2553aade471971ddbfc7f531ad188ac00000000";
        Transaction tx47ef23 = Transaction.fromBytes(hexToBytes(hex));
        assertEquals(
                "txHash: 47ef237ab882546d32216b9b4a5e92b8969d5c7e73cd281fb0da95bbac881daf, " +
                        "size: 224, version: 1, inputs: 1, " +
                        "0: (prevTxHash: 8073b78a3b45b120cced7f3d78c904ab794dcd0b98f3a39d3f8d8e1695394fd1, " +
                        "prevOut: 0, " +
                        "scriptSig: (sig(r: f9444b348def806904ea441231320616f671db4bc3fb1ce90127a01566c3ae88, " +
                        "s: 50cd43345913d05923bfa19e0d5e64bc3d1c221b917857776e4f441362b2aa9d, signHash: 1) " +
                        "pubKey(1GKYZCsk6KQiLg4pUEhyZ5J6HNFSVNBm57)), sequence: -1), " +
                        "outputs: 1, 0: (value: 1000.00000000, scriptPubKey: (dup hash160 " +
                        "pubKeyHash(1LYJHSep7gAtvePMPVdsXvnKeNM7ifHkEK) equalverify checksig)), lockTime: 0",
                tx47ef23.toString()
        );
        assertNotThrows(() -> txF44566.verifyInput(0, tx47ef23));
        hex = "010000000662fb60f0162c1a9d98fafc16fc09f4b420039b4b21aacdeb5cc4added169092f000000004a4930460221" +
                "00bc41ce4e5ea7114c7a0c8ea9118a9ff622b67af3264327d42667a99fb806ad270221009a701c8d05baf353f1ac" +
                "6f7b60b95f4e1b18abaf5edf64b88c9c7a661822851201ffffffffa9d6a8d752a6711da22833d5b1bd6b0bafbcdf" +
                "e9ca33e83eee74447974d99fbb0000000049483045022100b03120fb73efd591187a6a01d929ff35583d43e60a25" +
                "af5d1c87c275f9d1984c02206557a6773e56b736e920a15a06abc65eb7a0575d1d8813b92108920572dbd57401ff" +
                "ffffff7cf121922950acf020420c664e464b1ee3d5cc10e84a630fba036ffac1a4b15e000000004a493046022100" +
                "ac083c3ce6dcb442af357208d9a493f4bb0d5d8fe84c45e2c8626299938aa5f1022100cb61f67733d420fad09bcc" +
                "a79ee8d92ec43eb041ea2e6ed7489be25062bd0ef701ffffffffc3945b12584f90bafdf5e4ff98fbb3f0c19b70b0" +
                "0d02cafef73e4ee2964bcc3700000000494830450221009c9376eda8b357baf6ce82827ccd4eaefe37ae3caa1a5e" +
                "d0cfe96ae93569070802206d57de501255460cc6d9f3bf4dc2264d5355ac06ec0e94b3d93becb57767626401ffff" +
                "ffffd1c8af804abd0bcdaaa43f1ad4b56c9e03eead5fce878a1f26aee78992613052000000008a47304402205815" +
                "0e984e576fa106f4353e64d894dfc5312862d4ce1d968475f480d4cfc2a002202187c45378f2c38fb3bb5bb57b47" +
                "be0c2b3af7a10260235070e5303dd834358f01410459429ca260a69fcdab5fc92e68c4b470cb53c59bf49ba80a4d" +
                "afb50a4c9bf0c50efde2cf2182d9fd0121e7be07c017a8c45bd3cdb7027df2f4bb57accf83a01fffffffff433c20" +
                "6bf802d0ba26736f6f290c569cc62965cd66183edf671692799738a27c000000008c493046022100b3bfb4cdc4d6" +
                "58df832feeb0062b95a7b8a4f64be74ebbe83d818c6cdbb0010f022100d3b848e35ddf4de502cc7714401f167e38" +
                "40124cb6b6e77497f3a03f1e411f12014104ecd330fce1878c3c8faac79e462083c98006ef7bb0a221a5c53ddc97" +
                "41c77fd8b296b014cb740e37ac4365e7f3711377d92c51a7a1ddba4eb57321ba5c4ba775ffffffff0100e8764817" +
                "0000001976a914a80bb70eba8a9059dfab72a1cb3b308c4052b6f288ac00000000";
        Transaction tx8073b7 = Transaction.fromBytes(hexToBytes(hex));
        assertNotThrows(() -> tx47ef23.verifyInput(0, tx8073b7));
    }

    public void verify1() {
        String hex = "0100000001fb475510a9f21723f3bc01c6413c4ef3650e6bebb1e2138a0ac2da6b7f18391f01000000" +
                "6a47304402200f29cea98272cd55f8bdc9d1f953f2e42b9d8db1d33f01b86467d7e3289277e002204bab089" +
                "60221b3c4a53fd48d6c4d2d341659dbc748d1f62d78ab7d3ca4201526012103140fbfaa9243a1c4fd825000" +
                "172e40c49fb7ea5d2c823fbffacdbc33abc91bdeffffffff02443b0100000000001976a91411b366edfc0a8" +
                "b66feebae5c2e25a7b6a5d1cf3188ac34431100000000001976a9144b46bf8892b3d93db1bb1f4201539c18" +
                "1e9acd3c88ac00000000";
        Transaction tx = Transaction.fromBytes(hexToBytes(hex));
        assertEquals("595cf9da27359b36196ad457acba9a409ef68f51d6cd130e5449a4f418fb62f1", tx.getHash());
        String prevHex = "01000000023f5ae6f5782edec110473e04f8c89a0da8b1ac3bca23c1725862e0c2d800e2bd0000" +
                "00006b483045022100ad179cd0f9ba9e2e7394ea12b459b1db29e28dddcc99d46f3bf01fdb4eb4a24502205" +
                "13dd6796601a3c060cf435e47e1e6f7e4c46c09976fc910ed9e26809aca6f09012102e62bef63dd2712bb95" +
                "48a1bd61b40e1a0f1755491a982e0030d5b27dba7ba202fffffffff5e5892280add934d85b31322d9f28b3a" +
                "3033152afe2eef2258e7922aea981f4000000006a4730440220581e55d5f64180e68a06d967d29670f93af1" +
                "2f28e4eca5f46e3ca7f7fb521c2502205e09692a9b6268b5950fadda745d8a5dffd83916131508c79fd1125" +
                "b3b8025b0012102e4b7be3b4bb7236aca982d5e85095754518098c0528935355215e0a71eb92838ffffffff" +
                "02c0725400000000001976a914f5829e1700fcbcd78163370249ac1a6db8fde24c88ac240d1300000000001" +
                "976a9146a4d0e378ba77c56cb09ae0623135b889430ed7888ac00000000";
        Transaction prevTx = Transaction.fromBytes(hexToBytes(prevHex));
        assertNotThrows(() -> tx.verifyInput(0, prevTx));
    }

    public void witness() {
        String hex = "020000000001013fd827a131dd3890c6615a664085cbecc5c49c62de73719be22ac6cec430e2ce0100000000fd" +
                "ffffff02203702000000000017a914e4e4060da4a98ebb569c1cbb3d4c97b48a7dc721877192140000000000160014a" +
                "8355d71f764113a6fe6edcbc361b58ee6a3904c0247304402207acd42ce5ccc048d6f5372f7b6019801958d1ab3afde" +
                "9cd281dc85549b78421d022010e3ebae53437b32b6a8760b156d57083ca3435c09233974a3b80eef5ff473da012102e" +
                "16181da5b160a483ae616dabff22f567b82e4114e19db318a64465cce65857b00000000";
        Transaction tx = Transaction.fromBytes(hexToBytes(hex));
        assertEquals(
                "txHash: 436d05736e56ab9751066140923fec0c53e2da144fb7090fb697fe4a3c96dae5, " +
                        "size: 223, version: 2, inputs: 1, " +
                        "0: (prevTxHash: cee230c4cec62ae29b7173de629cc4c5eccb8540665a61c69038dd31a127d83f, " +
                        "prevOut: 1, scriptSig: (), sequence: -3), outputs: 2, " +
                        "0: (value: 0.00145184, scriptPubKey: (hash160 " +
                        "scriptHash(3NZH9eB5coHNJqithpNCccXPr3rAPedenx) equal)), 1: (value: 0.01348209, " +
                        "scriptPubKey: (0 data(a8 5]q f7 d 11 :o e6edcbc3 a b58ee6a390 L))), witnesses: 2, lockTime: 0",
                tx.toString()
        );
    }

    public void p2pk() {
        String hex = "01000000010000000000000000000000000000000000000000000000000000000000000000ffffffff0704a7bc" +
                "201c0101ffffffff0100f2052a010000004341040343b7263ccac12d17ce60229341370e449fad7b81d578ae541c6c0" +
                "7a79dd9763bb49da21c4efc7e43d48e0e0247e10466dda3b0a76cc36d13dcf1157bef3bbeac00000000";
        Transaction tx00dfba = Transaction.fromBytes(hexToBytes(hex));
        assertEquals(
                "txHash: 00dfbae1030d8ce18e3345bad67604e8657c17d098b0a9755ad3bdc3c382aadd, " +
                        "size: 134, version: 1, inputs: 1, " +
                        "0: (prevTxHash: 0000000000000000000000000000000000000000000000000000000000000000, " +
                        "prevOut: -1, scriptSig: (data(a7bc   1c) data(01)), sequence: -1), outputs: 1, " +
                        "0: (value: 50.00000000, " +
                        "scriptPubKey: (pubKey(16SSznxYFz5nMkWsdJ8GqA1ECdCUz8VzZH) checksig)), lockTime: 0",
                tx00dfba.toString()
        );
        ArrayList<Transaction.Output> outputs = new ArrayList<>();
        assertTrue(tx00dfba.searchTxOutput(new Address("16SSznxYFz5nMkWsdJ8GqA1ECdCUz8VzZH"), outputs));

        hex = "0100000005ddaa82c3c3bdd35a75a9b098d0177c65e80476d6ba45338ee18c0d03e1badf000000000049483045022100c5" +
                "fad8a8d535a373baa83ccc00d8af1aeb6463105f036d342b214269dd70f32b0220675e9e9a735bf68519c516fa82a5a0" +
                "f11848c095bdf800339e6234fcd3ed19bf01ffffffffc2603a7c33878163eadd9d83605b5cd6fe0365be866bd46b24bb" +
                "bef8c56823760000000048473044022075ebb7de909ae195d80a9c66c49ad95faa44deb35bbde0edf28cb0c637d66126" +
                "022016a176d98c8a2cbcae81b150776d2e8ba63d529915b59e1213fcccaf8e1d613601fffffffff69821790e3bbb7b60" +
                "e3dae84e081532af5409c6cee0b9a918317009788f2e3c000000004847304402205aa2925e573b3364361e9fa754af9c" +
                "12ac28d6982ff547ea0b220b26f862dca5022022998c135243a9026801197e0e6cfce54243a567c0bb9e3ad4951d3e1f" +
                "d2636101ffffffff80a136146ffa82e55f142cfe0e59e0c884e4338fc1fc6c8a0e9d1320d33233f60000000049483045" +
                "022100c2fc1fefcfbe1f7b8300184a6319f6de8d34fac423e0bc9bd7fd77c2d59b20a4022057ff9362510a452d00c147" +
                "cf7b7e70260fde7e7f95e740fffe96fa383cbe6e2001ffffffff99737b85a5a5f5266d91fd640d78492d0bfccb61e7d5" +
                "06ed2e1a1e16d52fbeb0000000004948304502210088e68c18c390d6938c8d2ff539c1c92f93d5c5f8ace85f4ced2f32" +
                "910b7d9d1602207a72c0057d89365fd361e50254a9dc4889aa8b5ba6182609edb79c35f31ecca701ffffffff0200f082" +
                "96050000001976a914a6e64dba84849fe07d61018fffe1a50f9d707a9a88ac00ca9a3b000000001976a9149906d3eace" +
                "ec24046b70b9b7e30d4500034ffd5388ac00000000";
        Transaction txb16546 = Transaction.fromBytes(hexToBytes(hex));
        assertEquals(
                "txHash: b16546b6b62b2c51d1ed89921f44a9b8443be607d1ca824d34509582f849f161, " +
                        "size: 646, version: 1, inputs: 5, " +
                        "0: (prevTxHash: 00dfbae1030d8ce18e3345bad67604e8657c17d098b0a9755ad3bdc3c382aadd, " +
                        "prevOut: 0, " +
                        "scriptSig: (sig(r: c5fad8a8d535a373baa83ccc00d8af1aeb6463105f036d342b214269dd70f32b, " +
                        "s: 675e9e9a735bf68519c516fa82a5a0f11848c095bdf800339e6234fcd3ed19bf, signHash: 1)), " +
                        "sequence: -1), " +
                        "1: (prevTxHash: 762368c5f8bebb246bd46b86be6503fed65c5b60839dddea638187337c3a60c2, " +
                        "prevOut: 0, " +
                        "scriptSig: (sig(r: 75ebb7de909ae195d80a9c66c49ad95faa44deb35bbde0edf28cb0c637d66126, " +
                        "s: 16a176d98c8a2cbcae81b150776d2e8ba63d529915b59e1213fcccaf8e1d6136, signHash: 1)), " +
                        "sequence: -1), " +
                        "2: (prevTxHash: 3c2e8f7809703118a9b9e0cec60954af3215084ee8dae3607bbb3b0e792198f6, " +
                        "prevOut: 0, " +
                        "scriptSig: (sig(r: 5aa2925e573b3364361e9fa754af9c12ac28d6982ff547ea0b220b26f862dca5, " +
                        "s: 22998c135243a9026801197e0e6cfce54243a567c0bb9e3ad4951d3e1fd26361, signHash: 1)), " +
                        "sequence: -1), " +
                        "3: (prevTxHash: f63332d320139d0e8a6cfcc18f33e484c8e0590efe2c145fe582fa6f1436a180, " +
                        "prevOut: 0, " +
                        "scriptSig: (sig(r: c2fc1fefcfbe1f7b8300184a6319f6de8d34fac423e0bc9bd7fd77c2d59b20a4, " +
                        "s: 57ff9362510a452d00c147cf7b7e70260fde7e7f95e740fffe96fa383cbe6e20, signHash: 1)), " +
                        "sequence: -1), " +
                        "4: (prevTxHash: b0be2fd5161e1a2eed06d5e761cbfc0b2d49780d64fd916d26f5a5a5857b7399, " +
                        "prevOut: 0, " +
                        "scriptSig: (sig(r: 88e68c18c390d6938c8d2ff539c1c92f93d5c5f8ace85f4ced2f32910b7d9d16, " +
                        "s: 7a72c0057d89365fd361e50254a9dc4889aa8b5ba6182609edb79c35f31ecca7, signHash: 1)), " +
                        "sequence: -1), outputs: 2, 0: (value: 240.00000000, " +
                        "scriptPubKey: (dup hash160 pubKeyHash(1GDV4cz38Unob83UpaETmjzqkyfoiJmYjG) " +
                        "equalverify checksig)), 1: (value: 10.00000000, " +
                        "scriptPubKey: (dup hash160 pubKeyHash(1Ex8ZkjZ7SJeWbC4ebpt2F7chPPTNzfrHB) " +
                        "equalverify checksig)), lockTime: 0",
                txb16546.toString()
        );
        assertNotThrows(() -> txb16546.verifyInput(0, tx00dfba));
        byte[] bytes = hexToBytes(hex);
        bytes[55]++;
        assertThrows(Message.Exception.class, () -> Transaction.fromBytes(bytes).verifyInput(0, tx00dfba));
        assertTrue(!txb16546.searchTxOutput(new Address("16SSznxYFz5nMkWsdJ8GqA1ECdCUz8VzZH"), outputs));
        assertTrue(txb16546.searchTxOutput(new Address("1Ex8ZkjZ7SJeWbC4ebpt2F7chPPTNzfrHB"), outputs));
        assertEquals(
                "value: 50.00000000, scriptPubKey: (pubKey(16SSznxYFz5nMkWsdJ8GqA1ECdCUz8VzZH) checksig)",
                outputs.get(0).toString()
        );
        assertEquals(
                "value: 10.00000000, scriptPubKey: (dup hash160 " +
                        "pubKeyHash(1Ex8ZkjZ7SJeWbC4ebpt2F7chPPTNzfrHB) equalverify checksig)",
                outputs.get(1).toString()
        );
    }
}