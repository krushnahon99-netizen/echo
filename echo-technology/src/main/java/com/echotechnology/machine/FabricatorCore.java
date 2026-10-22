package com.echotechnology.machine;

import com.echotechnology.energy.EchoEnergyStorage;
import com.echotechnology.recipe.FabricatorRecipe;
import com.echotechnology.recipe.FabricatorRecipes;
import com.echotechnology.upgrade.UpgradeModifiers;

import java.util.Optional;

/**
 * Server-side processing state machine for the Echo Fabricator. Kept free of
 * Minecraft types so it can be unit tested; the block entity adapts its
 * ItemStacks through {@link Slots}.
 */
public final class FabricatorCore {
    public static final int INPUT_A = 0;
    public static final int INPUT_B = 1;
    public static final int OUTPUT = 2;

    public enum Status { IDLE, OUTPUT_BLOCKED, NO_ENERGY, WORKING, COMPLETED }

    /** Minimal inventory view used by the core. Empty slots report an empty id and count 0. */
    public interface Slots {
        String itemId(int slot);

        int count(int slot);

        int maxStackSize(String itemId);

        void remove(int slot, int amount);

        void addToOutput(String itemId, int amount);
    }

    private String activeRecipe = "";
    private int progress;
    private Status lastStatus = Status.IDLE;

    public Status tick(Slots slots, EchoEnergyStorage energy, UpgradeModifiers upgrades, double speedConfig, double costConfig) {
        Optional<FabricatorRecipe> found = FabricatorRecipes.find(
                slots.itemId(INPUT_A), slots.count(INPUT_A), slots.itemId(INPUT_B), slots.count(INPUT_B));
        if (found.isEmpty()) {
            reset();
            return lastStatus = Status.IDLE;
        }
        FabricatorRecipe recipe = found.get();
        if (!recipe.id().equals(activeRecipe)) {
            activeRecipe = recipe.id();
            progress = 0;
        }
        if (!canAcceptOutput(slots.itemId(OUTPUT), slots.count(OUTPUT),
                recipe.outputItem(), recipe.outputCount(), slots.maxStackSize(recipe.outputItem()))) {
            return lastStatus = Status.OUTPUT_BLOCKED;
        }
        int duration = duration(recipe, upgrades, speedConfig);
        int cost = energyPerTick(recipe, upgrades, speedConfig, costConfig);
        if (!energy.consume(cost)) {
            return lastStatus = Status.NO_ENERGY;
        }
        progress++;
        if (progress < duration) {
            return lastStatus = Status.WORKING;
        }
        // Inputs were validated this tick; consume exactly what the recipe requires.
        int order = recipe.match(slots.itemId(INPUT_A), slots.count(INPUT_A), slots.itemId(INPUT_B), slots.count(INPUT_B));
        int firstSlot = order == 0 ? INPUT_A : INPUT_B;
        int secondSlot = order == 0 ? INPUT_B : INPUT_A;
        slots.remove(firstSlot, recipe.first().count());
        slots.remove(secondSlot, recipe.second().count());
        slots.addToOutput(recipe.outputItem(), recipe.outputCount());
        progress = 0;
        return lastStatus = Status.COMPLETED;
    }

    public static boolean canAcceptOutput(String currentId, int currentCount, String outputId, int outputCount, int maxStack) {
        if (currentCount <= 0 || currentId == null || currentId.isEmpty()) {
            return outputCount <= maxStack;
        }
        return currentId.equals(outputId) && currentCount + outputCount <= maxStack;
    }

    public static int duration(FabricatorRecipe recipe, UpgradeModifiers upgrades, double speedConfig) {
        double speed = Math.max(0.01, upgrades.speedMultiplier() * speedConfig);
        return Math.max(1, (int) Math.ceil(recipe.ticks() / speed));
    }

    /** Spreads the recipe's (upgrade-adjusted) total energy evenly over the adjusted duration. */
    public static int energyPerTick(FabricatorRecipe recipe, UpgradeModifiers upgrades, double speedConfig, double costConfig) {
        double total = recipe.totalBaseEnergy() * upgrades.energyCostMultiplier() * Math.max(0, costConfig);
        return (int) Math.ceil(total / duration(recipe, upgrades, speedConfig));
    }

    public void reset() {
        activeRecipe = "";
        progress = 0;
    }

    public int progress() {
        return progress;
    }

    public String activeRecipe() {
        return activeRecipe;
    }

    public Status lastStatus() {
        return lastStatus;
    }

    public void load(String recipeId, int savedProgress) {
        this.activeRecipe = FabricatorRecipes.byId(recipeId).isPresent() ? recipeId : "";
        this.progress = activeRecipe.isEmpty() ? 0 : Math.max(0, savedProgress);
    }
}
