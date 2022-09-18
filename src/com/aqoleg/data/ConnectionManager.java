/*
creates and manages several connections

usage:
    int activeConnections = ConnectionManager.getActiveConnections();
    ConnectionManager.start();
    ConnectionManager.stop();
    ConnectionManager.downloadBlock();
    ConnectionManager.sendTransaction();
*/

package com.aqoleg.data;

import java.util.ArrayList;
import java.util.Iterator;

public class ConnectionManager {
    private static final ArrayList<Connection> connections = new ArrayList<>();
    private static final int targetActiveConnections = 7;
    private static boolean run = false;
    private static int activeConnections = 0;

    /**
     * @return number of active connections
     */
    public static int getActiveConnections() {
        return activeConnections;
    }

    /**
     * connects to nodes
     */
    public static void start() {
        synchronized (connections) {
            if (run) {
                return;
            }
            run = true;
            new Thread(() -> {
                while (run) {
                    loop();
                }
            }).start();
        }
    }

    /**
     * closes all connections
     */
    public static void stop() {
        synchronized (connections) {
            if (!run) {
                return;
            }
            Iterator<Connection> iterator = connections.iterator();
            Connection connection;
            while (iterator.hasNext()) {
                connection = iterator.next();
                connection.close();
                iterator.remove();
            }
            run = false;
            activeConnections = 0;
        }
    }

    /**
     * sends messages about blocks to all connected nodes
     */
    static void downloadBlock() {
        synchronized (connections) {
            start();
            for (Connection connection : connections) {
                connection.sendGetData();
            }
        }
    }

    /**
     * sends messages about transaction to all connected nodes
     */
    static void sendTransaction() {
        synchronized (connections) {
            start();
            for (Connection connection : connections) {
                connection.sendTransaction();
            }
        }
    }

    private static void loop() {
        synchronized (connections) {
            int activeConnections = 0;
            Iterator<Connection> iterator = connections.iterator();
            Connection connection;
            while (iterator.hasNext()) {
                connection = iterator.next();
                if (connection.closed()) {
                    iterator.remove();
                } else {
                    activeConnections++;
                }
            }
            ConnectionManager.activeConnections = activeConnections;
            while (activeConnections++ < targetActiveConnections) {
                connections.add(new Connection());
            }
        }
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}