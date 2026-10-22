package com.echotechnology.machine;

import com.echotechnology.energy.EchoEnergyStorage;
import com.echotechnology.upgrade.UpgradeModifiers;

/**
 * Charging math for the Echo Charger. Charging has a transfer loss (25% base,
 * reduced by efficiency upgrades) so the loss factor is always >= 1 and a
 * charger can never output more energy than it consumed.
 */
public final class ChargerCore {
    public enum Status { IDLE, NO_ENERGY, CHARGING, FULL }

    private ChargerCore() {
    }

    public static double lossFactor(UpgradeModifiers upgrades) {
        return 1.0 + 0.25 * Math.min(1.0, upgrades.energyCostMultiplier());
    }

    /**
     * Plans one tick of charging.
     *
     * @return {deliver, cost}: EE added to the item and EE drawn from the buffer
     */
    public static int[] plan(int stored, int itemSpace, int rate, double loss) {
        if (stored <= 0 || itemSpace <= 0 || rate <= 0 || loss < 1.0) {
            return new int[]{0, 0};
        }
        int deliver = Math.min(rate, itemSpace);
        int cost = (int) Math.ceil(deliver * loss);
        if (cost > stored) {
            deliver = (int) Math.floor(stored / loss);
            cost = (int) Math.ceil(deliver * loss);
            while (deliver > 0 && cost > stored) {
                deliver--;
                cost = (int) Math.ceil(deliver * loss);
            }
        }
        return deliver <= 0 ? new int[]{0, 0} : new int[]{deliver, cost};
    }

    /** Applies a plan against a real buffer; returns EE delivered (0 if the buffer could not pay). */
    public static int charge(EchoEnergyStorage buffer, int itemSpace, int rate, double loss) {
        int[] plan = plan(buffer.getEnergy(), itemSpace, rate, loss);
        if (plan[0] <= 0 || !buffer.consume(plan[1])) {
            return 0;
        }
        return plan[0];
    }
}
