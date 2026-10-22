package com.echotechnology.energy;

/** Conservation-safe transfer between two storages. */
public final class EnergyTransfer {
    private EnergyTransfer() {
    }

    /**
     * Moves up to {@code limit} EE from {@code source} to {@code target}. Both sides
     * are simulated first so the amount removed always equals the amount added.
     */
    public static int move(EchoEnergyStorage source, EchoEnergyStorage target, int limit) {
        if (source == null || target == null || source == target || limit <= 0) {
            return 0;
        }
        int available = source.extract(limit, true);
        if (available <= 0) {
            return 0;
        }
        int accepted = target.insert(available, true);
        if (accepted <= 0) {
            return 0;
        }
        int extracted = source.extract(accepted, false);
        int inserted = target.insert(extracted, false);
        if (inserted != extracted) {
            // Defensive: should be unreachable because both sides were simulated.
            source.generate(extracted - inserted);
        }
        return inserted;
    }

    /**
     * Diffusion step used between two conduits: moves half of the difference so
     * that energy spreads along a line of conduits without oscillating.
     */
    public static int equalize(EchoEnergyStorage from, EchoEnergyStorage to, int limit) {
        int difference = from.getEnergy() - to.getEnergy();
        if (difference <= 1) {
            return 0;
        }
        return move(from, to, Math.min(limit, difference / 2));
    }
}
