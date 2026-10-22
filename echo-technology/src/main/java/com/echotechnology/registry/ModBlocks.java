package com.echotechnology.registry;

import com.echotechnology.EchoTechnology;
import com.echotechnology.block.EchoChargerBlock;
import com.echotechnology.block.EchoConduitBlock;
import com.echotechnology.block.EchoFabricatorBlock;
import com.echotechnology.block.EchoReactorBlock;
import com.echotechnology.block.MachineBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.ExperienceDroppingBlock;
import net.minecraft.block.MapColor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.intprovider.UniformIntProvider;

public final class ModBlocks {
    public static final Block DEEPSLATE_ECHO_CRYSTAL_ORE = register("deepslate_echo_crystal_ore",
            new ExperienceDroppingBlock(UniformIntProvider.create(3, 7),
                    AbstractBlock.Settings.copy(Blocks.DEEPSLATE_DIAMOND_ORE).luminance(state -> 3)));

    public static final Block ECHO_REACTOR = register("echo_reactor", new EchoReactorBlock(machineSettings()));
    public static final Block ECHO_FABRICATOR = register("echo_fabricator", new EchoFabricatorBlock(machineSettings()));
    public static final Block ECHO_CHARGER = register("echo_charger", new EchoChargerBlock(machineSettings()));
    public static final Block ECHO_CONDUIT = register("echo_conduit", new EchoConduitBlock(
            AbstractBlock.Settings.create().mapColor(MapColor.CYAN).strength(1.5f, 6.0f).requiresTool()
                    .sounds(BlockSoundGroup.COPPER).nonOpaque().luminance(state -> 4)));

    private ModBlocks() {
    }

    private static AbstractBlock.Settings machineSettings() {
        return AbstractBlock.Settings.create()
                .mapColor(MapColor.BLACK)
                .strength(3.5f, 6.0f)
                .requiresTool()
                .sounds(BlockSoundGroup.NETHERITE)
                .luminance(state -> state.contains(MachineBlock.LIT) && state.get(MachineBlock.LIT) ? 10 : 2);
    }

    private static Block register(String name, Block block) {
        Block registered = Registry.register(Registries.BLOCK, EchoTechnology.id(name), block);
        Registry.register(Registries.ITEM, EchoTechnology.id(name), new BlockItem(registered, new Item.Settings()));
        return registered;
    }

    public static void init() {
    }
}
