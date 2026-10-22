package com.echotechnology.item;

import com.echotechnology.upgrade.UpgradeModifiers;
import com.echotechnology.upgrade.UpgradeType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.Locale;

public class UpgradeItem extends Item {
    private final UpgradeType type;

    public UpgradeItem(UpgradeType type, Settings settings) {
        super(settings);
        this.type = type;
    }

    public UpgradeType getType() {
        return type;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType options) {
        String key = "tooltip.echo_technology.upgrade." + type.name().toLowerCase(Locale.ROOT);
        tooltip.add(Text.translatable(key + ".line1").formatted(Formatting.GRAY));
        tooltip.add(Text.translatable(key + ".line2").formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("tooltip.echo_technology.upgrade.max", UpgradeModifiers.MAX_PER_TYPE)
                .formatted(Formatting.DARK_AQUA));
    }
}
