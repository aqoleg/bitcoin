package space.aqoleg.messages.test;

import org.junit.jupiter.api.Test;
import space.aqoleg.messages.Reject;
import space.aqoleg.utils.Converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RejectTest {

    @Test
    void test() {
        // noinspection ResultOfMethodCallIgnored
        assertThrows(NullPointerException.class, () -> Reject.toString(null));
        // noinspection ResultOfMethodCallIgnored
        assertThrows(IndexOutOfBoundsException.class, () -> Reject.toString(new byte[]{22, 0}));
        String input = "02" + "7478" + "12" + "15" + "6261642d74786e732d696e707574732d7370656e74"
                + "394715fcab51093be7bfca5a31005972947baf86a31017939575fb2354222821";
        String output = "message: tx, ccode: 0x12, reason: bad-txns-inputs-spent, "
                + "data: 394715fcab51093be7bfca5a31005972947baf86a31017939575fb2354222821";
        assertEquals(output, Reject.toString(Converter.hexToBytes(input)));
    }
}