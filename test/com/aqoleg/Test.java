/*
the small test library

usage:
    public class Example extends Test {
        public void test1() {
            assertNull(actualObject);
            assertTrue(actualBoolean);
            assertEquals(required, actual);
        }
        public void test2() {
            byte[] bytes = hexToBytes(hexString);
            String hex = bytesToHex(byteArray);
            assertNotThrows(() -> doSomething());
            assertThrows(RequiredException.class, () -> doSomething());
        }
    }
    new Example().test1();
    boolean ok = new Example().testAll();
*/

package com.aqoleg;

import com.aqoleg.crypto.test.*;
import com.aqoleg.data.test.*;
import com.aqoleg.keys.test.*;
import com.aqoleg.messages.test.*;
import com.aqoleg.utils.test.Base58Test;
import com.aqoleg.utils.test.BytesInputTest;
import com.aqoleg.utils.test.BytesOutputTest;
import com.aqoleg.utils.test.ConverterTest;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class Test {
    private boolean ok = false;

    public static void main(String[] args) {
        System.out.println("all tests");
        System.out.println();
        boolean ok = new Test().testAll();
        ok &= new EccTest().testAll();
        ok &= new HmacSha512Test().testAll();
        ok &= new Ripemd160Test().testAll();
        ok &= new Sha256Test().testAll();
        ok &= new Sha512Test().testAll();
        ok &= new AddressesTest().testAll();
        ok &= new BlockLoaderTest().testAll();
        ok &= new ConnectionManagerTest().testAll();
        ok &= new ConnectionTest().testAll();
        ok &= new StorageTest().testAll();
        ok &= new TransactionSenderTest().testAll();
        ok &= new AddressTest().testAll();
        ok &= new HdKeyPairTest().testAll();
        ok &= new KeyPairTest().testAll();
        ok &= new MnemonicTest().testAll();
        ok &= new PublicKeyTest().testAll();
        ok &= new AddrTest().testAll();
        ok &= new AlertTest().testAll();
        ok &= new BlockTest().testAll();
        ok &= new GetAddrTest().testAll();
        ok &= new GetBlocksTest().testAll();
        ok &= new GetDataTest().testAll();
        ok &= new InventoryTest().testAll();
        ok &= new InvTest().testAll();
        ok &= new MessageTest().testAll();
        ok &= new NetAddressTest().testAll();
        ok &= new NotFoundTest().testAll();
        ok &= new PingTest().testAll();
        ok &= new PongTest().testAll();
        ok &= new RejectTest().testAll();
        ok &= new ScriptTest().testAll();
        ok &= new TransactionTest().testAll();
        ok &= new TxBuilderTest().testAll();
        ok &= new VerAckTest().testAll();
        ok &= new VersionTest().testAll();
        ok &= new Base58Test().testAll();
        ok &= new BytesInputTest().testAll();
        ok &= new BytesOutputTest().testAll();
        ok &= new ConverterTest().testAll();
        System.out.println("all tests " + (ok ? "ok" : "not ok"));
        System.out.println("the end");
    }

    /**
     * invokes all public function in the class or subclass,
     * except main() and testAll() functions
     *
     * @return true if all tests in the class or subclass are passed
     */
    public boolean testAll() {
        boolean ok = true;
        System.out.println(this.getClass().getName());
        Method[] methods = this.getClass().getDeclaredMethods();
        for (Method method : methods) {
            if (!Modifier.isPublic(method.getModifiers())) {
                continue;
            } else if (method.getName().equals("main") || method.getName().equals("testAll")) {
                continue;
            }
            try {
                Test test = this.getClass().newInstance();
                test.ok = true;
                System.out.print(method.getName());
                method.invoke(test);
                System.out.println(test.ok ? " ok" : " not ok");
                ok &= test.ok;
            } catch (Throwable throwable) {
                System.out.println(method.getName() + " not ok, test stopped");
                ok = false;
                throwable.printStackTrace();
            }
        }
        System.out.println(this.getClass().getName() + (ok ? " ok" : " not ok"));
        System.out.println();
        return ok;
    }

    protected byte[] hexToBytes(String hex) {
        int length = hex.length();
        byte[] bytes = new byte[length / 2];
        int firstChar;
        int secondChar;
        for (int i = 0; i < length; i += 2) {
            firstChar = Character.digit(hex.charAt(i), 16);
            secondChar = Character.digit(hex.charAt(i + 1), 16);
            bytes[i / 2] = (byte) (firstChar << 4 | secondChar);
        }
        return bytes;
    }

    protected String bytesToHex(byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder();
        for (byte b : bytes) {
            stringBuilder.append(String.format("%02x", b));
        }
        return stringBuilder.toString();
    }

    protected void assertNull(Object actual) {
        if (actual != null) {
            logError(actual.getClass().getName());
        }
    }

    protected void assertTrue(boolean actual) {
        if (!actual) {
            logError("false");
        }
    }

    protected void assertEquals(int required, int actual) {
        if (actual != required) {
            logError(Integer.toString(actual));
        }
    }

    protected void assertEquals(long required, long actual) {
        if (actual != required) {
            logError(Long.toString(actual));
        }
    }

    protected void assertEquals(String required, String actual) {
        if (!actual.equals(required)) {
            logError(actual);
        }
    }

    protected void assertEquals(String required, byte[] actual) {
        assertEquals(required, bytesToHex(actual));
    }

    protected void assertEquals(byte[] required, byte[] actual) {
        if (actual.length != required.length) {
            logError("length " + actual.length);
            return;
        }
        for (int i = required.length - 1; i >= 0; i--) {
            if (actual[i] != required[i]) {
                logError("i" + i + ": " + actual[i]);
                return;
            }
        }
    }

    protected void assertNotThrows(Function function) {
        try {
            function.run();
        } catch (Throwable throwable) {
            logError(throwable.toString());
        }
    }

    protected void assertThrows(Class requiredException, Function function) {
        try {
            function.run();
        } catch (Throwable throwable) {
            if (!requiredException.isInstance(throwable)) {
                logError(throwable.toString());
            }
            return;
        }
        logError("no exception");
    }

    private void logError(String message) {
        try {
            throw new Throwable(message);
        } catch (Throwable throwable) {
            ok = false;
            throwable.printStackTrace();
        }
    }

    protected interface Function {
        void run() throws Throwable;
    }
}