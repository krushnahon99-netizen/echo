package com.echotechnology.recipe;

import com.echotechnology.recipe.FabricatorRecipe.Ingredient;

import java.util.List;
import java.util.Optional;

/** The built-in Echo Fabricator recipe book. */
public final class FabricatorRecipes {
    private static final String MOD = "echo_technology:";

    public static final List<FabricatorRecipe> ALL = List.of(
            new FabricatorRecipe("resonant_alloy",
                    new Ingredient(MOD + "echo_crystal", 1), new Ingredient("minecraft:iron_ingot", 2),
                    MOD + "resonant_alloy", 2, 100, 20),
            new FabricatorRecipe("echo_circuit",
                    new Ingredient(MOD + "resonant_alloy", 2), new Ingredient("minecraft:redstone", 2),
                    MOD + "echo_circuit", 1, 160, 30),
            new FabricatorRecipe("efficiency_upgrade",
                    new Ingredient(MOD + "echo_circuit", 1), new Ingredient("minecraft:amethyst_shard", 4),
                    MOD + "efficiency_upgrade", 1, 240, 40),
            new FabricatorRecipe("capacity_upgrade",
                    new Ingredient(MOD + "echo_circuit", 1), new Ingredient(MOD + "resonant_alloy", 4),
                    MOD + "capacity_upgrade", 1, 240, 40),
            new FabricatorRecipe("speed_upgrade",
                    new Ingredient(MOD + "echo_circuit", 1), new Ingredient("minecraft:quartz", 4),
                    MOD + "speed_upgrade", 1, 240, 40),
            new FabricatorRecipe("echo_shard",
                    new Ingredient(MOD + "echo_crystal", 4), new Ingredient("minecraft:sculk", 8),
                    "minecraft:echo_shard", 1, 400, 50));

    private FabricatorRecipes() {
    }

    public static Optional<FabricatorRecipe> byId(String id) {
        return ALL.stream().filter(r -> r.id().equals(id)).findFirst();
    }

    public static Optional<FabricatorRecipe> find(String itemA, int countA, String itemB, int countB) {
        return ALL.stream().filter(r -> r.match(itemA, countA, itemB, countB) >= 0).findFirst();
    }
}
