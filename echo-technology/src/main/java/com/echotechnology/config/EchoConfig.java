package com.echotechnology.config;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Simple properties-file config (config/echo_technology.properties). Missing or
 * invalid entries fall back to defaults and are clamped to sane ranges.
 */
public final class EchoConfig {
    public int reactorCapacity = 50_000;
    public int reactorGenerationPerTick = 40;
    public int reactorOutputPerTick = 400;
    public int conduitCapacity = 2_000;
    public int conduitTransferPerTick = 400;
    public int fabricatorCapacity = 20_000;
    public int fabricatorInputPerTick = 400;
    public double fabricatorSpeedMultiplier = 1.0;
    public double fabricatorEnergyMultiplier = 1.0;
    public int chargerCapacity = 20_000;
    public int chargerInputPerTick = 400;
    public int chargerRatePerTick = 100;
    public int drillCapacity = 20_000;
    public int drillEnergyPerBlock = 40;
    public int scannerCapacity = 10_000;
    public int scannerEnergyPerScan = 500;
    public int scannerRadius = 10;
    public int scannerCooldownTicks = 60;

    private static EchoConfig instance = new EchoConfig();

    public static EchoConfig get() {
        return instance;
    }

    public static EchoConfig load(Path file) {
        EchoConfig config = new EchoConfig();
        Properties props = new Properties();
        if (Files.exists(file)) {
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                props.load(reader);
            } catch (IOException e) {
                System.err.println("[echo_technology] Failed to read config, using defaults: " + e.getMessage());
            }
        }
        config.apply(props);
        try {
            Files.createDirectories(file.getParent());
            try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                config.toProperties().store(writer, "Echo Technology configuration (energy unit: EE)");
            }
        } catch (IOException e) {
            System.err.println("[echo_technology] Failed to write config: " + e.getMessage());
        }
        instance = config;
        return config;
    }

    public void apply(Properties p) {
        reactorCapacity = readInt(p, "reactor.capacity", reactorCapacity, 1_000, 10_000_000);
        reactorGenerationPerTick = readInt(p, "reactor.generation_per_tick", reactorGenerationPerTick, 1, 100_000);
        reactorOutputPerTick = readInt(p, "reactor.output_per_tick", reactorOutputPerTick, 1, 1_000_000);
        conduitCapacity = readInt(p, "conduit.capacity", conduitCapacity, 100, 1_000_000);
        conduitTransferPerTick = readInt(p, "conduit.transfer_per_tick", conduitTransferPerTick, 1, 1_000_000);
        fabricatorCapacity = readInt(p, "fabricator.capacity", fabricatorCapacity, 1_000, 10_000_000);
        fabricatorInputPerTick = readInt(p, "fabricator.input_per_tick", fabricatorInputPerTick, 1, 1_000_000);
        fabricatorSpeedMultiplier = readDouble(p, "fabricator.speed_multiplier", fabricatorSpeedMultiplier, 0.1, 10.0);
        fabricatorEnergyMultiplier = readDouble(p, "fabricator.energy_multiplier", fabricatorEnergyMultiplier, 0.0, 10.0);
        chargerCapacity = readInt(p, "charger.capacity", chargerCapacity, 1_000, 10_000_000);
        chargerInputPerTick = readInt(p, "charger.input_per_tick", chargerInputPerTick, 1, 1_000_000);
        chargerRatePerTick = readInt(p, "charger.rate_per_tick", chargerRatePerTick, 1, 1_000_000);
        drillCapacity = readInt(p, "drill.capacity", drillCapacity, 100, 10_000_000);
        drillEnergyPerBlock = readInt(p, "drill.energy_per_block", drillEnergyPerBlock, 0, 100_000);
        scannerCapacity = readInt(p, "scanner.capacity", scannerCapacity, 100, 10_000_000);
        scannerEnergyPerScan = readInt(p, "scanner.energy_per_scan", scannerEnergyPerScan, 0, 1_000_000);
        scannerRadius = readInt(p, "scanner.radius", scannerRadius, 2, 16);
        scannerCooldownTicks = readInt(p, "scanner.cooldown_ticks", scannerCooldownTicks, 0, 1_200);
    }

    public Properties toProperties() {
        Properties p = new Properties();
        p.setProperty("reactor.capacity", Integer.toString(reactorCapacity));
        p.setProperty("reactor.generation_per_tick", Integer.toString(reactorGenerationPerTick));
        p.setProperty("reactor.output_per_tick", Integer.toString(reactorOutputPerTick));
        p.setProperty("conduit.capacity", Integer.toString(conduitCapacity));
        p.setProperty("conduit.transfer_per_tick", Integer.toString(conduitTransferPerTick));
        p.setProperty("fabricator.capacity", Integer.toString(fabricatorCapacity));
        p.setProperty("fabricator.input_per_tick", Integer.toString(fabricatorInputPerTick));
        p.setProperty("fabricator.speed_multiplier", Double.toString(fabricatorSpeedMultiplier));
        p.setProperty("fabricator.energy_multiplier", Double.toString(fabricatorEnergyMultiplier));
        p.setProperty("charger.capacity", Integer.toString(chargerCapacity));
        p.setProperty("charger.input_per_tick", Integer.toString(chargerInputPerTick));
        p.setProperty("charger.rate_per_tick", Integer.toString(chargerRatePerTick));
        p.setProperty("drill.capacity", Integer.toString(drillCapacity));
        p.setProperty("drill.energy_per_block", Integer.toString(drillEnergyPerBlock));
        p.setProperty("scanner.capacity", Integer.toString(scannerCapacity));
        p.setProperty("scanner.energy_per_scan", Integer.toString(scannerEnergyPerScan));
        p.setProperty("scanner.radius", Integer.toString(scannerRadius));
        p.setProperty("scanner.cooldown_ticks", Integer.toString(scannerCooldownTicks));
        return p;
    }

    private static int readInt(Properties p, String key, int fallback, int min, int max) {
        String raw = p.getProperty(key);
        if (raw == null) {
            return fallback;
        }
        try {
            return Math.max(min, Math.min(max, Integer.parseInt(raw.trim())));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static double readDouble(Properties p, String key, double fallback, double min, double max) {
        String raw = p.getProperty(key);
        if (raw == null) {
            return fallback;
        }
        try {
            double value = Double.parseDouble(raw.trim());
            return Double.isFinite(value) ? Math.max(min, Math.min(max, value)) : fallback;
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
