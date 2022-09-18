package com.aqoleg.crypto.test;

import com.aqoleg.Test;
import com.aqoleg.crypto.Ecc;

import java.math.BigInteger;
import java.util.Random;

@SuppressWarnings("unused")
public class EccTest extends Test {

    public static void main(String[] args) {
        new EccTest().testAll();
    }

    public void createRandomPrivateKey() {
        for (int i = 0; i < 100; i++) {
            assertNotThrows(() -> Ecc.checkPrivateKey(Ecc.createRandomPrivateKey()));
        }
    }

    public void checkPrivateKey() {
        assertThrows(NullPointerException.class, () -> Ecc.checkPrivateKey(null));
        assertThrows(Ecc.Exception.class, () -> Ecc.checkPrivateKey(BigInteger.ZERO));
        assertThrows(Ecc.Exception.class, () -> Ecc.checkPrivateKey(BigInteger.TEN.negate()));
        BigInteger d = new BigInteger("fffffffffffffffffffffffffffffffebaaedce6af48a03bbfd25e8cd0364142", 16);
        assertThrows(Ecc.Exception.class, () -> Ecc.checkPrivateKey(d));
        assertNotThrows(() -> Ecc.checkPrivateKey(BigInteger.TEN));
    }

    public void modN() {
        assertThrows(NullPointerException.class, () -> Ecc.modN(null));
        BigInteger a = BigInteger.ZERO;
        assertEquals(0, a.compareTo(Ecc.modN(a)));
        a = new BigInteger("fffffffffffffffffffffffffffffffebaaedce6af48a03bbfd25e8cd036414b", 16);
        assertEquals(0, BigInteger.TEN.compareTo(Ecc.modN(a)));
    }

