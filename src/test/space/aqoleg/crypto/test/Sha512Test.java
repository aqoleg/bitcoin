package space.aqoleg.crypto.test;

import org.junit.jupiter.api.Test;
import space.aqoleg.crypto.Sha512;
import space.aqoleg.utils.Converter;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Sha512Test {

    @Test
    void main() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(stream));
        Sha512.main(new String[]{});
        assertEquals("cf83e1357eefb8bdf1542850d66d8007d620e4050b5715dc83f4a921d36ce9ce47d0d13c5d85f2b0ff8318d2877ee"
                + "c2f63b931bd47417a81a538327af927da3e\n", stream.toString());
        stream.reset();
        Sha512.main(new String[]{""});
        assertEquals("cf83e1357eefb8bdf1542850d66d8007d620e4050b5715dc83f4a921d36ce9ce47d0d13c5d85f2b0ff8318d2877ee"
                + "c2f63b931bd47417a81a538327af927da3e\n", stream.toString());
        stream.reset();
        Sha512.main(new String[]{"abc"});
        assertEquals("ddaf35a193617abacc417349ae20413112e6fa4e89a97ea20a9eeee64b55d39a2192992a274fc1a836ba3c23a3fee"
                + "bbd454d4423643ce80e2a9ac94fa54ca49f\n", stream.toString());
        stream.reset();
        Sha512.main(new String[]{"The quick", "brown fox jumps", "over the lazy", "dog"});
        assertEquals("07e547d9586f6a73f73fbac0435ed76951218fb7d0c8d788a309d785436bbb642e93a252a954f23912547d1e8a3b5"
                + "ed6e1bfd7097821233fa0538f3db854fee6\n", stream.toString());
    }

    @Test
    void test() {
        assertThrows(NullPointerException.class, () -> Sha512.getHash(null));
        assertThrows(NullPointerException.class, () -> Sha512.getHash(null, 9, 9));
        assertThrows(IndexOutOfBoundsException.class, () -> Sha512.getHash(new byte[]{1, 1, 1, 1}, 2, 9));

        areEquals(
                "cf83e1357eefb8bdf1542850d66d8007d620e4050b5715dc83f4a921d36ce9ce47d0d13c5d85f2b0ff8318d2877eec2f63"
                        + "b931bd47417a81a538327af927da3e",
                Sha512.getHash(new byte[]{})
        );

        areEquals(
                "cf83e1357eefb8bdf1542850d66d8007d620e4050b5715dc83f4a921d36ce9ce47d0d13c5d85f2b0ff8318d2877eec2f63"
                        + "b931bd47417a81a538327af927da3e",
                Sha512.getHash("".getBytes())
        );

        areEquals(
                "ddaf35a193617abacc417349ae20413112e6fa4e89a97ea20a9eeee64b55d39a2192992a274fc1a836ba3c23a3feebbd45"
                        + "4d4423643ce80e2a9ac94fa54ca49f",
                Sha512.getHash("abc".getBytes())
        );

        byte[] bytes = new byte[100];
        bytes[55] = 'a';
        bytes[56] = 'b';
        bytes[57] = 'c';
        areEquals(
                "ddaf35a193617abacc417349ae20413112e6fa4e89a97ea20a9eeee64b55d39a2192992a274fc1a836ba3c23a3feebbd45"
                        + "4d4423643ce80e2a9ac94fa54ca49f",
                Sha512.getHash(bytes, 55, 3)
        );

        areEquals(
                "204a8fc6dda82f0a0ced7beb8e08a41657c16ef468b228a8279be331a703c33596fd15c13b1b07f9aa1d3bea57789ca031"
                        + "ad85c7a71dd70354ec631238ca3445",
                Sha512.getHash("abcdbcdecdefdefgefghfghighijhijkijkljklmklmnlmnomnopnopq".getBytes())
        );

        areEquals(
                "8e959b75dae313da8cf4f72814fc143f8f7779c6eb9f7fa17299aeadb6889018501d289e4900f7e4331b99dec4b5433ac7"
                        + "d329eeb6dd26545e96e55b874be909",
                Sha512.getHash(("abcdefghbcdefghicdefghijdefghijkefghijklfghijklmghijklmnhijklmnoijklmno" +
                        "pjklmnopqklmnopqrlmnopqrsmnopqrstnopqrstu").getBytes())
        );

        byte[] millionA = new byte[1000000];
        Arrays.fill(millionA, (byte) 'a');
        areEquals(
                "e718483d0ce769644e2e42c7bc15b4638e1f98b13b2044285632a803afa973ebde0ff244877ea60a4cb0432ce577c31beb"
                        + "009c5c2c49aa2e4eadb217ad8cc09b",
                Sha512.getHash(millionA)
        );

        areEquals(
                "07e547d9586f6a73f73fbac0435ed76951218fb7d0c8d788a309d785436bbb642e93a252a954f23912547d1e8a3b5ed6e1"
                        + "bfd7097821233fa0538f3db854fee6",
                Sha512.getHash("The quick brown fox jumps over the lazy dog".getBytes())
        );

        areEquals(
                "93900bb12866efeb0ab60f99825a69e945ef3f3d1379e93118dba8c49768ef812f94b21f7e52eb18fa9a59e8de080fa23a"
                        + "48ba2e9a6c5cfedf03f5da965b07fb",
                Sha512.getHash(("122-byte string-----12345678901234567890123456789012345678901234567890123456789012"
                        + "3456789012345678901234567890123456789012").getBytes())
        );

        areEquals(
                "35f40eb93756b442c4087153c76c3571545026dceac4cbf5a125d0dc8fe0f98ce2bc39337f9082bb8f0abda7c018018985"
                        + "58e8d135eb7f22bfe225335c9bdcde",
                Sha512.getHash(("123-byte string-----12345678901234567890123456789012345678901234567890123456789012"
                        + "34567890123456789012345678901234567890123").getBytes())
        );

        areEquals(
                "ab5df71401ebff6596e24cd95ea57d396fa59a285a1c832f5551304c4123bf91f2f0d0f00db0edaf452ad776b30073142c"
                        + "5d27a21825eeec359ce95a42a11fb0",
                Sha512.getHash(("124-byte string-----12345678901234567890123456789012345678901234567890123456789012"
                        + "345678901234567890123456789012345678901234").getBytes())
        );

        areEquals(
                "40e31ddd7f29389abbfbed61217ad92b38242f6ea4c9cd28009078b80bdad65192f4a03753bdd8910a63ed266891b5fd6c"
                        + "85bef595c122bac6fbb7894e764d7b",
                Sha512.getHash(("125-byte string-----12345678901234567890123456789012345678901234567890123456789012"
                        + "3456789012345678901234567890123456789012345").getBytes())
        );

        areEquals(
                "e60a35187134a6c3321379ff9f801b919479c37bc84ac43eeca34d0ed8253cef56bcceb7f080e61f5d34c1797d301af3b8"
                        + "2d6435fd51d742b6bed29935d25987",
                Sha512.getHash(("126-byte string-----12345678901234567890123456789012345678901234567890123456789012"
                        + "34567890123456789012345678901234567890123456").getBytes())
        );

        areEquals(
                "3fbe1b881fe94eb39dff0176e013ba8f04685516fc7a5b1b3ce05a5e9dbd5a1ccf8aa37bfec80145fb63c2fef2ef26e8c8"
                        + "85f3e2a8f502365f9d258a94d9ac26",
                Sha512.getHash(("127-byte string-----12345678901234567890123456789012345678901234567890123456789012"
                        + "345678901234567890123456789012345678901234567").getBytes())
        );

        areEquals(
                "56f820423b6345cf19dd169b5b57ce6179929eb8b68b15c6b31ac485c5344febeb3431cb6a3239a470de1d5892404312af"
                        + "5022c6f02b03d56b8afa8256b2aa43",
                Sha512.getHash(("128-byte string-----12345678901234567890123456789012345678901234567890123456789012"
                        + "3456789012345678901234567890123456789012345678").getBytes())
        );

        areEquals(
                "3645ed3b516bbf5fa061343b89bdac5b6910a30102f745d0a3b92e1ef128fe024986a759912f2c5807d1d833fbd7733c8f"
                        + "3ab7c8641b13da27f18e0866e3ca88",
                Sha512.getHash(("129-byte string-----12345678901234567890123456789012345678901234567890123456789012"
                        + "34567890123456789012345678901234567890123456789").getBytes())
        );
    }

    private static void areEquals(String correctHash, byte[] hash) {
        assertEquals(correctHash, Converter.bytesToHex(hash, false, false));
    }
}