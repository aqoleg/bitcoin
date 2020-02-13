package space.aqoleg.messages.test;

import org.junit.jupiter.api.Test;
import space.aqoleg.messages.Ping;
import space.aqoleg.utils.Converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PingTest {

    @Test
    void test() {
        assertThrows(NullPointerException.class, () -> Ping.getNonce(null));
        assertThrows(IndexOutOfBoundsException.class, () -> Ping.getNonce(new byte[7]));
        assertThrows(UnsupportedOperationException.class, () -> Ping.getNonce(new byte[9]));
        assertEquals(0x4dafe21121109400L, Ping.getNonce(Converter.hexToBytes("0094102111e2af4d")));
        assertEquals(0, Ping.getNonce(new byte[0]));
    }
}