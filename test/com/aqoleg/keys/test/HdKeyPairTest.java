package com.aqoleg.keys.test;

import com.aqoleg.Test;
import com.aqoleg.keys.HdKeyPair;
import com.aqoleg.keys.KeyPair;
import com.aqoleg.keys.Mnemonic;

@SuppressWarnings("unused")
public class HdKeyPairTest extends Test {

    public static void main(String[] args) {
        new HdKeyPairTest().testAll();
    }

    public void test1() {
        assertThrows(NullPointerException.class, () -> HdKeyPair.createMaster(null));

        byte[] seed = hexToBytes("000102030405060708090a0b0c0d0e0f");
        HdKeyPair master = HdKeyPair.createMaster(seed);
        String xprv = "xprv9s21ZrQH143K3QTDL4LXw2F7HEK3wJUD2nW2nRk4stbPy6cq3jPPqjiChkVvvNKmPGJxWUtg6LnF5kejMRNN"
                + "U3TGtRBeJgk33yuGBxrMPHi";
        String xpub = "xpub661MyMwAqRbcFtXgS5sYJABqqG9YLmC4Q1Rdap9gSE8NqtwybGhePY2gZ29ESFjqJoCu1Rupje8YtGqsefD265TMg7u"
                + "sUDFdp6W1EGMcet8";
        assertEquals(xprv, master.serialize(true));
        assertEquals(xpub, master.serialize(false));
        assertEquals("m", master.path);
        desrerializeTest(xprv, master, "m");
        // m/0h
        HdKeyPair hdKeyPair = master.generateChild(0, true);
        xprv = "xprv9uHRZZhk6KAJC1avXpDAp4MDc3sQKNxDiPvvkX8Br5ngLNv1TxvUxt4cV1rGL5hj6KCesnDYUhd7oWgT11eZG7XnxHr"
                + "nYeSvkzY7d2bhkJ7";
        xpub = "xpub68Gmy5EdvgibQVfPdqkBBCHxA5htiqg55crXYuXoQRKfDBFA1WEjWgP6LHhwBZeNK1VTsfTFUHCdrfp1bgwQ9xv5ski"
                + "8PX9rL2dZXvgGDnw";
        assertEquals(xprv, hdKeyPair.serialize(true));
        assertEquals(xpub, hdKeyPair.serialize(false));
        desrerializeTest(xprv, hdKeyPair, "m/0'");
        // m/0h/1
        hdKeyPair = hdKeyPair.generateChild(1, false);
        xprv = "xprv9wTYmMFdV23N2TdNG573QoEsfRrWKQgWeibmLntzniatZvR9BmLnvSxqu53Kw1UmYPxLgboyZQaXwTCg8MSY3H2EU4p"
                + "WcQDnRnrVA1xe8fs";
        xpub = "xpub6ASuArnXKPbfEwhqN6e3mwBcDTgzisQN1wXN9BJcM47sSikHjJf3UFHKkNAWbWMiGj7Wf5uMash7SyYq527Hqck2AxY"
                + "ysAA7xmALppuCkwQ";
        assertEquals(xprv, hdKeyPair.serialize(true));
        assertEquals(xpub, hdKeyPair.serialize(false));
        desrerializeTest(xprv, hdKeyPair, "m//1");
        // m/0h/1/2h
        hdKeyPair = hdKeyPair.generateChild(2, true);
        xprv = "xprv9z4pot5VBttmtdRTWfWQmoH1taj2axGVzFqSb8C9xaxKymcFzXBDptWmT7FwuEzG3ryjH4ktypQSAewRiNMjANTtpgP"
                + "4mLTj34bhnZX7UiM";
        xpub = "xpub6D4BDPcP2GT577Vvch3R8wDkScZWzQzMMUm3PWbmWvVJrZwQY4VUNgqFJPMM3No2dFDFGTsxxpG5uJh7n7epu4trkrX"
                + "7x7DogT5Uv6fcLW5";
        assertEquals(xprv, hdKeyPair.serialize(true));
        assertEquals(xpub, hdKeyPair.serialize(false));
        desrerializeTest(xprv, hdKeyPair, "m///2'");
        // m/0h/1/2h/2
        hdKeyPair = hdKeyPair.generateChild(2, false);
        xprv = "xprvA2JDeKCSNNZky6uBCviVfJSKyQ1mDYahRjijr5idH2WwLsEd4Hsb2Tyh8RfQMuPh7f7RtyzTtdrbdqqsunu5Mm3wDvU"
                + "AKRHSC34sJ7in334";
        xpub = "xpub6FHa3pjLCk84BayeJxFW2SP4XRrFd1JYnxeLeU8EqN3vDfZmbqBqaGJAyiLjTAwm6ZLRQUMv1ZACTj37sR62cfN7fe5"
                + "JnJ7dh8zL4fiyLHV";
        assertEquals(xprv, hdKeyPair.serialize(true));
        assertEquals(xpub, hdKeyPair.serialize(false));
        desrerializeTest(xprv, hdKeyPair, "m////2");
        // m/0h/1/2h/2/1000000000
        hdKeyPair = master.generateChild("0'/1/2'/2/1000000000");
        xprv = "xprvA41z7zogVVwxVSgdKUHDy1SKmdb533PjDz7J6N6mV6uS3ze1ai8FHa8kmHScGpWmj4WggLyQjgPie1rFSruoUihUZRE"
                + "PSL39UNdE3BBDu76";
        xpub = "xpub6H1LXWLaKsWFhvm6RVpEL9P4KfRZSW7abD2ttkWP3SSQvnyA8FSVqNTEcYFgJS2UaFcxupHiYkro49S8yGasTvXEYBV"
                + "PamhGW6cFJodrTHy";
        assertEquals(xprv, hdKeyPair.serialize(true));
        assertEquals(xpub, hdKeyPair.serialize(false));
        desrerializeTest(xprv, hdKeyPair, "m/////1000000000");
        assertEquals(1000000000, hdKeyPair.keyNumber);
        assertTrue(!hdKeyPair.isHardened);
        assertEquals("m/0'/1/2'/2/1000000000", hdKeyPair.path);
    }

