package space.aqoleg.messages.test;

import org.junit.jupiter.api.Test;
import space.aqoleg.messages.Alert;
import space.aqoleg.utils.Converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AlertTest {

    @Test
    void test() {
        String input = "73" + "01000000" + "3766404f00000000" + "b305434f00000000" + "f2030000" + "f1030000"
                + "00" + "10270000" + "48ee0000" + "00" + "64000000" + "00" + "46"
                + "53656520626974636f696e2e6f72672f666562323020696620796f7520686176652074"
                + "726f75626c6520636f6e6e656374696e67206166746572203230204665627275617279" + "00" + "47" + "30"
                + "45" + "02" + "21" + "008389df45f0703f39ec8c1cc42c13810ffcae14995bb648340219e353b63b53eb"
                + "02" + "20" + "09ec65e1c1aaeec1fd334c6b684bde2b3f573060d5b70c3a46723326e4e8a4f1";
        String output = "version: 1, relayUntil: 1329620535, expiration: 1329792435, id: 1010, cancel: 1009, "
                + "setCancelLength: 0, minVer: 10000, maxVer: 61000, setSubVerLength: 0, priority: 100, comment: , "
                + "statusBar: See bitcoin.org/feb20 if you have trouble connecting after 20 February, "
                + "reserved: , verified: true";
        // noinspection ResultOfMethodCallIgnored
        assertThrows(NullPointerException.class, () -> Alert.toString(null));
        // noinspection ResultOfMethodCallIgnored
        assertThrows(IndexOutOfBoundsException.class, () -> Alert.toString(new byte[2]));
        String longInput = input + "11";
        // noinspection ResultOfMethodCallIgnored
        assertThrows(UnsupportedOperationException.class, () -> Alert.toString(Converter.hexToBytes(longInput)));

        assertEquals(output, Alert.toString(Converter.hexToBytes(input)));
        input = "73" + "01000000" + "3766404f00000000" + "b305434f00000000" + "f2030000" + "f1030000" + "00"
                + "10270000" + "48ee0000" + "00" + "64000000" + "00" + "46"
                + "53656520626974636f696e2e6f72672f666562323020696620796f7520686176652074"
                + "726f75626c6520636f6e6e656374696e67206166746572203230204665627275617279" + "00" + "47" + "30"
                + "45" + "02" + "21" + "008389df45f0703f39ec8c1cc42c13810ffcae14995bb648340219e353b63b53eb"
                + "02" + "20" + "09ec65e1c1aaeec1fd334c6b684bde2b3f573060d5b70c3a46723326e4e8a4f2";
        output = "version: 1, relayUntil: 1329620535, expiration: 1329792435, id: 1010, cancel: 1009, "
                + "setCancelLength: 0, minVer: 10000, maxVer: 61000, setSubVerLength: 0, priority: 100, comment: , "
                + "statusBar: See bitcoin.org/feb20 if you have trouble connecting after 20 February, "
                + "reserved: , verified: false";
        assertEquals(output, Alert.toString(Converter.hexToBytes(input)));
    }
}
