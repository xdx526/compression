package com.logicmonitor.xdx526.compression;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Dongxu Xiang at 5/17/16 15:15
 */
public class TestBifBuffer {
    @Test
    public void testNormal() {
        BitBuffer bf = new BitBuffer(1024);

        bf.add(10, 4);
        bf.add(10, 4);
        bf.add(Long.MAX_VALUE, 64);

        long t = System.currentTimeMillis();
        bf.add(t, Long.SIZE - Long.numberOfLeadingZeros(t));

        assertEquals(10, bf.get(0, 4));
        assertEquals(10, bf.get(4, 4));
        assertEquals(Long.MAX_VALUE, bf.get(8, 64));
        assertEquals(t, bf.get(72, Long.SIZE - Long.numberOfLeadingZeros(t)));
    }
}