    public void multiplyG() {
        assertThrows(NullPointerException.class, () -> Ecc.multiplyG(null));
        BigInteger n = new BigInteger("fffffffffffffffffffffffffffffffebaaedce6af48a03bbfd25e8cd0364141", 16);
        Ecc.Point p = Ecc.multiplyG(n);
        assertNull(p.x);
        assertNull(p.y);
        areTheSamePoint(
                Ecc.multiplyG(BigInteger.valueOf(1)),
                "79BE667EF9DCBBAC55A06295CE870B07029BFCDB2DCE28D959F2815B16F81798",
                "483ADA7726A3C4655DA4FBFC0E1108A8FD17B448A68554199C47D08FFB10D4B8"
        );
        areTheSamePoint(
                Ecc.multiplyG(new BigInteger("fffffffffffffffffffffffffffffffebaaedce6af48a03bbfd25e8cd0364142", 16)),
                "79BE667EF9DCBBAC55A06295CE870B07029BFCDB2DCE28D959F2815B16F81798",
                "483ADA7726A3C4655DA4FBFC0E1108A8FD17B448A68554199C47D08FFB10D4B8"
        );
        areTheSamePoint(
                Ecc.multiplyG(BigInteger.valueOf(2)),
                "C6047F9441ED7D6D3045406E95C07CD85C778E4B8CEF3CA7ABAC09B95C709EE5",
                "1AE168FEA63DC339A3C58419466CEAEEF7F632653266D0E1236431A950CFE52A"
        );
        areTheSamePoint(
                Ecc.multiplyG(BigInteger.valueOf(3)),
                "F9308A019258C31049344F85F89D5229B531C845836F99B08601F113BCE036F9",
                "388F7B0F632DE8140FE337E62A37F3566500A99934C2231B6CB9FD7584B8E672"
        );
        areTheSamePoint(
                Ecc.multiplyG(BigInteger.valueOf(4)),
                "E493DBF1C10D80F3581E4904930B1404CC6C13900EE0758474FA94ABE8C4CD13",
                "51ED993EA0D455B75642E2098EA51448D967AE33BFBDFE40CFE97BDC47739922"
        );
        areTheSamePoint(
                Ecc.multiplyG(BigInteger.valueOf(5)),
                "2F8BDE4D1A07209355B4A7250A5C5128E88B84BDDC619AB7CBA8D569B240EFE4",
                "D8AC222636E5E3D6D4DBA9DDA6C9C426F788271BAB0D6840DCA87D3AA6AC62D6"
        );
        areTheSamePoint(
                Ecc.multiplyG(BigInteger.valueOf(6)),
                "FFF97BD5755EEEA420453A14355235D382F6472F8568A18B2F057A1460297556",
                "AE12777AACFBB620F3BE96017F45C560DE80F0F6518FE4A03C870C36B075F297"
        );
        areTheSamePoint(
                Ecc.multiplyG(BigInteger.valueOf(7)),
                "5CBDF0646E5DB4EAA398F365F2EA7A0E3D419B7E0330E39CE92BDDEDCAC4F9BC",
                "6AEBCA40BA255960A3178D6D861A54DBA813D0B813FDE7B5A5082628087264DA"
        );
        areTheSamePoint(
                Ecc.multiplyG(BigInteger.valueOf(16)),
                "E60FCE93B59E9EC53011AABC21C23E97B2A31369B87A5AE9C44EE89E2A6DEC0A",
                "F7E3507399E595929DB99F34F57937101296891E44D23F0BE1F32CCE69616821"
        );
        areTheSamePoint(
                Ecc.multiplyG(BigInteger.valueOf(20)),
                "4CE119C96E2FA357200B559B2F7DD5A5F02D5290AFF74B03F3E471B273211C97",
                "12BA26DCB10EC1625DA61FA10A844C676162948271D96967450288EE9233DC3A"
        );
        areTheSamePoint(
                Ecc.multiplyG(BigInteger.valueOf(21)),
                "352BBF4A4CDD12564F93FA332CE333301D9AD40271F8107181340AEF25BE59D5",
                "321EB4075348F534D59C18259DDA3E1F4A1B3B2E71B1039C67BD3D8BCF81998C"
        );
        areTheSamePoint(
                Ecc.multiplyG(new BigInteger("AA5E28D6A97A2479A65527F7290311A3624D4CC0FA1578598EE3C2613BF99522", 16)),
                "34F9460F0E4F08393D192B3C5133A6BA099AA0AD9FD54EBCCFACDFA239FF49C6",
                "B71EA9BD730FD8923F6D25A7A91E7DD7728A960686CB5A901BB419E0F2CA232"
        );
        areTheSamePoint(
                Ecc.multiplyG(new BigInteger("7E2B897B8CEBC6361663AD410835639826D590F393D90A9538881735256DFAE3", 16)),
                "D74BF844B0862475103D96A611CF2D898447E288D34B360BC885CB8CE7C00575",
                "131C670D414C4546B88AC3FF664611B1C38CEB1C21D76369D7A7A0969D61D97D"
        );
        areTheSamePoint(
                Ecc.multiplyG(new BigInteger("376A3A2CDCD12581EFFF13EE4AD44C4044B8A0524C42422A7E1E181E4DEECCEC", 16)),
                "14890E61FCD4B0BD92E5B36C81372CA6FED471EF3AA60A3E415EE4FE987DABA1",
                "297B858D9F752AB42D3BCA67EE0EB6DCD1C2B7B0DBE23397E66ADC272263F982"
        );
        areTheSamePoint(
                Ecc.multiplyG(new BigInteger("1B22644A7BE026548810C378D0B2994EEFA6D2B9881803CB02CEFF865287D1B9", 16)),
                "F73C65EAD01C5126F28F442D087689BFA08E12763E0CEC1D35B01751FD735ED3",
                "F449A8376906482A84ED01479BD18882B919C140D638307F0C0934BA12590BDE"
        );
        areTheSamePoint(
                Ecc.multiplyG(new BigInteger("ebb2c082fd7727890a28ac82f6bdf97bad8de9f5d7c9028692de1a255cad3e0f", 16)),
                "779DD197A5DF977ED2CF6CB31D82D43328B790DC6B3B7D4437A427BD5847DFCD",
                "E94B724A555B6D017BB7607C3E3281DAF5B1699D6EF4124975C9237B917D426F"
        );
    }

