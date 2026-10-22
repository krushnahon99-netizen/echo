package com.echotechnology.upgrade;

import java.util.EnumMap;
import java.util.Map;

/**
 * Immutable summary of the upgrades installed in a machine. Each type stacks up
 * to {@link #MAX_PER_TYPE}; extra upgrades have no effect.
 */
public final class UpgradeModifiers {
    public static final int MAX_PER_TYPE = 4;
    public static final UpgradeModifiers NONE = new UpgradeModifiers(0, 0, 0);

    private final int efficiency;
    private final int capacity;
    private final int speed;

    public UpgradeModifiers(int efficiency, int capacity, int speed) {
        this.efficiency = clamp(efficiency);
        this.capacity = clamp(capacity);
        this.speed = clamp(speed);
    }

    public static UpgradeModifiers fromCounts(Map<UpgradeType, Integer> counts) {
        Map<UpgradeType, Integer> safe = new EnumMap<>(UpgradeType.class);
        safe.putAll(counts);
        return new UpgradeModifiers(
                safe.getOrDefault(UpgradeType.EFFICIENCY, 0),
                safe.getOrDefault(UpgradeType.CAPACITY, 0),
                safe.getOrDefault(UpgradeType.SPEED, 0));
    }

    private static int clamp(int count) {
        return Math.max(0, Math.min(MAX_PER_TYPE, count));
    }

    public int efficiency() {
        return efficiency;
    }

    public int capacity() {
        return capacity;
    }

    public int speed() {
        return speed;
    }

    /** Machine energy cost multiplier: -15% per efficiency, +10% per speed upgrade. */
    public double energyCostMultiplier() {
        return Math.max(0.1, (1.0 - 0.15 * efficiency) * (1.0 + 0.10 * speed));
    }

    /** Buffer size multiplier: +50% per capacity upgrade. */
    public double capacityMultiplier() {
        return 1.0 + 0.5 * capacity;
    }

    /** Work rate multiplier: +50% per speed upgrade. */
    public double speedMultiplier() {
        return 1.0 + 0.5 * speed;
    }

    /** Reactor fuel yield multiplier: +25% EE per fuel item per efficiency upgrade. */
    public double fuelYieldMultiplier() {
        return 1.0 + 0.25 * efficiency;
    }

    public int scaleCapacity(int baseCapacity) {
        return (int) Math.min(Integer.MAX_VALUE, Math.round(baseCapacity * capacityMultiplier()));
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof UpgradeModifiers other
                && other.efficiency == efficiency && other.capacity == capacity && other.speed == speed;
    }

    @Override
    public int hashCode() {
        return efficiency * 31 * 31 + capacity * 31 + speed;
    }
}
