package com.aqoleg.data.test;

import com.aqoleg.Test;
import com.aqoleg.data.Addresses;
import com.aqoleg.messages.Addr;
import com.aqoleg.messages.NetAddress;
import com.aqoleg.utils.BytesOutput;

@SuppressWarnings("unused")
public class AddressesTest extends Test {

    public static void main(String[] args) {
        new AddressesTest().testAll();
    }

    public void test() {
        NetAddress netAddress0 = Addresses.next();
        NetAddress netAddress1 = Addresses.next();
        NetAddress netAddress2 = Addresses.next();
        BytesOutput bytesOutput = new BytesOutput();
        bytesOutput.writeVariableLength(3);
        netAddress2.write(bytesOutput, false);
        netAddress1.write(bytesOutput, false);
        netAddress0.write(bytesOutput, false);
        Addr addr = new Addr(bytesOutput.toByteArray());
        assertThrows(NullPointerException.class, () -> Addresses.save(null));
        Addresses.save(addr);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertTrue(netAddress2.equals(Addresses.next()));
        boolean equals = netAddress1.equals(netAddress2);
        assertTrue(netAddress1.equals(Addresses.next()) || equals);
        equals = equals || netAddress0.equals(netAddress1) || netAddress0.equals(netAddress2);
        assertTrue(netAddress0.equals(Addresses.next()) || equals);
    }
}