    public void test2() {
        byte[] seed = hexToBytes("fffcf9f6f3f0edeae7e4e1dedbd8d5d2cfccc9c6c3c0bdbab7b4b1aeaba8a5a29f9c999693"
                + "908d8a8784817e7b7875726f6c696663605d5a5754514e4b484542");
        HdKeyPair hdKeyPair = HdKeyPair.createMaster(seed);
        String xprv = "xprv9s21ZrQH143K31xYSDQpPDxsXRTUcvj2iNHm5NUtrGiGG5e2DtALGdso3pGz6ssrdK4PFmM8NSpSBHNqPqm5"
                + "5Qn3LqFtT2emdEXVYsCzC2U";
        String xpub = "xpub661MyMwAqRbcFW31YEwpkMuc5THy2PSt5bDMsktWQcFF8syAmRUapSCGu8ED9W6oDMSgv6Zz8idoc4a6mr8BDzTJY47"
                + "LJhkJ8UB7WEGuduB";
        assertEquals(xprv, hdKeyPair.serialize(true));
        assertEquals(xpub, hdKeyPair.serialize(false));
        desrerializeTest(xprv, hdKeyPair, "m");
        // m/0
        HdKeyPair hdKey1 = hdKeyPair.generateChild(0, false);
        xprv = "xprv9vHkqa6EV4sPZHYqZznhT2NPtPCjKuDKGY38FBWLvgaDx45zo9WQRUT3dKYnjwih2yJD9mkrocEZXo1ex8G81dwSM1f"
                + "wqWpWkeS3v86pgKt";
        xpub = "xpub69H7F5d8KSRgmmdJg2KhpAK8SR3DjMwAdkxj3ZuxV27CprR9LgpeyGmXUbC6wb7ERfvrnKZjXoUmmDznezpbZb7ap6r"
                + "1D3tgFxHmwMkQTPH";
        assertEquals(xprv, hdKey1.serialize(true));
        assertEquals(xpub, hdKey1.serialize(false));
        desrerializeTest(xprv, hdKey1, "m/0");
        // m/0/2147483647h
        HdKeyPair hdKey2 = hdKey1.generateChild(2147483647, true);
        HdKeyPair hdKey2_2 = hdKeyPair.generateChild(0, false).generateChild(2147483647, true);
        xprv = "xprv9wSp6B7kry3Vj9m1zSnLvN3xH8RdsPP1Mh7fAaR7aRLcQMKTR2vidYEeEg2mUCTAwCd6vnxVrcjfy2kRgVsFawNzmju"
                + "Hc2YmYRmagcEPdU9";
        xpub = "xpub6ASAVgeehLbnwdqV6UKMHVzgqAG8Gr6riv3Fxxpj8ksbH9ebxaEyBLZ85ySDhKiLDBrQSARLq1uNRts8RuJiHjaDMBU"
                + "4Zn9h8LZNnBC5y4a";
        assertEquals(xprv, hdKey2.serialize(true));
        assertEquals(xprv, hdKey2_2.serialize(true));
        assertEquals(xpub, hdKey2.serialize(false));
        assertEquals(xpub, hdKey2_2.serialize(false));
        desrerializeTest(xprv, hdKey2, "m//2147483647'");
        // m/0/2147483647h/1
        HdKeyPair hdKey3 = hdKey2.generateChild(1, false);
        xprv = "xprv9zFnWC6h2cLgpmSA46vutJzBcfJ8yaJGg8cX1e5StJh45BBciYTRXSd25UEPVuesF9yog62tGAQtHjXajPPdbRCHuWS"
                + "6T8XA2ECKADdw4Ef";
        xpub = "xpub6DF8uhdarytz3FWdA8TvFSvvAh8dP3283MY7p2V4SeE2wyWmG5mg5EwVvmdMVCQcoNJxGoWaU9DCWh89LojfZ537wTf"
                + "unKau47EL2dhHKon";
        assertEquals(xprv, hdKey3.serialize(true));
        assertEquals(xpub, hdKey3.serialize(false));
        desrerializeTest(xprv, hdKey3, "m///1");
        // m/0/2147483647h/1/2147483646h
        HdKeyPair hdKey4 = hdKey3.generateChild(2147483646, true);
        HdKeyPair hdKey4_2 = hdKey2_2.generateChild(1, false).generateChild(2147483646, true);
        xprv = "xprvA1RpRA33e1JQ7ifknakTFpgNXPmW2YvmhqLQYMmrj4xJXXWYpDPS3xz7iAxn8L39njGVyuoseXzU6rcxFLJ8HFsTjSy"
                + "QbLYnMpCqE2VbFWc";
        xpub = "xpub6ERApfZwUNrhLCkDtcHTcxd75RbzS1ed54G1LkBUHQVHQKqhMkhgbmJbZRkrgZw4koxb5JaHWkY4ALHY2grBGRjaDMz"
                + "QLcgJvLJuZZvRcEL";
        assertEquals(xprv, hdKey4.serialize(true));
        assertEquals(xprv, hdKey4_2.serialize(true));
        assertEquals(xpub, hdKey4.serialize(false));
        assertEquals(xpub, hdKey4_2.serialize(false));
        desrerializeTest(xprv, hdKey4, "m////2147483646'");
        // m/0/2147483647h/1/2147483646h/2
        hdKey4.generateChild(22, false);
        hdKeyPair = hdKey4.generateChild(2, false);
        xprv = "xprvA2nrNbFZABcdryreWet9Ea4LvTJcGsqrMzxHx98MMrotbir7yrKCEXw7nadnHM8Dq38EGfSh6dqA9QWTyefMLEcBYJU"
                + "uekgW4BYPJcr9E7j";
        xpub = "xpub6FnCn6nSzZAw5Tw7cgR9bi15UV96gLZhjDstkXXxvCLsUXBGXPdSnLFbdpq8p9HmGsApME5hQTZ3emM2rnY5agb9rXp"
                + "VGyy3bdW6EEgAtqt";
        assertEquals(xprv, hdKeyPair.serialize(true));
        assertEquals(xpub, hdKeyPair.serialize(false));
        desrerializeTest(xprv, hdKeyPair, "m/////2");
    }

