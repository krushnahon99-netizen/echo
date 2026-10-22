package com.echotechnology.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;

/**
 * Shared screen handler logic: player inventory layout, shift-click routing and
 * the synced energy properties (indices 0-3 for every machine).
 */
public abstract class MachineScreenHandler extends ScreenHandler {
    protected final Inventory inventory;
    protected final PropertyDelegate properties;
    private final int machineSlotCount;

    protected MachineScreenHandler(ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory,
                                   Inventory inventory, PropertyDelegate properties, int expectedSize, int expectedProperties) {
        super(type, syncId);
        checkSize(inventory, expectedSize);
        checkDataCount(properties, expectedProperties);
        this.inventory = inventory;
        this.properties = properties;
        this.machineSlotCount = expectedSize;
        inventory.onOpen(playerInventory.player);
        addMachineSlots();
        addPlayerInventory(playerInventory);
        addProperties(properties);
    }

    /** Adds exactly {@code expectedSize} machine slots, in inventory index order. */
    protected abstract void addMachineSlots();

    private void addPlayerInventory(PlayerInventory playerInventory) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    public int getEnergy() {
        return SyncedInts.join(properties.get(0), properties.get(1));
    }

    public int getCapacity() {
        return SyncedInts.join(properties.get(2), properties.get(3));
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return inventory.canPlayerUse(player);
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        inventory.onClose(player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasStack()) {
            return ItemStack.EMPTY;
        }
        ItemStack original = slot.getStack();
        ItemStack copy = original.copy();
        int playerStart = machineSlotCount;
        int hotbarStart = playerStart + 27;
        int end = slots.size();

        if (index < machineSlotCount) {
            if (!insertItem(original, playerStart, end, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickTransfer(original, copy);
        } else {
            boolean moved = false;
            for (int i = 0; i < machineSlotCount && !original.isEmpty(); i++) {
                if (slots.get(i).canInsert(original) && insertItem(original, i, i + 1, false)) {
                    moved = true;
                }
            }
            if (!moved) {
                boolean fromMain = index < hotbarStart;
                if (!insertItem(original, fromMain ? hotbarStart : playerStart, fromMain ? end : hotbarStart, false)) {
                    return ItemStack.EMPTY;
                }
            }
        }

        if (original.isEmpty()) {
            slot.setStack(ItemStack.EMPTY);
        } else {
            slot.markDirty();
        }
        if (original.getCount() == copy.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTakeItem(player, original);
        return copy;
    }
}
