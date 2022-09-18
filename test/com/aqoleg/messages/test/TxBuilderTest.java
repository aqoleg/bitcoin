package com.aqoleg.messages.test;

import com.aqoleg.Test;
import com.aqoleg.crypto.Sha256;
import com.aqoleg.keys.Address;
import com.aqoleg.keys.KeyPair;
import com.aqoleg.messages.Message;
import com.aqoleg.messages.Script;
import com.aqoleg.messages.Transaction;
import com.aqoleg.messages.TxBuilder;
import com.aqoleg.utils.BytesOutput;

import java.math.BigInteger;
import java.util.ArrayList;

@SuppressWarnings("unused")
public class TxBuilderTest extends Test {

    public static void main(String[] args) {
        new TxBuilderTest().testAll();
    }

    public void create() {
        byte[] hash = Sha256.getHash(new byte[2]);
        KeyPair keyPair = new KeyPair(BigInteger.TEN, true);
        Address address = Address.createFromPublicKey(keyPair.publicKey);
        Script script = Script.createScriptPubKey(address);
        BytesOutput bytesOutput = new BytesOutput();
        bytesOutput.writeIntLE(1);
        bytesOutput.writeVariableLength(1);
        bytesOutput.writeBytes(new byte[32]);
        bytesOutput.writeIntLE(0);
        bytesOutput.writeVariableLength(0);
        bytesOutput.writeIntLE(0);
        bytesOutput.writeVariableLength(1);
        bytesOutput.writeLongLE(50000000);
        script.write(bytesOutput);
        bytesOutput.writeIntLE(0);
        Transaction tx0 = Transaction.fromBytes(bytesOutput.toByteArray());

        TxBuilder txBuilder1 = new TxBuilder();
        assertEquals("inputs: 0, outputs: 0, fee: 0.00000000, size: 10", txBuilder1.toString());
        assertEquals(0, txBuilder1.calculateFee());
        assertEquals(10, txBuilder1.getSize());
        assertThrows(NullPointerException.class, () -> txBuilder1.addInput(null, null));
        assertThrows(Message.Exception.class, () -> txBuilder1.addInput(tx0.getTxOutput(0), new KeyPair(true)));
        assertThrows(NullPointerException.class, () -> txBuilder1.addOutput(0, null));
        assertThrows(Message.Exception.class, txBuilder1::build);

        txBuilder1.addInput(tx0.getTxOutput(0), keyPair);
        assertThrows(
                Message.Exception.class,
                () -> txBuilder1.addInput(tx0.getTxOutput(0), keyPair)
        );
        assertEquals(
                "inputs: 1, 0: (13DaZ9nfmJLfzU6oBnD2sdCiDmf3M5fmLx: 0.50000000), " +
                        "outputs: 0, fee: 0.50000000, size: 157",
                txBuilder1.toString()
        );
        Transaction tx1 = txBuilder1.build();
        int size = txBuilder1.getSize();
        assertTrue(size >= 156 && size <= 158);
        assertEquals(size, tx1.getSize());
        assertEquals(50000000, txBuilder1.calculateFee());
        tx1.verifyInput(0, tx0);
        txBuilder1.build().verifyInput(0, tx0);
        txBuilder1.build().verifyInput(0, tx0);

        TxBuilder txBuilder2 = new TxBuilder();
        KeyPair keyPair1 = new KeyPair(BigInteger.ONE, false);
        Address address1 = Address.createFromPublicKey(keyPair1.publicKey);
        txBuilder2.addOutput(20000000, address1);
        txBuilder2.addOutput(20000000, address);
        assertEquals(
                "inputs: 0, outputs: 2, " +
                        "0: (1EHNa6Q4Jz2uvNExL497mE43ikXhwF6kZm: 0.20000000), " +
                        "1: (13DaZ9nfmJLfzU6oBnD2sdCiDmf3M5fmLx: 0.20000000), fee: -0.40000000, size: 78",
                txBuilder2.toString()
        );
        assertThrows(Message.Exception.class, txBuilder2::build);
        txBuilder2.setChange(100, address1);
        assertEquals(
                "inputs: 0, outputs: 3, " +
                        "0: (1EHNa6Q4Jz2uvNExL497mE43ikXhwF6kZm: 0.20000000), " +
                        "1: (13DaZ9nfmJLfzU6oBnD2sdCiDmf3M5fmLx: 0.20000000), " +
                        "2(change): (1EHNa6Q4Jz2uvNExL497mE43ikXhwF6kZm: -0.40011200), fee: 0.00011200, size: 112",
                txBuilder2.toString()
        );
        assertThrows(Message.Exception.class, txBuilder2::build);
        txBuilder2.addInput(tx0.getTxOutput(0), keyPair);
        Transaction tx2 = txBuilder2.build();
        assertTrue(258 <= tx2.getSize() && tx2.getSize() <= 260);
        ArrayList<Transaction.Output> outputs = new ArrayList<>();
        assertTrue(tx2.searchTxOutput(address1, outputs));
        assertEquals(
                "value: 0.20000000, scriptPubKey: (dup hash160 " +
                        "pubKeyHash(1EHNa6Q4Jz2uvNExL497mE43ikXhwF6kZm) equalverify checksig)",
                outputs.get(0).toString()
        );
        assertEquals(2, outputs.get(1).index);
        assertEquals(9974100, outputs.get(1).value);
        tx2.verifyInput(0, tx0);
        assertThrows(Message.Exception.class, () -> tx2.verifyInput(0, tx1));

        TxBuilder txBuilder3 = new TxBuilder();
        txBuilder3.setChange(200, address1).addInput(tx2.getTxOutput(1), keyPair);
        assertEquals(
                "inputs: 1, 0: (13DaZ9nfmJLfzU6oBnD2sdCiDmf3M5fmLx: 0.20000000), outputs: 1, " +
                        "0(change): (1EHNa6Q4Jz2uvNExL497mE43ikXhwF6kZm: 0.19961800), fee: 0.00038200, size: 191",
                txBuilder3.toString()
        );
        txBuilder3.addOutput(30000000, address).addInput(tx0.getTxOutput(0), keyPair).setChange(0, null);
        assertEquals(
                "inputs: 2, 0: (13DaZ9nfmJLfzU6oBnD2sdCiDmf3M5fmLx: 0.20000000), " +
                        "1: (13DaZ9nfmJLfzU6oBnD2sdCiDmf3M5fmLx: 0.50000000), outputs: 1, " +
                        "0: (13DaZ9nfmJLfzU6oBnD2sdCiDmf3M5fmLx: 0.30000000), fee: 0.40000000, size: 338",
                txBuilder3.toString()
        );
        Transaction tx3 = txBuilder3.build();
        assertTrue(336 <= tx3.getSize() && tx3.getSize() <= 340);
        tx3.verifyInput(0, tx2);
        tx3.verifyInput(1, tx0);
    }
}