    public void test3() {
        byte[] seed = hexToBytes("4b381541583be4423346c643850da4b320e46a87ae3d2a4e6da11eba819cd4acba45d23931"
                + "9ac14f863b8d5ab5a0d0c64d2e8a1e7d1457df2e5a3c51c73235be");
        HdKeyPair hdKeyPair = HdKeyPair.createMaster(seed);
        String xprv = "xprv9s21ZrQH143K25QhxbucbDDuQ4naNntJRi4KUfWT7xo4EKsHt2QJDu7KXp1A3u7Bi1j8ph3EGsZ9Xvz9dGuV"
                + "rtHHs7pXeTzjuxBrCmmhgC6";
        String xpub = "xpub661MyMwAqRbcEZVB4dScxMAdx6d4nFc9nvyvH3v4gJL378CSRZiYmhRoP7mBy6gSPSCYk6SzXPTf3ND1cZAceL7SfJ1"
                + "Z3GC8vBgp2epUt13";
        assertEquals(xprv, hdKeyPair.serialize(true));
        assertEquals(xpub, hdKeyPair.serialize(false));
        desrerializeTest(xprv, hdKeyPair, "m");
        // m/0h
        hdKeyPair = hdKeyPair.generateChild(0, true);
        xprv = "xprv9uPDJpEQgRQfDcW7BkF7eTya6RPxXeJCqCJGHuCJ4GiRVLzkTXBAJMu2qaMWPrS7AANYqdq6vcBcBUdJCVVFceUvJFj"
                + "aPdGZ2y9WACViL4L";
        xpub = "xpub68NZiKmJWnxxS6aaHmn81bvJeTESw724CRDs6HbuccFQN9Ku14VQrADWgqbhhTHBaohPX4CjNLf9fq9MYo6oDaPPLPx"
                + "Sb7gwQN3ih19Zm4Y";
        assertEquals(xprv, hdKeyPair.serialize(true));
        assertEquals(xpub, hdKeyPair.serialize(false));
        desrerializeTest(xprv, hdKeyPair, "m/0'");
        // leading zeros
        seed = hexToBytes("3ddd5602285899a946114506157c7997e5444528f3003f6134712147db19b678");
        hdKeyPair = HdKeyPair.createMaster(seed);
        xprv = "xprv9s21ZrQH143K48vGoLGRPxgo2JNkJ3J3fqkirQC2zVdk5Dgd5w14S7fRDyHH4dWNHUgkvsvNDCkvAwcSHNAQwhwgNMg"
                + "ZhLtQC63zxwhQmRv";
        xpub = "xpub661MyMwAqRbcGczjuMoRm6dXaLDEhW1u34gKenbeYqAix21mdUKJyuyu5F1rzYGVxyL6tmgBUAEPrEz92mBXjByMRiJ"
                + "dba9wpnN37RLLAXa";
        assertEquals(xprv, hdKeyPair.serialize(true));
        assertEquals(xpub, hdKeyPair.serialize(false));
        desrerializeTest(xprv, hdKeyPair, "m");
        // m/0h
        hdKeyPair = hdKeyPair.generateChild(0, true);
        xprv = "xprv9vB7xEWwNp9kh1wQRfCCQMnZUEG21LpbR9NPCNN1dwhiZkjjeGRnaALmPXCX7SgjFTiCTT6bXes17boXtjq3xLpcDjz"
                + "EuGLQBM5ohqkao9G";
        xpub = "xpub69AUMk3qDBi3uW1sXgjCmVjJ2G6WQoYSnNHyzkmdCHEhSZ4tBok37xfFEqHd2AddP56Tqp4o56AePAgCjYdvpW2PU2j"
                + "bUPFKsav5ut6Ch1m";
        assertEquals(xprv, hdKeyPair.serialize(true));
        assertEquals(xpub, hdKeyPair.serialize(false));
        desrerializeTest(xprv, hdKeyPair, "m/0'");
        // m/0h/1h
        hdKeyPair = hdKeyPair.generateChild(1, true);
        xprv = "xprv9xJocDuwtYCMNAo3Zw76WENQeAS6WGXQ55RCy7tDJ8oALr4FWkuVoHJeHVAcAqiZLE7Je3vZJHxspZdFHfnBEjHqU5h"
                + "G1Jaj32dVoS6XLT1";
        xpub = "xpub6BJA1jSqiukeaesWfxe6sNK9CCGaujFFSJLomWHprUL9DePQ4JDkM5d88n49sMGJxrhpjazuXYWdMf17C9T5Xnxkopa"
                + "eS7jGk1GyyVziaMt";
        assertEquals(xprv, hdKeyPair.serialize(true));
        assertEquals(xpub, hdKeyPair.serialize(false));
        desrerializeTest(xprv, hdKeyPair, "m//1'");
    }

