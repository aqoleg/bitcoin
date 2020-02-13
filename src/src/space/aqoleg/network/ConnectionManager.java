// singleton to manage all connections
package space.aqoleg.network;

import space.aqoleg.messages.Inv;
import space.aqoleg.messages.Inventory;
import space.aqoleg.messages.Message;
import space.aqoleg.messages.Transaction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import static space.aqoleg.network.Addresses.addresses;

public class ConnectionManager implements Connection.Callback {
    public static final ConnectionManager connectionManager = new ConnectionManager(); // singleton

    private Callback callback;
    private final ArrayList<Connection> connectionList = new ArrayList<>();
    private int totalConnections = 0;
    private int activeConnections = 0;
    private boolean run;
    private boolean hasTx = false;
    private Inventory inventory;
    private byte[] txMessageBytes;

    private ConnectionManager() {
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

    /**
     * @return total number of connections
     */
    public int getTotalConnections() {
        return totalConnections;
    }

    /**
     * @return number of active connections
     */
    public int getActiveConnections() {
        return activeConnections;
    }

    /**
     * connects to nodes
     *
     * @param callback Callback
     */
    public void connect(Callback callback) {
        this.callback = callback;
        connectionList.clear();
        run = true;
        hasTx = false;
        new Thread(() -> {
            addresses.open();
            while (run) {
                synchronized (connectionList) {
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
                        if (address == null) {
                            break;
                        }
                        connectionList.add(Connection.create(this, address));
                    }
                    totalConnections = connectionList.size();
                    this.activeConnections = activeConnections;
                }
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * sends transaction
     *
     * @param txPayload byte array with transaction payload
     */
    public void sendTx(byte[] txPayload) {
        new Thread(() -> {
            inventory = Inventory.create(Inventory.typeMsgTx, Transaction.getHash(txPayload, true));
            txMessageBytes = Message.toByteArray(Transaction.command, txPayload);
            hasTx = true;
            byte[] invBytes = new Inv(new Inventory[]{inventory}).toByteArray();
            synchronized (connectionList) {
                for (Connection connection : connectionList) {
                    connection.sendTx(invBytes);
                }
            }
        }).start();
    }

    /**
     * closes all connections
     */
    public void close() {
        Thread thread = new Thread(() -> {
            run = false;
            synchronized (connectionList) {
                for (Connection connection : connectionList) {
                    connection.close();
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public interface Callback {

        /**
         * @param message null if transaction was successfully broadcast, else reject message
         */
        void txSend(String message);
    }
}