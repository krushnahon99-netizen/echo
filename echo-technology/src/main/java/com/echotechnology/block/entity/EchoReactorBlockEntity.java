package com.echotechnology.block.entity;

import com.echotechnology.config.EchoConfig;
import com.echotechnology.energy.EchoEnergyStorage;
import com.echotechnology.energy.EnergyDistribution;
import com.echotechnology.machine.ReactorCore;
import com.echotechnology.registry.ModBlockEntities;
import com.echotechnology.screen.ReactorScreenHandler;
import com.echotechnology.screen.SyncedInts;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class EchoReactorBlockEntity extends MachineBlockEntity {
    public static final int FUEL_SLOT = 0;
    public static final int[] UPGRADE_SLOTS = {1, 2};
    public static final int SLOT_COUNT = 3;
    public static final int PROPERTY_COUNT = 10;
    private static final int[] HOPPER_SLOTS = {FUEL_SLOT};

    private final ReactorCore core = new ReactorCore();
    private ReactorCore.Status status = ReactorCore.Status.NO_FUEL;
    private int generationRate;

    private final ReactorCore.FuelSlot fuelSlot = new ReactorCore.FuelSlot() {
        @Override
        public String itemId() {
            return idOf(items.get(FUEL_SLOT));
        }

        @Override
        public void consumeOne() {
            items.get(FUEL_SLOT).decrement(1);
            markDirty();
        }
    };

    private final PropertyDelegate properties = new PropertyDelegate() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> SyncedInts.lo(energy.getEnergy());
                case 1 -> SyncedInts.hi(energy.getEnergy());
                case 2 -> SyncedInts.lo(energy.getCapacity());
                case 3 -> SyncedInts.hi(energy.getCapacity());
                case 4 -> SyncedInts.lo(core.fuelRemaining());
                case 5 -> SyncedInts.hi(core.fuelRemaining());
                case 6 -> SyncedInts.lo(core.fuelTotal());
                case 7 -> SyncedInts.hi(core.fuelTotal());
                case 8 -> status.ordinal();
                case 9 -> generationRate;
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

    public EchoReactorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ECHO_REACTOR, pos, state, SLOT_COUNT,
                new EchoEnergyStorage(EchoConfig.get().reactorCapacity, 0, EchoConfig.get().reactorOutputPerTick));
    }

    public static void serverTick(World world, BlockPos pos, BlockState state, EchoReactorBlockEntity reactor) {
        reactor.refreshUpgrades();
        EchoConfig config = EchoConfig.get();
        reactor.generationRate = (int) Math.round(config.reactorGenerationPerTick * reactor.getUpgrades().speedMultiplier());
        reactor.status = reactor.core.tick(reactor.fuelSlot, reactor.energy, reactor.generationRate,
                reactor.getUpgrades().fuelYieldMultiplier());
        reactor.setLit(reactor.status == ReactorCore.Status.GENERATING);
        EnergyDistribution.push(world, pos, reactor.energy, reactor.energy.getMaxExtract(), false);
    }

    public ReactorCore.Status getStatus() {
        return status;
    }

    @Override
    protected int baseCapacity() {
        return EchoConfig.get().reactorCapacity;
    }

    @Override
    protected int[] upgradeSlots() {
        return UPGRADE_SLOTS;
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        return slot == FUEL_SLOT ? ReactorCore.isFuel(idOf(stack)) : isUpgrade(stack);
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return HOPPER_SLOTS;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return slot == FUEL_SLOT && ReactorCore.isFuel(idOf(stack));
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return false;
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        nbt.putInt("FuelRemaining", core.fuelRemaining());
        nbt.putInt("FuelTotal", core.fuelTotal());
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        core.load(nbt.getInt("FuelRemaining"), nbt.getInt("FuelTotal"));
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new ReactorScreenHandler(syncId, playerInventory, this, properties);
    }
}