    public void createPoint() {
        assertThrows(NullPointerException.class, () -> Ecc.createPoint(null, BigInteger.TEN));
        assertThrows(NullPointerException.class, () -> Ecc.createPoint(BigInteger.TEN, null));
        BigInteger p = new BigInteger("fffffffffffffffffffffffffffffffffffffffffffffffffffffffefffffc2f", 16);
        assertThrows(Ecc.Exception.class, () -> Ecc.createPoint(BigInteger.TEN.negate(), BigInteger.TEN));
        assertThrows(Ecc.Exception.class, () -> Ecc.createPoint(p, BigInteger.TEN));
        assertThrows(Ecc.Exception.class, () -> Ecc.createPoint(BigInteger.TEN, BigInteger.TEN.negate()));
        assertThrows(Ecc.Exception.class, () -> Ecc.createPoint(BigInteger.TEN, p));
        assertThrows(Ecc.Exception.class, () -> Ecc.createPoint(BigInteger.TEN, BigInteger.TEN));

        Ecc.Point point = Ecc.createPoint(
                new BigInteger("779DD197A5DF977ED2CF6CB31D82D43328B790DC6B3B7D4437A427BD5847DFCD", 16),
                new BigInteger("E94B724A555B6D017BB7607C3E3281DAF5B1699D6EF4124975C9237B917D426F", 16)
        );
        areTheSamePoint(
                point,
                "779DD197A5DF977ED2CF6CB31D82D43328B790DC6B3B7D4437A427BD5847DFCD",
                "E94B724A555B6D017BB7607C3E3281DAF5B1699D6EF4124975C9237B917D426F"
        );

        point = Ecc.createPoint(
                new BigInteger("39AAF690D50BDF25EC5E44CC37B6C0295058F6152F5081B67B95A0C5D4B0ECEE", 16),
                new BigInteger("56E9539C948D4E984C8D9870E2F412FC94B34DA59A4FD5ADFBC8ECD0AABDDAD0", 16)
        );
        areTheSamePoint(
                point,
                "39AAF690D50BDF25EC5E44CC37B6C0295058F6152F5081B67B95A0C5D4B0ECEE",
                "56E9539C948D4E984C8D9870E2F412FC94B34DA59A4FD5ADFBC8ECD0AABDDAD0"
        );
    }

    public void createPointByX() {
        assertThrows(NullPointerException.class, () -> Ecc.createPoint(null, true));
        BigInteger p = new BigInteger("fffffffffffffffffffffffffffffffffffffffffffffffffffffffefffffc2f", 16);
        assertThrows(Ecc.Exception.class, () -> Ecc.createPoint(BigInteger.TEN.negate(), true));
        assertThrows(Ecc.Exception.class, () -> Ecc.createPoint(p, true));
        assertThrows(Ecc.Exception.class, () -> Ecc.createPoint(BigInteger.TEN, true));

        String x = "79BE667EF9DCBBAC55A06295CE870B07029BFCDB2DCE28D959F2815B16F81798";
        assertEquals(
                "483ADA7726A3C4655DA4FBFC0E1108A8FD17B448A68554199C47D08FFB10D4B8",
                Ecc.createPoint(new BigInteger(x, 16), true).y.toString(16).toUpperCase()
        );
        x = "C6047F9441ED7D6D3045406E95C07CD85C778E4B8CEF3CA7ABAC09B95C709EE5";
        assertEquals(
                "1AE168FEA63DC339A3C58419466CEAEEF7F632653266D0E1236431A950CFE52A",
                Ecc.createPoint(new BigInteger(x, 16), true).y.toString(16).toUpperCase()
        );
        x = "F9308A019258C31049344F85F89D5229B531C845836F99B08601F113BCE036F9";
        assertEquals(
                "388F7B0F632DE8140FE337E62A37F3566500A99934C2231B6CB9FD7584B8E672",
                Ecc.createPoint(new BigInteger(x, 16), true).y.toString(16).toUpperCase()
        );
        x = "E493DBF1C10D80F3581E4904930B1404CC6C13900EE0758474FA94ABE8C4CD13";
        assertEquals(
                "51ED993EA0D455B75642E2098EA51448D967AE33BFBDFE40CFE97BDC47739922",
                Ecc.createPoint(new BigInteger(x, 16), true).y.toString(16).toUpperCase()
        );
        x = "2F8BDE4D1A07209355B4A7250A5C5128E88B84BDDC619AB7CBA8D569B240EFE4";
        assertEquals(
                "D8AC222636E5E3D6D4DBA9DDA6C9C426F788271BAB0D6840DCA87D3AA6AC62D6",
                Ecc.createPoint(new BigInteger(x, 16), true).y.toString(16).toUpperCase()
        );
        x = "FFF97BD5755EEEA420453A14355235D382F6472F8568A18B2F057A1460297556";
        assertEquals(
                "AE12777AACFBB620F3BE96017F45C560DE80F0F6518FE4A03C870C36B075F297",
                Ecc.createPoint(new BigInteger(x, 16), false).y.toString(16).toUpperCase()
        );
        x = "E60FCE93B59E9EC53011AABC21C23E97B2A31369B87A5AE9C44EE89E2A6DEC0A";
        assertEquals(
                "F7E3507399E595929DB99F34F57937101296891E44D23F0BE1F32CCE69616821",
                Ecc.createPoint(new BigInteger(x, 16), false).y.toString(16).toUpperCase()
        );

        Random random = new Random();
        BigInteger d;
        for (int i = 0; i < 100; i++) {
            if (i % 20 == 0) {
                System.out.print(".");
            }
            d = new BigInteger(250, random);
            Ecc.Point point = Ecc.multiplyG(d);
            assertEquals(0, point.y.compareTo(Ecc.createPoint(point.x, !point.y.testBit(0)).y));
        }
    }

