package com.echotechnology.registry;

import com.echotechnology.EchoTechnology;
import com.echotechnology.item.ChargeableItem;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.world.gen.GenerationStep;

public final class ModItemGroups {
    public static final RegistryKey<ItemGroup> MAIN = RegistryKey.of(RegistryKeys.ITEM_GROUP, EchoTechnology.id("main"));

    private ModItemGroups() {
    }

    public static void init() {
        Registry.register(Registries.ITEM_GROUP, MAIN, FabricItemGroup.builder()
                .icon(() -> new ItemStack(ModBlocks.ECHO_REACTOR))
                .displayName(Text.translatable("itemGroup.echo_technology.main"))
                .entries((context, entries) -> {
                    entries.add(ModBlocks.DEEPSLATE_ECHO_CRYSTAL_ORE);
                    entries.add(ModItems.ECHO_CRYSTAL);
                    entries.add(ModItems.RESONANT_ALLOY);
                    entries.add(ModItems.ECHO_CIRCUIT);
                    entries.add(ModBlocks.ECHO_REACTOR);
                    entries.add(ModBlocks.ECHO_CONDUIT);
                    entries.add(ModBlocks.ECHO_FABRICATOR);
                    entries.add(ModBlocks.ECHO_CHARGER);
                    entries.add(ModItems.EFFICIENCY_UPGRADE);
                    entries.add(ModItems.CAPACITY_UPGRADE);
                    entries.add(ModItems.SPEED_UPGRADE);
                    entries.add(ModItems.ECHO_DRILL);
                    entries.add(charged(ModItems.ECHO_DRILL));
                    entries.add(ModItems.ECHO_SCANNER);
                    entries.add(charged(ModItems.ECHO_SCANNER));
                })
                .build());

        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Feature.UNDERGROUND_ORES,
                RegistryKey.of(RegistryKeys.PLACED_FEATURE, EchoTechnology.id("echo_crystal_ore")));
    }

    private static ItemStack charged(Item item) {
        ItemStack stack = new ItemStack(item);
        if (item instanceof ChargeableItem chargeable) {
            chargeable.setEnergy(stack, chargeable.getEnergyCapacity());
        }
        return stack;
    }
}
