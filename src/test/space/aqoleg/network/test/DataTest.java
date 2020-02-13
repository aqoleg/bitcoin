package space.aqoleg.network.test;

import org.junit.jupiter.api.Test;
import space.aqoleg.network.Data;
import space.aqoleg.utils.Converter;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class DataTest {

    @Test
    void getBalance() {
        assertNull(Data.getBalance("null"));
        assertNull(Data.getBalance("899"));

        Data.Balance balance = Data.getBalance("1Bjrramo5EisLKeUXjw9GcUcfXcVnpi6vK");
        assertNotNull(balance);
        assertEquals(2, balance.txN);
        assertEquals("50.00000588", balance.value.toPlainString());

        balance = Data.getBalance("1FJNKtXWjbNA1TBzCyTEnoMEbC8XsFPmFF");
        assertNotNull(balance);
        assertEquals(1, balance.txN);
        assertEquals("50.00000000", balance.value.toPlainString());
    }

    @Test
    void getUnspentOutputs() {
        assertNull(Data.getUnspentOutputs("null"));
        assertNull(Data.getUnspentOutputs("899"));

        ArrayList<Data.UnspentOutput> utx = Data.getUnspentOutputs("1Bjrramo5EisLKeUXjw9GcUcfXcVnpi6vK");
        assertNotNull(utx);
        assertEquals(2, utx.size());
        assertArrayEquals(
                Converter.hexToBytes("5bc9bd461b455db119181c060085bd656430d9d431bbe4539eb0e367e9c77ac8"),
                utx.get(0).transactionHash
        );
        assertEquals(0, utx.get(0).outIndex);
        assertArrayEquals(
                Converter.hexToBytes("4104" + "953e5321dc6538f67ac273fe70cbfdec386bd389b0bf0c81a257f9351c7c0d9b"
                        + "6f8cb3e4b14fe322158935b04474e5afbded15f3ed5edd055d15368d708704fe" + "ac"),
                utx.get(0).scriptPubKey
        );
        assertEquals("50.00000000", utx.get(0).value.toPlainString());
        assertArrayEquals(
                Converter.hexToBytes("c74d59ef2de2029bc5f45d74673f05e743ceab463c1685ae72e33bd0527b9d80"),
                utx.get(1).transactionHash
        );
        assertEquals(31, utx.get(1).outIndex);
        assertArrayEquals(
                Converter.hexToBytes("76a914" + "75cc58509c6571c76808661acd803dac8a5ea869" + "88ac"),
                utx.get(1).scriptPubKey
        );
        assertEquals("0.00000588", utx.get(1).value.toPlainString());

        utx = Data.getUnspentOutputs("1FJNKtXWjbNA1TBzCyTEnoMEbC8XsFPmFF");
        assertNotNull(utx);
        assertEquals(1, utx.size());
        assertArrayEquals(
                Converter.hexToBytes("a6f66e5bc206d09745c05bc590113409ff7cbd9199b995cc0a6274a4690ede80"),
                utx.get(0).transactionHash
        );
        assertEquals(0, utx.get(0).outIndex);
        assertArrayEquals(
                Converter.hexToBytes("4104" + "329493cb21f89d080d7842952cbbdadc8a12d76ad53173aae8bbd74352ee3282"
                        + "33e326503e27c4399415e5fabacb98cf9cdf2a07effc590e8e29a6d4a385a394" + "ac"),
                utx.get(0).scriptPubKey
        );
        assertEquals("50.00000000", utx.get(0).value.toPlainString());
    }
}