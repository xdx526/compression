package com.logicmonitor.xdx526.compression;

import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

/**
 * Created by Dongxu Xiang at 5/17/16 15:15
 */
public class TestDoubleXorCompressor {
    @Test
    public void testNormal() {
        Random rnd = new Random();
        int count = 1440; int rangeCount = 100;

        // fixed value
        double[] v1 = new double[count];
        double v = rnd.nextDouble();
        Arrays.fill(v1, v);
        System.out.print("fixed value:");
        _test(v1);

        // random
        double[] v2 = new double[count];
        for (int i = 0; i < v2.length; i++) {
            v2[i] = rnd.nextDouble();
        }
        System.out.print("random:");
        _test(v2);

        // range
        double[] r1 = new double[rangeCount];
        for (int i = 0 ; i < r1.length; i++) {
            r1[i] = rnd.nextDouble();
        }
        double[] v3 = new double[count];
        for (int i = 0; i < v3.length; i++) {
            v3[i] = r1[rnd.nextInt(rangeCount)];
        }
        System.out.print("range-random(100):");
        _test(v3);

        // range2
        double[] r2 = new double[rangeCount];
        double x1 = 1024;
        for (int i = 0; i < r2.length; i++) {
            r2[i] = x1 + i;
        }
        double[] v4 = new double[count];
        for (int i = 0; i < v4.length; i++) {
            v4[i] = r2[rnd.nextInt(rangeCount)];
        }
        System.out.print("range-increased-random2(100):");
        _test(v4);
    }

    static void _test(double[] values) {
        DoubleXorCompressor compressor = new DoubleXorCompressor(Long.SIZE * values.length);
        for (double v: values) {
            compressor.add(v);
        }
        System.out.println(compressor.count() + " " + compressor.compressionRatio());

        DoubleXorCompressor.Iterator iterator = compressor.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            double v = iterator.next();
            if (0 != Double.compare(v, values[i])) {
                System.out.println(i + " -> " + v + ":" + values[i]);
            }
            i++;
        }
    }
}
