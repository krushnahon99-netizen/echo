package com.echotechnology.registry;

import com.echotechnology.EchoTechnology;
import com.echotechnology.block.entity.EchoChargerBlockEntity;
import com.echotechnology.block.entity.EchoConduitBlockEntity;
import com.echotechnology.block.entity.EchoFabricatorBlockEntity;
import com.echotechnology.block.entity.EchoReactorBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public final class ModBlockEntities {
    public static final BlockEntityType<EchoReactorBlockEntity> ECHO_REACTOR =
            register("echo_reactor", EchoReactorBlockEntity::new, ModBlocks.ECHO_REACTOR);
    public static final BlockEntityType<EchoFabricatorBlockEntity> ECHO_FABRICATOR =
            register("echo_fabricator", EchoFabricatorBlockEntity::new, ModBlocks.ECHO_FABRICATOR);
    public static final BlockEntityType<EchoChargerBlockEntity> ECHO_CHARGER =
            register("echo_charger", EchoChargerBlockEntity::new, ModBlocks.ECHO_CHARGER);
    public static final BlockEntityType<EchoConduitBlockEntity> ECHO_CONDUIT =
            register("echo_conduit", EchoConduitBlockEntity::new, ModBlocks.ECHO_CONDUIT);

    private ModBlockEntities() {
    }

    private static <T extends BlockEntity> BlockEntityType<T> register(
            String name, BlockEntityType.BlockEntityFactory<T> factory, Block block) {
        return Registry.register(Registries.BLOCK_ENTITY_TYPE, EchoTechnology.id(name),
                BlockEntityType.Builder.create(factory, block).build(null));
    }

    public static void init() {
    }
}
