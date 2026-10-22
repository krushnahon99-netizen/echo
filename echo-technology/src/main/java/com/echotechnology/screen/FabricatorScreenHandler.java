package com.echotechnology.screen;

import com.echotechnology.block.entity.EchoFabricatorBlockEntity;
import com.echotechnology.machine.FabricatorCore;
import com.echotechnology.registry.ModScreenHandlers;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;

public class FabricatorScreenHandler extends MachineScreenHandler {
    public FabricatorScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(EchoFabricatorBlockEntity.SLOT_COUNT),
                new ArrayPropertyDelegate(EchoFabricatorBlockEntity.PROPERTY_COUNT));
    }

    public FabricatorScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, PropertyDelegate properties) {
        super(ModScreenHandlers.FABRICATOR, syncId, playerInventory, inventory, properties,
                EchoFabricatorBlockEntity.SLOT_COUNT, EchoFabricatorBlockEntity.PROPERTY_COUNT);
    }

    @Override
    protected void addMachineSlots() {
        addSlot(new MachineSlots.Filtered(inventory, EchoFabricatorBlockEntity.INPUT_A, 40, 25,
                EchoFabricatorBlockEntity::isIngredient, 64));
        addSlot(new MachineSlots.Filtered(inventory, EchoFabricatorBlockEntity.INPUT_B, 40, 47,
                EchoFabricatorBlockEntity::isIngredient, 64));
        addSlot(new MachineSlots.Output(inventory, EchoFabricatorBlockEntity.OUTPUT, 106, 36));
        addSlot(MachineSlots.upgrade(inventory, 3, 8, 26));
        addSlot(MachineSlots.upgrade(inventory, 4, 8, 46));
    }

    public int getProgress() {
        return properties.get(4);
    }

    public int getDuration() {
        return properties.get(5);
    }

    public FabricatorCore.Status getStatus() {
        FabricatorCore.Status[] values = FabricatorCore.Status.values();
        int ordinal = properties.get(6);
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : FabricatorCore.Status.IDLE;
    }

    public int getEnergyPerTick() {
        return properties.get(7);
    }
}
