/*
connection with one socket

usage:
    Connection connection = new Connection();
    boolean isClosed = connection.closed();
    connection.sendGetData();
    connection.sendTransaction();
    connection.close();
*/

package com.aqoleg.data;

import com.aqoleg.messages.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;

public class Connection {
    private final Socket socket = new Socket();
    private int version = 31800;

    /**
     * creates and starts new connection
     */
    public Connection() {
        new Thread(() -> {
            try {
                run(Addresses.next());
            } catch (NoRouteToHostException | ConnectException | SocketTimeoutException ignored) {
                close();
            } catch (SocketException exception) {
                String message = exception.getMessage();
                if (!message.contains("Socket closed") && !message.contains("Connection reset")) {
                    exception.printStackTrace();
                }
                close();
            } catch (IOException exception) {
                if (!exception.getMessage().contains("the end of stream")) {
                    exception.printStackTrace();
                }
                close();
            }
        }).start();
    }

    /**
     * @return true if connection was closed and can be deleted
     */
    public boolean closed() {
        return socket.isClosed();
    }

    /**
     * closes this connection
     */
    public void close() {
        try {
            socket.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * sends GetData message from the BlockLoader
     */
    void sendGetData() {
        synchronized (socket) {
            if (!socket.isConnected() || socket.isClosed()) {
                return;
            }
            try {
                OutputStream outputStream = socket.getOutputStream();
                BlockLoader.writeGetData(outputStream);
                outputStream.flush();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }

    /**
     * sends Transaction message from the TransactionSender
     */
    void sendTransaction() {
        synchronized (socket) {
            if (!socket.isConnected() || socket.isClosed()) {
                return;
            }
            try {
                OutputStream outputStream = socket.getOutputStream();
                TransactionSender.writeTransaction(outputStream);
                outputStream.flush();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }

    private void run(NetAddress netAddress) throws IOException {
        final String[] commands = new String[]{Version.command, VerAck.command, Ping.command, Addr.command,
                Block.command, Reject.command};
        InetSocketAddress socketAddress = new InetSocketAddress(netAddress.getInetAddress(), netAddress.getPort());
        socket.connect(socketAddress, 5000);
        socket.setSoTimeout(5000);
        send(Version.create(version, netAddress).toByteArray());
        InputStream inputStream = socket.getInputStream();
        Message message;
        do {
            try {
                message = Message.read(inputStream, commands);
            } catch (Message.Exception exception) {
                exception.printStackTrace();
                continue;
            }
            if (message instanceof Version) {
                int version = ((Version) message).getVersion();
                if (version < this.version) {
                    this.version = version;
                }
            } else if (message instanceof VerAck) {
                socket.setSoTimeout(0);
                send(VerAck.toByteArray());
                send(GetAddr.toByteArray());
                if (BlockLoader.hasDataToDownload()) {
                    sendGetData();
                }
                if (TransactionSender.hasTransactionToSend()) {
                    sendTransaction();
                }
            } else if (message instanceof Ping) {
                long nonce = ((Ping) message).nonce;
                if (nonce != 0) {
                    send(Pong.toByteArray(nonce));
                }
            } else if (message instanceof Addr) {
                Addresses.save((Addr) message);
            } else if (message instanceof Block) {
                BlockLoader.onBlockReceive((Block) message);
            } else if (message instanceof Reject) {
                TransactionSender.onRejectReceived((Reject) message);
            }
        } while (!closed());
    }

    private void send(byte[] bytes) {
        synchronized (socket) {
            try {
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write(bytes);
                outputStream.flush();
            } catch (IOException ignored) {
                ignored.printStackTrace();
            }
        }
    }
}