    public void verify() {
        byte[] message = hexToBytes("9302bda273a887cb40c13e02a50b4071a31fd3aae3ae04021b0b843dd61ad18e");
        Ecc.Point publicKey = Ecc.createPoint(
                new BigInteger("50863ad64a87ae8a2fe83c1af1a8403cb53f53e486d8511dad8a04887e5b2352", 16),
                new BigInteger("2cd470243453a299fa9e77237716103abc11a1df38855ed6f2ee187e9c582ba6", 16)
        );
        BigInteger[] signature = new BigInteger[]{
                new BigInteger("009e0339f72c793a89e664a8a932df073962a3f84eda0bd9e02084a6a9567f75aa", 16),
                new BigInteger("00bd9cbaca2e5ec195751efdfac164b76250b1e21302e51ca86dd7ebd7020cdc06", 16)
        };
        assertThrows(NullPointerException.class, () -> Ecc.verify(null, publicKey, signature));
        assertThrows(NullPointerException.class, () -> Ecc.verify(message, null, signature));
        assertThrows(NullPointerException.class, () -> Ecc.verify(message, publicKey, null));
        assertThrows(
                ArrayIndexOutOfBoundsException.class,
                () -> Ecc.verify(message, publicKey, new BigInteger[]{BigInteger.ONE})
        );
        byte[] longMessage = new byte[100];
        longMessage[0] = 1;
        assertThrows(Ecc.Exception.class, () -> Ecc.verify(longMessage, publicKey, signature));
        BigInteger big = new BigInteger("fffffffffffffffffffffffffffffffebaaedce6af48a03bbfd25e8cd0364141", 16);
        assertThrows(
                Ecc.Exception.class,
                () -> Ecc.verify(message, publicKey, new BigInteger[]{BigInteger.ZERO, BigInteger.TEN})
        );
        assertThrows(
                Ecc.Exception.class,
                () -> Ecc.verify(message, publicKey, new BigInteger[]{big, BigInteger.TEN})
        );
        assertThrows(
                Ecc.Exception.class,
                () -> Ecc.verify(message, publicKey, new BigInteger[]{BigInteger.TEN, BigInteger.ZERO})
        );
        assertThrows(
                Ecc.Exception.class,
                () -> Ecc.verify(message, publicKey, new BigInteger[]{BigInteger.TEN, big})
        );
        byte[] otherMessage = hexToBytes("9302bda273a887cb40c13e02a50b4071a31fd3aae3ae04021b0b843dd61ad18f");
        assertThrows(Ecc.Exception.class, () -> Ecc.verify(otherMessage, publicKey, signature));
        assertThrows(Ecc.Exception.class, () -> Ecc.verify(message, Ecc.multiplyG(BigInteger.TEN), signature));
        BigInteger otherR = new BigInteger("009e0339f72c793a89e664a8a932df073962a3f84eda0bd9e02084a6a9567f75ab", 16);
        BigInteger otherS = new BigInteger("00bd9cbaca2e5ec195751efdfac164b76250b1e21302e51ca86dd7ebd7020cdc05", 16);
        assertThrows(Ecc.Exception.class, () -> Ecc.verify(message, publicKey, new BigInteger[]{otherR, signature[1]}));
        assertThrows(Ecc.Exception.class, () -> Ecc.verify(message, publicKey, new BigInteger[]{signature[0], otherS}));
        assertNotThrows(() -> Ecc.verify(message, publicKey, signature));

        byte[] messageB = hexToBytes("4b688df40bcedbe641ddb16ff0a1842d9c67ea1c3bf63f3e0471baa664531d1a");
        Ecc.Point publicKeyB = Ecc.createPoint(
                new BigInteger("779dd197a5df977ed2cf6cb31d82d43328b790dc6b3b7d4437a427bd5847dfcd", 16),
                new BigInteger("e94b724a555b6d017bb7607c3e3281daf5b1699d6ef4124975c9237b917d426f", 16)
        );
        BigInteger[] signatureB = new BigInteger[]{
                new BigInteger("241097efbf8b63bf145c8961dbdf10c310efbb3b2676bbc0f8b08505c9e2f795", 16),
                new BigInteger("021006b7838609339e8b415a7f9acb1b661828131aef1ecbc7955dfb01f3ca0e", 16)
        };
        assertThrows(Ecc.Exception.class, () -> Ecc.verify(message, publicKey, signatureB));
        assertThrows(Ecc.Exception.class, () -> Ecc.verify(message, publicKeyB, signature));
        assertThrows(Ecc.Exception.class, () -> Ecc.verify(message, publicKeyB, signatureB));
        assertThrows(Ecc.Exception.class, () -> Ecc.verify(messageB, publicKey, signatureB));
        assertThrows(Ecc.Exception.class, () -> Ecc.verify(messageB, publicKey, signature));
        assertThrows(Ecc.Exception.class, () -> Ecc.verify(messageB, publicKeyB, signature));
        assertNotThrows(() -> Ecc.verify(messageB, publicKeyB, signatureB));
    }

