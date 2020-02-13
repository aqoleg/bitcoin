package space.aqoleg.messages.test;

import org.junit.jupiter.api.Test;
import space.aqoleg.messages.VerAck;
import space.aqoleg.utils.Converter;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VerAckTest {

    @Test
    void test() {
        assertEquals(
                "f9beb4d9" + "76657261636b000000000000" + "00000000" + "5df6e0e2",
                Converter.bytesToHex(VerAck.toByteArray(), false, false)
        );
    }
}
