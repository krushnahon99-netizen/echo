package com.echotechnology.energy;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/** World-aware helper that pushes energy from a node into its loaded neighbours. */
public final class EnergyDistribution {
    private static final Direction[] DIRECTIONS = Direction.values();

    private EnergyDistribution() {
    }

    /**
     * Pushes up to {@code budget} EE this tick from {@code source} into adjacent
     * receivers. Conduit neighbours receive via diffusion (half the difference)
     * when {@code diffuseIntoConduits} is set, which prevents energy from
     * bouncing back and forth between conduits.
     *
     * @return total EE moved
     */
    public static int push(World world, BlockPos pos, EchoEnergyStorage source, int budget, boolean diffuseIntoConduits) {
        if (world.isClient || budget <= 0 || source.getEnergy() <= 0) {
            return 0;
        }
        int remaining = budget;
        // Rotate the start direction every tick so no side is permanently favoured.
        int start = (int) Math.floorMod(world.getTime(), (long) DIRECTIONS.length);
        for (int i = 0; i < DIRECTIONS.length && remaining > 0 && source.getEnergy() > 0; i++) {
            Direction direction = DIRECTIONS[(start + i) % DIRECTIONS.length];
            EnergyNode neighbour = loadedNode(world, pos.offset(direction));
            if (neighbour == null) {
                continue;
            }
            EchoEnergyStorage target = neighbour.getEnergyStorage();
            int moved = diffuseIntoConduits && neighbour.isConduit()
                    ? EnergyTransfer.equalize(source, target, remaining)
                    : EnergyTransfer.move(source, target, remaining);
            remaining -= moved;
        }
        return budget - remaining;
    }

    /** Returns the energy node at {@code pos} without force-loading chunks. */
    @Nullable
    public static EnergyNode loadedNode(World world, BlockPos pos) {
        if (!world.isChunkLoaded(ChunkSectionPos.getSectionCoord(pos.getX()), ChunkSectionPos.getSectionCoord(pos.getZ()))) {
            return null;
        }
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof EnergyNode node && !blockEntity.isRemoved()) {
            return node;
        }
        return null;
    }
}
