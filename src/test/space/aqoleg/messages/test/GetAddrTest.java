package space.aqoleg.messages.test;

import org.junit.jupiter.api.Test;
import space.aqoleg.messages.GetAddr;
import space.aqoleg.utils.Converter;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GetAddrTest {

    @Test
    void test() {
        assertEquals(
                "f9beb4d9" + "676574616464720000000000" + "00000000" + "5df6e0e2",
                Converter.bytesToHex(GetAddr.toByteArray(), false, false)
        );
    }
}
