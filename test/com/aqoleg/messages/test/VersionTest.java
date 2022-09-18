package com.aqoleg.messages.test;

import com.aqoleg.Test;
import com.aqoleg.messages.Message;
import com.aqoleg.messages.NetAddress;
import com.aqoleg.messages.Version;

import java.net.InetAddress;
import java.net.UnknownHostException;

@SuppressWarnings("unused")
public class VersionTest extends Test {

    public static void main(String[] args) {
        new VersionTest().testAll();
    }

    public void test() {
        String payload = "62ea0000" + "0100000000000000" + "11b2d05000000000"
                + "0100000000000000" + "00000000000000000000ffff00000000" + "0000"
                + "0000000000000000" + "00000000000000000000ffff00000000" + "0000"
                + "3b2eb35d8ce61765" + "0f" + "2f5361746f7368693a302e372e322f" + "c03e0300";
        String message = "f9beb4d9" + "76657273696f6e0000000000" + "64000000" + "358d4932" + payload;
        assertThrows(NullPointerException.class, () -> new Version(null));
        assertThrows(Message.Exception.class, () -> new Version(new byte[5]));
        assertThrows(Message.Exception.class, () -> new Version(hexToBytes(payload + "1111")));
        Version version = new Version(hexToBytes(payload));
        assertEquals(
                "version: 60002, services: 1, time: 2012-12-18T18:12:33Z, " +
                        "addrRecv: (time: 0, services: 1, ip: 0.0.0.0, port: 0), " +
                        "addrFrom: (time: 0, services: 0, ip: 0.0.0.0, port: 0), " +
                        "nonce: 7284544412836900411, userAgent: /Satoshi:0.7.2/, startHeight: 212672",
                version.toString()
        );
        assertEquals(60002, version.getVersion());
        assertEquals(message, version.toByteArray());

        assertThrows(NullPointerException.class, () -> Version.create(1, 0, 0, null, null, 0, "", 0));
        try {
            InetAddress inetAddress = InetAddress.getByName("127.0.0.1");
            version = Version.create(
                    70001,
                    0,
                    1415484102,
                    NetAddress.fromInetAddress(inetAddress),
                    NetAddress.fromInetAddress(inetAddress),
                    0,
                    "/Bitcoin.org Example:0.9.3/",
                    329107
            );
            message = "f9beb4d9" + "76657273696f6e0000000000" + "71000000" + "d1daed29" + "71110100"
                    + "0000000000000000" + "c6925e5400000000" + "0000000000000000" + "00000000000000000000ffff7f000001"
                    + "208d" + "0000000000000000" + "00000000000000000000ffff7f000001" + "208d" + "0000000000000000"
                    + "1b" + "2f426974636f696e2e6f7267204578616d706c653a302e392e332f" + "93050500" + "00";
            assertEquals(
                    "version: 70001, services: 0, time: 2014-11-08T22:01:42Z, " +
                            "addrRecv: (time: 0, services: 0, ip: 127.0.0.1, port: 8333), " +
                            "addrFrom: (time: 0, services: 0, ip: 127.0.0.1, port: 8333), nonce: 0, " +
                            "userAgent: /Bitcoin.org Example:0.9.3/, startHeight: 329107, relay: false",
                    version.toString()
            );
            assertEquals(70001, version.getVersion());
            assertEquals(message, version.toByteArray());
        } catch (UnknownHostException exception) {
            System.out.println("cannot test...");
            exception.printStackTrace();
        }
    }
}