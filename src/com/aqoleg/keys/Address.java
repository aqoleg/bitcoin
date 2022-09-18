/*
p2pkh and p2sh addresses

usage:
    $ java com.aqoleg.keys.Address 14Wu4nRjjTNggBfoZ4Md8t1gZv788cdvqT

    Address address = new Address(string);
    Address address = Address.createFromHash(bytes, isP2pkh);
    Address address = Address.createFromPublicKey(publicKey);
    String string = address.toString();
    boolean equals = address.equals(otherAddress);
    byte[] hash = address.getHash();
    boolean isP2pkh = address.p2pkh;

address bytes:
    1 byte, version, 0 - p2pkh, 5 - p2sh
    byte[20], hash
    byte[4], checksum, first 4 bytes of sha256(sha256(bytes without checksum))
*/

package com.aqoleg.keys;

import com.aqoleg.crypto.Sha256;
import com.aqoleg.utils.Base58;
import com.aqoleg.utils.Converter;

import java.util.Arrays;

public class Address {
    public final boolean p2pkh; // true if p2pkh address, else p2sh address
    private final String address;

    private Address(byte[] hash, boolean p2pkh) {
        this.p2pkh = p2pkh;
        byte[] bytes = new byte[25];
        if (!p2pkh) {
            bytes[0] = 0x05;
        }
        System.arraycopy(hash, 0, bytes, 1, 20);
        byte[] checksum = Sha256.getHash(Sha256.getHash(bytes, 0, 21));
        System.arraycopy(checksum, 0, bytes, 21, 4);
        address = Base58.encode(bytes);
    }

    /**
     * @param address String containing address
     * @throws NullPointerException if address == null
     * @throws Address.Exception    if address is incorrect
     */
    public Address(String address) {
        byte[] bytes;
        try {
            bytes = Base58.decode(address);
        } catch (Base58.Exception exception) {
            throw new Exception(exception.getMessage());
        }
        if (bytes.length != 25) {
            throw new Exception("incorrect length");
        }
        if (bytes[0] == 0x00) {
            p2pkh = true;
        } else if (bytes[0] == 0x05) {
            p2pkh = false;
        } else {
            throw new Exception("incorrect version " + bytes[0] + ", requires 0x00 or 0x05");
        }
        byte[] checksum = Sha256.getHash(Sha256.getHash(bytes, 0, 21));
        for (int i = 0; i < 4; i++) {
            if (checksum[i] != bytes[21 + i]) {
                throw new Exception("incorrect checksum");
            }
        }
        this.address = address;
    }

    /**
     * @param publicKey PublicKey from which will be created Address
     * @return Address created from this publicKey
     * @throws NullPointerException if publicKey == null
     */
    public static Address createFromPublicKey(PublicKey publicKey) {
        return new Address(publicKey.getHash(), true); // p2pkh address
    }

    /**
     * @param hash  bytes from which will be created Address
     * @param p2pkh type of the Address, p2pkh or p2sh
     * @return Address created from this hash
     * @throws NullPointerException if hash == null
     * @throws Address.Exception    if hash length != 20
     */
    public static Address createFromHash(byte[] hash, boolean p2pkh) {
        if (hash.length != 20) {
            throw new Exception("incorrect length " + hash.length + ", requires 20 bytes");
        }
        return new Address(hash, p2pkh);
    }

    /**
     * prints info about entered address
     *
     * @param args String address or empty
     */
    public static void main(String[] args) {
        if (args.length == 1) {
            System.out.println(toString(args[0]));
        } else if (System.console() == null) {
            System.out.println(toString(""));
        } else {
            while (true) {
                String input = System.console().readLine("enter address or 'exit': ");
                if (input.equals("exit")) {
                    System.exit(0);
                }
                System.out.println(toString(input));
            }
        }
    }

    /**
     * @return address as String
     */
    @Override
    public String toString() {
        return address;
    }

    /**
     * @param object to be compared with
     * @return true if both addresses are the same
     */
    @Override
    public boolean equals(Object object) {
        return object instanceof Address && address.equals(((Address) object).address);
    }

    /**
     * @return 20 bytes hash of the script or of the public key
     */
    public byte[] getHash() {
        return Arrays.copyOfRange(Base58.decode(address), 1, 21);
    }

    private static String toString(String input) {
        try {
            Address address = new Address(input);
            String out = "address '" + input;
            out += address.p2pkh ? "', p2pkh, public key hash: " : "', p2sh, script hash: ";
            out += Converter.bytesToHex(address.getHash(), false, false);
            return out;
        } catch (Address.Exception exception) {
            return "incorrect address '" + input + "': " + exception.getMessage();
        }
    }

    public static class Exception extends RuntimeException {
        private Exception(String message) {
            super(message);
        }
    }
}