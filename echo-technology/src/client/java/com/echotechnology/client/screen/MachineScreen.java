package com.echotechnology.client.screen;

import com.echotechnology.screen.MachineScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Code-rendered Echo machine GUI: dark sculk-steel panel, inset slots, a live
 * cyan energy gauge on the right, and readable cyan/grey labels. All values are
 * read from the synced screen-handler properties every frame.
 */
public abstract class MachineScreen<T extends MachineScreenHandler> extends HandledScreen<T> {
    protected static final int PANEL_OUTER = 0xFF05080B;
    protected static final int PANEL = 0xFF12191F;
    protected static final int PANEL_HIGHLIGHT = 0xFF1E2A33;
    protected static final int SLOT_DARK = 0xFF070B0E;
    protected static final int SLOT_EDGE = 0xFF2B3B46;
    protected static final int CYAN = 0xFF29DFEB;
    protected static final int CYAN_DEEP = 0xFF0B6E78;
    protected static final int TEXT_TITLE = 0x7FF5FF;
    protected static final int TEXT_LABEL = 0x9AAAB4;
    protected static final int TEXT_WARN = 0xFFB347;

    private static final int GAUGE_X = 152;
    private static final int GAUGE_Y = 17;
    private static final int GAUGE_W = 16;
    private static final int GAUGE_H = 54;

    protected MachineScreen(T handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 176;
        this.backgroundHeight = 166;
        this.playerInventoryTitleY = this.backgroundHeight - 94;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int left = x;
        int top = y;
        context.fill(left, top, left + backgroundWidth, top + backgroundHeight, PANEL_OUTER);
        context.fill(left + 1, top + 1, left + backgroundWidth - 1, top + backgroundHeight - 1, PANEL);
        context.fill(left + 1, top + 1, left + backgroundWidth - 1, top + 2, PANEL_HIGHLIGHT);
        // Thin cyan accent under the title and a divider above the player inventory.
        context.fill(left + 7, top + 15, left + 60, top + 16, CYAN_DEEP);
        context.fill(left + 7, top + 79, left + backgroundWidth - 7, top + 80, PANEL_HIGHLIGHT);

        for (Slot slot : handler.slots) {
            drawSlot(context, left + slot.x, top + slot.y);
        }
        drawEnergyGauge(context, left + GAUGE_X, top + GAUGE_Y);
        drawMachineDetails(context, left, top);
    }

    private void drawSlot(DrawContext context, int sx, int sy) {
        context.fill(sx - 1, sy - 1, sx + 17, sy + 17, SLOT_EDGE);
        context.fill(sx - 1, sy - 1, sx + 16, sy + 16, SLOT_DARK);
        context.fill(sx, sy, sx + 16, sy + 16, 0xFF0D1318);
    }

    private void drawEnergyGauge(DrawContext context, int gx, int gy) {
        context.fill(gx - 1, gy - 1, gx + GAUGE_W + 1, gy + GAUGE_H + 1, SLOT_EDGE);
        context.fill(gx, gy, gx + GAUGE_W, gy + GAUGE_H, SLOT_DARK);
        int filled = scaled(handler.getEnergy(), handler.getCapacity(), GAUGE_H - 2);
        if (filled > 0) {
            int bottom = gy + GAUGE_H - 1;
            context.fillGradient(gx + 1, bottom - filled, gx + GAUGE_W - 1, bottom, CYAN, CYAN_DEEP);
            // Slow scanline drifting up the gauge, a subtle "live energy" cue.
            int phase = (int) ((System.currentTimeMillis() / 60) % Math.max(1, filled));
            context.fill(gx + 1, bottom - phase - 1, gx + GAUGE_W - 1, bottom - phase, 0x55FFFFFF);
        }
        for (int i = 1; i < 6; i++) {
            int tickY = gy + i * GAUGE_H / 6;
            context.fill(gx, tickY, gx + 3, tickY + 1, SLOT_EDGE);
        }
    }

    /** Machine-specific gauges and labels. Coordinates are absolute screen positions. */
    protected abstract void drawMachineDetails(DrawContext context, int left, int top);

    /** Extra tooltip lines for machine-specific regions; return empty when not hovering one. */
    protected List<Text> machineTooltip(int localX, int localY) {
        return List.of();
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        context.drawText(textRenderer, title, titleX, titleY, TEXT_TITLE, false);
        context.drawText(textRenderer, playerInventoryTitle, playerInventoryTitleX, playerInventoryTitleY, TEXT_LABEL, false);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        int localX = mouseX - x;
        int localY = mouseY - y;
        List<Text> lines = new ArrayList<>();
        if (localX >= GAUGE_X - 1 && localX <= GAUGE_X + GAUGE_W && localY >= GAUGE_Y - 1 && localY <= GAUGE_Y + GAUGE_H) {
            lines.add(Text.translatable("gui.echo_technology.energy",
                    format(handler.getEnergy()), format(handler.getCapacity())));
        } else {
            lines.addAll(machineTooltip(localX, localY));
        }
        if (!lines.isEmpty()) {
            context.drawTooltip(textRenderer, lines, mouseX, mouseY);
        } else {
            drawMouseoverTooltip(context, mouseX, mouseY);
        }
    }

    protected static int scaled(int value, int max, int pixels) {
        if (max <= 0 || value <= 0) {
            return 0;
        }
        return (int) Math.min(pixels, (long) value * pixels / max);
    }

    protected static String format(int value) {
        return String.format("%,d", value);
    }

    protected void drawHorizontalBar(DrawContext context, int bx, int by, int width, int height, int value, int max) {
        context.fill(bx - 1, by - 1, bx + width + 1, by + height + 1, SLOT_EDGE);
        context.fill(bx, by, bx + width, by + height, SLOT_DARK);
        int filled = scaled(value, max, width);
        if (filled > 0) {
            context.fillGradient(bx, by, bx + filled, by + height, CYAN, CYAN_DEEP);
        }
    }

    protected void drawLabel(DrawContext context, Text text, int lx, int ly, int color) {
        context.drawText(textRenderer, text, lx, ly, color, false);
    }
}
