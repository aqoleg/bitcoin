/*
broadcasts the transaction

usage:
    TransactionSender.sendTransaction(transactionToSend);
    boolean hasTransactions = TransactionSender.hasTransactionToSend();
    TransactionSender.writeTransaction(outputStream);
    TransactionSender.onRejectReceived(reject);
*/

package com.aqoleg.data;

import com.aqoleg.messages.Reject;
import com.aqoleg.messages.Transaction;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public class TransactionSender {
    private static final ArrayList<Transaction> transactions = new ArrayList<>(); // synchronized object

    /**
     * @param transaction Transaction to send
     * @throws NullPointerException if transaction == null
     */
    public static void sendTransaction(Transaction transaction) {
        if (transaction == null) {
            throw new NullPointerException();
        }
        new Thread(() -> {
            synchronized (transactions) {
                transactions.add(transaction);
                ConnectionManager.sendTransaction();
            }
        }).start();
    }

    /**
     * @return true if there are transactions to send
     */
    static boolean hasTransactionToSend() {
        synchronized (transactions) {
            return transactions.size() > 0;
        }
    }

    /**
     * writes Transaction message
     *
     * @param outputStream to write message in
     * @throws IOException if cannot write
     */
    static void writeTransaction(OutputStream outputStream) throws IOException {
        synchronized (transactions) {
            for (Transaction transaction : transactions) {
                outputStream.write(transaction.toByteArray());
            }
        }
    }

    static void onRejectReceived(Reject reject) {
        System.out.println(reject);
    }
}