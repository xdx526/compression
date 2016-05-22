package com.logicmonitor.xdx526.compression;

/**
 * Created by Dongxu Xiang at 5/17/16 14:54
 *
 * A bit buffer implementation
 */
public final class BitBuffer {
    private long[] _data;
    private int _capacity;
    private int _count = 0;

    public BitBuffer(final int initialCapacity) {
        _capacity = initialCapacity;
        _data = new long[_calculateCapacity(initialCapacity)];
    }

    private static int _calculateCapacity(int capacity) {
        return (int) Math.ceil(capacity / (double) Long.SIZE);
    }

    public void add(long value, int len) {
        // resize
        if (_count + len >= _capacity) {
            long[] buffer = new long[_calculateCapacity(_capacity * 2)];
            System.arraycopy(_data, 0, buffer, 0, _data.length);
            _data = buffer;
            _capacity *= 2;
        }

        int i = (_count >> 6);               // how many Longs
        int j = (_count - (_count & (-64))); // remaining bytes, same as (_count - (i << 6))
        int k = Long.SIZE - j;

        if (k < len) {
            _data[i] |= (value >>> (len - k)); // high k
            _data[i + 1] |= (value << (Long.SIZE - (len -k)));  // low len-k bits
        }
        else {
            _data[i] |= (value << (k - len));
        }
        _count += len;
    }

    public long get(int start, int len) {
        int i = (start >> 6);
        int j = (start - (start & (-64)));
        int k = Long.SIZE - j;

        if (k < len) {
            long v1 = (_data[i] & ((-1L) >>> j));
            long v2 = (_data[i + 1] >>> (Long.SIZE - (len - k)));
            return  ((v1 << (len - k)) | v2);
        }
        else {
            return (((1L << len) - 1) & (_data[i] >>> (k - len)));
        }
    }

    public int size() {
        return _count;
    }

    public int capacity() {
        return _capacity;
    }

    public String toString() {
        int i = (_count >> 6);
        int j = _count - (i << 6);

        StringBuilder sb = new StringBuilder(1024);
        sb.append("bits(").append(String.format("%04d", _count)).append(")=");
        for (int k = 0; k < i; k++) {
            sb.append(k).append(":").append(Long.toBinaryString(_data[k])).append("\n");
        }

        if (j > 0) {
            sb.append(i).append(":").append(Long.toBinaryString(_data[i] >>> (Long.SIZE - j)));
        }

        return sb.toString();
    }
}
