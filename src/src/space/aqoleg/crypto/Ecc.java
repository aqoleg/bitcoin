// elliptic curve cryptography
// creates and validates elliptic curve over finite field, or returns secp256k1 curve
// creates and validates point on that curve by x and y or just x
// operations: p == q, p == infinity, p + q, p * r, g * r
// signs and verifies messages
package space.aqoleg.crypto;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;

public class Ecc {
    public static final Ecc secp256k1 = new Ecc();
    private static final BigInteger two = BigInteger.valueOf(2);
    private static final BigInteger three = BigInteger.valueOf(3);
    private static final BigInteger four = BigInteger.valueOf(4);
    // elliptic curve over finite field y**2 = x**3 + a * x + b (mod p)
    private final BigInteger a;
    private final BigInteger b;
    private final BigInteger p;
    // point at infinity, infinity + q = q, infinity * r = infinity
    private final Point infinity = new Point();
    // subgroup, g * r = g * (r % n)
    // n is the smallest such that g * n = infinity, n < p, private key < n
    private BigInteger n = BigInteger.ZERO; // order of the subgroup
    private BigInteger halfN = BigInteger.ZERO; // = n / 2
    private Point g = infinity; // base point, generator

    // secp256k1
    private Ecc() {
        a = BigInteger.ZERO;
        b = BigInteger.valueOf(7);
        p = new BigInteger("fffffffffffffffffffffffffffffffffffffffffffffffffffffffefffffc2f", 16);
        n = new BigInteger("fffffffffffffffffffffffffffffffebaaedce6af48a03bbfd25e8cd0364141", 16);
        halfN = n.divide(BigInteger.valueOf(2));
        g = new Point(
                new BigInteger("79BE667EF9DCBBAC55A06295CE870B07029BFCDB2DCE28D959F2815B16F81798", 16),
                new BigInteger("483ADA7726A3C4655DA4FBFC0E1108A8FD17B448A68554199C47D08FFB10D4B8", 16)
        );
    }

    private Ecc(BigInteger a, BigInteger b, BigInteger p) {
        if (four.multiply(a.pow(3)).add(BigInteger.valueOf(27).multiply(b.pow(2))).signum() == 0) {
            throw new UnsupportedOperationException("the curve is singular, requires 4 * a**3 + 27 * b**2 != 0");
        }
        if (!p.isProbablePrime(10)) {
            throw new UnsupportedOperationException("this is not a field, requires prime p");
        }
        this.a = a;
        this.b = b;
        this.p = p;
    }

    /**
     * @param a curve y**2 = x**3 + a * x + b
     * @param b curve y**2 = x**3 + a * x + b
     * @param p finite field over primes p
     * @return Ecc with specified parameters
     * @throws NullPointerException          if a == null or b == null or p == null
     * @throws UnsupportedOperationException if 4 * a**3 + 27 * b**2 == 0 or p is non-prime
     */
    public static Ecc getEcc(BigInteger a, BigInteger b, BigInteger p) {
        return new Ecc(a, b, p);
    }

    /**
     * @param a  curve y**2 = x**3 + a * x + b
     * @param b  curve y**2 = x**3 + a * x + b
     * @param p  finite field over primes p
     * @param n  order of the subgroup
     * @param gx coordinate x of the base point g
     * @param gy coordinate y of the base point g
     * @return Ecc with specified parameters
     * @throws NullPointerException          if a, b, p, n, gx or gy is null
     * @throws UnsupportedOperationException if 4 * a**3 + 27 * b**2 == 0 or p is non-prime or g * n != infinity
     */
    public static Ecc getEcc(
            BigInteger a,
            BigInteger b,
            BigInteger p,
            BigInteger n,
            BigInteger gx,
            BigInteger gy
    ) {
        Ecc curve = new Ecc(a, b, p);
        curve.n = n;
        curve.halfN = n.divide(two);
        curve.g = curve.createPoint(gx, gy);
        if (!curve.gMultiply(n).isInfinity()) {
            throw new UnsupportedOperationException("incorrect n for this base point, requires g * n = infinity");
        }
        return curve;
    }

    /**
     * @return order of the subgroup n
     */
    public BigInteger getN() {
        return n;
    }

    /**
     * @return base point g
     */
    public Point getG() {
        return g;
    }

    /**
     * @return point at infinity
     */
    public Point infinity() {
        return infinity;
    }

