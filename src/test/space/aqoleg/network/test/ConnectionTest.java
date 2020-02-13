// manual tests - see logs
package space.aqoleg.network.test;

import org.junit.jupiter.api.Test;
import space.aqoleg.keys.Address;
import space.aqoleg.keys.KeyPair;
import space.aqoleg.messages.Inv;
import space.aqoleg.messages.Inventory;
import space.aqoleg.messages.Message;
import space.aqoleg.messages.Transaction;
import space.aqoleg.network.Connection;
import space.aqoleg.utils.Converter;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static space.aqoleg.network.Addresses.addresses;

class ConnectionTest {

    @Test
    void test() throws InterruptedException {
        assertThrows(NullPointerException.class, () -> Connection.create(new Callback(), null));
        assertThrows(NullPointerException.class, () -> Connection.create(null, "1.1.1.1 1"));

        Callback callback = new Callback();
        Transaction transaction = new Transaction();
        transaction.addOutput(200000000, new Address("3DThRV9yAeaafhWphzXgACDW2Y88wtiQLq"));
        transaction.addInput(
                Converter.hexToBytes("f6c15db11aa9a685131a8a0fcfa3d5a2bb641cdc1dfb3692167b33cbc42fb96b"),
                false,
                0,
                Converter.hexToBytes("76a914a2af9e80f9310c069d524eaa193e9a6a99b1bf1088ac"),
                new KeyPair(new BigInteger("46475"), true)
        );
        byte[] txBytes = transaction.getPayload();
        callback.inventory = Inventory.create(Inventory.typeMsgTx, Transaction.getHash(txBytes, true));
        callback.txMessageBytes = Message.toByteArray(Transaction.command, txBytes);
        callback.hasTx = Math.random() > 0.5;

        addresses.open();
        String address;
        float i = 0;
        do {
            address = addresses.getNextAddress();
            if (address == null) {
                Thread.sleep(100);
            } else {
                i += Math.random();
            }
        } while (i < 10);

        Connection connection = Connection.create(callback, address);
        Thread.sleep(200);
        if (!connection.isReady()) {
            Thread.sleep(5000);
        }
        if (connection.isAlive() && !callback.hasTx) {
            callback.hasTx = true;
            connection.sendTx(new Inv(new Inventory[]{callback.inventory}).toByteArray());
        }
        Thread.sleep(7000);
        connection.isAlive();
        connection.close();
    }

    // see logs
    @Test
    void testWithLogs() throws InterruptedException {
        Callback callback = new Callback();
        Transaction transaction = new Transaction();
        transaction.addOutput(200000000, new Address("3DThRV9yAeaafhWphzXgACDW2Y88wtiQLq"));
        transaction.addInput(
                Converter.hexToBytes("f6c15db11aa9a685131a8a0fcfa3d5a2bb641cdc1dfb3692167b33cbc42fb96b"),
                false,
                0,
                Converter.hexToBytes("76a914a2af9e80f9310c069d524eaa193e9a6a99b1bf1088ac"),
                new KeyPair(new BigInteger("46475"), true)
        );
        byte[] txBytes = transaction.getPayload();
        callback.inventory = Inventory.create(Inventory.typeMsgTx, Transaction.getHash(txBytes, true));
        callback.txMessageBytes = Message.toByteArray(Transaction.command, txBytes);
        callback.hasTx = Math.random() > 0.5;

        ConnectionWithLogs connection = ConnectionWithLogs.create(callback, "something");
        connection.sendTx(new Inv(new Inventory[]{callback.inventory}).toByteArray());
        connection.isAlive();
        Thread.sleep(2000);
        connection.close();
        System.out.println("next");
        connection = ConnectionWithLogs.create(callback, "1.1.1.1 1");
        connection.isAlive();
        connection.sendTx(new Inv(new Inventory[]{callback.inventory}).toByteArray());
        Thread.sleep(2000);
        connection.isAlive();
        connection.sendTx(new Inv(new Inventory[]{callback.inventory}).toByteArray());
        connection.close();
        System.out.println("next");
        addresses.open();
        String address;
        float i = 0;
        do {
            address = addresses.getNextAddress();
            if (address == null) {
                Thread.sleep(100);
            } else {
                i += Math.random();
            }
        } while (i < 10);

        connection = ConnectionWithLogs.create(callback, address);
        Thread.sleep(200);
        if (!connection.isReady() && connection.isAlive()) {
            Thread.sleep(8000);
        }
        if (connection.isAlive() && !callback.hasTx) {
            callback.hasTx = true;
            connection.sendTx(new Inv(new Inventory[]{callback.inventory}).toByteArray());
        }
        if (connection.isAlive()) {
            Thread.sleep(8000);
        }
        connection.isAlive();
        connection.close();
    }

    private class Callback implements Connection.Callback {
        private boolean hasTx;
        private Inventory inventory;
        private byte[] txMessageBytes;

        @Override
        public boolean hasTx() {
            return hasTx;
        }

        @Override
        public Inventory getInventory() {
            return inventory;
        }

        @Override
        public byte[] getTxMessageBytes() {
            return txMessageBytes;
        }

        @Override
        public void txSend(String message) {
            System.out.println(message == null ? "tx send" : message);
        }
    }
}