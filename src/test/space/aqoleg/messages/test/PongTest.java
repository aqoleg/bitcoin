package space.aqoleg.messages.test;

import org.junit.jupiter.api.Test;
import space.aqoleg.messages.Pong;
import space.aqoleg.utils.Converter;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PongTest {

    @Test
    void test() {
        assertEquals(
                "f9beb4d9" + "706f6e670000000000000000" + "08000000" + "88ea8176" + "0094102111e2af4d",
                Converter.bytesToHex(Pong.toByteArray(0x4dafe21121109400L), false, false)
        );
    }
}