    /**
     * @param x coordinate x
     * @param y coordinate y
     * @return Point (x, y)
     * @throws NullPointerException          if x == null or y == null
     * @throws UnsupportedOperationException if x or y is not in the field Fp or point is not on the curve
     */
    public Point createPoint(BigInteger x, BigInteger y) {
        if (x.signum() < 0 || x.compareTo(p) >= 0) {
            throw new UnsupportedOperationException("x is not in the field Fp, requires 0 <= x < p");
        }
        if (y.signum() < 0 || y.compareTo(p) >= 0) {
            throw new UnsupportedOperationException("y is not in the field Fp, requires 0 <= y < p");
        }
        // y**2 % p = (x**3 + a * x + b) % p
        if (y.pow(2).mod(p).compareTo(x.pow(3).add(a.multiply(x)).add(b).mod(p)) != 0) {
            throw new UnsupportedOperationException("the point is not on the curve");
        }
        return new Point(x, y);
    }

    /**
     * @param x    coordinate x
     * @param even true if y is even
     * @return Point (x, y)
     * @throws NullPointerException          if x == null
     * @throws UnsupportedOperationException if x is not in the field Fp or point is not exist or is not on the curve
     */
    public Point createPoint(BigInteger x, boolean even) {
        if (x.signum() < 0 || x.compareTo(p) >= 0) {
            throw new UnsupportedOperationException("x is not in the field Fp, requires 0 <= x < p");
        }
        BigInteger r = (x.pow(3).add(a.multiply(x)).add(b)).mod(p); // y**2 % p =
        if (legendre(r) != 1) {
            throw new UnsupportedOperationException("there is no point with this x");
        }
        // use tonelli - shanks algorithm
        // find q and s such that p - 1 = q * (2 ** s), q is odd number
        int s = 0;
        BigInteger q = p.subtract(BigInteger.ONE);
        do {
            s++;
        } while (!q.testBit(s));
        q = q.shiftRight(s);
        // find y
        BigInteger y;
        if (s == 1) {
            // secp256k1
            y = r.modPow(p.add(BigInteger.ONE).divide(four), p);
        } else {
            // find quadratic non-residue z
            BigInteger z = two;
            while (legendre(z) != -1) {
                z = z.add(BigInteger.ONE);
            }
            // set c, t, y
            BigInteger c = z.modPow(q, p);
            BigInteger t = r.modPow(q, p);
            y = r.modPow(q.add(BigInteger.ONE).divide(two), p);
            // loop
            while (t.compareTo(BigInteger.ONE) != 0) {
                // find the least i, 0 < i < s, such that t**2**i = 1
                int i = 1;
                while (t.modPow(BigInteger.ONE.shiftLeft(i), p).compareTo(BigInteger.ONE) != 0) {
                    i++;
                }
                BigInteger b = c.modPow(BigInteger.ONE.shiftLeft(s - i - 1), p);
                s = i;
                c = b.multiply(b).mod(p);
                t = t.multiply(b).multiply(b).mod(p);
                y = y.multiply(b).mod(p);
            }
        }
        // odd or even
        if (y.testBit(0) ^ !even) {
            y = p.subtract(y);
        }
        return new Point(x, y);
    }

    /**
     * @param r value to be multiplied by g
     * @return scalar multiplication base point and r, g * r
     * @throws NullPointerException if r == null
     */
    public Point gMultiply(BigInteger r) {
        return g.multiply(r);
    }

    /**
     * @param message the message to be signed
     * @param d       private key to sign message
     * @return BigInteger [r, s]
     * @throws NullPointerException          if message == null or d == null
     * @throws UnsupportedOperationException if message is too big
     */
    public BigInteger[] sign(byte[] message, BigInteger d) {
        BigInteger z = new BigInteger(1, message);
        if (z.bitLength() > n.bitLength()) {
            throw new UnsupportedOperationException("the message is too big, requires bitLength <= n.bitLength");
        }
        BigInteger r;
        BigInteger s = BigInteger.ZERO;
        do {
            // random integer k, 1 <= k < n
            BigInteger k;
            Random random = new SecureRandom();
            do {
                k = new BigInteger(n.bitLength(), random);
            } while (k.bitLength() < (n.bitLength() >> 1) || k.compareTo(n) >= 0);
            // p = g * k
            // r = px % n
            r = gMultiply(k).x.mod(n);
            if (r.signum() == 0) {
                continue;
            }
            // s * k = (z + r * d)
            // s = k**(-1) * (z + r * d) % n
            s = k.modInverse(n).multiply(z.add(r.multiply(d))).mod(n);
            if (s.compareTo(halfN) > 0) {
                s = n.subtract(s);
            }
        } while (s.signum() == 0);
        return new BigInteger[]{r, s};
    }

