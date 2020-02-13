// manual tests - see logs
package space.aqoleg.network.test;

import org.junit.jupiter.api.Test;
import space.aqoleg.network.Addresses;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static space.aqoleg.network.Addresses.addresses;

class AddressesTest {

    @Test
    void test() throws IOException {
        File appDirectory = new File(System.getProperty("user.home"), ".crypto");
        File tempDirectory = new File(System.getProperty("user.home"), ".cryptotmp");
        boolean hasDirectory = appDirectory.isDirectory();
        if (hasDirectory) {
            Files.move(appDirectory.toPath(), tempDirectory.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        File file = new File(appDirectory, "addresses.txt");

        // tests
        seed(file); // prints addresses got from seed
        readWrite(file); // auto test
        seedWithLogs(); // get addresses from seed using 3 threads, see logs
        readWriteWithLogs(file); // 2 threads reads, 1 thread writes, 1 thread checks, see logs
        randomTest(file); // random values and delay, 2 threads reads, 2 threads writes, 1 threads checks, see logs
        // end tests

        if (hasDirectory) {
            Files.move(tempDirectory.toPath(), appDirectory.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } else {
            assertTrue(appDirectory.delete());
        }
    }

    private void seed(File file) {
        assertTrue(addresses.canAdd());
        int i = 0;
        boolean first = true;
        do {
            String nextAddress = addresses.getNextAddress();
            if (nextAddress == null) {
                first = true;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                if (first) {
                    i++;
                }
                first = false;
                System.out.println(nextAddress);
            }
        } while (i != 9);
        assertTrue(addresses.canAdd());
        addresses.open();
        assertTrue(file.delete());
    }

    private void readWrite(File file) {
        addresses.open();
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < Addresses.capacity; i++) {
            list.add(String.valueOf(i));
        }
        write(list, file);
        addresses.open();
        for (int i = 0; i < Addresses.capacity / 4; i++) {
            assertEquals(String.valueOf(Addresses.capacity - i - 1), addresses.getNextAddress());
        }
        assertTrue(addresses.canAdd());
        addresses.add("one");
        assertEquals("one", addresses.getNextAddress());
        for (int i = Addresses.capacity / 4; i < Addresses.capacity / 2; i++) {
            assertEquals(String.valueOf(Addresses.capacity - i - 1), addresses.getNextAddress());
        }
        addresses.add("two");
        addresses.add("one");
        assertEquals("two", addresses.getNextAddress());
        for (int i = Addresses.capacity / 2; i < Addresses.capacity; i++) {
            assertEquals(String.valueOf(Addresses.capacity - i - 1), addresses.getNextAddress());
        }
        assertNull(addresses.getNextAddress());
        while (addresses.getNextAddress() == null) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        assertTrue(addresses.getNextAddress().endsWith(" 8333"));
        addresses.add("two");
        for (int i = 0; i < Addresses.capacity / 2; i++) {
            addresses.add(String.valueOf(i));
        }
        assertFalse(addresses.canAdd());
        assertEquals(String.valueOf(0), addresses.getNextAddress());
        assertEquals(String.valueOf(1), addresses.getNextAddress());
        list = read(file);
        for (int i = 0; i < Addresses.capacity / 2; i++) {
            assertEquals(String.valueOf(Addresses.capacity / 2 + i), list.get(i));
        }
        assertEquals("one", list.get(Addresses.capacity / 2));
        assertEquals("two", list.get(Addresses.capacity / 2 + 1));
        for (int i = 0; i < Addresses.capacity / 2 - 2; i++) {
            assertEquals(String.valueOf(i), list.get(Addresses.capacity / 2 + 2 + i));
        }
        assertTrue(file.delete());
    }

    private void seedWithLogs() {
        new Thread(() -> {
            int i = 100;
            while (i-- != 0) {
                String s = AddressesWithLogs.addresses.getNextAddress();
                System.out.println(Thread.currentThread().toString() + ", i " + i + ", " + s);
                try {
                    Thread.sleep(s == null ? 100 : 10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        new Thread(() -> {
            int i = 100;
            while (i-- != 0) {
                String s = AddressesWithLogs.addresses.getNextAddress();
                System.out.println(Thread.currentThread().toString() + ", i " + i + ", " + s);
                try {
                    Thread.sleep(s == null ? 50 : 10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        int i = 150;
        while (i-- != 0) {
            String s = AddressesWithLogs.addresses.getNextAddress();
            System.out.println(Thread.currentThread().toString() + ", i " + i + ", " + s);
            try {
                Thread.sleep(s == null ? 200 : 10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void readWriteWithLogs(File file) {
        AddressesWithLogs.addresses.open();
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < 11; i++) {
            list.add(String.valueOf(i));
        }
        write(list, file);
        AddressesWithLogs.addresses.open();

        new Thread(() -> {
            int i = 50;
            while (i-- != 0) {
                String s = AddressesWithLogs.addresses.getNextAddress();
                System.out.println(Thread.currentThread().toString() + ", i " + i + ", read " + s);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        new Thread(() -> {
            int i = 50;
            while (i-- != 0) {
                String s = AddressesWithLogs.addresses.getNextAddress();
                System.out.println(Thread.currentThread().toString() + ", i " + i + ", read " + s);
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        new Thread(() -> {
            int i = 50;
            while (i-- != 0) {
                AddressesWithLogs.addresses.add(String.valueOf(i));
                System.out.println(Thread.currentThread().toString() + ", i " + i + ", write " + i);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        int i = 20;
        while (i-- != 0) {
            boolean b = AddressesWithLogs.addresses.canAdd();
            System.out.println(Thread.currentThread().toString() + ", i " + i + ", can add " + b);
            try {
                Thread.sleep(600);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        list = read(file);
        System.out.println("in file");
        for (String s : list) {
            System.out.println(s);
        }
        assertTrue(file.delete());
    }

    private void randomTest(File file) {
        AddressesWithLogs.addresses.open();
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            list.add(String.valueOf((int) (Math.random() * 30)));
        }
        System.out.println("in file");
        for (String s : list) {
            System.out.println(s);
        }
        write(list, file);
        AddressesWithLogs.addresses.open();

        new Thread(() -> {
            int i = 30;
            while (i-- != 0) {
                String s = AddressesWithLogs.addresses.getNextAddress();
                System.out.println(Thread.currentThread().toString() + ", i " + i + ", read " + s);
                try {
                    Thread.sleep((int) (Math.random() * 100));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        new Thread(() -> {
            int i = 30;
            while (i-- != 0) {
                String s = AddressesWithLogs.addresses.getNextAddress();
                System.out.println(Thread.currentThread().toString() + ", i " + i + ", read " + s);
                try {
                    Thread.sleep((int) (Math.random() * 100));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        new Thread(() -> {
            int i = 30;
            while (i-- != 0) {
                String s = String.valueOf((int) (Math.random() * 30));
                AddressesWithLogs.addresses.add(s);
                System.out.println(Thread.currentThread().toString() + ", i " + i + ", write " + s);
                try {
                    Thread.sleep((int) (Math.random() * 100));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        new Thread(() -> {
            int i = 30;
            while (i-- != 0) {
                String s = String.valueOf((int) (Math.random() * 30));
                AddressesWithLogs.addresses.add(s);
                System.out.println(Thread.currentThread().toString() + ", i " + i + ", write " + s);
                try {
                    Thread.sleep((int) (Math.random() * 100));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        int i = 30;
        while (i-- != 0) {
            boolean b = AddressesWithLogs.addresses.canAdd();
            System.out.println(Thread.currentThread().toString() + ", i " + i + ", can add " + b);
            try {
                Thread.sleep((int) (Math.random() * 100));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        list = read(file);
        System.out.println("in file");
        for (String s : list) {
            System.out.println(s);
        }
        assertTrue(file.delete());
    }

    private static void write(ArrayList<String> list, File file) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            for (String s : list) {
                writer.write(s);
                writer.newLine();
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static ArrayList<String> read(File file) {
        ArrayList<String> list = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while (true) {
                line = reader.readLine();
                if (line == null) {
                    break;
                }
                list.add(line);
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}