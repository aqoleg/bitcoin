package space.aqoleg.keys.test;

import org.junit.jupiter.api.Test;
import space.aqoleg.keys.Mnemonic;
import space.aqoleg.utils.Converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MnemonicTest {

    @Test
    void createSeed() {
        assertThrows(NullPointerException.class, () -> Mnemonic.createSeed(null, "1"));
        assertThrows(NullPointerException.class, () -> Mnemonic.createSeed("1", null));

        areEquals(
                "aac2a6302e48577ab4b46f23dbae0774e2e62c796f797d0a1b5faeb528301e3064342dafb79069e7c4c6b8c38ae11d7a97"
                        + "3bec0d4f70626f8cc5184a8d0b0756",
                "wild father tree among universe such mobile favorite target dynamic credit identify",
                "electrum"
        );
        areEquals(
                "741b72fd15effece6bfe5a26a52184f66811bd2be363190e07a42cca442b1a5bb22b3ad0eb338197287e6d314866c7fba8"
                        + "63ac65d3f156087a5052ebc7157fce",
                "foobar",
                "electrum" + "none"
        );
        areEquals(
                "c55257c360c07c72029aebc1b53c05ed0362ada38ead3e3e9efa3708e53495531f09a6987599d18264c1e1c92f2cf14163"
                        + "0c7a3c4ab7c81b2f001698e7463b04",
                "abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about",
                "mnemonic" + "TREZOR"
        );
        areEquals(
                "d71de856f81a8acc65e6fc851a38d4d7ec216fd0796d0a6827a3ad6ed5511a30fa280f12eb2e47ed2ac03b5c462a0358d1"
                        + "8d69fe4f985ec81778c1b370b652a8",
                "letter advice cage absurd amount doctor acoustic avoid letter advice cage above",
                "mnemonic" + "TREZOR"
        );
        areEquals(
                "ac27495480225222079d7be181583751e86f571027b0497b5b5d11218e0a8a13332572917f0f8e5a589620c6f15b11c61d"
                        + "ee327651a14c34e18231052e48c069",
                "zoo zoo zoo zoo zoo zoo zoo zoo zoo zoo zoo wrong",
                "mnemonic" + "TREZOR"
        );
        areEquals(
                "deb5f45449e615feff5640f2e49f933ff51895de3b4381832b3139941c57b59205a42480c52175b6efcffaa58a2503887c"
                        + "1e8b363a707256bdd2b587b46541f5",
                "cat swing flag economy stadium alone churn speed unique patch report train",
                "mnemonic" + "TREZOR"
        );
        areEquals(
                "26e975ec644423f4a4c4f4215ef09b4bd7ef924e85d1d17c4cf3f136c2863cf6df0a475045652c57eb5fb41513ca2a2d67"
                        + "722b77e954b4b3fc11f7590449191d",
                "all hour make first leader extend hole alien behind guard gospel lava path output census museum "
                        + "junior mass reopen famous sing advance salt reform",
                "mnemonic" + "TREZOR"
        );
        areEquals(
                "7b4a10be9d98e6cba265566db7f136718e1398c71cb581e1b2f464cac1ceedf4f3e274dc270003c670ad8d02c4558b2f8e"
                        + "39edea2775c9e232c7cb798b069e88",
                "scissors invite lock maple supreme raw rapid void congress muscle digital elegant little brisk hair "
                        + "mango congress clump",
                "mnemonic" + "TREZOR"
        );
        areEquals(
                "01f5bced59dec48e362f2c45b5de68b9fd6c92c6634f44d6d40aab69056506f0e35524a518034ddc1192e1dacd32c1ed3e"
                        + "aa3c3b131c88ed8e7e54c49a5d0998",
                "void come effort suffer camp survey warrior heavy shoot primary clutch crush open amazing screen "
                        + "patrol group space point ten exist slush involve unfold",
                "mnemonic" + "TREZOR"
        );
        areEquals(
                "7dc61341dd16c16dcd2d201d77b23874841195cc1886e92a325d4e5a11bd287caf6c23ca5e63c8eb831810b8b250a975e2"
                        + "2a872dae309cb7e8727f1c11ddd0ce",
                "memory coach exit",
                "mnemonic"
        );
        areEquals(
                "248340b42d9d49f19c4e62b087dff0d44765ea1c34abe2467a1edf63567d7f5917d8374fb7460dcd560a19f5be35d6438d"
                        + "a4deded6dc3e477b489a2ea7d183b9",
                "identify piece abstract dinner spell cash trash gather beef harbor opera elite ivory absorb glide "
                        + "toast lock river leopard scorpion parrot pretty siege try",
                "mnemonic" + "identify piece abstract dinner spell cash trash gather beef harbor opera elite ivory "
                        + "absorb glide toast lock river leopard scorpion parrot pretty siege try"
        );
        areEquals(
                "f46dda165b2068a91106a15980a8550d510685a012c49b0eea13056de5143f58436fce0c7b8f5dc5e84892194a3eb711f9"
                        + "268ca12b9b59607acee1627dbaa56c",
                "acquire address mirror check ceiling salt",
                "mnemonic"
        );
        areEquals(
                "84fbe72835916fd3bc12e209ba30f01468dd8fadfb3b120c8e40be36fbe4f3d716124bb63f1c0d94caa245c202fcc68c82"
                        + "cbbdf30afc1ef96e15299ad63315a7",
                "elegant aunt snow squeeze opera then surface start action dad logic three",
                "mnemonic"
        );
        areEquals(
                "9be8cb93ff66226d6cf478670e3bb21fbe38e1adc6b710625f642de5385d71b16be9421f437d1ee3626bf30a7298d45903"
                        + "8d74221f06f7521daecfa2ceb262f9",
                "danger agree whip",
                "mnemonic"
        );
    }

    private void areEquals(String correctSeed, String mnemonic, String passphrase) {
        byte[] seed = Mnemonic.createSeed(mnemonic, passphrase);
        assertEquals(correctSeed, Converter.bytesToHex(seed, false, false));
    }
}