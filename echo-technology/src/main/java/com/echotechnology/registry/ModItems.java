package com.echotechnology.registry;

import com.echotechnology.EchoTechnology;
import com.echotechnology.item.EchoDrillItem;
import com.echotechnology.item.EchoScannerItem;
import com.echotechnology.item.UpgradeItem;
import com.echotechnology.upgrade.UpgradeType;
import net.minecraft.item.Item;
import net.minecraft.item.MiningToolItem;
import net.minecraft.item.ToolMaterials;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Rarity;

public final class ModItems {
    public static final Item ECHO_CRYSTAL = register("echo_crystal", new Item(new Item.Settings()));
    public static final Item RESONANT_ALLOY = register("resonant_alloy", new Item(new Item.Settings()));
    public static final Item ECHO_CIRCUIT = register("echo_circuit", new Item(new Item.Settings()));

    public static final Item EFFICIENCY_UPGRADE = register("efficiency_upgrade",
            new UpgradeItem(UpgradeType.EFFICIENCY, new Item.Settings().maxCount(16).rarity(Rarity.UNCOMMON)));
    public static final Item CAPACITY_UPGRADE = register("capacity_upgrade",
            new UpgradeItem(UpgradeType.CAPACITY, new Item.Settings().maxCount(16).rarity(Rarity.UNCOMMON)));
    public static final Item SPEED_UPGRADE = register("speed_upgrade",
            new UpgradeItem(UpgradeType.SPEED, new Item.Settings().maxCount(16).rarity(Rarity.UNCOMMON)));

    public static final Item ECHO_DRILL = register("echo_drill", new EchoDrillItem(new Item.Settings()
            .maxCount(1)
            .rarity(Rarity.RARE)
            .component(DataComponentTypes.TOOL, EchoDrillItem.createToolComponent())
            .component(ModComponents.ENERGY, 0)
            .component(ModComponents.AREA_MODE, false)
            .attributeModifiers(MiningToolItem.createAttributeModifiers(ToolMaterials.DIAMOND, 1.0f, -2.8f))));

    public static final Item ECHO_SCANNER = register("echo_scanner", new EchoScannerItem(new Item.Settings()
            .maxCount(1)
            .rarity(Rarity.RARE)
            .component(ModComponents.ENERGY, 0)));

    private ModItems() {
    }

    private static Item register(String name, Item item) {
        return Registry.register(Registries.ITEM, EchoTechnology.id(name), item);
    }

    public static void init() {
    }
}
