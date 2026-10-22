package com.echotechnology.item;

import com.echotechnology.registry.ModComponents;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

/** Items that store Echo Energy in the {@code echo_technology:energy} data component. */
public interface ChargeableItem {
    int ENERGY_BAR_COLOR = 0x29DFEB;

    int getEnergyCapacity();

    default int getEnergy(ItemStack stack) {
        return MathHelper.clamp(stack.getOrDefault(ModComponents.ENERGY, 0), 0, getEnergyCapacity());
    }

    default void setEnergy(ItemStack stack, int value) {
        stack.set(ModComponents.ENERGY, MathHelper.clamp(value, 0, getEnergyCapacity()));
    }

    /** All-or-nothing energy use. Must only be called on the logical server. */
    default boolean tryConsume(ItemStack stack, int amount) {
        if (amount < 0) {
            return false;
        }
        int current = getEnergy(stack);
        if (current < amount) {
            return false;
        }
        setEnergy(stack, current - amount);
        return true;
    }

    default int energyBarStep(ItemStack stack) {
        int capacity = getEnergyCapacity();
        return capacity <= 0 ? 0 : Math.round(13.0f * getEnergy(stack) / capacity);
    }

    default Text energyTooltip(ItemStack stack) {
        return Text.translatable("tooltip.echo_technology.energy",
                String.format("%,d", getEnergy(stack)), String.format("%,d", getEnergyCapacity()))
                .formatted(Formatting.AQUA);
    }
}
