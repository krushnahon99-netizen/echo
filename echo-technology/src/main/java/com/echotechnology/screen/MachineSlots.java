package com.echotechnology.screen;

import com.echotechnology.block.entity.MachineBlockEntity;
import com.echotechnology.upgrade.UpgradeModifiers;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

import java.util.function.Predicate;

public final class MachineSlots {
    private MachineSlots() {
    }

    /** Slot that only accepts stacks matching a predicate, optionally with a lower stack limit. */
    public static class Filtered extends Slot {
        private final Predicate<ItemStack> filter;
        private final int maxCount;

        public Filtered(Inventory inventory, int index, int x, int y, Predicate<ItemStack> filter, int maxCount) {
            super(inventory, index, x, y);
            this.filter = filter;
            this.maxCount = maxCount;
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return filter.test(stack);
        }

        @Override
        public int getMaxItemCount() {
            return Math.min(maxCount, super.getMaxItemCount());
        }
    }

    /** Upgrade slot capped at the number of upgrades that actually have an effect. */
    public static Filtered upgrade(Inventory inventory, int index, int x, int y) {
        return new Filtered(inventory, index, x, y, MachineBlockEntity::isUpgrade, UpgradeModifiers.MAX_PER_TYPE);
    }

    /** Take-only slot for machine outputs. */
    public static class Output extends Slot {
        public Output(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return false;
        }
    }
}
