package com.echotechnology.machine;

import com.echotechnology.energy.EchoEnergyStorage;

import java.util.Map;

/**
 * Echo Reactor generation logic. Each fuel item holds a fixed amount of EE
 * which is released at the generation rate. Burning pauses (without wasting
 * fuel) while the buffer is full, so total output always equals fuel value.
 */
public final class ReactorCore {
    /** Base EE yield per fuel item. */
    public static final Map<String, Integer> FUEL_VALUES = Map.of(
            "minecraft:sculk_vein", 200,
            "minecraft:sculk", 1_000,
            "minecraft:sculk_catalyst", 12_000,
            "echo_technology:echo_crystal", 16_000,
            "minecraft:echo_shard", 48_000);

    public enum Status { NO_FUEL, BUFFER_FULL, GENERATING }

    /** Abstraction over the fuel slot so the core stays testable. */
    public interface FuelSlot {
        String itemId();

        /** Removes one fuel item; must only be called when a valid item is present. */
        void consumeOne();
    }

    private int fuelRemaining;
    private int fuelTotal;

    public static int fuelValue(String itemId) {
        return FUEL_VALUES.getOrDefault(itemId, 0);
    }

    public static boolean isFuel(String itemId) {
        return fuelValue(itemId) > 0;
    }

    public Status tick(FuelSlot fuel, EchoEnergyStorage energy, int generationRate, double fuelYieldMultiplier) {
        if (energy.isFull()) {
            return Status.BUFFER_FULL;
        }
        if (fuelRemaining <= 0) {
            int value = fuelValue(fuel.itemId());
            if (value <= 0) {
                fuelRemaining = 0;
                fuelTotal = 0;
                return Status.NO_FUEL;
            }
            fuel.consumeOne();
            fuelTotal = (int) Math.min(Integer.MAX_VALUE, Math.round(value * fuelYieldMultiplier));
            fuelRemaining = fuelTotal;
        }
        int produced = energy.generate(Math.min(generationRate, fuelRemaining));
        fuelRemaining -= produced;
        return Status.GENERATING;
    }

    public boolean isBurning() {
        return fuelRemaining > 0;
    }

    public int fuelRemaining() {
        return fuelRemaining;
    }

    public int fuelTotal() {
        return fuelTotal;
    }

    public void load(int remaining, int total) {
        this.fuelTotal = Math.max(0, total);
        this.fuelRemaining = Math.max(0, Math.min(remaining, this.fuelTotal));
    }
}
