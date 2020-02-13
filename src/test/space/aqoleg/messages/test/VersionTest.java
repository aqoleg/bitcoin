package space.aqoleg.messages.test;

import org.junit.jupiter.api.Test;
import space.aqoleg.messages.NetAddress;
import space.aqoleg.messages.Version;
import space.aqoleg.utils.Converter;

import static org.junit.jupiter.api.Assertions.*;

class VersionTest {

    @Test
    void test() {
        String payload = "62ea0000" + "0100000000000000" + "11b2d05000000000"
                + "0100000000000000" + "00000000000000000000ffff00000000" + "0000"
                + "0000000000000000" + "00000000000000000000ffff00000000" + "0000"
                + "3b2eb35d8ce61765" + "0f" + "2f5361746f7368693a302e372e322f" + "c03e0300";
        String message = "f9beb4d9" + "76657273696f6e0000000000" + "64000000" + "358d4932" + payload;
        assertThrows(NullPointerException.class, () -> Version.parse(null));
        assertThrows(IndexOutOfBoundsException.class, () -> Version.parse(new byte[5]));
        assertThrows(UnsupportedOperationException.class, () -> Version.parse(Converter.hexToBytes(payload + "1111")));
        Version version = Version.parse(Converter.hexToBytes(payload));
        assertEquals(message, Converter.bytesToHex(version.toByteArray(), false, false));
        assertEquals(60002, version.version);
        assertEquals(1, version.services);
        assertEquals(0x50d0b211, version.timestamp);
        assertEquals("0.0.0.0 0", version.addrRecv.addressToString());
        assertEquals(0, version.addrFrom.services);
        assertEquals(0x6517e68c5db32e3bL, version.nonce);
        assertEquals("/Satoshi:0.7.2/", version.userAgent);
        assertEquals(0x33ec0, version.startHeight);
        assertFalse(version.relay);

        version = new Version(
                70001,
                0,
                1415484102,
                NetAddress.create("127.0.0.1", 8333),
                NetAddress.create("127.0.0.1", 8333),
                0,
                "/Bitcoin.org Example:0.9.3/",
                329107,
                false
        );
        message = "f9beb4d9" + "76657273696f6e0000000000" + "71000000" + "d1daed29" + "71110100" + "0000000000000000"
                + "c6925e5400000000" + "0000000000000000" + "00000000000000000000ffff7f000001" + "208d"
                + "0000000000000000" + "00000000000000000000ffff7f000001" + "208d" + "0000000000000000" + "1b"
                + "2f426974636f696e2e6f7267204578616d706c653a302e392e332f" + "93050500" + "00";
        assertEquals(message, Converter.bytesToHex(version.toByteArray(), false, false));

        assertThrows(NullPointerException.class, () -> Version.create(10, null, 10));
        assertThrows(StringIndexOutOfBoundsException.class, () -> Version.create(10, "11.", 10));
        assertThrows(NumberFormatException.class, () -> Version.create(10, "4d.", 10));
        version = Version.create(61000, "11.22.33.2", 344);
        String s = "version: 61000, services: 0, timestamp: " + version.timestamp + ", addrRecvServices: 0"
                + ", addrRecvTime: " + version.addrRecv.time + ", addrRecv: 11.22.33.2 344, addrFromServices: 0"
                + ", addrFromTime: 0, addrFrom: null, nonce: " + version.nonce + ", userAgent: /crypto:0.0.1/"
                + ", startHeight: 0";
        assertEquals(s, version.toString());
    }
}