    public void test4() {
        HdKeyPair hdKeyPair = HdKeyPair.createMaster(Mnemonic.createSeed("size tragic sausage", "mnemonic12"));
        String xprv = "xprv9s21ZrQH143K2KxX1jvd6bBW8jFY9b3fqrfPqoNLzCC6HTaufRxrgFyxWBmSC6fg13KzoRcCdLhWk3PSE6M"
                + "J1RqyWoqRhWouJu99oaZJpiz";
        assertEquals(xprv, hdKeyPair.serialize(true));
        assertEquals("m", hdKeyPair.path);
        assertEquals(0, hdKeyPair.depth);
        assertEquals(0, hdKeyPair.keyNumber);
        assertTrue(!hdKeyPair.isHardened);
        desrerializeTest(xprv, hdKeyPair, "m");

        HdKeyPair hdKeyPair2 = hdKeyPair.generateChild("9'");
        assertThrows(HdKeyPair.Exception.class, () -> hdKeyPair2.generateChild(-9, true));
        assertThrows(NullPointerException.class, () -> hdKeyPair2.generateChild(null));
        assertThrows(HdKeyPair.Exception.class, () -> hdKeyPair2.generateChild("k0"));

        HdKeyPair hdKeyPair1 = hdKeyPair.generateChild("/42/");
        xprv = "xprv9uykT4TmtzYVHPndMDjMLePL6ZVhenJoR5M21x2Rex7F7h4enpCRdh9YKj2ZsXkcM1tmzvFxNZC7xbsV7N9ZGtTMh"
                + "RNqzpMuSFmD1SLQWMX";
        assertEquals(xprv, hdKeyPair1.serialize(true));
        assertEquals(
                "xpub68y6rZzfjN6nVss6TFGMhnL4ebLC4F2enJGcpLS3DHeDzVPoLMWgBVU2AzvJEHQgqpqsff1YKvSByDfPcxgWBEe8xvKZmn"
                        + "7VciAxPLwW6pP",
                hdKeyPair1.serialize(false)
        );
        assertEquals("m/42", hdKeyPair1.path);
        assertEquals(1, hdKeyPair1.depth);
        assertEquals(42, hdKeyPair1.keyNumber);
        assertTrue(!hdKeyPair1.isHardened);
        desrerializeTest(xprv, hdKeyPair1, "m/42");

        hdKeyPair1 = hdKeyPair.generateChild(426788, true);
        xprv = "xprv9uykT4TvEkenKrLzQdc7s11RmRcP3q5AzjgdVC2VfjyQip8jbKpt8QyDu2jf6jGXp4FWCp8f3hTDpP9tBhhzTGL37wzo7d"
                + "vvaHvPg9dvje9";
        assertEquals(xprv, hdKeyPair1.serialize(true));
        assertEquals(
                "xpub68y6rZzp58D5YLRTWf98E8xAKTSsTHo2MxcEHaS7E5WPbcTt8s98gDHhkKitvY44LGunD2NunKfjL9U7eptQNDW8HUCsSq"
                        + "kJAxyiCKjHtLi",
                hdKeyPair1.serialize(false)
        );
        assertEquals("m/426788'", hdKeyPair1.path);
        assertEquals(1, hdKeyPair1.depth);
        assertEquals(426788, hdKeyPair1.keyNumber);
        assertTrue(hdKeyPair1.isHardened);
        desrerializeTest(xprv, hdKeyPair1, "m/426788'");

        hdKeyPair1 = hdKeyPair.generateChild("426788h/3");
        assertEquals("m/426788'/3", hdKeyPair1.path);
        assertEquals(2, hdKeyPair1.depth);
        assertEquals(3, hdKeyPair1.keyNumber);
        assertTrue(!hdKeyPair1.isHardened);
        assertEquals(
                "L4rEhWkf9sAXRM9dQzjyaP1jRdSouueHrtSj8X183K24hstB8FNw",
                hdKeyPair1.encode()
        );
        assertEquals(
                "1PckU5bcQY3o8w81RD6ojbzgzMHL8YBPA3",
                hdKeyPair1.getAddress()
        );
        desrerializeTest(hdKeyPair1.serialize(true), hdKeyPair1, "m//3");

        hdKeyPair = HdKeyPair.createMaster(Mnemonic.createSeed("fury great shell", "mnemonic"))
                .generateChild("426788'/677/5777'/9/0/0/8887h/78/0");
        hdKeyPair1 = hdKeyPair.generateChild(7, true);
        assertEquals("m/426788'/677/5777'/9/0/0/8887'/78/0/7'", hdKeyPair1.path);
        assertEquals(10, hdKeyPair1.depth);
        assertEquals(7, hdKeyPair1.keyNumber);
        assertTrue(hdKeyPair1.isHardened);
        assertEquals(
                "Kzpej1ZPs1siQCSqFJ4Uj7HKNPHbhHK8rpeBoNbq6K2TAse5pyx2",
                hdKeyPair1.encode()
        );
        assertEquals(
                "1FS5fULPcy4wDgmtQoTBb1eDzFoUpCU8k",
                hdKeyPair1.getAddress()
        );
        hdKeyPair1 = hdKeyPair.generateChild(17, true);
        assertEquals("m/426788'/677/5777'/9/0/0/8887'/78/0/17'", hdKeyPair1.path);
        assertEquals(10, hdKeyPair1.depth);
        assertEquals(17, hdKeyPair1.keyNumber);
        assertTrue(hdKeyPair1.isHardened);
        assertEquals(
                "KxaPE46BxLLk8sjDiU6YcQZeSpDSm56L6kjyWRstYiUkf8QEqRZH",
                hdKeyPair1.encode()
        );
        assertEquals(
                "1QvfYoqcDrpjnntYRbvKKQ9GDQxrcsFXE",
                hdKeyPair1.getAddress()
        );
        desrerializeTest(hdKeyPair1.serialize(true), hdKeyPair1, "m//////////17'");
    }

