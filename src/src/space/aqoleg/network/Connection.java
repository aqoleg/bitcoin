// handles socket and messages
package space.aqoleg.network;

import space.aqoleg.messages.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import static space.aqoleg.network.Addresses.addresses;

public class Connection {
    private static final int connectTimeout = 30000; // ms from creating till get verAck message
    private static final int minVersion = 31800;

    private final Callback callback;
    private final long startTime; // ms
    private int version = minVersion; // current version using for this connection, the lowest version of two nodes
    private Socket socket = null; // became not null in the beginning, do not became null after
    private boolean alive = true; // became false when closing, do not became false after
    private boolean ready = false; // became true when verAck message received, do not became true after

    private Connection(Callback callback) {
        this.callback = callback;
        startTime = System.currentTimeMillis();
    }

    /**
     * @param callback callback
     * @param address  String with form <ipv4><one space><port>
     * @return new started Connection
     * @throws NullPointerException if callback == null or address == null
     */
    public static Connection create(Callback callback, String address) {
        if (callback == null) {
            throw new NullPointerException("callback is null");
        }
        if (address == null) {
            throw new NullPointerException("address is null");
        }
        Connection connection = new Connection(callback);
        connection.run(address);
        return connection;
    }

    /**
     * closes connection, if connectTimeout is over
     * from the beginning the connection is alive, when connection became not alive, it will not became alive after
     *
     * @return false if connection is closed and can be removed
     */
    public boolean isAlive() {
        if (!alive) {
            return false;
        }
        if (ready || System.currentTimeMillis() - startTime <= connectTimeout) {
            return true;
        }
        close();
        return false;
    }

    /**
     * from the beginning the connection is not ready, when connection became ready, it will not became not ready after
     *
     * @return true if this connection is ready to send tx
     */
    public boolean isReady() {
        return ready;
    }

    /**
     * send transaction
     *
     * @param invBytes bytes of the Inv message containing hash of the transaction
     * @throws NullPointerException if invBytes == null
     */
    public void sendTx(byte[] invBytes) {
        if (invBytes == null) {
            throw new NullPointerException("invBytes is null");
        }
        if (alive && ready) {
            write(invBytes);
        }
    }

    /**
     * close this connection
     */
    public void close() {
        alive = false;
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException ignored) {
                // not synchronized
            }
        }
    }

    synchronized private void write(byte[] bytes) {
        try {
            socket.getOutputStream().write(bytes); // socket is not null
        } catch (IOException e) {
            close();
        }
    }

    private void run(String address) {
        new Thread(() -> {
            try {
                int spacePos = address.indexOf(" ");
                String ip = address.substring(0, spacePos);
                int port = Integer.parseInt(address.substring(spacePos + 1));
                socket = new Socket(ip, port);
                InputStream inputStream = socket.getInputStream();
                // send version, read messages
                write(Version.create(version, ip, port).toByteArray());
                Message message = new Message();
                // noinspection InfiniteLoopStatement
                do {
                    String command = message.read(inputStream);
                    switch (command) {
                        case Version.command:
                            // set the lowest version, send verAck
                            int remoteVersion = Version.parse(message.finish()).version;
                            if (remoteVersion < minVersion) {
                                throw new UnsupportedOperationException("too low version");
                            }
                            if (version > remoteVersion) {
                                version = remoteVersion;
                            }
                            write(VerAck.toByteArray());
                            break;
                        case VerAck.command:
                            // send getAddr, connection is ready
                            message.reset();
                            ready = true;
                            if (addresses.canAdd()) {
                                write(GetAddr.toByteArray());
                            }
                            if (callback.hasTx()) {
                                write(new Inv(new Inventory[]{callback.getInventory()}).toByteArray());
                            }
                            break;
                        case Ping.command:
                            // send pong if version >= 60001
                            if (version >= 60001) {
                                long nonce = Ping.getNonce(message.finish());
                                write(Pong.toByteArray(nonce));
                            } else {
                                message.reset();
                            }
                            break;
                        case Addr.command:
                            if (addresses.canAdd()) {
                                Addr addr = Addr.parse(message.finish());
                                while (addr.hasNext()) {
                                    String s = addr.getNext().addressToString();
                                    if (s != null) {
                                        addresses.add(s);
                                    }
                                }
                            } else {
                                message.reset();
                            }
                            break;
                        case GetData.command:
                            if (callback.hasTx()) {
                                if (GetData.parse(message.finish()).hasInventory(callback.getInventory())) {
                                    write(callback.getTxMessageBytes());
                                }
                            } else {
                                message.reset();
                            }
                            break;
                        case Inv.command:
                            if (callback.hasTx()) {
                                if (Inv.parse(message.finish()).hasInventory(callback.getInventory())) {
                                    callback.txSend(null);
                                }
                            } else {
                                message.reset();
                            }
                            break;
                        case Reject.command:
                            callback.txSend(Reject.toString(message.finish()));
                            break;
                        default:
                            message.reset();
                    }
                } while (true);
            } catch (Exception e) {
                if (e instanceof IndexOutOfBoundsException | e instanceof IllegalArgumentException
                        | e instanceof UnsupportedOperationException | e instanceof SecurityException) {
                    System.out.println("can not connect to \"" + address + "\" " + e.toString());
                } else if (!(e instanceof IOException)) {
                    e.printStackTrace(); // unexpected
                }
            }
            close();
        }).start();
    }

    public interface Callback {

        /**
         * @return true if has transaction to send
         */
        boolean hasTx();

        /**
         * @return Inventory with the transaction hash
         */
        Inventory getInventory();

        /**
         * @return byte array with transaction message
         */
        byte[] getTxMessageBytes();

        /**
         * @param message null if transaction was successfully broadcast, else reject message
         */
        void txSend(String message);
    }
}