package com.echotechnology;

import com.echotechnology.config.EchoConfig;
import com.echotechnology.registry.ModBlockEntities;
import com.echotechnology.registry.ModBlocks;
import com.echotechnology.registry.ModComponents;
import com.echotechnology.registry.ModItemGroups;
import com.echotechnology.registry.ModItems;
import com.echotechnology.registry.ModScreenHandlers;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class EchoTechnology implements ModInitializer {
    public static final String MOD_ID = "echo_technology";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        // Config must load first: block entities and items read it at runtime.
        EchoConfig.load(FabricLoader.getInstance().getConfigDir().resolve(MOD_ID + ".properties"));
        ModComponents.init();
        ModBlocks.init();
        ModItems.init();
        ModBlockEntities.init();
        ModScreenHandlers.init();
        ModItemGroups.init();
        LOGGER.info("Echo Technology initialized");
    }
}
