package com.aqoleg.crypto.test;

import com.aqoleg.Test;
import com.aqoleg.crypto.Sha256;

import java.util.Arrays;

@SuppressWarnings("unused")
public class Sha256Test extends Test {

    public static void main(String[] args) {
        new Sha256Test().testAll();
    }

    public void getHash() {
        assertThrows(NullPointerException.class, () -> Sha256.getHash(null));
        assertThrows(NullPointerException.class, () -> Sha256.getHash(null, 9, 9));
        assertThrows(IndexOutOfBoundsException.class, () -> Sha256.getHash(new byte[]{1, 1, 1, 1}, 2, 9));

        assertEquals(
                "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
                Sha256.getHash(new byte[]{})
        );

        assertEquals(
                "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
                Sha256.getHash("".getBytes())
        );

        assertEquals(
                "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
                Sha256.getHash("abc".getBytes())
        );

        byte[] bytes = new byte[100];
        bytes[55] = 'a';
        bytes[56] = 'b';
        bytes[57] = 'c';
        assertEquals(
                "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
                Sha256.getHash(bytes, 55, 3)
        );

        assertEquals(
                "248d6a61d20638b8e5c026930c3e6039a33ce45964ff2167f6ecedd419db06c1",
                Sha256.getHash("abcdbcdecdefdefgefghfghighijhijkijkljklmklmnlmnomnopnopq".getBytes())
        );

        assertEquals(
                "cf5b16a778af8380036ce59e7b0492370b249b11e8f07a51afac45037afee9d1",
                Sha256.getHash(("abcdefghbcdefghicdefghijdefghijkefghijklfghijklmghijklmnhijklmnoijklmno" +
                        "pjklmnopqklmnopqrlmnopqrsmnopqrstnopqrstu").getBytes())
        );

        byte[] millionA = new byte[1000000];
        Arrays.fill(millionA, (byte) 'a');
        assertEquals(
                "cdc76e5c9914fb9281a1c7e284d73e67f1809a48a497200e046d39ccc7112cd0",
                Sha256.getHash(millionA)
        );

        assertEquals(
                "ab64eff7e88e2e46165e29f2bce41826bd4c7b3552f6b382a9e7d3af47c245f8",
                Sha256.getHash("This is exactly 64 bytes long, not counting the terminating byte".getBytes())
        );

        assertEquals(
                "f08a78cbbaee082b052ae0708f32fa1e50c5c421aa772ba5dbb406a2ea6be342",
                Sha256.getHash("For this sample, this 63-byte string will be used as input data".getBytes())
        );

        assertEquals(
                "d7a8fbb307d7809469ca9abcb0082e4f8d5651e46d3cdb762d02d0bf37c9e592",
                Sha256.getHash(("The quick brown fox jumps over the lazy " + Character.toString((char) 100) +
                        Character.toString((char) 111) + Character.toString((char) 103)).getBytes())
        );

        assertEquals(
                "c642d408b5326a21f41395c7f703327bff8f18ad17f97f903fbdfd1e250306cf",
                Sha256.getHash("62-byte string------123456789012345678901234567890123456789012".getBytes())
        );

        assertEquals(
                "5112c66ca2f03f43678f42b9b84f8f1142e92d9026585db21c77efc691faf445",
                Sha256.getHash("65-byte string------123456789012345678901234567890123456789012345".getBytes())
        );

        assertEquals(
                "16072200d29137f587a52d59f567298e44ee8af5ff424c9311cf0f980b0cfaec",
                Sha256.getHash("some test stuff".getBytes())
        );

        assertEquals(
                "9595c9df90075148eb06860365df33584b75bff782a510c6cd4883a419833d50",
                Sha256.getHash(Sha256.getHash("hello".getBytes()))
        );

        bytes = hexToBytes("0100000001eccf7e3034189b851985d871f91384b8ee357cd47c3024736e5676eb2debb3f2010000001"
                + "976a914010966776006953d5567439e5e39f86a0d273bee88acffffffff01605af405000000001976a91409707252443"
                + "8d003d23a2f23edb65aae1bb3e46988ac0000000001000000");
        assertEquals(
                "9302bda273a887cb40c13e02a50b4071a31fd3aae3ae04021b0b843dd61ad18e",
                Sha256.getHash(Sha256.getHash(bytes))
        );
    }
}