    public void test5() {
        assertThrows(NullPointerException.class, () -> HdKeyPair.deserialize(null));
        assertThrows(HdKeyPair.Exception.class, () -> HdKeyPair.deserialize("II"));
        assertThrows(HdKeyPair.Exception.class, () -> HdKeyPair.deserialize("shirtmsg"));
        String xdrv = "xdrv9s21ZrQH143K3QTDL4LXw2F7HEK3wJUD2nW2nRk4stbPy6cq3jPPqjiChkVvvNKmPGJxWUtg6LnF5kejMRNN"
                + "U3TGtRBeJgk33yuGBxrMPHi";
        assertThrows(HdKeyPair.Exception.class, () -> HdKeyPair.deserialize(xdrv));
        String xpub = "xpub68y6rZzfjN6nVss6TFGMhnL4ebLC4F2enJGcpLS3DHeDzVPoLMWgBVU2AzvJEHQgqpqsff1YKvSByDfPcxgW"
                + "BEe8xvKZmn7VciAxPLwW6pP";
        assertThrows(HdKeyPair.Exception.class, () -> HdKeyPair.deserialize(xpub));
        String checksum = "xprv9s21ZrQH143K3QTDL4LXw2F7HEK3wJUD2nW2nRk4stbPy6cq3jPPqjiChkVvvNKmPGJxWUtg6LnF5kejMRNN"
                + "U3TGtRBeJgk33yuGBxrMPHj";
        assertThrows(HdKeyPair.Exception.class, () -> HdKeyPair.deserialize(checksum));
        String nonZero = "xprv9s2SPatNQ9Vc6GTbVMFPFo7jsaZySyzk7L8n2uqKXJen3KUmvQNTuLh3fhZMBoG3G4ZW1N2kZuHEPY53qmbZ"
                + "zCHshoQnNf4GvELZfqTUrcv";
        assertThrows(HdKeyPair.Exception.class, () -> HdKeyPair.deserialize(nonZero));
        String nonZeroIndex = "xprv9s21ZrQH4r4TsiLvyLXqM9P7k1K3EYhA1kkD6xuquB5i39AU8KF42acDyL3qsDbU9NmZn6MsGSUYZEs"
                + "uoePmjzsB3eFKSUEh3Gu1N3cqVUN";
        assertThrows(HdKeyPair.Exception.class, () -> HdKeyPair.deserialize(nonZeroIndex));
        String key = "xprv9s21ZrQH143K24Mfq5zL5MhWK9hUhhGbd45hLXo2Pq2oqzMMo63oStZzF93Y5wvzdUayhgkkFoicQZcP3y52uPPxF"
                + "nfoLZB21Teqt1VvEHx";
        assertThrows(KeyPair.Exception.class, () -> HdKeyPair.deserialize(key));
        String key2 = "xprv9s21ZrQH143K24Mfq5zL5MhWK9hUhhGbd45hLXo2Pq2oqzMMo63oStZzFAzHGBP2UuGCqWLTAPLcMtD5SDKr24z"
                + "3aiUvKr9bJpdrcLg1y3G";
        assertThrows(KeyPair.Exception.class, () -> HdKeyPair.deserialize(key2));
        String checksum2 = "xprv9s21ZrQH143K3QTDL4LXw2F7HEK3wJUD2nW2nRk4stbPy6cq3jPPqjiChkVvvNKmPGJxWUtg6LnF5kejMRN"
                + "NU3TGtRBeJgk33yuGBxrMPHL";
        assertThrows(HdKeyPair.Exception.class, () -> HdKeyPair.deserialize(checksum2));
    }

    private void desrerializeTest(String serialized, HdKeyPair hdKeyPair, String path) {
        HdKeyPair deserialized = HdKeyPair.deserialize(serialized);
        assertEquals(hdKeyPair.keyNumber, deserialized.keyNumber);
        assertEquals(hdKeyPair.depth, deserialized.depth);
        assertTrue(hdKeyPair.isHardened == deserialized.isHardened);
        assertEquals(0, hdKeyPair.privateKey.compareTo(deserialized.privateKey));
        assertEquals(hdKeyPair.getAddress(), deserialized.getAddress());
        assertEquals(path, deserialized.path);
        assertEquals(serialized, HdKeyPair.deserialize(serialized).serialize(true));
    }
}