package space.aqoleg.ui;

import space.aqoleg.keys.Address;
import space.aqoleg.keys.HdKeyPair;
import space.aqoleg.keys.KeyPair;
import space.aqoleg.keys.Mnemonic;
import space.aqoleg.messages.Transaction;
import space.aqoleg.network.ConnectionManager;
import space.aqoleg.network.Data;
import space.aqoleg.utils.Converter;

import java.math.BigDecimal;
import java.util.ArrayList;

import static space.aqoleg.network.ConnectionManager.connectionManager;

class Ui implements ConnectionManager.Callback {
    private Transaction transaction = new Transaction();
    private HdKeyPair master;
    private HdKeyPair root; // root HdKeyPair of the externals and internals HdKeyPairs
    private BigDecimal sum = BigDecimal.ZERO; // = (sum(inputs) - sum(outputs)), btc

    Ui() {
        connectionManager.connect(this);
        enter();
        mainMenu();
    }

    @Override
    public void txSend(String message) {
        System.out.println(message == null ? "transaction sent" : message);
    }

    // main menu loop
    private void mainMenu() {
        do {
            System.out.println("menu/, print command:");
            System.out.println("'n' - reset mnemonic");
            System.out.println("'m/path' - set path, for example m/44' or m/111h/0/99/1'/0");
            System.out.println("'w' - view wallet addresses and balance");
            System.out.println("'d' - view details");
            System.out.println("'t' - reset transaction");
            System.out.println("'i' - add transaction input");
            System.out.println("'o' - add transaction output");
            System.out.println("'c' - view and create transaction");
            System.out.println("'s' - view connection state");
            System.out.println("'e' - exit");
            String input = System.console().readLine();
            switch (input) {
                case "n":
                    enter();
                    break;
                case "w":
                    viewWallet();
                    break;
                case "d":
                    viewDetails();
                    break;
                case "t":
                    transaction = new Transaction();
                    sum = BigDecimal.ZERO;
                    break;
                case "i":
                    addInput();
                    break;
                case "o":
                    addOutput();
                    break;
                case "c":
                    viewTransaction();
                    break;
                case "e":
                    connectionManager.close();
                    System.exit(0);
                case "s":
                    System.out.println("active connections " + connectionManager.getActiveConnections()
                            + ", total connections " + connectionManager.getTotalConnections()
                            + ", threads " + Thread.activeCount());
                    break;
                default:
                    if (input.startsWith("m/")) {
                        try {
                            root = master.generateChild(input.substring(2));
                        } catch (IndexOutOfBoundsException | UnsupportedOperationException | NumberFormatException e) {
                            System.out.println(e.toString());
                        }
                    }
            }
        } while (true);
    }

    private void enter() {
        System.out.println("enter bip39 mnemonic:");
        String mnemonic = System.console().readLine();
        System.out.println("enter passphrase:");
        String passphrase = System.console().readLine();
        master = HdKeyPair.createMaster(Mnemonic.createSeed(mnemonic, passphrase)); // exception?
        root = master;
    }

    private void viewWallet() {
        HdKeyPair external = root.generateChild(0, false);
        BigDecimal total = BigDecimal.ZERO;
        System.out.println("external");
        for (int i = 0; i < 200; i++) {
            String address = external.generateChild(i, false).keyPair.getAddress();
            System.out.print(address);
            Data.Balance balance = Data.getBalance(address);
            if (balance != null) {
                System.out.println(", " + balance.value.toPlainString() + " btc, " + balance.txN + " tx#");
                total = total.add(balance.value);
                if (balance.txN == 0) {
                    break;
                }
            } else {
                System.out.println();
            }
        }
        HdKeyPair internal = root.generateChild(1, false);
        System.out.println("internal");
        for (int i = 0; i < 200; i++) {
            String address = internal.generateChild(i, false).keyPair.getAddress();
            System.out.print(address);
            Data.Balance balance = Data.getBalance(address);
            if (balance != null) {
                System.out.println(", " + balance.value.toPlainString() + " btc, " + balance.txN + " tx#");
                total = total.add(balance.value);
                if (balance.txN == 0) {
                    break;
                }
            } else {
                System.out.println();
            }
        }
        System.out.println("balance " + total.toPlainString() + " btc");
    }

    private void viewDetails() {
        printDetails(root);
        do {
            System.out.println("menu/details, print:");
            System.out.println("'/path' - view details of the path, for example /0/0 or /1/49");
            System.out.println("'b' - back to the main menu");
            String input = System.console().readLine();
            switch (input) {
                case "b":
                    return;
                default:
                    if (input.startsWith("/")) {
                        try {
                            printDetails(root.generateChild(input.substring(1)));
                        } catch (IndexOutOfBoundsException | UnsupportedOperationException | NumberFormatException e) {
                            System.out.println(e.toString());
                        }
                    }
            }
        } while (true);
    }