    public void signVerifyTest() {
        assertThrows(NullPointerException.class, () -> Ecc.sign(null, BigInteger.ONE));
        assertThrows(NullPointerException.class, () -> Ecc.sign(new byte[]{8, 9}, null));
        assertThrows(Ecc.Exception.class, () -> Ecc.sign(new byte[]{8, 9}, BigInteger.TEN.negate()));
        BigInteger d = new BigInteger("fffffffffffffffffffffffffffffffebaaedce6af48a03bbfd25e8cd0364142", 16);
        assertThrows(Ecc.Exception.class, () -> Ecc.sign(new byte[]{8, 9}, d));

        byte[] longMessage = new byte[33];
        longMessage[0] = 1;
        assertThrows(Ecc.Exception.class, () -> Ecc.sign(longMessage, BigInteger.TEN));

        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            if (i % 4 == 0) {
                System.out.print(".");
            }
            byte[] message = new BigInteger(250, random).toByteArray();
            BigInteger privateKey = new BigInteger(160, random);
            Ecc.Point publicKey = Ecc.multiplyG(privateKey);
            BigInteger[] signature = Ecc.sign(message, privateKey);
            assertThrows(
                    Ecc.Exception.class,
                    () -> Ecc.verify(message, publicKey, new BigInteger[]{signature[1], signature[0]})
            );
            assertNotThrows(() -> Ecc.verify(message, publicKey, signature));
        }
    }

    private void areTheSamePoint(Ecc.Point point, String x, String y) {
        assertEquals(x, point.x.toString(16).toUpperCase());
        assertEquals(y, point.y.toString(16).toUpperCase());
    }
}