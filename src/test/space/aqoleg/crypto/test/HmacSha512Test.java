package space.aqoleg.crypto.test;

import org.junit.jupiter.api.Test;
import space.aqoleg.crypto.HmacSha512;
import space.aqoleg.utils.Converter;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HmacSha512Test {

    @Test
    void main() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(stream));
        HmacSha512.main(new String[]{});
        assertEquals("b936cee86c9f87aa5d3c6f2e84cb5a4239a5fe50480a6ec66b70ab5b1f4ac6730c6c515421b327ec1d69402e53dfb"
                + "49ad7381eb067b338fd7b0cb22247225d47\n", stream.toString());
        stream.reset();
        HmacSha512.main(new String[]{"", "", "s"});
        assertEquals("b936cee86c9f87aa5d3c6f2e84cb5a4239a5fe50480a6ec66b70ab5b1f4ac6730c6c515421b327ec1d69402e53dfb"
                + "49ad7381eb067b338fd7b0cb22247225d47\n", stream.toString());
        stream.reset();
        HmacSha512.main(new String[]{"The quick brown fox jumps over the lazy dog", "key"});
        assertEquals("b42af09057bac1e2d41708e48a902e09b5ff7f12ab428a4fe86653c73dd248fb82f948a549f7b791a5b41915ee4d1"
                + "ec3935357e4e2317250d0372afa2ebeeb3a\n", stream.toString());
    }

    @Test
    void test() {
        assertThrows(NullPointerException.class, () -> HmacSha512.getMac(null, new byte[]{1, 1, 1, 1}));
        assertThrows(NullPointerException.class, () -> HmacSha512.getMac(new byte[]{1, 1, 1, 1}, null));

        areEquals(
                "b936cee86c9f87aa5d3c6f2e84cb5a4239a5fe50480a6ec66b70ab5b1f4ac6730c6c515421b327ec1d69402e53dfb49ad7"
                        + "381eb067b338fd7b0cb22247225d47",
                HmacSha512.getMac(new byte[]{}, new byte[]{})
        );

        areEquals(
                "b42af09057bac1e2d41708e48a902e09b5ff7f12ab428a4fe86653c73dd248fb82f948a549f7b791a5b41915ee4d1ec393"
                        + "5357e4e2317250d0372afa2ebeeb3a",
                HmacSha512.getMac("The quick brown fox jumps over the lazy dog".getBytes(), "key".getBytes())
        );

        byte[] key = new byte[20];
        Arrays.fill(key, (byte) 0xb);
        areEquals(
                "87aa7cdea5ef619d4ff0b4241a1d6cb02379f4e2ce4ec2787ad0b30545e17cdedaa833b7d6b8a702038b274eaea3f4e4be"
                        + "9d914eeb61f1702e696c203a126854",
                HmacSha512.getMac("Hi There".getBytes(), key)
        );

        areEquals(
                "fa633a14dfe8b28492a869326d4db52d76715443618adb49dd90104bdc306ef3c8364cb5fefca19454c41c63751444864f"
                        + "9a7d810cee507e119224cd4cb2e24a",
                HmacSha512.getMac("message".getBytes(), ("122-byte string-----1234567890123456789012345678901234567"
                        + "89012345678901234567890123456789012345678901234567890123456789012").getBytes())
        );

        areEquals(
                "8b638c4f60dbef584d730234be69ae0649faa2419aca6ddf5361643bbdb58aa6d6df8e8a936c588115faf755e9df587ee3"
                        + "3e46542f97846f768f106213d98d3b",
                HmacSha512.getMac("message".getBytes(), ("128-byte string-----1234567890123456789012345678901234567"
                        + "89012345678901234567890123456789012345678901234567890123456789012345678").getBytes())
        );

        areEquals(
                "fdb623c4769ef4fd9b730c23b2a11318f1e084e4e9e0ede4b6c265e955af9e19e0a4d32a2780e38a25e87595be4f490369"
                        + "a8e8ae473df1ca17825ef59d252001",
                HmacSha512.getMac("message".getBytes(), ("129-byte string-----1234567890123456789012345678901234567"
                        + "890123456789012345678901234567890123456789012345678901234567890123456789").getBytes())
        );

        byte[] bytes = ("140-byte string-----1234567890123456789012345678901234567890123456789012345678901234567890"
                + "12345678901234567890123456789012345678901234567890").getBytes();
        areEquals(
                "63ceb0a617b0bf809fccacb4090ce885ee74c6e5c5d2c5d45e84e6d9669099dac8be1394d7ab7e54b7e3af5d775f927aeb"
                        + "14795a96f2c38d2c27579d09c38788",
                HmacSha512.getMac(bytes, bytes)
        );

        areEquals(
                "7b6b510e10c0f01e7fa70b7b291f900f95c6336858e69e36c36037792c1b6c96d35d8ee2dbaaa8346e2d52f7c6cd133377"
                        + "e509cbe3ae0915e9f0c2b85afdb52b",
                HmacSha512.getMac("a Hash-based message authentication code (HMAC)".getBytes(), "Secret key".getBytes())
        );

        areEquals(
                "f444116aee8627fac2612d8220ed25c031d2efa41acd1df7ee0d27dce47467d561d4e1a08e88bc97214be747d057b3687d"
                        + "8398fb86f0e028ac5906a23ccc8d4e",
                HmacSha512.getMac(("the loooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
                        + "oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
                        + "oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
                        + "oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
                        + "ooooooooooooooooooooooong message").getBytes(), "this is the key".getBytes())
        );

        areEquals(
                "f7b89dc9a77df79f7411eed2591a229b07451b57f48cd6631171f781fd563f418fe11366c80556e1c4fec991f79cef29b5"
                        + "8c28bb7d8df31fd1f1976cdad7948a",
                HmacSha512.getMac("TEST MESSAGE".getBytes(), "TEST KEY".getBytes())
        );

        areEquals(
                "939be05b51f5aceb5de75954f9d9b2077ff84350f3b752db92ee7386958af2296de6781a726c18c3cb8b97b3f2a3274717"
                        + "aebfdbaf50bdde4b8ebf52304bbfca",
                HmacSha512.getMac("abcdef".getBytes(), "0123456789".getBytes())
        );

        areEquals(
                "8a22f4c4e3fac004ef15491a8a7f42413130312b78a318509dad271e7fe843b183ec71b8b59b5873c34c2c9a630b084dad"
                        + "44482d4c7a24a74f975ba9cc5ed888",
                HmacSha512.getMac(("939be05b51f5aceb5de75954f9d9b2077ff84350f3b752db92ee7386958af2296de6781a726c18c"
                        + "3cb8b97b3f2a3274717aebfdbaf50bdde4b8ebf52304bbfca939be05b51f5aceb5de75954f9d9b2077ff8435"
                        + "0f3b752db92ee7386958af2296de6781a726c18c3cb8b97b3f2a3274717aebfdbaf50bdde4b8ebf52304bbfc"
                        + "a").getBytes(), ("939be05b51f5aceb5de75954f9d9b2077ff84350f3b752db92ee7386958af2296de678"
                        + "1a726c18c3cb8b97b3f2a3274717aebfdbaf50bdde4b8ebf52304bbfca939be05b51f5aceb5de75954f9d9b2"
                        + "077ff84350f3b752db92ee7386958af2296de6781a726c18c3cb8b97b3f2a3274717aebfdbaf50bdde4b8ebf"
                        + "52304bbfca").getBytes())
        );
    }

    private static void areEquals(String correctMac, byte[] mac) {
        assertEquals(correctMac, Converter.bytesToHex(mac, false, false));
    }
}