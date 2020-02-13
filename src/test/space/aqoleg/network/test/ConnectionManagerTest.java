// manual tests - see logs
package space.aqoleg.network.test;

import org.junit.jupiter.api.Test;
import space.aqoleg.keys.Address;
import space.aqoleg.keys.KeyPair;
import space.aqoleg.messages.Transaction;
import space.aqoleg.network.ConnectionManager;
import space.aqoleg.utils.Converter;

import java.math.BigInteger;

import static space.aqoleg.network.test.ConnectionManagerWithLogs.connectionManager;

class ConnectionManagerTest {

    @Test
    void test() throws InterruptedException {
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

        connectionManager.connect(callback);
        Thread.sleep(20000);
        connectionManager.sendTx(transaction);
        System.out.println("active " + connectionManager.getActiveConnections());
        System.out.println("total " + connectionManager.getTotalConnections());
        Thread.sleep(10000);
        connectionManager.close();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class Callback implements ConnectionManager.Callback {

        @Override
        public void txSend(String message) {
            System.out.println("txSend, message " + message);
        }
    }
}
