// the same as ConnectionManager, but with logs
package space.aqoleg.network.test;

import space.aqoleg.messages.Inv;
import space.aqoleg.messages.Inventory;
import space.aqoleg.messages.Message;
import space.aqoleg.messages.Transaction;
import space.aqoleg.network.Connection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import static space.aqoleg.network.Addresses.addresses;

class ConnectionManagerWithLogs implements Connection.Callback { // package-private
    static final ConnectionManagerWithLogs connectionManager = new ConnectionManagerWithLogs(); // package-private

    private space.aqoleg.network.ConnectionManager.Callback callback;
    private final ArrayList<Connection> connectionList = new ArrayList<>();
    private int totalConnections = 0;
    private int activeConnections = 0;
    private boolean run;
    private boolean hasTx = false;
    private Inventory inventory;
    private byte[] txMessageBytes;

    private ConnectionManagerWithLogs() {
    }

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
        return Arrays.copyOf(txMessageBytes, txMessageBytes.length);
    }

    @Override
    public void txSend(String message) {
        hasTx = false;
        callback.txSend(message);
    }

    int getTotalConnections() { // package-private
        return totalConnections;
    }

    int getActiveConnections() { // package-private
        return activeConnections;
    }

    void connect(space.aqoleg.network.ConnectionManager.Callback callback) { // package-private
        this.callback = callback;
        connectionList.clear();
        run = true;
        hasTx = false;
        new Thread(() -> {
            addresses.open();
            while (run) {
                synchronized (connectionList) {
                    System.out.println("run() synchronized");
                    int activeConnections = 0;
                    Iterator<Connection> iterator = connectionList.iterator();
                    while (iterator.hasNext()) {
                        Connection connection = iterator.next();
                        if (!connection.isAlive()) {
                            iterator.remove();
                        } else if (connection.isReady()) {
                            activeConnections++;
                        }
                    }
                    int i = activeConnections;
                    while (i++ < 6) {
                        String address = addresses.getNextAddress();
                        System.out.println("run() " + address);
                        if (address == null) {
                            break;
                        }
                        connectionList.add(Connection.create(this, address));
                    }
                    totalConnections = connectionList.size();
                    this.activeConnections = activeConnections;
                    System.out.println("run() " + this.activeConnections + "/" + totalConnections);
                }
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("connect() end");
        }).start();
    }

    void sendTx(Transaction transaction) { // package-private
        new Thread(() -> {
            byte[] txBytes = transaction.getPayload();
            inventory = Inventory.create(Inventory.typeMsgTx, Transaction.getHash(txBytes, true));
            txMessageBytes = Message.toByteArray(Transaction.command, txBytes);
            hasTx = true;
            byte[] invBytes = new Inv(new Inventory[]{inventory}).toByteArray();
            synchronized (connectionList) {
                System.out.println("sendTx() synchronized");
                for (Connection connection : connectionList) {
                    connection.sendTx(invBytes);
                }
            }
            System.out.println("sendTx() end");
        }).start();
    }

    void close() { // package-private
        Thread thread = new Thread(() -> {
            run = false;
            synchronized (connectionList) {
                System.out.println("close() synchronized");
                for (Connection connection : connectionList) {
                    connection.close();
                }
            }
            System.out.println("threads " + Thread.activeCount() + ", close() end");
        });
        thread.setDaemon(true);
        thread.start();
    }
}