    private void printDetails(HdKeyPair keys) {
        System.out.println("path " + keys.path);
        System.out.println("xpub " + keys.serialize(false));
        System.out.println("xprv " + keys.serialize(true));
        System.out.println("address " + keys.keyPair.getAddress());
        System.out.println("wif " + keys.keyPair.encode());
        System.out.println("public key hex " + Converter.bytesToHex(keys.keyPair.publicKey.toByteArray(), false, true));
        System.out.println("private key hex " + keys.keyPair.d.toString(16).toUpperCase());
    }

    private void addInput() {
        System.out.println("root path " + root.path);
        String address = root.keyPair.getAddress();
        Data.Balance balance = Data.getBalance(address);
        if (balance != null && balance.value.signum() > 0) {
            System.out.println("r, " + balance.value.toPlainString() + " btc, " + address);
        }
        HdKeyPair external = root.generateChild(0, false);
        for (int i = 0; i < 200; i++) {
            HdKeyPair item = external.generateChild(i, false);
            address = item.keyPair.getAddress();
            balance = Data.getBalance(address);
            if (balance != null) {
                if (balance.txN == 0) {
                    break;
                }
                if (balance.value.signum() > 0) {
                    System.out.print("r" + item.path.substring(root.path.length()));
                    System.out.println(", " + balance.value.toPlainString() + " btc, " + address);
                }
            }
        }
        HdKeyPair internal = root.generateChild(1, false);
        for (int i = 0; i < 200; i++) {
            HdKeyPair item = internal.generateChild(i, false);
            address = item.keyPair.getAddress();
            balance = Data.getBalance(address);
            if (balance != null) {
                if (balance.txN == 0) {
                    break;
                }
                if (balance.value.signum() > 0) {
                    System.out.print("r" + item.path.substring(root.path.length()));
                    System.out.println(", " + balance.value.toPlainString() + " btc, " + address);
                }
            }
        }
        do {
            System.out.println("menu/input, print:");
            System.out.println("'r' - select root path");
            System.out.println("'r/path' - select path, for example r/0/0 or r/1/9");
            System.out.println("'b' - back to the main menu");
            String input = System.console().readLine();
            switch (input) {
                case "r":
                    selectUtx(root);
                    break;
                case "b":
                    return;
                default:
                    if (input.startsWith("r/")) {
                        try {
                            selectUtx(root.generateChild(input.substring(2)));
                        } catch (IndexOutOfBoundsException | UnsupportedOperationException | NumberFormatException e) {
                            System.out.println(e.toString());
                        }
                    }
            }
        }
        while (true);
    }

    private void selectUtx(HdKeyPair key) {
        ArrayList<Data.UnspentOutput> utx = Data.getUnspentOutputs(key.keyPair.getAddress());
        if (utx == null) {
            System.out.println("no unspent outputs");
            return;
        }
        for (int i = 0; i < utx.size(); i++) {
            System.out.print(i + ", " + utx.get(i).value.toPlainString() + " btc, ");
            System.out.println(Converter.bytesToHex(utx.get(i).transactionHash, false, false));
        }
        if (utx.size() == 1) {
            addUtx(utx.get(0), key.keyPair);
            return;
        }
        do {
            System.out.println("menu/input/utx, print:");
            System.out.println("'#' - select number of the unspent output, for example 0 or 4");
            System.out.println("'b' - back");
            String input = System.console().readLine();
            switch (input) {
                case "b":
                    return;
                default:
                    try {
                        addUtx(utx.get(Integer.parseInt(input)), key.keyPair);
                    } catch (IndexOutOfBoundsException | NumberFormatException e) {
                        System.out.println(e.toString());
                    }
            }
        } while (true);
    }

    private void addUtx(Data.UnspentOutput utx, KeyPair keyPair) {
        int n = transaction.addInput(
                utx.transactionHash,
                false,
                utx.outIndex,
                utx.scriptPubKey,
                keyPair
        );
        sum = sum.add(utx.value);
        System.out.println("input#" + n + ", sum " + sum.toPlainString() + " btc");
    }

