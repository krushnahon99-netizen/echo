package com.echotechnology.screen;

/**
 * Vanilla screen-handler properties are sent to the client as 16-bit shorts, so
 * 32-bit values (energy, capacity) are split into two properties and rejoined.
 */
public final class SyncedInts {
    private SyncedInts() {
    }

    public static int lo(int value) {
        return value & 0xFFFF;
    }

    public static int hi(int value) {
        return (value >>> 16) & 0xFFFF;
    }

    public static int join(int lo, int hi) {
        return (lo & 0xFFFF) | ((hi & 0xFFFF) << 16);
    }
}
