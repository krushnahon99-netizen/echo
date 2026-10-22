package com.echotechnology.registry;

import com.echotechnology.EchoTechnology;
import com.echotechnology.screen.ChargerScreenHandler;
import com.echotechnology.screen.FabricatorScreenHandler;
import com.echotechnology.screen.ReactorScreenHandler;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;

public final class ModScreenHandlers {
    public static final ScreenHandlerType<ReactorScreenHandler> REACTOR = Registry.register(Registries.SCREEN_HANDLER,
            EchoTechnology.id("echo_reactor"), new ScreenHandlerType<>(ReactorScreenHandler::new, FeatureFlags.VANILLA_FEATURES));
    public static final ScreenHandlerType<FabricatorScreenHandler> FABRICATOR = Registry.register(Registries.SCREEN_HANDLER,
            EchoTechnology.id("echo_fabricator"), new ScreenHandlerType<>(FabricatorScreenHandler::new, FeatureFlags.VANILLA_FEATURES));
    public static final ScreenHandlerType<ChargerScreenHandler> CHARGER = Registry.register(Registries.SCREEN_HANDLER,
            EchoTechnology.id("echo_charger"), new ScreenHandlerType<>(ChargerScreenHandler::new, FeatureFlags.VANILLA_FEATURES));

    private ModScreenHandlers() {
    }

    public static void init() {
    }
}
