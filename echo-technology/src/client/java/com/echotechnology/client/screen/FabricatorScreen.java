package com.echotechnology.client.screen;

import com.echotechnology.machine.FabricatorCore;
import com.echotechnology.screen.FabricatorScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Locale;

public class FabricatorScreen extends MachineScreen<FabricatorScreenHandler> {
    private static final int ARROW_X = 64;
    private static final int ARROW_Y = 40;
    private static final int ARROW_W = 34;

    public FabricatorScreen(FabricatorScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void drawMachineDetails(DrawContext context, int left, int top) {
        int ax = left + ARROW_X;
        int ay = top + ARROW_Y;
        // Progress "arrow": a track with a chevron head, filled cyan as work progresses.
        drawHorizontalBar(context, ax, ay, ARROW_W - 6, 6, handler.getProgress(), handler.getDuration());
        int filled = scaled(handler.getProgress(), handler.getDuration(), ARROW_W);
        for (int i = 0; i < 5; i++) {
            int headColor = filled >= ARROW_W - 5 + i ? CYAN : SLOT_EDGE;
            context.fill(ax + ARROW_W - 6 + i, ay - 2 + i, ax + ARROW_W - 5 + i, ay + 8 - i, headColor);
        }
        // Bracket joining the two inputs into the arrow.
        context.fill(left + 58, top + 33, left + 60, top + 56, SLOT_EDGE);
        context.fill(left + 60, top + 42, left + 63, top + 44, SLOT_EDGE);

        FabricatorCore.Status status = handler.getStatus();
        boolean active = status == FabricatorCore.Status.WORKING || status == FabricatorCore.Status.COMPLETED;
        String key = "gui.echo_technology.fabricator.status." + status.name().toLowerCase(Locale.ROOT);
        drawLabel(context, Text.translatable(key), left + 62, top + 22, active ? TEXT_TITLE : TEXT_WARN);
        if (handler.getEnergyPerTick() > 0) {
            drawLabel(context, Text.translatable("gui.echo_technology.cost", handler.getEnergyPerTick()),
                    left + 62, top + 60, TEXT_LABEL);
        }
        drawLabel(context, Text.translatable("gui.echo_technology.upgrades"), left + 8, top + 64, 0x5E6E78);
    }

    @Override
    protected List<Text> machineTooltip(int localX, int localY) {
        if (localX >= ARROW_X && localX <= ARROW_X + ARROW_W && localY >= ARROW_Y - 3 && localY <= ARROW_Y + 9
                && handler.getDuration() > 0) {
            int percent = (int) (100L * handler.getProgress() / handler.getDuration());
            return List.of(Text.translatable("gui.echo_technology.fabricator.progress", percent,
                    handler.getProgress(), handler.getDuration()));
        }
        return List.of();
    }
}
