/**
 * Copyright (c) 2011 Tim Buktu (tbuktu@hotmail.com)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package net.sf.ntru.arith;

import static org.junit.Assert.*;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Random;

import net.sf.ntru.arith.SchönhageStrassen;

import org.junit.Test;

public class SchönhageStrassenTest {
    
    @Test
    public void testMult() {
        testMult(BigInteger.valueOf(0), BigInteger.valueOf(0));
        testMult(BigInteger.valueOf(100), BigInteger.valueOf(100));
        testMult(BigInteger.valueOf(-394786896548787L), BigInteger.valueOf(604984572698687L));
        testMult(BigInteger.valueOf(415338904376L), BigInteger.valueOf(527401434558L));
        testMult(new BigInteger("9145524700683826415"), new BigInteger("1786442289234590209543"));
        
        testMult(BigInteger.valueOf(1).shiftLeft(524287), BigInteger.valueOf(1).shiftLeft(1024).subtract(BigInteger.ONE));
        testMult(BigInteger.valueOf(1).shiftLeft(524287), BigInteger.valueOf(1).shiftLeft(524287));
        testMult(BigInteger.valueOf(1).shiftLeft(524287), BigInteger.valueOf(1).shiftLeft(524287).subtract(BigInteger.ONE));
        testMult(BigInteger.valueOf(1).shiftLeft(524287), BigInteger.valueOf(1).shiftLeft(524287).add(BigInteger.ONE));
        testMult(BigInteger.valueOf(1).shiftLeft(524288), BigInteger.valueOf(1).shiftLeft(524288));
        testMult(BigInteger.valueOf(1).shiftLeft(524288), BigInteger.valueOf(1).shiftLeft(524288).subtract(BigInteger.ONE));
        testMult(BigInteger.valueOf(1).shiftLeft(524288), BigInteger.valueOf(1).shiftLeft(524288).add(BigInteger.ONE));
        
        Random rng = new Random();
        testMult(BigInteger.valueOf(rng.nextInt(1000000000)+524288), BigInteger.valueOf(rng.nextInt(1000000000)+524288));
        testMult(BigInteger.valueOf((rng.nextLong()>>>1)+1000), BigInteger.valueOf((rng.nextLong()>>>1)+1000));
        
        testMult(BigInteger.valueOf(rng.nextInt(1000000000)+524288), BigInteger.valueOf(rng.nextInt(1000000000)+524288));
        testMult(BigInteger.valueOf((rng.nextLong()>>>1)+1000), BigInteger.valueOf((rng.nextLong()>>>1)+1000));

        int aLength = 80000 + rng.nextInt(20000);
        int bLength = 80000 + rng.nextInt(20000);
        for (int i=0; i<2; i++) {
            byte[] aArr = new byte[aLength];
            rng.nextBytes(aArr);
            byte[] bArr = new byte[bLength];
            rng.nextBytes(bArr);
            BigInteger a = new BigInteger(aArr);
            BigInteger b = new BigInteger(bArr);
            testMult(a, b);
            
            // double the length and test again so an even and an odd m is tested
            aLength *= 2;
            bLength *= 2;
        }
    }
    
    private void testMult(BigInteger a, BigInteger b) {
        assertEquals(a.multiply(b), SchönhageStrassen.mult(a, b));
    }
    
    @Test
    public void testMultKaratsuba() {
        testMult(new int[] {9, 2}, new int[] {5, 6});
        testMult(new int[] {0, -4}, new int[] {-2, -4});
        testMult(new int[] {-5, 4, 0}, new int[] {3, 2, -2});
        testMult(new int[] {-5, 4, 0, -4}, new int[] {3, 2, -2, -4});
        testMult(new int[] {2, -2, 0, -1, -4}, new int[] {2, -3, -1, 0, -5});
        
        Random rng = new Random();
        for (int i=0; i<10; i++) {
            int[] a = new int[rng.nextInt(1000)];
            int[] b = new int[a.length];
            for (int j=0; j<a.length; j++) {
                a[j] = rng.nextInt(1000) - 500;
                b[j] = rng.nextInt(1000) - 500;
            }
            testMult(a, b);
        }
    }
    
    private void testMult(int[] a, int[] b) {
        int[] cSimple = SchönhageStrassen.multSimple(a, b);
        int[] cKara = SchönhageStrassen.multKaratsuba(a, b);
        int maxLength = Math.max(cSimple.length, cKara.length);
        assertArrayEquals(Arrays.copyOf(cSimple, maxLength), Arrays.copyOf(cKara, maxLength));
    }
    
    @Test
    public void testDftIdft() {
        for (int i=0; i<10; i++)
            testInversion();
    }
    
    /** verifies idft(dft(a)) = a */
    private void testInversion() {
        Random rng = new Random();
        
        int m = 7 + rng.nextInt(10);
        int n = m/2 + 1;
        int numElements = m%2==0 ? 1<<n : 1<<(n+1);
        numElements /= 2;
        int[][] a = new int[numElements][1<<(n+1-5)];
        for (int i=0; i<a.length; i++)
            for (int j=0; j<a[i].length; j++)
                a[i][j] = rng.nextInt();
        SchönhageStrassen.modFnFull(a);
        
        int[][] aOrig = new int[a.length][];
        for (int i=0; i<a.length; i++)
            aOrig[i] = a[i].clone();
        SchönhageStrassen.dft(a, m, n);
        SchönhageStrassen.idft(a, m, n);
        SchönhageStrassen.modFnFull(a);
        for (int j=0; j<aOrig.length; j++)
            assertArrayEquals(aOrig[j], a[j]);
    }
    
    @Test
    public void testAddModFn() {
        Random rng = new Random();
        int n = 5 + rng.nextInt(10);
        int len = 1 << (n+1-5);
        int[] aArr = new int[len];
        for (int i=0; i<aArr.length; i++)
            aArr[i] = rng.nextInt();
        BigInteger a = SchönhageStrassen.toBigInteger(aArr);
        int[] bArr = new int[len];
        for (int i=0; i<bArr.length; i++)
            bArr[i] = rng.nextInt();
        BigInteger b = SchönhageStrassen.toBigInteger(bArr);
        SchönhageStrassen.addModFn(aArr, bArr);
        SchönhageStrassen.modFn(aArr);
        BigInteger Fn = BigInteger.valueOf(2).pow(1<<n).add(BigInteger.ONE);
        BigInteger c = a.add(b).mod(Fn);
        assertEquals(c, SchönhageStrassen.toBigInteger(aArr));
    }
    
    @Test
    public void testMultModFn() {
        assertArrayEquals(new int[] {1713569892, -280255914}, SchönhageStrassen.multModFn(new int[] {-142491638, 0}, new int[] {-142491638, 0}));
    }
    
    @Test
    public void testModFn() {
        int[] a = new int[] {50593286, 151520511};
        SchönhageStrassen.modFn(a);
        assertArrayEquals(new int[] {-100927224, 0}, a);
        
        a = new int[] {1157041776, -1895306073, -1094584616, -218513495};
        SchönhageStrassen.modFn(a);
        assertArrayEquals(new int[] {-2043340903, -1676792579, 0, 0}, a);
    }
    
    @Test
    public void testCyclicShift() {
        int[] arr = new int[] {16712450, -2139160576};
        
        // test cyclicShiftLeft
        assertArrayEquals(new int[] {33424901, 16646144}, SchönhageStrassen.cyclicShiftLeftBits(arr, 1));
        assertArrayEquals(new int[] {-16579968, 2130706432}, SchönhageStrassen.cyclicShiftLeftBits(arr, 8));
        assertArrayEquals(new int[] {50495615, 255}, SchönhageStrassen.cyclicShiftLeftBits(arr, 16));
        assertArrayEquals(new int[] {41975552, 65283}, SchönhageStrassen.cyclicShiftLeftBits(arr, 24));
        assertArrayEquals(new int[] {-2139160576, 16712450}, SchönhageStrassen.cyclicShiftLeftBits(arr, 32));
        assertArrayEquals(arr, SchönhageStrassen.cyclicShiftLeftBits(arr, 64));
        int[] arr2 = SchönhageStrassen.cyclicShiftLeftBits(arr, 17);
        arr2 = SchönhageStrassen.cyclicShiftLeftBits(arr2, 12);
        arr2 = SchönhageStrassen.cyclicShiftLeftBits(arr2, 1);
        arr2 = SchönhageStrassen.cyclicShiftLeftBits(arr2, 1);
        arr2 = SchönhageStrassen.cyclicShiftLeftBits(arr2, 24);
        arr2 = SchönhageStrassen.cyclicShiftLeftBits(arr2, 9);
        assertArrayEquals(arr, arr2);
        
        // test cyclicShiftRight
        assertArrayEquals(new int[] {8356225, 1077903360}, SchönhageStrassen.cyclicShiftRight(arr, 1));
        assertArrayEquals(new int[] {65283, 41975552}, SchönhageStrassen.cyclicShiftRight(arr, 8));
        assertArrayEquals(new int[] {255, 50495615}, SchönhageStrassen.cyclicShiftRight(arr, 16));
        assertArrayEquals(new int[] {2130706432, -16579968}, SchönhageStrassen.cyclicShiftRight(arr, 24));
        assertArrayEquals(new int[] {-2139160576, 16712450}, SchönhageStrassen.cyclicShiftRight(arr, 32));
        assertArrayEquals(new int[] {41975552, 65283}, SchönhageStrassen.cyclicShiftRight(arr, 40));
        assertArrayEquals(arr, SchönhageStrassen.cyclicShiftRight(arr, 64));
        arr2 = SchönhageStrassen.cyclicShiftRight(arr, 17);
        arr2 = SchönhageStrassen.cyclicShiftRight(arr2, 12);
        arr2 = SchönhageStrassen.cyclicShiftRight(arr2, 1);
        arr2 = SchönhageStrassen.cyclicShiftRight(arr2, 1);
        arr2 = SchönhageStrassen.cyclicShiftRight(arr2, 24);
        arr2 = SchönhageStrassen.cyclicShiftRight(arr2, 9);
        assertArrayEquals(arr, arr2);
        
        // shift left, then right by the same amount
        arr2 = SchönhageStrassen.cyclicShiftLeftBits(arr, 22);
        arr2 = SchönhageStrassen.cyclicShiftRight(arr2, 22);
        assertArrayEquals(arr, arr2);
        arr2 = SchönhageStrassen.cyclicShiftLeftBits(arr, 9);
        arr2 = SchönhageStrassen.cyclicShiftRight(arr2, 14);
        arr2 = SchönhageStrassen.cyclicShiftRight(arr2, 9);
        arr2 = SchönhageStrassen.cyclicShiftLeftBits(arr2, 14);
        assertArrayEquals(arr, arr2);
    }
    
    @Test
    public void testSubModPow2() {
        int[] a = new int[] {3844, 0, 0};
        int[] b = new int[] {627199739, 1091992276, 2332};
        SchönhageStrassen.subModPow2(a, b, 12);
        assertArrayEquals(new int[] {9, 0, 0}, a);
    }
    
    @Test
    public void testAddShifted() {
        int[] a = new int[] {1522485231, 1933026569};
        int[] b = new int[] {233616584};
        SchönhageStrassen.addShifted(a, b, 1);
        assertArrayEquals(a, new int[] {1522485231, -2128324143});
        
        a = new int[] {796591014, -1050856894, 1260609160};
        b = new int[] {2093350350, -1822145887};
        SchönhageStrassen.addShifted(a, b, 1);
        assertArrayEquals(a, new int[] {796591014, 1042493456, -561536726});
        
        a = new int[] {-1135845471, 1374513806, 391471507};
        b = new int[] {980775637, 1136222341};
        SchönhageStrassen.addShifted(a, b, 1);
        assertArrayEquals(a, new int[] {-1135845471, -1939677853, 1527693848});
    }
    
    @Test
    public void testAppendBits() {
        int[] a = new int[] {3615777, 0};
        SchönhageStrassen.appendBits(a, 22, new int[] {-77, 61797}, 1, 13);
        assertArrayEquals(new int[] {1500982305, 4}, a);
    }
    
    @Test
    public void testToBigInteger() {
        Random rng = new Random();
        byte[] a = new byte[1+rng.nextInt(100)];
        rng.nextBytes(a);
        int[] b = SchönhageStrassen.toIntArray(new BigInteger(1, a));
        BigInteger c = SchönhageStrassen.toBigInteger(b);
        assertEquals(new BigInteger(1, a), c);
    }
}