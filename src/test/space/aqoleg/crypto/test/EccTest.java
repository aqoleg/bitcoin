package space.aqoleg.crypto.test;

import org.junit.jupiter.api.Test;
import space.aqoleg.crypto.Ecc;
import space.aqoleg.utils.Converter;

import java.math.BigInteger;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class EccTest {

    @Test
    void getEcc() {
        assertThrows(
                NullPointerException.class,
                () -> Ecc.getEcc(null, BigInteger.ZERO, BigInteger.ONE)
        );
        assertThrows(
                NullPointerException.class,
                () -> Ecc.getEcc(BigInteger.ZERO, null, BigInteger.ONE)
        );
        assertThrows(
                NullPointerException.class,
                () -> Ecc.getEcc(BigInteger.ZERO, BigInteger.ONE, null)
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> Ecc.getEcc(BigInteger.ZERO, BigInteger.ZERO, BigInteger.ONE)
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> Ecc.getEcc(BigInteger.valueOf(-3), BigInteger.valueOf(2), BigInteger.ONE)
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> Ecc.getEcc(BigInteger.valueOf(3), BigInteger.ZERO, BigInteger.valueOf(62))
        );
        assertNotNull(Ecc.getEcc(BigInteger.valueOf(3), BigInteger.ZERO, BigInteger.valueOf(61)));

        BigInteger a = new BigInteger("6127C24C05F38A0AAAF65C0EF02C", 16);
        BigInteger b = new BigInteger("51DEF1815DB5ED74FCC34C85D709", 16);
        BigInteger p = new BigInteger("DB7C2ABF62E35E668076BEAD208B", 16);
        BigInteger n = new BigInteger("36DF0AAFD8B8D7597CA10520D04B", 16);
        BigInteger gx = new BigInteger("4BA30AB5E892B4E1649DD0928643", 16);
        BigInteger gy = new BigInteger("ADCD46F5882E3747DEF36E956E97", 16);
        Ecc ecc = Ecc.getEcc(a, b, p, n, gx, gy);
        assertEquals(0, n.compareTo(ecc.getN()));
        Ecc.Point g = ecc.createPoint(gx, gy);
        assertTrue(g.isEqual(ecc.getG()));
        assertTrue(ecc.gMultiply(BigInteger.valueOf(3)).isEqual(ecc.getG().add(ecc.getG()).add(ecc.getG())));

        assertThrows(
                NullPointerException.class,
                () -> Ecc.getEcc(a, b, p, null, BigInteger.ZERO, BigInteger.ONE)
        );
        assertThrows(
                NullPointerException.class,
                () -> Ecc.getEcc(a, b, p, BigInteger.ZERO, null, BigInteger.ONE)
        );
        assertThrows(
                NullPointerException.class,
                () -> Ecc.getEcc(a, b, p, BigInteger.ZERO, BigInteger.ONE, null)
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> Ecc.getEcc(a, b, p, n, gy, gx)
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> Ecc.getEcc(a, b, p, n.add(BigInteger.ONE), gx, gy)
        );
    }

    @Test
    void inOutPoint() {
        Ecc eccA = Ecc.getEcc(
                BigInteger.valueOf(6),
                BigInteger.ONE,
                BigInteger.valueOf(73)
        );
        assertThrows(
                NullPointerException.class,
                () -> eccA.createPoint(null, BigInteger.ONE)
        );
        assertThrows(
                NullPointerException.class,
                () -> eccA.createPoint(BigInteger.valueOf(16), null)
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> eccA.createPoint(BigInteger.valueOf(-1), BigInteger.ONE)
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> eccA.createPoint(BigInteger.valueOf(167), BigInteger.ONE)
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> eccA.createPoint(BigInteger.valueOf(71), BigInteger.valueOf(-1))
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> eccA.createPoint(BigInteger.valueOf(69), BigInteger.valueOf(74))
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> eccA.createPoint(BigInteger.valueOf(22), BigInteger.valueOf(15))
        );

        areTheSamePoint(
                eccA.createPoint(BigInteger.valueOf(22), BigInteger.valueOf(14)),
                "16",
                "E"
        );
        areTheSamePoint(
                eccA.createPoint(new BigInteger("F", 16), new BigInteger("14", 16)),
                "F",
                "14"
        );

        Ecc ecc = Ecc.getEcc(
                BigInteger.valueOf(6),
                BigInteger.ONE,
                BigInteger.valueOf(991)
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> ecc.createPoint(new BigInteger("15", 16), new BigInteger("20", 16))
        );
        areTheSamePoint(
                ecc.createPoint(new BigInteger("3A9", 16), new BigInteger("2A", 16)),
                "3A9",
                "2A"
        );
        areTheSamePoint(
                ecc.createPoint(new BigInteger("375", 16), new BigInteger("1C0", 16)),
                "375",
                "1C0"
        );
        areTheSamePoint(
                ecc.createPoint(BigInteger.valueOf(42), BigInteger.valueOf(4)),
                "2A",
                "4"
        );
        areTheSamePoint(
                Ecc.secp256k1.createPoint(
                        new BigInteger("39AAF690D50BDF25EC5E44CC37B6C0295058F6152F5081B67B95A0C5D4B0ECEE", 16),
                        new BigInteger("56E9539C948D4E984C8D9870E2F412FC94B34DA59A4FD5ADFBC8ECD0AABDDAD0", 16)
                ),
                "39AAF690D50BDF25EC5E44CC37B6C0295058F6152F5081B67B95A0C5D4B0ECEE",
                "56E9539C948D4E984C8D9870E2F412FC94B34DA59A4FD5ADFBC8ECD0AABDDAD0"
        );
    }

    @Test
    void xPoint() {
        Ecc eccA = Ecc.getEcc(
                BigInteger.valueOf(6),
                BigInteger.ONE,
                BigInteger.valueOf(73)
        );
        assertThrows(
                NullPointerException.class,
                () -> eccA.createPoint(null, true)
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> eccA.createPoint(BigInteger.valueOf(-1), true)
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> eccA.createPoint(BigInteger.valueOf(167), true)
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> eccA.createPoint(BigInteger.valueOf(70), false)
        );

        Ecc.Point point = eccA.createPoint(BigInteger.valueOf(22), BigInteger.valueOf(14));
        for (int i = 0; i < 10; i++) {
            point = point.add(point);
            assertEquals(0, point.y.compareTo(eccA.createPoint(point.x, !point.y.testBit(0)).y));
        }

        Ecc eccB = Ecc.getEcc(
                BigInteger.valueOf(609),
                BigInteger.valueOf(124),
                BigInteger.valueOf(397)
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> eccB.createPoint(BigInteger.valueOf(15), false)
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> eccB.createPoint(BigInteger.valueOf(21), false)
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> eccB.createPoint(BigInteger.valueOf(32), false)
        );

        point = eccB.createPoint(BigInteger.valueOf(53), false);
        assertEquals(55, point.y.intValue());
        Ecc.Point m = eccB.infinity();
        for (int i = 2; i < 100; i++) {
            m = point.multiply(BigInteger.valueOf(i));
            assertEquals(0, m.y.compareTo(eccB.createPoint(m.x, !m.y.testBit(0)).y));
        }
        assertEquals(298, m.y.intValue());

        Ecc ecc = Ecc.secp256k1;
        String x = "79BE667EF9DCBBAC55A06295CE870B07029BFCDB2DCE28D959F2815B16F81798";
        assertEquals(
                ecc.createPoint(new BigInteger(x, 16), true).y.toString(16).toUpperCase(),
                "483ADA7726A3C4655DA4FBFC0E1108A8FD17B448A68554199C47D08FFB10D4B8"
        );
        x = "C6047F9441ED7D6D3045406E95C07CD85C778E4B8CEF3CA7ABAC09B95C709EE5";
        assertEquals(
                ecc.createPoint(new BigInteger(x, 16), true).y.toString(16).toUpperCase(),
                "1AE168FEA63DC339A3C58419466CEAEEF7F632653266D0E1236431A950CFE52A"
        );
        x = "F9308A019258C31049344F85F89D5229B531C845836F99B08601F113BCE036F9";
        assertEquals(
                ecc.createPoint(new BigInteger(x, 16), true).y.toString(16).toUpperCase(),
                "388F7B0F632DE8140FE337E62A37F3566500A99934C2231B6CB9FD7584B8E672"
        );
        x = "E493DBF1C10D80F3581E4904930B1404CC6C13900EE0758474FA94ABE8C4CD13";
        assertEquals(
                ecc.createPoint(new BigInteger(x, 16), true).y.toString(16).toUpperCase(),
                "51ED993EA0D455B75642E2098EA51448D967AE33BFBDFE40CFE97BDC47739922"
        );
        x = "2F8BDE4D1A07209355B4A7250A5C5128E88B84BDDC619AB7CBA8D569B240EFE4";
        assertEquals(
                ecc.createPoint(new BigInteger(x, 16), true).y.toString(16).toUpperCase(),
                "D8AC222636E5E3D6D4DBA9DDA6C9C426F788271BAB0D6840DCA87D3AA6AC62D6"
        );
        x = "FFF97BD5755EEEA420453A14355235D382F6472F8568A18B2F057A1460297556";
        assertEquals(
                ecc.createPoint(new BigInteger(x, 16), false).y.toString(16).toUpperCase(),
                "AE12777AACFBB620F3BE96017F45C560DE80F0F6518FE4A03C870C36B075F297"
        );
        x = "E60FCE93B59E9EC53011AABC21C23E97B2A31369B87A5AE9C44EE89E2A6DEC0A";
        assertEquals(
                ecc.createPoint(new BigInteger(x, 16), false).y.toString(16).toUpperCase(),
                "F7E3507399E595929DB99F34F57937101296891E44D23F0BE1F32CCE69616821"
        );

        Random random = new Random();
        BigInteger d;
        for (int i = 0; i < 100; i++) {
            d = new BigInteger(250, random);
            point = ecc.gMultiply(d);
            assertEquals(0, point.y.compareTo(ecc.createPoint(point.x, !point.y.testBit(0)).y));
        }

        BigInteger a = new BigInteger("6127C24C05F38A0AAAF65C0EF02C", 16);
        BigInteger b = new BigInteger("51DEF1815DB5ED74FCC34C85D709", 16);
        BigInteger p = new BigInteger("DB7C2ABF62E35E668076BEAD208B", 16);
        BigInteger n = new BigInteger("36DF0AAFD8B8D7597CA10520D04B", 16);
        BigInteger gx = new BigInteger("4BA30AB5E892B4E1649DD0928643", 16);
        BigInteger gy = new BigInteger("ADCD46F5882E3747DEF36E956E97", 16);
        ecc = Ecc.getEcc(a, b, p, n, gx, gy);
        for (int i = 0; i < 100; i++) {
            d = new BigInteger(100, random);
            point = ecc.gMultiply(d);
            assertEquals(0, point.y.compareTo(ecc.createPoint(point.x, !point.y.testBit(0)).y));
        }
    }

    @Test
    void gMultiply() {
        Ecc curve = Ecc.secp256k1;
        assertThrows(NullPointerException.class, () -> curve.gMultiply(null));

        assertTrue(curve.getG().multiply(curve.getN()).isInfinity());
        areTheSamePoint(
                curve.gMultiply(BigInteger.valueOf(1)),
                "79BE667EF9DCBBAC55A06295CE870B07029BFCDB2DCE28D959F2815B16F81798",
                "483ADA7726A3C4655DA4FBFC0E1108A8FD17B448A68554199C47D08FFB10D4B8"
        );
        areTheSamePoint(
                curve.gMultiply(BigInteger.valueOf(2)),
                "C6047F9441ED7D6D3045406E95C07CD85C778E4B8CEF3CA7ABAC09B95C709EE5",
                "1AE168FEA63DC339A3C58419466CEAEEF7F632653266D0E1236431A950CFE52A"
        );
        areTheSamePoint(
                curve.gMultiply(BigInteger.valueOf(3)),
                "F9308A019258C31049344F85F89D5229B531C845836F99B08601F113BCE036F9",
                "388F7B0F632DE8140FE337E62A37F3566500A99934C2231B6CB9FD7584B8E672"
        );
        areTheSamePoint(
                curve.gMultiply(BigInteger.valueOf(4)),
                "E493DBF1C10D80F3581E4904930B1404CC6C13900EE0758474FA94ABE8C4CD13",
                "51ED993EA0D455B75642E2098EA51448D967AE33BFBDFE40CFE97BDC47739922"
        );
        areTheSamePoint(
                curve.gMultiply(BigInteger.valueOf(5)),
                "2F8BDE4D1A07209355B4A7250A5C5128E88B84BDDC619AB7CBA8D569B240EFE4",
                "D8AC222636E5E3D6D4DBA9DDA6C9C426F788271BAB0D6840DCA87D3AA6AC62D6"
        );
        areTheSamePoint(
                curve.gMultiply(curve.getN().pow(2).add(BigInteger.valueOf(5))),
                "2F8BDE4D1A07209355B4A7250A5C5128E88B84BDDC619AB7CBA8D569B240EFE4",
                "D8AC222636E5E3D6D4DBA9DDA6C9C426F788271BAB0D6840DCA87D3AA6AC62D6"
        );
        areTheSamePoint(
                curve.gMultiply(BigInteger.valueOf(6)),
                "FFF97BD5755EEEA420453A14355235D382F6472F8568A18B2F057A1460297556",
                "AE12777AACFBB620F3BE96017F45C560DE80F0F6518FE4A03C870C36B075F297"
        );
        areTheSamePoint(
                curve.gMultiply(BigInteger.valueOf(7)),
                "5CBDF0646E5DB4EAA398F365F2EA7A0E3D419B7E0330E39CE92BDDEDCAC4F9BC",
                "6AEBCA40BA255960A3178D6D861A54DBA813D0B813FDE7B5A5082628087264DA"
        );
        areTheSamePoint(
                curve.gMultiply(curve.getN().negate().add(BigInteger.valueOf(7))),
                "5CBDF0646E5DB4EAA398F365F2EA7A0E3D419B7E0330E39CE92BDDEDCAC4F9BC",
                "6AEBCA40BA255960A3178D6D861A54DBA813D0B813FDE7B5A5082628087264DA"
        );
        areTheSamePoint(
                curve.gMultiply(curve.getN().pow(7).negate().add(BigInteger.valueOf(8))),
                "2F01E5E15CCA351DAFF3843FB70F3C2F0A1BDD05E5AF888A67784EF3E10A2A01",
                "5C4DA8A741539949293D082A132D13B4C2E213D6BA5B7617B5DA2CB76CBDE904"
        );
        areTheSamePoint(
                curve.gMultiply(BigInteger.valueOf(16)),
                "E60FCE93B59E9EC53011AABC21C23E97B2A31369B87A5AE9C44EE89E2A6DEC0A",
                "F7E3507399E595929DB99F34F57937101296891E44D23F0BE1F32CCE69616821"
        );
        areTheSamePoint(
                curve.gMultiply(BigInteger.valueOf(20)),
                "4CE119C96E2FA357200B559B2F7DD5A5F02D5290AFF74B03F3E471B273211C97",
                "12BA26DCB10EC1625DA61FA10A844C676162948271D96967450288EE9233DC3A"
        );
        areTheSamePoint(
                curve.gMultiply(BigInteger.valueOf(21)),
                "352BBF4A4CDD12564F93FA332CE333301D9AD40271F8107181340AEF25BE59D5",
                "321EB4075348F534D59C18259DDA3E1F4A1B3B2E71B1039C67BD3D8BCF81998C"
        );
        areTheSamePoint(
                curve.gMultiply(new BigInteger("AA5E28D6A97A2479A65527F7290311A3624D4CC0FA1578598EE3C2613BF99522", 16)),
                "34F9460F0E4F08393D192B3C5133A6BA099AA0AD9FD54EBCCFACDFA239FF49C6",
                "B71EA9BD730FD8923F6D25A7A91E7DD7728A960686CB5A901BB419E0F2CA232"
        );
        areTheSamePoint(
                curve.gMultiply(new BigInteger("7E2B897B8CEBC6361663AD410835639826D590F393D90A9538881735256DFAE3", 16)),
                "D74BF844B0862475103D96A611CF2D898447E288D34B360BC885CB8CE7C00575",
                "131C670D414C4546B88AC3FF664611B1C38CEB1C21D76369D7A7A0969D61D97D"
        );
        areTheSamePoint(
                curve.gMultiply(new BigInteger("376A3A2CDCD12581EFFF13EE4AD44C4044B8A0524C42422A7E1E181E4DEECCEC", 16)),
                "14890E61FCD4B0BD92E5B36C81372CA6FED471EF3AA60A3E415EE4FE987DABA1",
                "297B858D9F752AB42D3BCA67EE0EB6DCD1C2B7B0DBE23397E66ADC272263F982"
        );
        areTheSamePoint(
                curve.gMultiply(new BigInteger("1B22644A7BE026548810C378D0B2994EEFA6D2B9881803CB02CEFF865287D1B9", 16)),
                "F73C65EAD01C5126F28F442D087689BFA08E12763E0CEC1D35B01751FD735ED3",
                "F449A8376906482A84ED01479BD18882B919C140D638307F0C0934BA12590BDE"
        );
        areTheSamePoint(
                curve.gMultiply(new BigInteger("ebb2c082fd7727890a28ac82f6bdf97bad8de9f5d7c9028692de1a255cad3e0f", 16)),
                "779DD197A5DF977ED2CF6CB31D82D43328B790DC6B3B7D4437A427BD5847DFCD",
                "E94B724A555B6D017BB7607C3E3281DAF5B1699D6EF4124975C9237B917D426F"
        );

        BigInteger a = new BigInteger("6127C24C05F38A0AAAF65C0EF02C", 16);
        BigInteger b = new BigInteger("51DEF1815DB5ED74FCC34C85D709", 16);
        BigInteger p = new BigInteger("DB7C2ABF62E35E668076BEAD208B", 16);
        Ecc curveA = Ecc.getEcc(a, b, p);
        assertTrue(curveA.gMultiply(BigInteger.valueOf(3)).isInfinity());
    }

    @Test
    void add() {
        assertThrows(NullPointerException.class, () -> Ecc.secp256k1.getG().add(null));

        Ecc curve = Ecc.getEcc(BigInteger.ZERO, BigInteger.ONE, BigInteger.valueOf(107));
        Ecc.Point a = curve.createPoint(BigInteger.valueOf(25), BigInteger.valueOf(2));
        assertTrue(curve.infinity().add(curve.infinity()).isInfinity());
        assertTrue(a.add(curve.infinity()).isEqual(a));
        assertTrue(curve.infinity().add(a).isEqual(a));
        areTheSamePoint(a.add(a), "27", "10");
        assertTrue(a.add(curve.createPoint(BigInteger.valueOf(25), BigInteger.valueOf(105))).isInfinity());
        Ecc.Point b = curve.createPoint(BigInteger.valueOf(6), BigInteger.valueOf(89));
        assertTrue(a.add(b).isEqual(b.add(a)));
        areTheSamePoint(a.add(b), "56", "7");
        assertFalse(a.isEqual(b));
        a = curve.createPoint(BigInteger.valueOf(5), BigInteger.valueOf(33));
        b = curve.createPoint(BigInteger.valueOf(6), BigInteger.valueOf(89));
        assertTrue(a.add(b).isEqual(b.add(a)));
        areTheSamePoint(b.add(a), "16", "55");
        a = curve.createPoint(BigInteger.valueOf(7), BigInteger.valueOf(68));
        b = curve.createPoint(BigInteger.valueOf(17), BigInteger.valueOf(62));
        assertTrue(a.add(b).isEqual(b.add(a)));
        areTheSamePoint(a.add(b), "20", "36");
        curve = Ecc.getEcc(BigInteger.valueOf(2), BigInteger.valueOf(3), BigInteger.valueOf(97));
        a = curve.createPoint(BigInteger.valueOf(17), BigInteger.valueOf(10));
        b = curve.createPoint(BigInteger.valueOf(24), BigInteger.valueOf(95));
        assertTrue(a.add(b).isEqual(b.add(a)));
        areTheSamePoint(a.add(b), "35", "18");
        a = curve.createPoint(BigInteger.valueOf(32), BigInteger.valueOf(7));
        b = curve.createPoint(BigInteger.valueOf(12), BigInteger.valueOf(94));
        assertTrue(a.add(b).isEqual(b.add(a)));
        areTheSamePoint(a.add(b), "1D", "2B");
    }

    @Test
    void multiply() {
        assertThrows(NullPointerException.class, () -> Ecc.secp256k1.getG().multiply(null));

        Ecc curve = Ecc.getEcc(BigInteger.ONE, BigInteger.ONE, BigInteger.valueOf(109));
        Ecc.Point a = curve.createPoint(BigInteger.ZERO, BigInteger.ONE);
        assertTrue(curve.infinity().multiply(BigInteger.ONE).isInfinity());
        assertTrue(a.multiply(BigInteger.ZERO).isInfinity());
        assertTrue(a.multiply(BigInteger.ONE).isEqual(a));
        areTheSamePoint(a.multiply(BigInteger.ONE.negate()), "0", "6C");
        areTheSamePoint(a.multiply(BigInteger.valueOf(5)), "4C", "6C");
        areTheSamePoint(a.multiply(BigInteger.valueOf(29)), "C", "12");
        a = curve.createPoint(BigInteger.valueOf(19), BigInteger.valueOf(11));
        areTheSamePoint(a.multiply(BigInteger.valueOf(-45)), "4F", "3C");
    }

    @Test
    void test() {
        assertThrows(NullPointerException.class, () -> Ecc.secp256k1.infinity().isEqual(null));

        Random random = new Random();
        BigInteger a, b;
        Ecc.Point p, q, r;
        for (int i = 0; i < 100; i++) {
            // G * (a + b) = G * a + G * b = G * b + G * a
            a = new BigInteger(250, random);
            b = new BigInteger(250, random);
            p = Ecc.secp256k1.gMultiply(a);
            q = Ecc.secp256k1.gMultiply(b);
            r = Ecc.secp256k1.gMultiply(a.add(b));
            assertTrue(r.isEqual(p.add(q)));
            assertTrue(r.isEqual(q.add(p)));
        }
    }

    @Test
    void signVerifyTest() {
        assertThrows(NullPointerException.class, () -> Ecc.secp256k1.sign(null, BigInteger.ONE));
        assertThrows(NullPointerException.class, () -> Ecc.secp256k1.sign(new byte[]{8, 9}, null));
        byte[] bytes = new byte[]{9, 9};
        Ecc.Point point = Ecc.secp256k1.getG();
        BigInteger[] bigIntegers = new BigInteger[]{BigInteger.ONE, BigInteger.ONE};
        assertThrows(NullPointerException.class, () -> Ecc.secp256k1.verify(null, point, bigIntegers));
        assertThrows(NullPointerException.class, () -> Ecc.secp256k1.verify(bytes, null, bigIntegers));
        assertThrows(NullPointerException.class, () -> Ecc.secp256k1.verify(bytes, point, null));
        assertThrows(
                IndexOutOfBoundsException.class,
                () -> Ecc.secp256k1.verify(bytes, point, new BigInteger[]{BigInteger.ONE})
        );

        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            BigInteger privateKey = new BigInteger(31, random);
            Ecc.Point publicKey = Ecc.secp256k1.gMultiply(privateKey);
            byte[] message = new BigInteger(31, random).toByteArray();
            BigInteger[] signature = Ecc.secp256k1.sign(message, privateKey);
            assertTrue(Ecc.secp256k1.verify(message, publicKey, signature));
        }

        BigInteger a = new BigInteger("6127C24C05F38A0AAAF65C0EF02C", 16);
        BigInteger b = new BigInteger("51DEF1815DB5ED74FCC34C85D709", 16);
        BigInteger p = new BigInteger("DB7C2ABF62E35E668076BEAD208B", 16);
        BigInteger n = new BigInteger("36DF0AAFD8B8D7597CA10520D04B", 16);
        BigInteger gx = new BigInteger("4BA30AB5E892B4E1649DD0928643", 16);
        BigInteger gy = new BigInteger("ADCD46F5882E3747DEF36E956E97", 16);
        Ecc curve = Ecc.getEcc(a, b, p, n, gx, gy);
        BigInteger privateKey = new BigInteger("36DF0AAFD8B8D75CA10520D04B", 16);
        Ecc.Point publicKey = curve.gMultiply(privateKey);
        byte[] message = new byte[]{45, 44, 47, 0, 0, 99, 11};
        BigInteger[] signature = curve.sign(message, privateKey);
        assertThrows(
                UnsupportedOperationException.class,
                () -> curve.sign(new byte[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}, privateKey)
        );
        assertFalse(curve.verify(new byte[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}, publicKey, signature));
        assertFalse(curve.verify(message, publicKey.multiply(BigInteger.TEN), signature));
        assertFalse(curve.verify(message, publicKey, new BigInteger[]{BigInteger.ZERO, signature[1]}));
        assertFalse(curve.verify(message, publicKey, new BigInteger[]{signature[0], curve.getN()}));
        assertFalse(curve.verify(new byte[]{46, 44, 47, 0, 0, 99, 11}, publicKey, signature));
        assertFalse(curve.verify(message, curve.gMultiply(privateKey.add(BigInteger.ONE)), signature));
        assertFalse(curve.verify(message, publicKey, new BigInteger[]{signature[1], signature[0]}));
        assertTrue(curve.verify(message, publicKey, signature));

        Ecc curveA = Ecc.getEcc(a, b, p);
        assertThrows(UnsupportedOperationException.class, () -> curveA.sign(message, privateKey));
        assertFalse(curveA.verify(message, publicKey, signature));
    }

    @Test
    void verify() {
        byte[] message = Converter.hexToBytes("9302bda273a887cb40c13e02a50b4071a31fd3aae3ae04021b0b843dd61ad18e");
        Ecc.Point publicKey = Ecc.secp256k1.createPoint(
                new BigInteger("50863ad64a87ae8a2fe83c1af1a8403cb53f53e486d8511dad8a04887e5b2352", 16),
                new BigInteger("2cd470243453a299fa9e77237716103abc11a1df38855ed6f2ee187e9c582ba6", 16)
        );
        BigInteger[] signature = new BigInteger[]{
                new BigInteger("009e0339f72c793a89e664a8a932df073962a3f84eda0bd9e02084a6a9567f75aa", 16),
                new BigInteger("00bd9cbaca2e5ec195751efdfac164b76250b1e21302e51ca86dd7ebd7020cdc06", 16)
        };
        assertTrue(Ecc.secp256k1.verify(message, publicKey, signature));

        message = Converter.hexToBytes("4b688df40bcedbe641ddb16ff0a1842d9c67ea1c3bf63f3e0471baa664531d1a");
        publicKey = Ecc.secp256k1.createPoint(
                new BigInteger("779dd197a5df977ed2cf6cb31d82d43328b790dc6b3b7d4437a427bd5847dfcd", 16),
                new BigInteger("e94b724a555b6d017bb7607c3e3281daf5b1699d6ef4124975c9237b917d426f", 16)
        );
        signature = new BigInteger[]{
                new BigInteger("241097efbf8b63bf145c8961dbdf10c310efbb3b2676bbc0f8b08505c9e2f795", 16),
                new BigInteger("021006b7838609339e8b415a7f9acb1b661828131aef1ecbc7955dfb01f3ca0e", 16)
        };
        assertTrue(Ecc.secp256k1.verify(message, publicKey, signature));
    }

    private static void areTheSamePoint(Ecc.Point point, String x, String y) {
        assertEquals(x, point.x.toString(16).toUpperCase());
        assertEquals(y, point.y.toString(16).toUpperCase());
    }
}