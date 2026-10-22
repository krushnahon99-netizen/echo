package com.echotechnology.screen;

import com.echotechnology.block.entity.EchoReactorBlockEntity;
import com.echotechnology.block.entity.MachineBlockEntity;
import com.echotechnology.machine.ReactorCore;
import com.echotechnology.registry.ModScreenHandlers;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;

public class ReactorScreenHandler extends MachineScreenHandler {
    public ReactorScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(EchoReactorBlockEntity.SLOT_COUNT),
                new ArrayPropertyDelegate(EchoReactorBlockEntity.PROPERTY_COUNT));
    }

    public ReactorScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, PropertyDelegate properties) {
        super(ModScreenHandlers.REACTOR, syncId, playerInventory, inventory, properties,
                EchoReactorBlockEntity.SLOT_COUNT, EchoReactorBlockEntity.PROPERTY_COUNT);
    }

    @Override
    protected void addMachineSlots() {
        addSlot(new MachineSlots.Filtered(inventory, EchoReactorBlockEntity.FUEL_SLOT, 44, 36,
                stack -> ReactorCore.isFuel(MachineBlockEntity.idOf(stack)), 64));
        addSlot(MachineSlots.upgrade(inventory, 1, 8, 26));
        addSlot(MachineSlots.upgrade(inventory, 2, 8, 46));
    }

    public int getFuelRemaining() {
        return SyncedInts.join(properties.get(4), properties.get(5));
    }

    public int getFuelTotal() {
        return SyncedInts.join(properties.get(6), properties.get(7));
    }

    public ReactorCore.Status getStatus() {
        ReactorCore.Status[] values = ReactorCore.Status.values();
        int ordinal = properties.get(8);
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : ReactorCore.Status.NO_FUEL;
    }

    public int getGenerationRate() {
        return properties.get(9);
    }
}
