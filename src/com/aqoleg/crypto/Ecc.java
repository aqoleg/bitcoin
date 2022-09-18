/*
elliptic curve cryptography secp256k1

usage:
    BigInteger newPrivateKey = Ecc.createRandomPrivateKey();
    Ecc.checkPrivateKey(bigInteger);
    BigInteger a = Ecc.modN(bigInteger);
    Ecc.Point point = Ecc.multiplyG(bigInteger);
    Ecc.Point point = Ecc.createPoint(bigIntegerX, bigIntegerY);
    Ecc.Point point = Ecc.createPoint(bigIntegerX, isEven);
    BigInteger[] rs = Ecc.sign(messageBytes, bigInteger);
    Ecc.verify(messageBytes, point, rs);
    BigInteger x = point.x;
    BigInteger y = point.y;
*/

package com.aqoleg.crypto;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;

public class Ecc {
    private static final BigInteger two = BigInteger.valueOf(2);
    private static final BigInteger three = BigInteger.valueOf(3);
    private static final BigInteger four = BigInteger.valueOf(4);
    // elliptic curve over finite field y**2 = x**3 + a * x + b (mod p)
    // a = 0
    private static final BigInteger b = BigInteger.valueOf(7);
    private static final BigInteger p = new BigInteger(
            "fffffffffffffffffffffffffffffffffffffffffffffffffffffffefffffc2f",
            16
    );
    // subgroup, g * r = g * (r % n)
    // order of the subgroup n is the smallest integer such that g * n = infinity, n < p
    private static final BigInteger n = new BigInteger(
            "fffffffffffffffffffffffffffffffebaaedce6af48a03bbfd25e8cd0364141",
            16
    );
    private static final BigInteger halfN = n.divide(two);
    // base point, generator
    private static final Point g = new Point(
            new BigInteger("79BE667EF9DCBBAC55A06295CE870B07029BFCDB2DCE28D959F2815B16F81798", 16),
            new BigInteger("483ADA7726A3C4655DA4FBFC0E1108A8FD17B448A68554199C47D08FFB10D4B8", 16)
    );

    /**
     * @return new random private key
     */
    public static BigInteger createRandomPrivateKey() {
        Random random = new SecureRandom();
        BigInteger d;
        do {
            d = new BigInteger(n.bitLength(), random);
        } while (d.bitLength() < (n.bitLength() >> 2) || d.compareTo(n) >= 0);
        return d;
    }

    /**
     * 0 < privateKey < n
     *
     * @param privateKey private key as a BigInteger
     * @throws NullPointerException if privateKey == null
     * @throws Ecc.Exception        if private key is not correct
     */
    public static void checkPrivateKey(BigInteger privateKey) {
        if (privateKey.signum() <= 0) {
            throw new Exception("non-positive private key");
        } else if (privateKey.compareTo(n) >= 0) {
            throw new Exception("private key is too big");
        }
    }

    /**
     * @param a BigInteger
     * @return a % n
     * @throws NullPointerException if a == null
     */
    public static BigInteger modN(BigInteger a) {
        return a.mod(n);
    }

    /**
     * Point publicKey = Point g * BigInteger privateKey
     *
     * @param a BigInteger
     * @return scalar multiplication base point and a, g * a
     * @throws NullPointerException if a == null
     */
    public static Point multiplyG(BigInteger a) {
        return g.multiply(a);
    }

    /**
     * @param x coordinate x
     * @param y coordinate y
     * @return Point (x, y)
     * @throws NullPointerException if x == null or y == null
     * @throws Ecc.Exception        if x or y is not in the field Fp or point is not on the curve
     */
    public static Point createPoint(BigInteger x, BigInteger y) {
        if (x.signum() < 0) {
            throw new Exception("negative x");
        } else if (x.compareTo(p) >= 0) {
            throw new Exception("x is too big");
        } else if (y.signum() < 0) {
            throw new Exception("negative y");
        } else if (y.compareTo(p) >= 0) {
            throw new Exception("y is too big");
        }
        // y**2 % p = (x**3 + a * x + b) % p
        if (y.pow(2).mod(p).compareTo(x.pow(3).add(b).mod(p)) != 0) {
            throw new Exception("the point is not on the curve");
        }
        return new Point(x, y);
    }

