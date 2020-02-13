package space.aqoleg.crypto.test;

import org.junit.jupiter.api.Test;
import space.aqoleg.crypto.Shake128;
import space.aqoleg.utils.Converter;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Shake128Test {

    @Test
    void main() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(stream));
        Shake128.main(new String[]{});
        assertEquals("7f9c2ba4e88f827d616045507605853ed73b8093f6efbc88eb1a6eacfa66ef26\n", stream.toString());
        stream.reset();
        Shake128.main(new String[]{""});
        assertEquals("7f9c2ba4e88f827d616045507605853ed73b8093f6efbc88eb1a6eacfa66ef26\n", stream.toString());
        stream.reset();
        Shake128.main(new String[]{"The quick brown fox", "jumps over the lazy dog"});
        assertEquals("f4202e3c5852f9182a0430fd8144f0a74b95e7417ecae17db0f8cfeed0e3e66e\n", stream.toString());
    }

    @Test
    void test() {
        assertThrows(NullPointerException.class, () -> Shake128.getHash(2, null));
        assertThrows(NullPointerException.class, () -> Shake128.getHash(2, null, 9, 9));
        assertThrows(IndexOutOfBoundsException.class, () -> Shake128.getHash(2, new byte[]{0, 9, 9, 9}, 1, 89));
        assertThrows(UnsupportedOperationException.class, () -> Shake128.getHash(-2, "".getBytes()));
        assertThrows(UnsupportedOperationException.class, () -> Shake128.getHash(444444, "".getBytes()));

        areEquals(
                "7f9c2ba4e88f827d616045507605853ed73b8093f6efbc88eb1a6eacfa66ef26",
                Shake128.getHash(32, new byte[]{})
        );

        areEquals(
                "7f9c2ba4e88f827d616045507605853ed73b8093f6efbc88eb1a6eacfa66ef26",
                Shake128.getHash(32, "".getBytes())
        );

        areEquals(
                "f4202e3c5852f9182a0430fd8144f0a74b95e7417ecae17db0f8cfeed0e3e66e",
                Shake128.getHash(32, "The quick brown fox jumps over the lazy dog".getBytes())
        );

        byte[] bytes = new byte[200];
        byte[] msg = "The quick brown fox jumps over the lazy dof".getBytes();
        System.arraycopy(msg, 0, bytes, 34, msg.length);
        areEquals(
                "853f4538be0db9621a6cea659a06c1107b1f83f02b13d18297bd39d7411cf10c",
                Shake128.getHash(32, bytes, 34, msg.length)
        );

        areEquals(
                "96e753c4f374730de6a18e9725d25fce4297eee571f1a659e118df33eae506c6",
                Shake128.getHash(32, "Yoda said, Do or do not. There is no try.".getBytes())
        );

        Arrays.fill(bytes, (byte) 0b10100011);
        areEquals(
                "131ab8d2b594946b9c81333f9bb6e0ce75c3b93104fa3469d3917457385da037",
                Shake128.getHash(32, bytes)
        );

        Arrays.fill(bytes, (byte) '3');
        areEquals(
                "2592763d0723505d398b03fe957986922e6272598e8e791b92fc8e1225841c66",
                Shake128.getHash(32, bytes)
        );

        // https://csrc.nist.gov/csrc/media/projects/cryptographic-standards-and-guidelines/documents/examples/shake128_msg0.pdf
        areEquals(
                "7f9c2ba4e88f827d616045507605853ed73b8093f6efbc88eb1a6eacfa66ef26" +
                        "3cb1eea988004b93103cfb0aeefd2a686e01fa4a58e8a3639ca8a1e3f9ae57e2" +
                        "35b8cc873c23dc62b8d260169afa2f75ab916a58d974918835d25e6a435085b2" +
                        "badfd6dfaac359a5efbb7bcc4b59d538df9a04302e10c8bc1cbf1a0b3a5120ea" +
                        "17cda7cfad765f5623474d368ccca8af0007cd9f5e4c849f167a580b14aabdef" +
                        "aee7eef47cb0fca9767be1fda69419dfb927e9df07348b196691abaeb580b32d" +
                        "ef58538b8d23f87732ea63b02b4fa0f4873360e2841928cd60dd4cee8cc0d4c9" +
                        "22a96188d032675c8ac850933c7aff1533b94c834adbb69c6115bad4692d8619" +
                        "f90b0cdf8a7b9c264029ac185b70b83f2801f2f4b3f70c593ea3aeeb613a7f1b" +
                        "1de33fd75081f592305f2e4526edc09631b10958f464d889f31ba010250fda7f" +
                        "1368ec2967fc84ef2ae9aff268e0b1700affc6820b523a3d917135f2dff2ee06" +
                        "bfe72b3124721d4a26c04e53a75e30e73a7a9c4a95d91c55d495e9f51dd0b5e9" +
                        "d83c6d5e8ce803aa62b8d654db53d09b8dcff273cdfeb573fad8bcd45578bec2" +
                        "e770d01efde86e721a3f7c6cce275dabe6e2143f1af18da7efddc4c7b70b5e34" +
                        "5db93cc936bea323491ccb38a388f546a9ff00dd4e1300b9b2153d2041d205b4" +
                        "43e41b45a653f2a5c4492c1add544512dda2529833462b71a41a45be97290b6f",
                Shake128.getHash(512, "".getBytes())
        );

        areEquals(
                "7f9c2ba4e88f827d616045507605853ed73b8093f6efbc88eb1a6eacfa66ef26" +
                        "3cb1eea988004b93103cfb0aeefd2a686e01fa4a58e8a3639ca8a1e3f9ae57e2" +
                        "35b8cc873c23dc62b8d260169afa2f75ab916a58d974918835d25e6a435085b2" +
                        "badfd6dfaac359a5efbb7bcc4b59d538df9a04302e10c8bc1cbf1a0b3a5120ea" +
                        "17cda7cfad765f5623474d368ccca8af0007cd9f5e4c849f167a580b14aabdef" +
                        "aee7eef47cb0fca9767be1fda69419dfb927e9df07348b196691abaeb580b32d" +
                        "ef58538b8d23f87732ea63b02b4fa0f4873360e2841928cd60dd4cee8cc0d4c9" +
                        "22a96188d032675c8ac850933c7aff1533b94c834adbb69c6115bad4692d8619" +
                        "f90b0cdf8a7b9c264029ac185b70b83f2801f2f4b3f70c593ea3aeeb613a7f1b" +
                        "1de33fd75081f592305f2e4526edc09631b10958f464d889f31ba010250fda7f" +
                        "1368ec2967fc84ef2ae9aff268e0b1700affc6820b523a3d917135f2dff2ee06" +
                        "bfe72b3124721d4a26c04e53a75e30e73a7a9c4a95d91c55d495e9f51dd0b5e9" +
                        "d83c6d5e8ce803aa62b8d654db53d09b8dcff273cdfeb573fad8bcd45578bec2" +
                        "e770d01efde86e721a3f7c6cce275dabe6e2143f1af18da7efddc4c7b70b5e34" +
                        "5db93cc936bea323491ccb38a388f546a9ff00dd4e1300b9",
                Shake128.getHash(472, "".getBytes())
        );

        areEquals(
                "7f9c2ba4e88f827d616045507605853ed73b8093f6efbc88eb1a6eacfa66ef26" +
                        "3cb1eea988004b93103cfb0aeefd2a686e01fa4a58e8a3639ca8a1e3f9ae57e2" +
                        "35b8cc873c23dc62b8d260169afa2f75ab916a58d974918835d25e6a435085b2" +
                        "badfd6dfaac359a5efbb7bcc4b59d538df9a04302e10c8bc1cbf1a0b3a5120ea" +
                        "17cda7cfad765f5623474d368ccca8af0007cd9f5e4c849f167a580b14aabdef" +
                        "aee7eef47cb0fca9767be1fda69419dfb927e9df07348b196691abaeb580b32d" +
                        "ef58538b8d23f87732ea63b02b4fa0f4873360e2841928cd60dd4cee8cc0d4c9" +
                        "22a96188d032675c8ac850933c7aff1533b94c834adbb69c6115bad4692d8619" +
                        "f90b0cdf8a7b9c264029ac185b70b83f2801f2f4b3f70c593ea3aeeb613a7f1b" +
                        "1de33fd75081f592305f2e4526edc09631b10958f464d889f31ba010250fda7f" +
                        "1368ec2967fc84ef2ae9aff268e0b1700affc6820b523a3d917135f2dff2ee06" +
                        "bfe72b3124721d4a26c04e53a75e30e73a7a9c4a95d91c55d495e9f51dd0b5e9" +
                        "d83c6d5e8ce803aa62b8d654db53d09b",
                Shake128.getHash(400, "".getBytes())
        );

        areEquals(
                "b4bbba253fa1ffa538d80223b5e537430aa793e6dca390242f5ac5ef",
                Shake128.getHash(28, ("162-byte string-----1234567890123456789012345678901234567890"
                        + "12345678901234567890123456789012345678901234567890"
                        + "1234567890123456789012345678901234567890123456789012").getBytes())
        );

        areEquals(
                "255f883a4bc2128d4c7322dcd6070b439449263b111227fd1cbe4da4",
                Shake128.getHash(28, ("163-byte string-----1234567890123456789012345678901234567890"
                        + "12345678901234567890123456789012345678901234567890"
                        + "12345678901234567890123456789012345678901234567890123").getBytes())
        );

        areEquals(
                "3c77a5ad18e941d721d3e5213f4cc9c11605ffcab635dab5b372b6a44c",
                Shake128.getHash(29, ("164-byte string-----1234567890123456789012345678901234567890"
                        + "12345678901234567890123456789012345678901234567890"
                        + "123456789012345678901234567890123456789012345678901234").getBytes())
        );

        areEquals(
                "ceb1cee845aa4985103f48f7496ba612f0a058ef23eb61f5442068ed",
                Shake128.getHash(28, ("165-byte string-----1234567890123456789012345678901234567890"
                        + "12345678901234567890123456789012345678901234567890"
                        + "1234567890123456789012345678901234567890123456789012345").getBytes())
        );

        areEquals(
                "09e06adf4f9cd515cda05d25360ac910a527cf8a897c08cc01c2ae22",
                Shake128.getHash(28, ("166-byte string-----1234567890123456789012345678901234567890"
                        + "12345678901234567890123456789012345678901234567890"
                        + "12345678901234567890123456789012345678901234567890123456").getBytes())
        );

        areEquals(
                "8a29e8cec3889342546f6ab9048966ca994d331c790434e230f4cc79",
                Shake128.getHash(28, ("167-byte string-----1234567890123456789012345678901234567890"
                        + "12345678901234567890123456789012345678901234567890"
                        + "123456789012345678901234567890123456789012345678901234567").getBytes())
        );

        areEquals(
                "3ae7a623035506e0fb23fdb409ebbd8595ba603483356df665369928",
                Shake128.getHash(28, ("168-byte string-----1234567890123456789012345678901234567890"
                        + "12345678901234567890123456789012345678901234567890"
                        + "1234567890123456789012345678901234567890123456789012345678").getBytes())
        );

        areEquals(
                "40cdf5ec3b05932befc359124ff5781d76b30a108f77821ade58c1f7799ce5e7cac42f1ca5720454f056e2d4f2d80996b893",
                Shake128.getHash(50, ("169-byte string-----1234567890123456789012345678901234567890"
                        + "12345678901234567890123456789012345678901234567890"
                        + "12345678901234567890123456789012345678901234567890123456789").getBytes())
        );
    }

    private static void areEquals(String correctHash, byte[] hash) {
        assertEquals(correctHash, Converter.bytesToHex(hash, false, false));
    }
}