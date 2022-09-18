/*
gets node addresses from the file and seed; synchronized

usage:
    $ java com.aqoleg.data.Addresses 20

    NetAddress netAddress = Addresses.next();
    Addresses.save(Addr);
*/

package com.aqoleg.data;

import com.aqoleg.messages.Addr;
import com.aqoleg.messages.NetAddress;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;

public class Addresses {
    private static final String[] seeds = new String[]{
            "seed.bitcoin.sipa.be",
            "dnsseed.bluematt.me",
            "dnsseed.bitcoin.dashjr.org",
            "seed.bitcoinstats.com",
            "seed.bitcoin.jonasschnelli.ch",
            "seed.btc.petertodd.org"
    };
    private static int seedsPos = 0;
    private static Iterator<NetAddress> seedIterator;
    private static final ArrayList<NetAddress> receivedAddresses = new ArrayList<>(); // synchronized object
    private static int receivedAddressesPos = 0;

    /**
     * prints info about entered number of NetAddresses
     *
     * @param args String index of NetAddress or empty
     */
    public static void main(String[] args) {
        if (args.length == 1) {
            int i = 1;
            try {
                i = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {
            }
            while (i-- >= 1) {
                System.out.println(next());
            }
        } else if (System.console() == null) {
            System.out.println(next());
        } else {
            while (true) {
                String input = System.console().readLine("enter or 'exit': ");
                if (input.equals("exit")) {
                    System.exit(0);
                }
                System.out.println(next());
            }
        }
    }

    /**
     * first priority - addresses received from nodes
     * second priority - saved addresses
     * third priority - seed
     * then from the beginning
     *
     * @return next NetAddress
     */
    public static NetAddress next() {
        synchronized (receivedAddresses) {
            if (receivedAddressesPos < receivedAddresses.size()) {
                return receivedAddresses.get(receivedAddressesPos++);
            }
            NetAddress netAddresses = Storage.readNextAddress();
            if (netAddresses != null) {
                return netAddresses;
            }
            if (seedIterator != null && seedIterator.hasNext()) {
                return seedIterator.next();
            }
            if (seedsPos < seeds.length) {
                seedIterator = loadSeed(seeds[seedsPos++]);
                return next();
            }
            receivedAddressesPos = 0;
            Storage.resetAddressesPosition();
            seedIterator = null;
            seedsPos = 0;
            return next();
        }
    }

    /**
     * saves all NetAddress from this Addr
     *
     * @param addr Addr to be saved
     * @throws NullPointerException if addr == null
     */
    public static void save(Addr addr) {
        if (addr == null) {
            throw new NullPointerException();
        }
        new Thread(() -> {
            synchronized (receivedAddresses) {
                Iterator<NetAddress> iterator = addr.getIterator();
                NetAddress netAddress;
                while (iterator.hasNext()) {
                    netAddress = iterator.next();
                    if (receivedAddresses.contains(netAddress)) {
                        return;
                    }
                    receivedAddresses.add(netAddress);
                    Storage.writeAddress(netAddress);
                }
            }
        }).start();
    }

    private static Iterator<NetAddress> loadSeed(String seed) {
        ArrayList<NetAddress> netAddresses = new ArrayList<>();
        try {
            InetAddress[] inetAddresses = InetAddress.getAllByName(seed);
            for (InetAddress inetAddress : inetAddresses) {
                netAddresses.add(NetAddress.fromInetAddress(inetAddress));
            }
        } catch (UnknownHostException | SecurityException exception) {
            exception.printStackTrace();
        }
        return netAddresses.iterator();
    }
}