    /**
     * @param x    coordinate x
     * @param even true if y is even
     * @return Point (x, y)
     * @throws NullPointerException if x == null
     * @throws Ecc.Exception        if x is not in the field Fp or point does not exist or it is not on the curve
     */
    public static Point createPoint(BigInteger x, boolean even) {
        if (x.signum() < 0) {
            throw new Exception("negative x");
        } else if (x.compareTo(p) >= 0) {
            throw new Exception("x is too big");
        }
        BigInteger r = (x.pow(3).add(b)).mod(p); // y**2 % p =
        BigInteger legendre = r.modPow(p.subtract(BigInteger.ONE).divide(two), p);
        if (legendre.compareTo(BigInteger.ONE) != 0) {
            throw new Exception("point does not exist");
        }
        // find y
        BigInteger y = r.modPow(p.add(BigInteger.ONE).divide(four), p);
        // odd or even
        if (y.testBit(0) ^ !even) {
            y = p.subtract(y);
        }
        return new Point(x, y);
    }

    /**
     * @param message    the message to be signed
     * @param privateKey private key to sign message
     * @return BigInteger [r, s]
     * @throws NullPointerException if message == null or privateKey == null
     * @throws Ecc.Exception        if message is too big or privateKey is not correct
     */
    public static BigInteger[] sign(byte[] message, BigInteger privateKey) {
        checkPrivateKey(privateKey);
        BigInteger z = new BigInteger(1, message);
        if (z.bitLength() > n.bitLength()) {
            throw new Exception("message is too big");
        }
        BigInteger r;
        BigInteger s = BigInteger.ZERO;
        Random random = new SecureRandom();
        do {
            // random integer k, 1 <= k < n
            BigInteger k;
            do {
                k = new BigInteger(n.bitLength(), random);
            } while (k.bitLength() < (n.bitLength() >> 1) || k.compareTo(n) >= 0);
            // p = g * k
            // r = px % n
            r = multiplyG(k).x.mod(n);
            if (r.signum() == 0) {
                continue;
            }
            // s * k = (z + r * d)
            // s = k**(-1) * (z + r * d) % n
            s = k.modInverse(n).multiply(z.add(r.multiply(privateKey))).mod(n);
            if (s.compareTo(halfN) > 0) {
                s = n.subtract(s);
            }
        } while (s.signum() == 0);
        return new BigInteger[]{r, s};
    }

    /**
     * @param message   the message to be verified
     * @param publicKey as a Point
     * @param signature BigInteger [r, s]
     * @throws NullPointerException           if message == null or publicKey == null or signature == null
     * @throws ArrayIndexOutOfBoundsException if signature length < 2
     * @throws Ecc.Exception                  if message is not verified
     */
    public static void verify(byte[] message, Point publicKey, BigInteger[] signature) {
        BigInteger z = new BigInteger(1, message);
        if (z.bitLength() > n.bitLength()) {
            throw new Exception("message is too big");
        }
        BigInteger r = signature[0];
        BigInteger s = signature[1];
        if (r.signum() < 1) {
            throw new Exception("non-positive r");
        } else if (r.compareTo(n) >= 0) {
            throw new Exception("r is too big");
        } else if (s.signum() < 1) {
            throw new Exception("non-positive s");
        } else if (s.compareTo(n) >= 0) {
            throw new Exception("s is too big");
        }
        // p = g * k
        // s * k = (z + r * d)
        // p = g * s**(-1) * (z + r * d) = g * s**(-1) * z + g * d * s**(-1) * r
        // = g * s**(-1) * z + publicKey * s**(-1) * r
        BigInteger sInverse = s.modInverse(n);
        Point p = multiplyG(sInverse.multiply(z)).add(publicKey.multiply(sInverse.multiply(r)));
        // r == px % n
        if (p.x == null || r.compareTo(p.x.mod(n)) != 0) {
            throw new Exception("incorrect signature");
        }
    }

