package com.aqoleg.data.test;

import com.aqoleg.Test;
import com.aqoleg.data.ConnectionManager;

@SuppressWarnings("unused")
public class ConnectionManagerTest extends Test {

    public static void main(String[] args) {
        new ConnectionManagerTest().testAll();
    }

    public void test() {
        ConnectionManager.start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        do {
            try {
                Thread.sleep(800);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.print('.');
        } while (ConnectionManager.getActiveConnections() < 2);
        ConnectionManager.stop();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}