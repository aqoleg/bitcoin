// p2pkh and p2sh addresses
//
// address bytes:
//    1 byte, version, 0 - p2pkh, 5 - p2sh
//    byte[20], hash
//    byte[4], checksum, first 4 bytes of sha256(sha256(bytes without checksum))
package space.aqoleg.keys;

import space.aqoleg.crypto.Sha256;
import space.aqoleg.utils.Base58;

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
     * @throws NullPointerException          if address == null
     * @throws UnsupportedOperationException if address is incorrect
     */
    public Address(String address) {
        byte[] bytes = Base58.decode(address);
        if (bytes.length != 25) {
            throw new UnsupportedOperationException("incorrect length " + bytes.length + ", require 25 bytes");
        }
        if (bytes[0] == 0x00) {
            p2pkh = true;
        } else if (bytes[0] == 0x05) {
            p2pkh = false;
        } else {
            throw new UnsupportedOperationException("incorrect version " + bytes[0] + ", require 0x00 or 0x05");
        }
        byte[] checksum = Sha256.getHash(Sha256.getHash(bytes, 0, 21));
        for (int i = 0; i < 4; i++) {
            if (checksum[i] != bytes[21 + i]) {
                throw new UnsupportedOperationException("incorrect checksum");
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
     * @throws NullPointerException          if hash == null
     * @throws UnsupportedOperationException if hash length != 20
     */
    public static Address createFromHash(byte[] hash, boolean p2pkh) {
        if (hash.length != 20) {
            throw new UnsupportedOperationException("incorrect length of the hash, requires 20");
        }
        return new Address(hash, p2pkh);
    }

    /**
     * @return address as String
     */
    @Override
    public String toString() {
        return address;
    }

    /**
     * @return 20 bytes hash of the script or of the public key
     */
    public byte[] getHash() {
        return Arrays.copyOfRange(Base58.decode(address), 1, 21);
    }
}