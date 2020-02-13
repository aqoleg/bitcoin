// the same as Connection, but with logs
package space.aqoleg.network.test;

import space.aqoleg.messages.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import static space.aqoleg.network.Addresses.addresses;

class ConnectionWithLogs { // package-private
    private static final int connectTimeout = 3000; // reduced
    private static final int minVersion = 31800;

    private final space.aqoleg.network.Connection.Callback callback;
    private final long startTime;
    private int version = minVersion;
    private Socket socket = null;
    private boolean alive = true;
    private boolean ready = false;

    private ConnectionWithLogs(space.aqoleg.network.Connection.Callback callback) {
        this.callback = callback;
        startTime = System.currentTimeMillis();
    }

    static ConnectionWithLogs create(space.aqoleg.network.Connection.Callback callback, String address) { // package-private
        if (callback == null) {
            throw new NullPointerException("callback is null");
        }
        if (address == null) {
            throw new NullPointerException("address is null");
        }
        ConnectionWithLogs connection = new ConnectionWithLogs(callback);
        connection.run(address);
        System.out.println("create() created, startTime " + connection.startTime);
        return connection;
    }

    boolean isAlive() { // package-private
        System.out.println("isAlive() alive " + alive
                + (ready ? ", ready" : ", " + (System.currentTimeMillis() - startTime)));
        if (!alive) {
            return false;
        }
        if (ready || System.currentTimeMillis() - startTime <= connectTimeout) {
            return true;
        }
        close();
        return false;
    }

    boolean isReady() {
        return ready;
    } // package-private

    void sendTx(byte[] invBytes) { // package-private
        if (invBytes == null) {
            throw new NullPointerException("invBytes is null");
        }
        if (alive && ready) {
            write(invBytes);
        }
    }

    void close() { // package-private
        System.out.println(Thread.currentThread().toString() + " close()");
        alive = false;
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println(e.toString());
            }
        }
    }

    synchronized private void write(byte[] bytes) {
        System.out.println(Thread.currentThread().toString() + " write()");
        try {
            socket.getOutputStream().write(bytes);
        } catch (IOException e) {
            System.out.println(e.toString());
            close();
        }
    }

    private void run(String address) {
        new Thread(() -> {
            try {
                // delay
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                int spacePos = address.indexOf(" ");
                String ip = address.substring(0, spacePos);
                int port = Integer.parseInt(address.substring(spacePos + 1));
                socket = new Socket(ip, port);
                InputStream inputStream = socket.getInputStream();
                Version versionMessage = Version.create(version, ip, port); // print message
                System.out.println(versionMessage.toString());
                write(versionMessage.toByteArray());
                System.out.println("run wrote version");
                Message message = new Message();
                // noinspection InfiniteLoopStatement
                do {
                    String command = message.read(inputStream);
                    System.out.println("run read " + command);
                    switch (command) {
                        case Version.command:
                            versionMessage = Version.parse(message.finish()); // print message
                            System.out.println(versionMessage.toString());
                            int remoteVersion = versionMessage.version;
                            if (remoteVersion < minVersion) {
                                throw new UnsupportedOperationException("too low version");
                            }
                            if (version > remoteVersion) {
                                version = remoteVersion;
                            }
                            write(VerAck.toByteArray());
                            System.out.println("run wrote verack");
                            break;
                        case VerAck.command:
                            message.reset();
                            ready = true;
                            if (addresses.canAdd()) {
                                write(GetAddr.toByteArray());
                                System.out.println("run wrote getaddr");
                            }
                            if (callback.hasTx()) {
                                write(new Inv(new Inventory[]{callback.getInventory()}).toByteArray());
                                System.out.println("run wrote inv");
                            }
                            break;
                        case Ping.command:
                            if (version >= 60001) {
                                long nonce = Ping.getNonce(message.finish());
                                write(Pong.toByteArray(nonce));
                                System.out.println("run wrote pong " + nonce);
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
                                System.out.println("run added addresses " + addr.count);
                            } else {
                                message.reset();
                            }
                            break;
                        case GetData.command:
                            if (callback.hasTx()) {
                                System.out.println("run has tx");
                                if (GetData.parse(message.finish()).hasInventory(callback.getInventory())) {
                                    write(callback.getTxMessageBytes());
                                    System.out.println("run wrote tx");
                                }
                            } else {
                                message.reset();
                            }
                            break;
                        case Inv.command:
                            if (callback.hasTx()) {
                                if (Inv.parse(message.finish()).hasInventory(callback.getInventory())) {
                                    callback.txSend(null);
                                    System.out.println("run tx ok");
                                }
                            } else {
                                message.reset();
                            }
                            break;
                        case Reject.command:
                            String s = Reject.toString(message.finish()); // print message
                            callback.txSend(s);
                            System.out.println(s);
                            break;
                        // other commands
                        case Alert.command:
                            System.out.println(Alert.toString(message.finish()));
                            break;
                        default:
                            message.reset();
                    }
                } while (true);
            } catch (Exception e) {
                if (e instanceof IndexOutOfBoundsException | e instanceof IllegalArgumentException
                        | e instanceof UnsupportedOperationException | e instanceof SecurityException) {
                    System.out.println("can not connect to \"" + address + "\" " + e.toString());
                } else if (e instanceof IOException) {
                    System.out.println(e.toString()); // print
                } else {
                    e.printStackTrace();
                }
            }
            close();
            System.out.println("run thread end");
        }).start();
    }
    // no interface
}