    /**
     * point on the elliptic curve, immutable
     */
    public static class Point {
        public final BigInteger x; // null for the point at infinity
        public final BigInteger y; // null for the point at infinity

        private Point(BigInteger x, BigInteger y) {
            this.x = x;
            this.y = y;
        }

        /**
         * Point addition
         *
         * @param q Point to be added to this Point
         * @return this + q
         */
        private Point add(Point q) {
            if (x == null || y == null) {
                // 0 + q = q
                return q;
            } else if (q == null || q.x == null || q.y == null) {
                // p + 0 = p
                return this;
            }
            int xCompare = x.compareTo(q.x);
            if (xCompare == 0) {
                if (y.compareTo(q.y) == 0) {
                    // p + p = 2 * p
                    return getTwice();
                } else {
                    // p - p = 0
                    return new Point(null, null);
                }
            } else {
                // for different points slope m = (y2 - y1) / (x2 - x1)
                BigInteger m;
                BigInteger deltaX;
                if (xCompare > 0) {
                    m = y.subtract(q.y);
                    deltaX = x.subtract(q.x);
                } else {
                    m = q.y.subtract(y);
                    deltaX = q.x.subtract(x);
                }
                // deltaY / deltaX = deltaY * deltaX**(-1)
                if (deltaX.compareTo(BigInteger.ONE) > 0) {
                    m = m.multiply(deltaX.modInverse(p));
                }
                // y**2 = x**3 + t * x + k and y = m * x + n
                // x**3 + (-m**2) * x**2 + (t - 2 * m * n) * x + (k - n**2) = 0
                // x1 + x2 + x3 = -b / a = m**2
                // rx = m**2 - px - qx
                BigInteger rx = m.pow(2).subtract(x).subtract(q.x).mod(p);
                // m * (rx - px) = ry - py
                // ry = m * (rx - px) + py
                BigInteger ry = m.multiply(rx.subtract(x)).add(y).mod(p);
                // p + q + r = 0
                // r = -(p + q)
                return new Point(rx, p.subtract(ry));
            }
        }

        /**
         * scalar multiplication
         *
         * @param r value to be multiplied by this Point
         * @return this * r
         * @throws NullPointerException if r == null
         */
        private Point multiply(BigInteger r) {
            if (x == null || y == null) {
                // 0 * n = 0
                return this;
            }
            if (r.signum() == 0) {
                // p * 0 = 0
                return new Point(null, null);
            }
            if (r.signum() == -1) {
                // p * (-r) = -(p * r)
                Point inverse = multiply(r.negate());
                return new Point(inverse.x, p.subtract(inverse.y));
            }

            Point result = new Point(null, null);
            Point twice = this;
            int bitN = 0;
            int stop = r.bitLength();
            do {
                if (r.testBit(bitN)) {
                    result = result.add(twice);
                }
                twice = twice.getTwice();
                bitN++;
            } while (bitN != stop);
            return result;
        }

        /**
         * @return this * 2
         */
        private Point getTwice() {
            // tangent slope m = (3 * x**2 + a) / (2 * y)
            BigInteger m = three.multiply(x.pow(2)).multiply(two.multiply(y).modInverse(p));
            // rx = m**2 - px - px
            BigInteger rx = m.pow(2).subtract(x).subtract(x).mod(p);
            // ry = m * (rx - px) + py
            BigInteger ry = m.multiply(rx.subtract(x)).add(y).mod(p);
            return new Point(rx, p.subtract(ry));
        }
    }

    public static class Exception extends RuntimeException {
        private Exception(String message) {
            super(message);
        }
    }
}