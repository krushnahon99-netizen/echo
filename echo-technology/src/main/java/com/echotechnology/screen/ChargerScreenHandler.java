package com.echotechnology.screen;

import com.echotechnology.block.entity.EchoChargerBlockEntity;
import com.echotechnology.item.ChargeableItem;
import com.echotechnology.machine.ChargerCore;
import com.echotechnology.registry.ModScreenHandlers;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;

public class ChargerScreenHandler extends MachineScreenHandler {
    public ChargerScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(EchoChargerBlockEntity.SLOT_COUNT),
                new ArrayPropertyDelegate(EchoChargerBlockEntity.PROPERTY_COUNT));
    }

    public ChargerScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, PropertyDelegate properties) {
        super(ModScreenHandlers.CHARGER, syncId, playerInventory, inventory, properties,
                EchoChargerBlockEntity.SLOT_COUNT, EchoChargerBlockEntity.PROPERTY_COUNT);
    }

    @Override
    protected void addMachineSlots() {
        addSlot(new MachineSlots.Filtered(inventory, EchoChargerBlockEntity.CHARGE_SLOT, 80, 30,
                stack -> stack.getItem() instanceof ChargeableItem, 1));
        addSlot(MachineSlots.upgrade(inventory, 1, 8, 26));
        addSlot(MachineSlots.upgrade(inventory, 2, 8, 46));
    }

    public int getItemEnergy() {
        return SyncedInts.join(properties.get(4), properties.get(5));
    }

    public int getItemCapacity() {
        return SyncedInts.join(properties.get(6), properties.get(7));
    }

    public ChargerCore.Status getStatus() {
        ChargerCore.Status[] values = ChargerCore.Status.values();
        int ordinal = properties.get(8);
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : ChargerCore.Status.IDLE;
    }
}
