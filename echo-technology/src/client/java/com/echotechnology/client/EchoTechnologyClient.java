package com.echotechnology.client;

import com.echotechnology.client.screen.ChargerScreen;
import com.echotechnology.client.screen.FabricatorScreen;
import com.echotechnology.client.screen.ReactorScreen;
import com.echotechnology.registry.ModScreenHandlers;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

public final class EchoTechnologyClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HandledScreens.register(ModScreenHandlers.REACTOR, ReactorScreen::new);
        HandledScreens.register(ModScreenHandlers.FABRICATOR, FabricatorScreen::new);
        HandledScreens.register(ModScreenHandlers.CHARGER, ChargerScreen::new);
    }
}
