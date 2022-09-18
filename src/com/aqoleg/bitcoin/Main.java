package com.aqoleg.bitcoin;

import com.aqoleg.data.BlockLoader;
import com.aqoleg.data.ConnectionManager;
import com.aqoleg.data.TransactionSender;
import com.aqoleg.keys.Address;
import com.aqoleg.keys.HdKeyPair;
import com.aqoleg.keys.KeyPair;
import com.aqoleg.keys.Mnemonic;
import com.aqoleg.messages.Block;
import com.aqoleg.messages.Message;
import com.aqoleg.messages.Transaction;
import com.aqoleg.messages.TxBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;

class Main {
    private static final Address change = new Address("17ukXj5mUYfW5QgQ1aA4XZBMmdDk4yJ6Th"); // write aqoleg
    private static final ArrayList<Transaction.Output> outputs = new ArrayList<>();
    private static TxBuilder txBuilder;
    private static HdKeyPair master;
    private static KeyPair keyPair;

    public static void main(String[] args) {
        System.out.println("bitcoin.aqoleg.com v1.0.0");
        if (System.console() == null) {
            System.out.println("start with console!");
            return;
        }
        ConnectionManager.start();
        resetTxBuilder();
        printMainMenu();
        while (true) {
            if (!(mainMenu())) {
                System.out.println("exit");
                ConnectionManager.stop();
                break;
            }
        }
    }

    private static void resetTxBuilder() {
        txBuilder = new TxBuilder().setChange(1, change);
    }

    private static void printMainMenu() {
        System.out.println("main menu, commands:");
        System.out.println("'w' - enter wif key");
        System.out.println("'h' - enter hd key");
        System.out.println("'r' - reset transaction");
        System.out.println("'f' - set change and fee in transaction");
        System.out.println("'i' - add input to transaction");
        System.out.println("'o' - add output to transaction");
        System.out.println("'s' - view and send transaction");
        System.out.println("'t' - download, view, verify transaction");
        System.out.println("'a' - view info");
        System.out.println("'e' - exit");
    }

    private static boolean mainMenu() {
        switch (System.console().readLine("enter command: ")) {
            case "w":
                enterWif();
                break;
            case "h":
                enterHd();
                break;
            case "r":
                resetTxBuilder();
                System.out.println("transaction is empty");
                break;
            case "f":
                setFee();
                break;
            case "i":
                addInput();
                break;
            case "o":
                addOutput();
                break;
            case "s":
                buildTransaction();
                break;
            case "t":
                viewTransaction();
                break;
            case "a":
                System.gc();
                System.out.println("active connections: " + ConnectionManager.getActiveConnections());
                System.out.println("threads: " + Thread.activeCount());
                long memory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                System.out.println("memory used: " + memory / 1024 + " kB");
                break;
            case "e":
                return false;
            default:
                printMainMenu();
        }
        return true;
    }

    private static void enterWif() {
        keyPair = null;
        outputs.clear();
        String input = String.valueOf(System.console().readPassword("enter wif: "));
        try {
            keyPair = KeyPair.decode(input);
            System.out.println("address: " + keyPair.getAddress());
        } catch (KeyPair.Exception exception) {
            System.out.println(exception.getMessage());
        }
    }

    private static void enterHd() {
        keyPair = null;
        outputs.clear();
        if (master == null) {
            String mnemonic = String.valueOf(System.console().readPassword("enter hd mnemonic: "));
            String passphrase = String.valueOf(System.console().readPassword("enter hd passphrase: "));
            master = HdKeyPair.createMaster(Mnemonic.createSeed(mnemonic, passphrase));
        }
        System.out.println("hd master address: " + master.getAddress());
        do {
            String command = System.console().readLine("enter path 'm/...', 'r' - reset, 'b' - back: ");
            if (command.equals("r")) {
                master = null;
                enterHd();
                return;
            } else if (command.equals("b")) {
                return;
            }
            if (command.startsWith("m")) {
                command = command.substring(1);
            }
            try {
                keyPair = master.generateChild(command);
                System.out.println("hd " + ((HdKeyPair) keyPair).path + " address: " + keyPair.getAddress());
                return;
            } catch (KeyPair.Exception exception) {
                System.out.println(exception.getMessage());
            }
        } while (true);
    }

    private static void setFee() {
        if (keyPair == null) {
            System.out.println("enter key for change address first");
            return;
        }
        String input = System.console().readLine("enter fee, satoshi per byte, or zero to remove change: ");
        try {
            float fee = Float.parseFloat(input);
            Address address = fee == 0 ? null : Address.createFromPublicKey(keyPair.publicKey);
            txBuilder.setChange(fee, address);
            if (address == null) {
                System.out.println("no change address, all unused funds goes to miners");
            } else {
                System.out.println("change address " + address + " , fee " + fee + " sat/b");
            }
        } catch (NumberFormatException | Message.Exception exception) {
            System.out.println(exception.getMessage());
        }
    }

