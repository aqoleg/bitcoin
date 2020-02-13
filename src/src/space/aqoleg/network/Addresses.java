// singleton with network addresses
// addresses have form <ipv4><one space><port>
package space.aqoleg.network;

import java.io.*;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

public class Addresses {
    public static final Addresses addresses = new Addresses(); // singleton
    public static final int capacity = 2000;
    private static final String[] seedArray = {
            "seed.bitcoin.sipa.be",
            "dnsseed.bluematt.me",
            "dnsseed.bitcoin.dashjr.org",
            "seed.bitcoinstats.com",
            "seed.bitcoin.jonasschnelli.ch",
            "seed.btc.petertodd.org"
    };

    private final Object lock = new Object(); // for synchronize
    private File file = null; // last addresses is in the end
    private HashSet<String> savedSet = new HashSet<>(); // all addresses saved into the file, size <= capacity
    private ArrayList<String> loadedList = new ArrayList<>(); // all addresses loaded from the file
    private int loadedListPos = -1; // go from the last to the first
    private ArrayList<String> newList = new ArrayList<>(); // addresses received from nodes
    private int newListPos = 0; // go from the first to the last
    private int seedArrayPos = 0;
    private Iterator<String> seedIterator = null; // iterator through seed addresses
    private boolean hasLoader = false;

    private Addresses() {
    }

    /**
     * locked when other thread uses getNextAddress() or add(address)
     * opens file home/.crypto/addresses.txt, cuts it size to half of capacity, loads addresses from this file
     */
    public void open() {
        synchronized (lock) {
            // reset all variables
            file = null;
            savedSet.clear();
            loadedList.clear();
            loadedListPos = -1;
            newList.clear();
            newListPos = 0;
            seedArrayPos = 0;
            seedIterator = null;
            hasLoader = false;
            // get file
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
            // read all addresses from the file into loadedList
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
            } catch (IOException e) { // no FileNotFoundException
                System.out.println("can not read file " + e.toString());
            }
            loadedListPos = loadedList.size() - 1; // set to the last address
            // put loadedList into the savedSet and the file, cut size to the half of the capacity
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
        }
    }

    /**
     * locked when other thread uses open() or add(address)
     *
     * @return next address to connect, or null if seed is loading
     */
    public String getNextAddress() {
        synchronized (lock) {
            if (newListPos < newList.size()) {
                // first priority - received from nodes addresses
                return newList.get(newListPos++);
            } else if (loadedListPos >= 0) {
                // second priority - loaded addresses
                return loadedList.get(loadedListPos--);
            } else if (seedIterator != null && seedIterator.hasNext()) {
                // third priority - seed
                return seedIterator.next();
            } else if (seedArrayPos < seedArray.length) {
                // load next seed
                if (!hasLoader) {
                    hasLoader = true;
                    loadSeed(seedArray[seedArrayPos]);
                }
                return null;
            } else {
                // reset all positions and iterator and start again
                loadedListPos = loadedList.size() - 1;
                newListPos = 0;
                seedArrayPos = 0;
                seedIterator = null;
                return getNextAddress();
            }
        }
    }

    /**
     * add new address into file
     *
     * @param address address to be add, locked when other thread uses open() or getNextAddress()
     */
    public void add(String address) {
        synchronized (lock) {
            // add this address into the newList, avoiding the same data and oversize
            if (savedSet.size() >= capacity || !savedSet.add(address)) {
                return;
            }
            newList.add(address);
            // add address to the file
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
        }
    }

    /**
     * @return false if add(address) does nothing
     */
    public boolean canAdd() {
        return savedSet.size() < capacity;
    }

    private void loadSeed(String seed) {
        new Thread(() -> {
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
            synchronized (lock) {
                seedArrayPos++;
                seedIterator = set.iterator();
                hasLoader = false;
            }
        }).start();
    }
}