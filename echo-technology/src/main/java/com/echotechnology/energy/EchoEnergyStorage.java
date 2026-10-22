package com.echotechnology.energy;

/**
 * A bounded buffer of Echo Energy (EE), the mod's single internal energy unit.
 *
 * <p>Two families of operations exist:
 * <ul>
 *   <li>{@link #insert}/{@link #extract}: network-facing, limited by the per-call
 *   transfer rates. A rate of 0 means the buffer refuses that direction.</li>
 *   <li>{@link #generate}/{@link #consume}: machine-internal, ignore transfer rates
 *   but still respect capacity and available energy.</li>
 * </ul>
 * Every operation clamps to [0, capacity] so energy can never go negative,
 * overflow, or be created by a transfer.
 */
public class EchoEnergyStorage {
    private int energy;
    private int capacity;
    private final int maxInsert;
    private final int maxExtract;
    private Runnable changeListener = () -> {};

    public EchoEnergyStorage(int capacity, int maxInsert, int maxExtract) {
        if (capacity < 0 || maxInsert < 0 || maxExtract < 0) {
            throw new IllegalArgumentException("Energy limits must be non-negative");
        }
        this.capacity = capacity;
        this.maxInsert = maxInsert;
        this.maxExtract = maxExtract;
    }

    public void setChangeListener(Runnable listener) {
        this.changeListener = listener == null ? () -> {} : listener;
    }

    public int getEnergy() {
        return energy;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getSpace() {
        return capacity - energy;
    }

    public int getMaxInsert() {
        return maxInsert;
    }

    public int getMaxExtract() {
        return maxExtract;
    }

    public boolean canReceive() {
        return maxInsert > 0;
    }

    public boolean canExtract() {
        return maxExtract > 0;
    }

    public boolean isFull() {
        return energy >= capacity;
    }

    /** Network insert. Returns the amount accepted (or that would be accepted when simulating). */
    public int insert(int amount, boolean simulate) {
        if (amount <= 0 || maxInsert <= 0) {
            return 0;
        }
        return addClamped(Math.min(amount, maxInsert), simulate);
    }

    /** Network extract. Returns the amount removed (or that would be removed when simulating). */
    public int extract(int amount, boolean simulate) {
        if (amount <= 0 || maxExtract <= 0) {
            return 0;
        }
        return removeClamped(Math.min(amount, maxExtract), simulate);
    }

    /** Internal generation (e.g. reactor burning fuel). Ignores transfer rate limits. */
    public int generate(int amount) {
        return amount <= 0 ? 0 : addClamped(amount, false);
    }

    /**
     * Internal all-or-nothing consumption used by machines. Returns true and
     * removes the energy only if the full amount is available.
     */
    public boolean consume(int amount) {
        if (amount < 0) {
            return false;
        }
        if (amount == 0) {
            return true;
        }
        if (energy < amount) {
            return false;
        }
        energy -= amount;
        changeListener.run();
        return true;
    }

    /** Partial internal drain (e.g. a charger moving energy into an item). */
    public int drain(int amount) {
        return amount <= 0 ? 0 : removeClamped(amount, false);
    }

    /** Changing capacity (upgrades) clamps stored energy; surplus is discarded, never duplicated. */
    public void setCapacity(int newCapacity) {
        int clamped = Math.max(0, newCapacity);
        if (clamped == capacity) {
            return;
        }
        capacity = clamped;
        if (energy > capacity) {
            energy = capacity;
        }
        changeListener.run();
    }

    /** Used when loading saved data; invalid values are clamped into range. */
    public void setEnergy(int value) {
        int clamped = Math.max(0, Math.min(value, capacity));
        if (clamped != energy) {
            energy = clamped;
            changeListener.run();
        }
    }

    private int addClamped(int amount, boolean simulate) {
        int accepted = Math.min(amount, capacity - energy);
        if (accepted <= 0) {
            return 0;
        }
        if (!simulate) {
            energy += accepted;
            changeListener.run();
        }
        return accepted;
    }

    private int removeClamped(int amount, boolean simulate) {
        int removed = Math.min(amount, energy);
        if (removed <= 0) {
            return 0;
        }
        if (!simulate) {
            energy -= removed;
            changeListener.run();
        }
        return removed;
    }
}