    private static void addInput() {
        if (keyPair == null) {
            System.out.println("enter key of input address first");
            return;
        }
        Address address = Address.createFromPublicKey(keyPair.publicKey);
        System.out.println(address);
        Block block;
        do {
            for (int i = 0; i < outputs.size(); i++) {
                System.out.println(i + ": " + new BigDecimal(outputs.get(i).value).movePointLeft(8).toPlainString());
            }
            String print = outputs.size() > 0 ? "enter output number to add, " : "enter ";
            String input = System.console().readLine(print + "hash of block to search for outputs, 'b' - back: ");
            if (input.equals("b")) {
                return;
            }
            try {
                int outputIndex = Integer.parseInt(input);
                txBuilder.addInput(outputs.remove(outputIndex), keyPair);
                System.out.println("input added");
                continue;
            } catch (NumberFormatException | IndexOutOfBoundsException ignored) {
            } catch (Message.Exception exception) {
                System.out.println(exception.getMessage());
                continue;
            }
            try {
                block = BlockLoader.getBlock(input);
                System.out.print(block.searchTxOutput(address, outputs) ? "finds" : "does not find");
                System.out.println(" new outputs");
            } catch (Message.Exception exception) {
                System.out.println(exception.getMessage());
            }
        } while (true);
    }

    private static void addOutput() {
        try {
            Address address = new Address(System.console().readLine("enter output address: "));
            long value = new BigDecimal(System.console().readLine("enter value, btc: "))
                    .movePointRight(8)
                    .longValue();
            txBuilder.addOutput(value, address);
            System.out.println("set output " + value + " sat to " + address);
        } catch (Address.Exception | NumberFormatException | Message.Exception exception) {
            System.out.println(exception.getMessage());
        }
    }

    private static void buildTransaction() {
        Transaction transaction;
        try {
            transaction = txBuilder.build();
        } catch (Message.Exception exception) {
            System.out.println(exception.getMessage());
            System.out.println(txBuilder);
            return;
        }
        System.out.println(txBuilder);
        do {
            String print = "enter 't' - view transaction, 'h' - view transaction bytes, 's' - send, 'b' - back: ";
            String input = System.console().readLine(print);
            if (input.equals("b")) {
                return;
            } else if (input.equals("t")) {
                System.out.println(transaction);
                continue;
            } else if (input.equals("h")) {
                System.out.println(transaction.getHex());
                continue;
            } else if (!input.equals("s")) {
                continue;
            }
            if (System.console().readLine("enter 'send tx' - send tx: ").equals("send tx")) {
                TransactionSender.sendTransaction(transaction);
                System.out.println("transaction sent");
                resetTxBuilder();
                return;
            }
        } while (true);
    }

    private static void viewTransaction() {
        Block block;
        do {
            String input = System.console().readLine("enter hash of block with transaction to verify, 'b' - back: ");
            if (input.equals("b")) {
                return;
            }
            try {
                block = BlockLoader.getBlock(input);
            } catch (Message.Exception exception) {
                System.out.println(exception.getMessage());
                continue;
            }
            System.out.println(block.txNumber() + " transactions");
            do {
                input = System.console().readLine("enter tx hash, tx index, 'b' - back: ");
                if (input.equals("b")) {
                    break;
                }
                Transaction transaction;
                try {
                    transaction = block.getTx(Integer.parseInt(input));
                } catch (NumberFormatException | IndexOutOfBoundsException ignored) {
                    transaction = block.getTx(input);
                    if (transaction == null) {
                        System.out.println("transaction not found");
                        continue;
                    }
                }
                viewTransaction(transaction);
            } while (true);
        } while (true);
    }

    private static void viewTransaction(Transaction transaction) {
        System.out.println("transaction " + transaction.getHash());
        do {
            String input = System.console().readLine("enter index of input to verify, 'v' - view tx, 'b' - back: ");
            if (input.equals("b")) {
                return;
            } else if (input.equals("v")) {
                System.out.println(transaction);
                continue;
            }
            int inputIndex;
            try {
                inputIndex = Integer.parseInt(input);
            } catch (NumberFormatException ignored) {
                continue;
            }
            input = System.console().readLine("enter block hash with output for this input: ");
            try {
                Block block = BlockLoader.getBlock(input);
                transaction.verifyInput(inputIndex, block);
                System.out.println("verified");
            } catch (Message.Exception | IndexOutOfBoundsException exception) {
                System.out.println(exception.getMessage());
            }
        } while (true);
    }
}