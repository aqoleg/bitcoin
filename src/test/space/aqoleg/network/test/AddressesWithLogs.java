// the same as Addresses, but with logs
package space.aqoleg.network.test;

import java.io.*;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

class AddressesWithLogs { // package-private
    static final AddressesWithLogs addresses = new AddressesWithLogs(); // package-private
    private static final int capacity = 20; // private, reduced capacity
    private static final String[] seedArray = {
            "seed.bitcoin.sipa.be",
            "dnsseed.bluematt.me",
            "dnsseed.bitcoin.dashjr.org",
            "seed.bitcoinstats.com",
            "seed.bitcoin.jonasschnelli.ch",
            "seed.btc.petertodd.org"
    };

    private final Object lock = new Object();
    private File file = null;
    private HashSet<String> savedSet = new HashSet<>();
    private ArrayList<String> loadedList = new ArrayList<>();
    private int loadedListPos = -1;
    private ArrayList<String> newList = new ArrayList<>();
    private int newListPos = 0;
    private int seedArrayPos = 0;
    private Iterator<String> seedIterator = null;
    private boolean hasLoader = false;

    private AddressesWithLogs() {
    }

    void open() { // package-private
        System.out.println(Thread.currentThread().toString() + " open()");
        synchronized (lock) {
            System.out.println("open() synchronized");
            file = null;
            savedSet.clear();
            loadedList.clear();
            loadedListPos = -1;
            newList.clear();
            newListPos = 0;
            seedArrayPos = 0;
            seedIterator = null;
            hasLoader = false;
            System.out.println("open() did reset values");
            File appDirectory = new File(System.getProperty("user.home"), ".crypto");
            if (!appDirectory.isDirectory()) {
                if (!appDirectory.mkdirs()) {
                    System.out.println("can not create directory " + appDirectory.toString());
                    return;
                }
            }
            file = new File(appDirectory, "addresses.txt");
            if (!file.isFile()) {
                try {
                    if (!file.createNewFile()) {
                        throw new IOException("can not create file");
                    }
                } catch (IOException e) {
                    System.out.println("can not create file " + e.toString());
                    return;
                }
            }
            System.out.println("open() opened file");
            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line;
                while (true) {
                    line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                    if (!line.isEmpty()) {
                        loadedList.add(line);
                    }
                }
                reader.close();
            } catch (IOException e) {
                System.out.println("can not read file " + e.toString());
            }
            loadedListPos = loadedList.size() - 1;
            System.out.println("open() read file, loadedListPos " + loadedListPos);
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                int size = loadedList.size();
                String string;
                for (int i = size > capacity / 2 ? size - capacity / 2 : 0; i < size; i++) {
                    string = loadedList.get(i);
                    if (savedSet.add(string)) {
                        writer.write(string);
                        writer.newLine();
                    }
                }
                writer.close();
            } catch (IOException e) {
                System.out.println("can not write file " + e.toString());
            }
            System.out.println("open() wrote file, savedSet.size " + savedSet.size() + ", end");
        }
    }

    String getNextAddress() { // package-private
        System.out.println(Thread.currentThread().toString() + " getNextAddress()");
        synchronized (lock) {
            System.out.println("getNextAddress() synchronized, newList " + newListPos + "/" + newList.size()
                    + ", loadedList " + loadedListPos + "/" + loadedList.size() + ", seedIterator "
                    + (seedIterator == null ? "null" : (seedIterator.hasNext() ? "hasNext" : "end"))
                    + ", seedArray " + seedArrayPos + "/" + seedArray.length + ", hasLoader " + hasLoader);
            if (newListPos < newList.size()) {
                return newList.get(newListPos++);
            } else if (loadedListPos >= 0) {
                return loadedList.get(loadedListPos--);
            } else if (seedIterator != null && seedIterator.hasNext()) {
                return seedIterator.next();
            } else if (seedArrayPos < seedArray.length) {
                if (!hasLoader) {
                    hasLoader = true;
                    loadSeed(seedArray[seedArrayPos]);
                }
                return null;
            } else {
                loadedListPos = loadedList.size() - 1;
                newListPos = 0;
                seedArrayPos = 0;
                seedIterator = null;
                return getNextAddress();
            }
        }
    }

    void add(String address) { // package-private
        System.out.println(Thread.currentThread().toString() + " add(" + address + ")");
        synchronized (lock) {
            System.out.println("add() synchronized, savedSet.size " + savedSet.size());
            if (savedSet.size() >= capacity || !savedSet.add(address)) {
                return;
            }
            newList.add(address);
            System.out.println("add() added, newList.size " + newList.size());
            if (file != null) {
                try {
                    FileWriter writer = new FileWriter(file, true);
                    writer.append(address);
                    writer.append(System.getProperty("line.separator"));
                    writer.close();
                } catch (IOException e) {
                    System.out.println("can not write file " + e.toString());
                }
            }
            System.out.println("add() wrote");
        }
    }

    boolean canAdd() { // package-private
        System.out.println(Thread.currentThread().toString() + " canAdd() " + (savedSet.size() < capacity));
        return savedSet.size() < capacity;
    }

    private void loadSeed(String seed) {
        System.out.println("loadSeed(" + seed + ")");
        new Thread(() -> {
            System.out.println("loadSeed() thread");
            HashSet<String> set = new HashSet<>();
            try {
                InetAddress[] addressArray = InetAddress.getAllByName(seed);
                for (InetAddress address : addressArray) {
                    if (address instanceof Inet4Address) {
                        set.add(address.getHostAddress() + " 8333");
                    }
                }
            } catch (UnknownHostException | SecurityException e) {
                System.out.println("can not load seed " + seed + e.toString());
            }
            // delay
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("loadSeed() thread loaded " + set.size());
            synchronized (lock) {
                System.out.println("loadSeed() thread synchronized");
                seedArrayPos++;
                seedIterator = set.iterator();
                hasLoader = false;
            }
            System.out.println("loadSeed() thread end");
        }).start();
        System.out.println("loadSeed() end");
    }
}