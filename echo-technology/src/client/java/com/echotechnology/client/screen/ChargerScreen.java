package com.echotechnology.client.screen;

import com.echotechnology.machine.ChargerCore;
import com.echotechnology.screen.ChargerScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

import java.util.Locale;

public class ChargerScreen extends MachineScreen<ChargerScreenHandler> {
    public ChargerScreen(ChargerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void drawMachineDetails(DrawContext context, int left, int top) {
        drawHorizontalBar(context, left + 56, top + 52, 64, 4, handler.getItemEnergy(), handler.getItemCapacity());

        ChargerCore.Status status = handler.getStatus();
        String key = "gui.echo_technology.charger.status." + status.name().toLowerCase(Locale.ROOT);
        int color = status == ChargerCore.Status.CHARGING || status == ChargerCore.Status.FULL ? TEXT_TITLE : TEXT_WARN;
        Text statusText = Text.translatable(key);
        drawLabel(context, statusText, left + 88 - textRenderer.getWidth(statusText) / 2, top + 19, color);
        if (handler.getItemCapacity() > 0) {
            Text charge = Text.literal(format(handler.getItemEnergy()) + " / " + format(handler.getItemCapacity()));
            drawLabel(context, charge, left + 88 - textRenderer.getWidth(charge) / 2, top + 61, TEXT_LABEL);
        }
        drawLabel(context, Text.translatable("gui.echo_technology.upgrades"), left + 8, top + 64, 0x5E6E78);
    }
}
