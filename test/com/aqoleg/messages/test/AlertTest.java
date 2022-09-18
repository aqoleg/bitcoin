package com.aqoleg.messages.test;

import com.aqoleg.Test;
import com.aqoleg.messages.Alert;
import com.aqoleg.messages.Message;

@SuppressWarnings("unused")
public class AlertTest extends Test {

    public static void main(String[] args) {
        new AlertTest().testAll();
    }

    public void test() {
        String input = "73" + "01000000" + "3766404f00000000" + "b305434f00000000" + "f2030000" + "f1030000"
                + "00" + "10270000" + "48ee0000" + "00" + "64000000" + "00" + "46"
                + "53656520626974636f696e2e6f72672f666562323020696620796f7520686176652074"
                + "726f75626c6520636f6e6e656374696e67206166746572203230204665627275617279" + "00" + "47" + "30"
                + "45" + "02" + "21" + "008389df45f0703f39ec8c1cc42c13810ffcae14995bb648340219e353b63b53eb"
                + "02" + "20" + "09ec65e1c1aaeec1fd334c6b684bde2b3f573060d5b70c3a46723326e4e8a4f1";
        String output = "version: 1, relayUntil: 2012-02-19T03:02:15Z, expiration: 2012-02-21T02:47:15Z, id: 1010, "
                + "cancel: 1009, setCancel: 0, minVer: 10000, maxVer: 61000, setSubVer: 0, priority: 100, "
                + "statusBar: See bitcoin.org/feb20 if you have trouble connecting after 20 February";
        assertThrows(NullPointerException.class, () -> new Alert(null));
        assertThrows(Message.Exception.class, () -> new Alert(new byte[2]));
        String longInput = input + "11";
        assertThrows(Message.Exception.class, () -> new Alert(hexToBytes(longInput)));

        assertEquals(output, new Alert(hexToBytes(input)).toString());
        String incorrectSignature = "73" + "01000000" + "3766404f00000000" + "b305434f00000000" + "f2030000" + "f1030000"
                + "00" + "10270000" + "48ee0000" + "00" + "64000000" + "00" + "46"
                + "53656520626974636f696e2e6f72672f666562323020696620796f7520686176652074"
                + "726f75626c6520636f6e6e656374696e67206166746572203230204665627275617279" + "00" + "47" + "30"
                + "45" + "02" + "21" + "008389df45f0703f39ec8c1cc42c13810ffcae14995bb648340219e353b63b53eb"
                + "02" + "20" + "09ec65e1c1aaeec1fd334c6b684bde2b3f573060d5b70c3a46723326e4e8a4f2";
        assertThrows(Message.Exception.class, () -> new Alert(hexToBytes(incorrectSignature)));
    }
}