package com.echotechnology.recipe;

/**
 * A two-input fabrication recipe. Items are referenced by registry id strings
 * ("namespace:path") so matching logic stays independent of Minecraft classes
 * and can be unit-tested.
 *
 * @param energyPerTick base EE drawn every tick of processing (before upgrades)
 * @param ticks         base processing time in game ticks (before upgrades)
 */
public record FabricatorRecipe(
        String id,
        Ingredient first,
        Ingredient second,
        String outputItem,
        int outputCount,
        int ticks,
        int energyPerTick) {

    public record Ingredient(String item, int count) {
        public Ingredient {
            if (count <= 0) {
                throw new IllegalArgumentException("Ingredient count must be positive");
            }
        }

        public boolean accepts(String itemId, int available) {
            return item.equals(itemId) && available >= count;
        }
    }

    public FabricatorRecipe {
        if (outputCount <= 0 || ticks <= 0 || energyPerTick < 0) {
            throw new IllegalArgumentException("Invalid recipe values for " + id);
        }
        if (first.item().equals(second.item())) {
            throw new IllegalArgumentException("Recipe inputs must be distinct items: " + id);
        }
    }

    /**
     * Returns which slot holds the first ingredient: 0 when slot A matches
     * {@link #first}, 1 when the inputs are swapped, or -1 when the recipe
     * does not match. Inputs may be placed in either order.
     */
    public int match(String itemA, int countA, String itemB, int countB) {
        if (first.accepts(itemA, countA) && second.accepts(itemB, countB)) {
            return 0;
        }
        if (first.accepts(itemB, countB) && second.accepts(itemA, countA)) {
            return 1;
        }
        return -1;
    }

    public int totalBaseEnergy() {
        return ticks * energyPerTick;
    }
}
