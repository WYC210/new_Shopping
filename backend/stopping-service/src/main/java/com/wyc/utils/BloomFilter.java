package com.wyc.utils;

import java.util.BitSet;

/**
 * 简单布隆过滤器实现（适合小规模数据，生产建议用RedisBloom等更高效方案）
 */
public class BloomFilter {
    private static final int DEFAULT_SIZE = 1 << 24; // 约1600万bit
    private static final int[] seeds = new int[] { 7, 11, 13, 31, 37, 61 };
    private BitSet bitSet = new BitSet(DEFAULT_SIZE);
    private SimpleHash[] hashFunctions = new SimpleHash[seeds.length];

    public BloomFilter() {
        for (int i = 0; i < seeds.length; i++) {
            hashFunctions[i] = new SimpleHash(DEFAULT_SIZE, seeds[i]);
        }
    }

    public void add(String value) {
        for (SimpleHash f : hashFunctions) {
            bitSet.set(f.hash(value), true);
        }
    }

    public boolean mightContain(String value) {
        for (SimpleHash f : hashFunctions) {
            if (!bitSet.get(f.hash(value))) {
                return false;
            }
        }
        return true;
    }

    static class SimpleHash {
        private int cap;
        private int seed;

        public SimpleHash(int cap, int seed) {
            this.cap = cap;
            this.seed = seed;
        }

        public int hash(String value) {
            int result = 0;
            for (int i = 0; i < value.length(); i++) {
                result = seed * result + value.charAt(i);
            }
            return (cap - 1) & result;
        }
    }
}