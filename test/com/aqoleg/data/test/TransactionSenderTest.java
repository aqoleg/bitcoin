package com.aqoleg.data.test;

import com.aqoleg.Test;
import com.aqoleg.data.ConnectionManager;
import com.aqoleg.data.TransactionSender;
import com.aqoleg.messages.Transaction;

@SuppressWarnings("unused")
public class TransactionSenderTest extends Test {

    public static void main(String[] args) {
        new TransactionSenderTest().testAll();
    }

    public void test() {
        String hex = "01000000017d0514b3e1e7db2e5b5531637db3f55ea33851ba28398c3f9c9d86a04c43ae1b010000008b483045022" +
                "0313b83ffce249174594eb232334cf3843483457095a3896854586f0b79bca841022100c7115ba7f2347d50b9644b31b08" +
                "da4e7cb0dcd08d43c3b0e1150b1fbbb8056720141044b8b746f267f1527eb318696c1cb62770e9d425da87dafcc4cfec46" +
                "487a2c1fbdc31f0c283dd003a8d601a0ee80c936d557d678e381e7bc4a2c073129ff82321ffffffff01c025dda80200000" +
                "01976a914fb53e1a4c7f8ba5a5cb36b28496ae14c4ed9336f88ac00000000";
        TransactionSender.sendTransaction(Transaction.fromBytes(hexToBytes(hex)));
        for (int i = 0; i < 30; i++) {
            System.out.print('.');
            try {
                Thread.sleep(800);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        ConnectionManager.stop();
    }
}