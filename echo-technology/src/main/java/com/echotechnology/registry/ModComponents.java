package com.echotechnology.registry;

import com.echotechnology.EchoTechnology;
import com.mojang.serialization.Codec;
import net.minecraft.component.ComponentType;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

/** Item data components (1.20.5+ replacement for item NBT). */
public final class ModComponents {
    public static final ComponentType<Integer> ENERGY = Registry.register(Registries.DATA_COMPONENT_TYPE,
            EchoTechnology.id("energy"),
            ComponentType.<Integer>builder().codec(Codec.INT).packetCodec(PacketCodecs.VAR_INT).build());

    public static final ComponentType<Boolean> AREA_MODE = Registry.register(Registries.DATA_COMPONENT_TYPE,
            EchoTechnology.id("area_mode"),
            ComponentType.<Boolean>builder().codec(Codec.BOOL).packetCodec(PacketCodecs.BOOL).build());

    private ModComponents() {
    }

    public static void init() {
    }
}
