package com.echotechnology.block.entity;

import com.echotechnology.config.EchoConfig;
import com.echotechnology.energy.EchoEnergyStorage;
import com.echotechnology.item.ChargeableItem;
import com.echotechnology.machine.ChargerCore;
import com.echotechnology.registry.ModBlockEntities;
import com.echotechnology.screen.ChargerScreenHandler;
import com.echotechnology.screen.SyncedInts;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/** Second energy consumer: moves buffered EE into chargeable tools. */
public class EchoChargerBlockEntity extends MachineBlockEntity {
    public static final int CHARGE_SLOT = 0;
    public static final int[] UPGRADE_SLOTS = {1, 2};
    public static final int SLOT_COUNT = 3;
    public static final int PROPERTY_COUNT = 9;
    private static final int[] HOPPER_SLOTS = {CHARGE_SLOT};

    private ChargerCore.Status status = ChargerCore.Status.IDLE;

    private final PropertyDelegate properties = new PropertyDelegate() {
        @Override
        public int get(int index) {
            ItemStack stack = items.get(CHARGE_SLOT);
            int itemEnergy = stack.getItem() instanceof ChargeableItem c ? c.getEnergy(stack) : 0;
            int itemCapacity = stack.getItem() instanceof ChargeableItem c ? c.getEnergyCapacity() : 0;
            return switch (index) {
                case 0 -> SyncedInts.lo(energy.getEnergy());
                case 1 -> SyncedInts.hi(energy.getEnergy());
                case 2 -> SyncedInts.lo(energy.getCapacity());
                case 3 -> SyncedInts.hi(energy.getCapacity());
                case 4 -> SyncedInts.lo(itemEnergy);
                case 5 -> SyncedInts.hi(itemEnergy);
                case 6 -> SyncedInts.lo(itemCapacity);
                case 7 -> SyncedInts.hi(itemCapacity);
                case 8 -> status.ordinal();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int size() {
            return PROPERTY_COUNT;
        }
    };

    public EchoChargerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ECHO_CHARGER, pos, state, SLOT_COUNT,
                new EchoEnergyStorage(EchoConfig.get().chargerCapacity, EchoConfig.get().chargerInputPerTick, 0));
    }

    public static void serverTick(World world, BlockPos pos, BlockState state, EchoChargerBlockEntity charger) {
        charger.refreshUpgrades();
        charger.status = charger.chargeItem();
        charger.setLit(charger.status == ChargerCore.Status.CHARGING);
    }

    private ChargerCore.Status chargeItem() {
        ItemStack stack = items.get(CHARGE_SLOT);
        if (!(stack.getItem() instanceof ChargeableItem chargeable) || stack.getCount() != 1) {
            return ChargerCore.Status.IDLE;
        }
        int space = chargeable.getEnergyCapacity() - chargeable.getEnergy(stack);
        if (space <= 0) {
            return ChargerCore.Status.FULL;
        }
        int rate = (int) Math.round(EchoConfig.get().chargerRatePerTick * getUpgrades().speedMultiplier());
        int delivered = ChargerCore.charge(energy, space, rate, ChargerCore.lossFactor(getUpgrades()));
        if (delivered <= 0) {
            return ChargerCore.Status.NO_ENERGY;
        }
        chargeable.setEnergy(stack, chargeable.getEnergy(stack) + delivered);
        markDirty();
        return ChargerCore.Status.CHARGING;
    }

    @Override
    protected int baseCapacity() {
        return EchoConfig.get().chargerCapacity;
    }

    @Override
    protected int[] upgradeSlots() {
        return UPGRADE_SLOTS;
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        return slot == CHARGE_SLOT ? stack.getItem() instanceof ChargeableItem : isUpgrade(stack);
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return HOPPER_SLOTS;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return slot == CHARGE_SLOT && items.get(CHARGE_SLOT).isEmpty() && stack.getItem() instanceof ChargeableItem;
    }

    /** Hoppers below can pull out tools once they are fully charged. */
    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return slot == CHARGE_SLOT && dir == Direction.DOWN && stack.getItem() instanceof ChargeableItem c
                && c.getEnergy(stack) >= c.getEnergyCapacity();
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new ChargerScreenHandler(syncId, playerInventory, this, properties);
    }
}