    private void addOutput() {
        do {
            System.out.println("menu/output, print:");
            System.out.println("'a' - select this address");
            System.out.println("'a/path' - select address of the path, for example a/0/0 or a/1/9");
            System.out.println("'address' - enter address, for example 1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa");
            System.out.println("'b' - back to the main menu");
            String input = System.console().readLine();
            switch (input) {
                case "a":
                    selectValue(Address.createFromPublicKey(root.keyPair.publicKey));
                    break;
                case "b":
                    return;
                default:
                    try {
                        if (input.startsWith("a/")) {
                            KeyPair keyPair = root.generateChild(input.substring(2)).keyPair;
                            selectValue(Address.createFromPublicKey(keyPair.publicKey));
                        } else {
                            selectValue(new Address(input));
                        }
                    } catch (IndexOutOfBoundsException | UnsupportedOperationException | NumberFormatException e) {
                        System.out.println(e.toString());
                    }
            }
        } while (true);
    }

    private void selectValue(Address address) {
        System.out.println(address.toString());
        do {
            System.out.println("menu/output/value, print:");
            System.out.println("'b' - back");
            System.out.println("'#' - enter value in btc, for example 1 or 0.00134");
            String input = System.console().readLine();
            switch (input) {
                case "b":
                    return;
                default:
                    try {
                        BigDecimal value = new BigDecimal(input).setScale(8, BigDecimal.ROUND_DOWN);
                        int n = transaction.addOutput(value.movePointRight(8).longValue(), address);
                        sum = sum.subtract(value);
                        System.out.println("output#" + n + ", sum " + sum.toPlainString() + " btc");
                        return;
                    } catch (NumberFormatException | UnsupportedOperationException e) {
                        System.out.println(e.toString());
                    }
            }
        } while (true);
    }

    private void viewTransaction() {
        int inputsN = transaction.getInputsN();
        int outputsN = transaction.getOutputsN();
        System.out.println(outputsN + " outputs, " + inputsN + " inputs");
        System.out.println("sum " + sum.toPlainString() + " btc");
        if (inputsN == 0 || sum.signum() < 0) {
            return;
        }
        do {
            System.out.println("menu/transaction, print:");
            System.out.println("'c' - do not use change, all sum is a fee, create transaction");
            System.out.println("'#' - use change, enter fee in satoshi per byte, for example 10 or 340");
            System.out.println("'b' - back to the main menu");
            String input = System.console().readLine();
            switch (input) {
                case "c":
                    createTransaction();
                    return;
                case "b":
                    return;
                default:
                    try {
                        BigDecimal btcPerByte = new BigDecimal(input).movePointLeft(8);
                        if (btcPerByte.signum() < 0) {
                            System.out.println("negative fee");
                            continue;
                        }
                        BigDecimal bytes = BigDecimal.valueOf(transaction.getPayload().length + 34);
                        BigDecimal change = sum.subtract(btcPerByte.multiply(bytes).setScale(8, BigDecimal.ROUND_FLOOR));
                        if (change.signum() < 0) {
                            System.out.println("too big fee");
                            continue;
                        } else if (change.signum() > 0) {
                            createChange(change);
                        }
                        createTransaction();
                        return;
                    } catch (NumberFormatException e) {
                        System.out.println(e.toString());
                    }
            }
        } while (true);
    }

    private void createTransaction() {
        byte[] out = transaction.getPayload();
        BigDecimal feePerByte = sum.divide(BigDecimal.valueOf(out.length), BigDecimal.ROUND_CEILING);
        System.out.println("size " + out.length + " byte");
        System.out.println("fee " + feePerByte.movePointRight(8).toPlainString() + " sat/byte");
        System.out.println("transaction:");
        System.out.println(Converter.bytesToHex(out, false, false));
        do {
            System.out.println("menu/transaction/send, print:");
            System.out.println("'yes' - send transaction");
            System.out.println("'b' - back to main menu");
            String input = System.console().readLine();
            switch (input) {
                case "yes":
                    connectionManager.sendTx(out);
                    transaction = new Transaction();
                    sum = BigDecimal.ZERO;
                    return;
                case "b":
                    return;
            }
        } while (true);
    }

    private void createChange(BigDecimal change) {
        String address = "";
        HdKeyPair internal = root.generateChild(1, false);
        for (int i = 0; i < 200; i++) {
            address = internal.generateChild(i, false).keyPair.getAddress();
            Data.Balance balance = Data.getBalance(address);
            if (balance != null && balance.txN == 0) {
                break;
            }
        }
        System.out.println("change address " + address);
        int n = transaction.addOutput(change.movePointRight(8).longValue(), new Address(address));
        sum = sum.subtract(change);
        System.out.println("output#" + n + ", sum " + sum.toPlainString() + " btc");
    }
}