    /**
     * @param message   the message whose signature to be verify
     * @param publicKey as a Point
     * @param signature BigInteger [r, s]
     * @return true if signature is valid
     * @throws NullPointerException      if message == null or publicKey == null or signature == null
     * @throws IndexOutOfBoundsException if signature length < 2
     */
    public boolean verify(byte[] message, Point publicKey, BigInteger[] signature) {
        BigInteger z = new BigInteger(1, message);
        if (z.bitLength() > n.bitLength()) {
            return false;
        }
        BigInteger r = signature[0];
        BigInteger s = signature[1];
        if (r.signum() != 1 || r.compareTo(n) >= 0) {
            return false;
        }
        if (s.signum() != 1 || s.compareTo(n) >= 0) {
            return false;
        }
        // p = g * k
        // s * k = (z + r * d)
        // p = g * s**(-1) * (z + r * d) = g * s**(-1) * z + g * d * s**(-1) * r
        // = g * s**(-1) * z + publicKey * s**(-1) * r
        BigInteger sInverse = s.modInverse(n);
        Point p = gMultiply(sInverse.multiply(z)).add(publicKey.multiply(sInverse.multiply(r)));
        // r == px % n
        return r.compareTo(p.x.mod(n)) == 0;
    }

    private int legendre(BigInteger a) {
        // legendre symbol (a/p)
        // returns 1 if a is quadratic residue modulo p
        // returns -1 if a is quadratic non-residue modulo p
        // returns 0 if a % p = 0
        BigInteger symbol = a.modPow(p.subtract(BigInteger.ONE).divide(two), p);
        if (symbol.compareTo(BigInteger.ONE) > 0) {
            symbol = symbol.subtract(p);
        }
        return symbol.intValue();
    }

    public class Point {
        // point on the elliptic curve, immutable
        public final BigInteger x; // -1 for the point at infinity
        public final BigInteger y;

        // creates point at infinity
        private Point() {
            x = BigInteger.valueOf(-1);
            y = BigInteger.ZERO;
        }

        private Point(BigInteger x, BigInteger y) {
            this.x = x;
            this.y = y;
        }

        /**
         * @return true if this is the point at infinity
         */
        public boolean isInfinity() {
            return x.signum() < 0;
        }

        /**
         * @param q Point to be compared with this Point
         * @return this == q
         * @throws NullPointerException if q == null
         */
        public boolean isEqual(Point q) {
            if (isInfinity() || q.isInfinity()) {
                return isInfinity() == q.isInfinity();
            }
            return x.compareTo(q.x) == 0 && y.compareTo(q.y) == 0;
        }

        /**
         * Point addition
         *
         * @param q Point to be added to this Point
         * @return this + q
         * @throws NullPointerException if q == null
         */
        public Point add(Point q) {
            if (isInfinity()) {
                // 0 + q = q
                return q;
            }
            if (q.isInfinity()) {
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
                    return infinity;
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
        public Point multiply(BigInteger r) {
            if (isInfinity()) {
                // 0 * n = 0
                return this;
            }
            if (r.signum() == 0) {
                // p * 0 = 0
                return infinity;
            }
            if (r.signum() == -1) {
                // p * (-r) = -(p * r)
                Point inverse = multiply(r.negate());
                return new Point(inverse.x, p.subtract(inverse.y));
            }

            Point result = infinity;
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

        // returns this * 2
        private Point getTwice() {
            // tangent slope m = (3 * x**2 + a) / (2 * y)
            BigInteger m = three.multiply(x.pow(2)).add(a).multiply(two.multiply(y).modInverse(p));
            // rx = m**2 - px - px
            BigInteger rx = m.pow(2).subtract(x).subtract(x).mod(p);
            // ry = m * (rx - px) + py
            BigInteger ry = m.multiply(rx.subtract(x)).add(y).mod(p);
            return new Point(rx, p.subtract(ry));
        }
    }
}