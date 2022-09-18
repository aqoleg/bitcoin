package com.aqoleg.data.test;

import com.aqoleg.Test;
import com.aqoleg.data.Connection;

@SuppressWarnings("unused")
public class ConnectionTest extends Test {

    public static void main(String[] args) {
        new ConnectionTest().testAll();
    }

    public void test() {
        Connection connection = new Connection();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (connection.closed()) {
            connection.close();
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        connection.close();
    }
}