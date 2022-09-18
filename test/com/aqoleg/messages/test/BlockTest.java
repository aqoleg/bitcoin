package com.aqoleg.messages.test;

import com.aqoleg.Test;
import com.aqoleg.keys.Address;
import com.aqoleg.messages.Block;
import com.aqoleg.messages.Message;
import com.aqoleg.messages.Transaction;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

@SuppressWarnings("unused")
public class BlockTest extends Test {

    public static void main(String[] args) {
        new BlockTest().testAll();
    }

    public void genesis() {
        assertThrows(NullPointerException.class, () -> new Block(null));
        assertThrows(Message.Exception.class, () -> new Block(new byte[32]));
        String genesis = "01000000" // version
                + "0000000000000000000000000000000000000000000000000000000000000000" // prevBlock
                + "3BA3EDFD7A7B12B27AC72C3E67768F617FC81BC3888A51323A9FB8AA4B1E5E4A" // merkleRoot
                + "29AB5F49" // timestamp
                + "FFFF001D" // bits
                + "1DAC2B7C" // nonce
                + "01" // tx#
                + "01000000" // version
                + "01" // vin#
                + "0000000000000000000000000000000000000000000000000000000000000000" // previousTransactionHash
                + "FFFFFFFF" // previousOutIndex
                + "4D" // scriptSigLen
                + "04FFFF001D0104455468652054696D65732030332F4A616E2F32303039204368"
                + "616E63656C6C6F72206F6E206272696E6B206F66207365636F6E64206261696C6F757420666F722062616E6B73"
                + "FFFFFFFF" // sequence
                + "01" // vout#
                + "00F2052A01000000" // value
                + "43" // scriptPubKeyLen
                + "4104678AFDB0FE5548271967F1A67130B7105CD6A828E03909A67962E0EA1F61"
                + "DEB649F6BC3F4CEF38C4F35504E51EC112DE5C384DF7BA0B8D578A4C702B6BF11D5FAC"
                + "00000000"; // lockTime
        Block block = new Block(hexToBytes(genesis));
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        assertNotThrows(() -> block.write(stream));
        assertEquals(genesis.toLowerCase(), stream.toByteArray());
        assertEquals(
                "blockHash: 000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f, version: 1, " +
                        "prevBlock: 0000000000000000000000000000000000000000000000000000000000000000, " +
                        "merkleRoot: 4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b, " +
                        "timestamp: 2009-01-03T18:15:05Z, bits: 486604799, nonce: 2083236893, count: 1, " +
                        "0: (txHash: 4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b, " +
                        "size: 204, version: 1, inputs: 1, " +
                        "0: (prevTxHash: 0000000000000000000000000000000000000000000000000000000000000000, " +
                        "prevOut: -1, scriptSig: (data(ffff001d) data(04) data(The Times 03/Jan/2009 Chancellor " +
                        "on brink of second bailout for banks)), sequence: -1), outputs: 1, 0: (value: 50.00000000, " +
                        "scriptPubKey: (pubKey(1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa) checksig)), lockTime: 0)",
                block.toString()
        );
        assertEquals("000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f", block.getHash());
        assertEquals("000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f", block.getHash());
        assertEquals(1, block.txNumber());
        assertThrows(IndexOutOfBoundsException.class, () -> block.getTx(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> block.getTx(1));
        assertEquals(
                "txHash: 4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b, " +
                        "size: 204, version: 1, inputs: 1, " +
                        "0: (prevTxHash: 0000000000000000000000000000000000000000000000000000000000000000, " +
                        "prevOut: -1, scriptSig: (data(ffff001d) data(04) data(The Times 03/Jan/2009 Chancellor " +
                        "on brink of second bailout for banks)), sequence: -1), outputs: 1, 0: (value: 50.00000000, " +
                        "scriptPubKey: (pubKey(1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa) checksig)), lockTime: 0",
                block.getTx(0).toString());
        assertNull(block.getTx("ddd"));
        assertTrue(block.getTx("4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b") != null);
    }

    public void block() {
        String hex = "01000000df68de9c2cffebf8a37dfba3f63cd8ba31d1380071485167c21b0100000000001316418691801389b0bf23" +
                "f2c63141026ee75b35b1e2582775800851e8f86daa0c3efe4cd21c081b00ebf0a3070100000001000000000000000000000" +
                "0000000000000000000000000000000000000000000ffffffff0704d21c081b014dffffffff0100f2052a01000000434104" +
                "e0f1142b508d427a52e7980524faff61e41c548bf2a47747039e104510f600d01b53a1e04bdded3760be1b1a82373b38a43" +
                "6a5a7dbc483d3022920c6ea491ba4ac0000000001000000076511d3c734068a9251755517d69b244d1662197f508bacd3c1" +
                "1da8510e3913e50000000049483045022100f0daa4e1b257afe7f4d5903c5ec20db9e8afa5ddc8099d1b9aa81a0c7a8561a" +
                "b022000e5b6adbee64f5e116a1ae70892688136935eabd3ae71ac3e70553604a3f9b101ffffffff6f9757773500782abe25" +
                "527a01851d902b3cf9e6ede552097f478fd7a90e4cfb000000004a493046022100fabc12fad4b8c19fd83b8d4a67f4c8802" +
                "5648fcd20d438825b77575c35bd8180022100e3e4e0e07bc840f9c0041ff300a03ba07c34d37ea3c2fba347c266a26e3020" +
                "d101ffffffff300c08fac64e95ac040f55dc23528dedd4c8c1e5043a00ec32106e193948bf8600000000494830450220386" +
                "11af96220d3dcc3a39287c8a74013c95f22b58ca7646b26fd9d0a4f86bb51022100820f49bfea198669b1b009d8ffc8abc3" +
                "8663e0923d4f70a8d9f496680feb1fe501ffffffff4d2c4a20f13a122959305b6b767a6b677be4ac897a13753ea39110eaa" +
                "7a9d6e4000000004a493046022100d3cd1a4bd3715d6d61b17a778062402944ca11dbffcfe1f3137c6a63fe9a81e3022100" +
                "a280416a868b7a3ade807119a9a987e9b159785bb431d7afc086a44114e254a101ffffffffa84f678bcfc6e3374ba2b37da" +
                "bc1d9e15e6c75bc036dfb85fc6bbe3434bc7533000000004847304402200eb583421dd75356ba278725d775d72437b3376c" +
                "42161cf0759fb7eaf17cc0ae02202f83636c2d2f3514123150453f930d56dd3c9ba173034c50285efc276bfc76d201fffff" +
                "ffff7bf9d2c118c321d2d3fee79303c36de3c42b00297cbe3cfc6637f85a8d06818000000004a493046022100fa9569fae0" +
                "8cf20d23257dd017651160c3f5ab6602514c23d89104924b7545320221008d62f470cd0d2eb9aed69460efa80deeae79f04" +
                "79f3409e8685a555ba3dbc06a01fffffffffa3fc96e2dd887461f76f7637fe8d8b16ab39a9ba17bf811649484f0d99de673" +
                "000000004a49304602210089d3762b81b30c22156e1c858c17bf67e8327266ba30a913462690f5101f91ff022100ef78935" +
                "5a5e02ee242cfe88e5ef241663b2183332e4285d8772ffbd5fc087b1001ffffffff01009e2926080000001976a914d8b3e3" +
                "b10d65317eb54ce7c0f412f7fe73d8518288ac00000000010000000124f5b1e2e65d3149f89b9bdbc7dc1c171241031638a" +
                "0893a4498df31ef2102d3000000004a4930460221008effc0d542184378be0e1a69e36ab4fc744232400feac3e69a0e305f" +
                "a806df4e022100ed9f5717d6dd75e9febe6a563a6404c2d57a80778170235b6ddcff74f3f2473a01ffffffff0100f2052a0" +
                "10000001976a9145052d2f67feb5405d9711db5bdc2db26bf2416ea88ac00000000010000000a12a8b9cae1a9193537ef86" +
                "0dbcf602bf70ca4087f8f5b3a5312bdbc740f87b1600000000494830450221009cc8e872772a91cfcf246c8803285dc20ed" +
                "2ec94b1a7a01af0d2e3816ea57490022014f09d515bc5ca2867b6d7488ac7b58bd2aa4df8b85759302cdb04fcbafc45e501" +
                "ffffffffffbc0f261be92d1bea00b1030863ffaba51321a4628f6bb8af45696c4091a46b000000004948304502205b53663" +
                "a7ebc2bbb8423b628e7c4d4677df530467548687ec445da9b12f22acc02210093b159a16e1adfae098f2e2a8c244c32a0b1" +
                "c3cf1f92848ef43a9d227547aa8601ffffffff50730a0eab47cb98959fe8166c345920e8a030d6c3993e53151065316fb7c" +
                "510000000004847304402201336f4d5448e8d3d59f8eb69d99cedcea1e72d8be980e54de4b254a02936995f022030e1e2c5" +
                "d23efae4ba897c26c2c6af109abb3c4e6023dce5e704984dbf0b926901ffffffff51faa8796089624aa89003d7ccd0a041b" +
                "b542432a58834aa02ebf55312657686000000004a493046022100c2c382869cd7ca916531ca6f4bfb3917eb74f3c78f8413" +
                "b09399d66de77123b1022100b171846261f01c1c5ec1eed58aab5735091a249f2290a46362605047a731326501ffffffff6" +
                "40aa28027c024800b8786aa14eda198ed4c6a4fd89300ff682650476c0ebda8000000004847304402206ee134197e19d67f" +
                "10b7345c960203af7e2bb94057d4ce27d3cb182a5e53ebe2022033d6de62af561652bea167df708c3bd95342320aa6991e0" +
                "d8b1b4801cab08d3401ffffffff68245ca8c661ab9a052d7bad50014de639572177ab6adf87bffc69a34ba096cb00000000" +
                "4847304402203585e2b436dbc9b2c1048f2a44be158bb3d5ca1e29cf8b0fd7b1c7871bdb824e022065a5b29aa164615d05b" +
                "1c7b1ccbf49bd61899818abe972b41bde2f7dea7efd4901ffffffff7c32ce97fce9c9fc342b12c67ac0d841b7a2553d1d9f" +
                "38d9df58cde486a713b50000000049483045022100d02b5643e639faa0f6d2388fdfdf0eade3b32eb4c1bc5fd884d86f171" +
                "0ccd94202200419d89d70293624ef65950117e1cbe08547544b9afbc2482067991c027367d201ffffffff9d55fa0fb3dd4f" +
                "bc201afa8a0d20396797dcba1f2f5254607220dc2c62b5c53200000000494830450220350b8554d5f5b68a9eb1d84737307" +
                "45249bebc96fb1e40ffbd68086a7203b0af022100a3dd12b3d57dddf97d1c526efeaba5c877f72ad1acf48a81f9795890fe" +
                "0f9b7901ffffffffae4527631dc1ae66474bfcc6a2d354c695f15d3eca0bfd2377d3ffe64a43323c000000004a493046022" +
                "100b7fd1070d44d5e38aad0e112727c5270826c92d4a8eb3693f2f8925e865253380221009e2738e820d8ed113d39a94f2a" +
                "6f3442bd8163a836ff9ba7d114d6d71b55f63f01ffffffff39a76c15f06803a463a375b5992778bb6be6e768850cdfbfe34" +
                "8f9b0cfd799fc0000000048473044022038a45686376b7a0f2147daaa35ab1c028f8cff8d2b29ad1c077a3614bb92930a02" +
                "207df26dfcf1ab3bfa2b59e423184cd188b7175f52af640e2d389937cb1cf26b2801ffffffff0140b64aa40b0000001976a" +
                "914024b45c640959021cff61d4dd933949ce601cd4388ac000000000100000001cb89bed73998601d2aecdb869eb621f3b7" +
                "7aedf640ea1103ce5e658f4cde05cf0000000049483045022038ba2f108ff02e76e53cfaad8a6b304e4b18e98ba38da072e" +
                "efc3bcdf7bb3a050221008781f31a775ce5bd08d30d091e6c198693dfda0aaaa80160737bb1c93cefce0f01ffffffff0200" +
                "65cd1d000000001976a91471a134d80f6fa3785c954a4150a714151d35653b88ac008d380c010000001976a9147d30208e8" +
                "3153b76ab80bd6db51ba76c94f33d0b88ac0000000001000000017d0514b3e1e7db2e5b5531637db3f55ea33851ba28398c" +
                "3f9c9d86a04c43ae1b010000008b4830450220313b83ffce249174594eb232334cf3843483457095a3896854586f0b79bca" +
                "841022100c7115ba7f2347d50b9644b31b08da4e7cb0dcd08d43c3b0e1150b1fbbb8056720141044b8b746f267f1527eb31" +
                "8696c1cb62770e9d425da87dafcc4cfec46487a2c1fbdc31f0c283dd003a8d601a0ee80c936d557d678e381e7bc4a2c0731" +
                "29ff82321ffffffff01c025dda8020000001976a914fb53e1a4c7f8ba5a5cb36b28496ae14c4ed9336f88ac000000000100" +
                "000001b7d5dd874dbd54e11b206fedfc359cca3aa45ef6536d252af67f28a6a82f2a3f000000008b483045022100d9035bf" +
                "2112d5fb0cc0475877eace4ff34903ac06d5e1421b24932acadd8e58b022079395377033f290c61ae027c6edffd5ce9e67f" +
                "fab7a8ff9f5e1cc4a5f32f7adf014104a8b93eb5aa45f0d7cbd824d8484bfb31a2a656c5385e548e9ebfa844c060112c6f6" +
                "4c2422694cd74cefff834eb0ea66c0518d8424b730d20ee1beaf049cfc00affffffff02404b4c00000000001976a9140946" +
                "66102aa51936631865efe6fcd375ca95240288acc01805c7000000001976a914f766149545895eb7557cd3ecc2bbf366bdd" +
                "f6aa188ac00000000";
        Block block = new Block(hexToBytes(hex));
        assertEquals(7, block.txNumber());
        assertEquals("000000000003b97cec3e714136796147fb7ac36cd18eba941dffe346b330e7e1", block.getHash());
        assertEquals(
                "txHash: 518db5fdf62e6e5186f326f1867fe5e1fd5deeb428cb1960a48e698aa0e09f3f, " +
                        "size: 192, version: 1, inputs: 1, " +
                        "0: (prevTxHash: cf05de4c8f655ece0311ea40f6ed7ab7f321b69e86dbec2a1d609839d7be89cb, " +
                        "prevOut: 0, " +
                        "scriptSig: (sig(r: 38ba2f108ff02e76e53cfaad8a6b304e4b18e98ba38da072eefc3bcdf7bb3a05, " +
                        "s: 8781f31a775ce5bd08d30d091e6c198693dfda0aaaa80160737bb1c93cefce0f, signHash: 1)), " +
                        "sequence: -1), outputs: 2, 0: (value: 5.00000000, " +
                        "scriptPubKey: (dup hash160 pubKeyHash(1BMpUVjVKbWNSFPjChJpaL4rhtRLojjgjH) " +
                        "equalverify checksig)), 1: (value: 45.00000000, " +
                        "scriptPubKey: (dup hash160 pubKeyHash(1CQw7uz4ZS52D3gPgmhRVbkDrpL3CuinRm) " +
                        "equalverify checksig)), lockTime: 0",
                block.getTx("518db5fdf62e6e5186f326f1867fe5e1fd5deeb428cb1960a48e698aa0e09f3f").toString()
        );
        ArrayList<Transaction.Output> outputs = new ArrayList<>();
        assertTrue(block.searchTxOutput(new Address("18KiH4iBn5VjtiwGyjgAFsEBLn1A8qTauh"), outputs));
        assertTrue(!block.searchTxOutput(new Address("1FP3iFyCueBGMyQHQRQeojqamf1pt4eQVi"), outputs));
        assertTrue(block.searchTxOutput(new Address("1CQw7uz4ZS52D3gPgmhRVbkDrpL3CuinRm"), outputs));
        assertEquals(2, outputs.size());
        assertEquals(
                "value: 50.00000000, scriptPubKey: (dup hash160 pubKeyHash(18KiH4iBn5VjtiwGyjgAFsEBLn1A8qTauh) " +
                        "equalverify checksig)",
                outputs.get(0).toString()
        );
        assertEquals(
                4500000000L,
                outputs.get(1).value
        );

        hex = "01000000013f9fe0a08a698ea46019cb28b4ee5dfde1e57f86f126f386516e2ef6fdb58d51010000008c493046022100c654" +
                "f92e6316537dd2255e300a75a58a8f8c839ab34d28478348ed508e883ac6022100b9057c7c6ed69a984a65c36c24a96704" +
                "47aa9dd96e356e0637b8f62f57d488ef01410482c04d3cdd9604c56745e62cf277265e303a6ead1d2992a8d73e928c775f" +
                "e6f340b805746626d04a14506b8071f01b880123040049ca3a4c0a12d17981698abcffffffff0200e1f505000000001976" +
                "a914c569933942f2d600ac71633ceb5c7e33a84c5a8c88ac00ac4206010000001976a914e393475340403080a07c60384f" +
                "856e98374ff9b388ac00000000";
        Transaction tx = Transaction.fromBytes(hexToBytes(hex));
        assertThrows(IndexOutOfBoundsException.class, () -> tx.verifyInput(1, block));
        assertThrows(IndexOutOfBoundsException.class, () -> tx.verifyInput(-1, block));
        tx.verifyInput(0, block);
        String incorrectHash = "010000000" +
                "14f9fe0a08a698ea46019cb28b4ee5dfde1e57f86f126f386516e2ef6fdb58d51010000008c493046022100c654" +
                "f92e6316537dd2255e300a75a58a8f8c839ab34d28478348ed508e883ac6022100b9057c7c6ed69a984a65c36c24a96704" +
                "47aa9dd96e356e0637b8f62f57d488ef01410482c04d3cdd9604c56745e62cf277265e303a6ead1d2992a8d73e928c775f" +
                "e6f340b805746626d04a14506b8071f01b880123040049ca3a4c0a12d17981698abcffffffff0200e1f505000000001976" +
                "a914c569933942f2d600ac71633ceb5c7e33a84c5a8c88ac00ac4206010000001976a914e393475340403080a07c60384f" +
                "856e98374ff9b388ac00000000";
        assertThrows(
                Message.Exception.class,
                () -> Transaction.fromBytes(hexToBytes(incorrectHash)).verifyInput(0, block)
        );
        String incorrectSignature = "01000000013f9fe0a08a698ea46019cb28b4ee5dfde1e57f86f126f386516e2ef6fdb58d510100" +
                "00008c493046022100c655" +
                "f92e6316537dd2255e300a75a58a8f8c839ab34d28478348ed508e883ac6022100b9057c7c6ed69a984a65c36c24a96704" +
                "47aa9dd96e356e0637b8f62f57d488ef01410482c04d3cdd9604c56745e62cf277265e303a6ead1d2992a8d73e928c775f" +
                "e6f340b805746626d04a14506b8071f01b880123040049ca3a4c0a12d17981698abcffffffff0200e1f505000000001976" +
                "a914c569933942f2d600ac71633ceb5c7e33a84c5a8c88ac00ac4206010000001976a914e393475340403080a07c60384f" +
                "856e98374ff9b388ac00000000";
        assertThrows(
                Message.Exception.class,
                () -> Transaction.fromBytes(hexToBytes(incorrectSignature)).verifyInput(0, block)
        );
    }
}