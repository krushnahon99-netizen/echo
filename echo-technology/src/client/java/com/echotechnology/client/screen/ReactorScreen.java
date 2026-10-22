package com.echotechnology.client.screen;

import com.echotechnology.machine.ReactorCore;
import com.echotechnology.screen.ReactorScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Locale;

public class ReactorScreen extends MachineScreen<ReactorScreenHandler> {
    private static final int FUEL_BAR_X = 64;
    private static final int FUEL_BAR_Y = 20;
    private static final int FUEL_BAR_H = 50;

    public ReactorScreen(ReactorScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void drawMachineDetails(DrawContext context, int left, int top) {
        int bx = left + FUEL_BAR_X;
        int by = top + FUEL_BAR_Y;
        context.fill(bx - 1, by - 1, bx + 5, by + FUEL_BAR_H + 1, SLOT_EDGE);
        context.fill(bx, by, bx + 4, by + FUEL_BAR_H, SLOT_DARK);
        int fuel = scaled(handler.getFuelRemaining(), handler.getFuelTotal(), FUEL_BAR_H);
        if (fuel > 0) {
            context.fillGradient(bx, by + FUEL_BAR_H - fuel, bx + 4, by + FUEL_BAR_H, 0xFF9BFBFF, CYAN_DEEP);
        }

        ReactorCore.Status status = handler.getStatus();
        int color = status == ReactorCore.Status.GENERATING ? TEXT_TITLE : TEXT_WARN;
        String key = "gui.echo_technology.reactor.status." + status.name().toLowerCase(Locale.ROOT);
        drawLabel(context, Text.translatable(key), left + 74, top + 22, color);
        int rate = status == ReactorCore.Status.GENERATING ? handler.getGenerationRate() : 0;
        drawLabel(context, Text.translatable("gui.echo_technology.rate", rate), left + 74, top + 36, TEXT_LABEL);
        int percent = handler.getFuelTotal() > 0
                ? (int) (100L * handler.getFuelRemaining() / handler.getFuelTotal()) : 0;
        drawLabel(context, Text.translatable("gui.echo_technology.reactor.fuel", percent), left + 74, top + 50, TEXT_LABEL);
        drawLabel(context, Text.translatable("gui.echo_technology.upgrades"), left + 8, top + 64, 0x5E6E78);
    }

    @Override
    protected List<Text> machineTooltip(int localX, int localY) {
        if (localX >= FUEL_BAR_X - 1 && localX <= FUEL_BAR_X + 5 && localY >= FUEL_BAR_Y - 1 && localY <= FUEL_BAR_Y + FUEL_BAR_H) {
            return List.of(Text.translatable("gui.echo_technology.reactor.fuel_tooltip",
                    format(handler.getFuelRemaining()), format(handler.getFuelTotal())));
        }
        return List.of();
    }
}
