package com.echotechnology.block.entity;

import com.echotechnology.config.EchoConfig;
import com.echotechnology.energy.EchoEnergyStorage;
import com.echotechnology.energy.EnergyDistribution;
import com.echotechnology.energy.EnergyNode;
import com.echotechnology.registry.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * A small energy buffer that diffuses EE to neighbouring conduits and pushes it
 * into machines. Machines never push back into conduits (their extract rate is
 * 0), so energy flows one way: reactor -> conduits -> consumers.
 */
public class EchoConduitBlockEntity extends BlockEntity implements EnergyNode {
    private final EchoEnergyStorage energy;

    public EchoConduitBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ECHO_CONDUIT, pos, state);
        int rate = EchoConfig.get().conduitTransferPerTick;
        this.energy = new EchoEnergyStorage(EchoConfig.get().conduitCapacity, rate, rate);
        this.energy.setChangeListener(this::markDirty);
    }

    public static void serverTick(World world, BlockPos pos, BlockState state, EchoConduitBlockEntity conduit) {
        EnergyDistribution.push(world, pos, conduit.energy, conduit.energy.getMaxExtract(), true);
    }

    @Override
    public EchoEnergyStorage getEnergyStorage() {
        return energy;
    }

    @Override
    public boolean isConduit() {
        return true;
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        nbt.putInt("Energy", energy.getEnergy());
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        energy.setEnergy(nbt.getInt("Energy"));
    }
}
