package com.logicmonitor.xdx526.compression;

 /**
 * Created by Dongxu Xiang at 5/17/16 15:11
 */
public final class DoubleXorCompressor {
    static class Iterator {
        private int _bitIdx     = 0;
        private int _readCount  = 0;
        private long _lastXor   = 0;
        private long _lastValue = 0;
        private final DoubleXorCompressor _compressor;
        private final BitBuffer _data;

        public Iterator(final DoubleXorCompressor compressor) {
            _compressor = compressor;
            _data = _compressor._data;
            _lastValue = _compressor._baseValue;
        }

        public boolean hasNext() {
            return (_readCount < _compressor._cnt);
        }

        public double next() {
            long result;
            // the first base value
            if (0 == _readCount) {
                result = 0;
            }
            else {
                // 1 control bit: 0 means same value; 1 means not same
                long c0 = _data.get(_bitIdx++, 1);
                if (0 == c0) {
                    result = 0;
                }
                else {
                    // 1 control bit
                    long c1 = _data.get(_bitIdx++, 1);
                    // meaningful bits are less than previous value
                    if (0 == c1) {
                        int cl = Long.numberOfLeadingZeros(_lastXor);
                        int ct = Long.numberOfTrailingZeros(_lastXor);
                        int len = Long.SIZE - cl - ct;

                        long v = _data.get(_bitIdx, len);
                        _bitIdx += len;
                        result = (v << ct);
                    }
                    else {
                        // leading zeros, 6 bits ( 5 bit in original paper?)
                        int cl = (int)(_data.get(_bitIdx, 6) & ((1 << 6) - 1));
                        _bitIdx += 6;
                        // xor value length, 6 bits
                        int len = (int)(_data.get(_bitIdx, 6) & (( 1 << 6) - 1));
                        _bitIdx += 6;

                        long v = _data.get(_bitIdx, len);
                        _bitIdx += len;
                        result = (v << (Long.SIZE - cl - len));
                    }
                }
            }

            _lastXor = result;
            _lastValue ^= result;
            _readCount++;

            return Double.longBitsToDouble(_lastValue);
        }
    }

    private long _baseValue;
    private long _lastXor;
    private long _lastValue;
    private final BitBuffer _data;
    private int _cnt = 0;

    public DoubleXorCompressor(int capacity) {
        _data = new BitBuffer(capacity * Double.SIZE);
    }

    public void add(double value) {
        if (0 == _cnt) {
            _baseValue = Double.doubleToRawLongBits(value);
            _lastValue = _baseValue;
        }
        else {
            long l = Double.doubleToRawLongBits(value);

            long v = _lastValue ^ l;
            // the same value as previous
            if (0 == v) {
                // push 1 bit: 0
                _data.add(0, 1);
            }
            else {
                // push 1 bit: 1
                _data.add(1, 1);

                int vl = Long.numberOfLeadingZeros(v);
                int vt = Long.numberOfTrailingZeros(v);

                int cl = Long.numberOfLeadingZeros(_lastXor);
                int ct = Long.numberOfTrailingZeros(_lastXor);

                if (vl >= cl && vt >= ct) {
                    // push 1 bit:  0
                    _data.add(0, 1);
                    // push XOR'd value
                    _data.add((v >>> ct), Long.SIZE - cl - ct);
                }
                else {
                    // put 1 bit: 1
                    _data.add(1, 1);
                    //put 6 bits: number of leading zeros
                    // TODO: 5 bits descried in paper
                    _data.add(vl, 6);
                    //put 6 bits: length of XOR'd value
                    _data.add(Long.SIZE - vl - vt, 6);
                    // put xor value
                    _data.add((v >>> vt), Long.SIZE - vl - vt);
                }
            }
            _lastXor = v;
            _lastValue = l;
        }
        _cnt++;
    }

    public Iterator iterator() {
        return new Iterator(this);
    }

    public int count() {
        return _cnt;
    }

    public double base() {
        return Double.longBitsToDouble(_baseValue);
    }

    public double compressionRatio() {
        return (_data.size() / (_cnt * 1.0 * Double.SIZE));
    }

    public int bits() {
        return _data.size();
    }
}
