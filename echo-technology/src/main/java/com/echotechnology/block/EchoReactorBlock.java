package com.echotechnology.block;

import com.echotechnology.block.entity.EchoReactorBlockEntity;
import com.echotechnology.registry.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class EchoReactorBlock extends MachineBlock {
    public static final MapCodec<EchoReactorBlock> CODEC = createCodec(EchoReactorBlock::new);

    public EchoReactorBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new EchoReactorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return world.isClient ? null : validateTicker(type, ModBlockEntities.ECHO_REACTOR, EchoReactorBlockEntity::serverTick);
    }

    /** Client-only ambience: sculk souls drift from the core vent while the reactor burns. */
    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (!state.get(LIT)) {
            return;
        }
        double x = pos.getX() + 0.5;
        double z = pos.getZ() + 0.5;
        if (random.nextInt(3) == 0) {
            world.addParticle(ParticleTypes.SCULK_SOUL,
                    x + (random.nextDouble() - 0.5) * 0.4, pos.getY() + 1.02, z + (random.nextDouble() - 0.5) * 0.4,
                    0.0, 0.03, 0.0);
        }
        Direction facing = state.get(FACING);
        if (random.nextInt(4) == 0) {
            double fx = x + facing.getOffsetX() * 0.52 + (random.nextDouble() - 0.5) * 0.5 * Math.abs(facing.getOffsetZ());
            double fz = z + facing.getOffsetZ() * 0.52 + (random.nextDouble() - 0.5) * 0.5 * Math.abs(facing.getOffsetX());
            world.addParticle(ParticleTypes.ELECTRIC_SPARK, fx, pos.getY() + 0.3 + random.nextDouble() * 0.4, fz, 0.0, 0.0, 0.0);
        }
        if (random.nextInt(60) == 0) {
            world.playSound(x, pos.getY() + 0.5, z, SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.BLOCKS,
                    0.4f, 0.6f + random.nextFloat() * 0.2f, false);
        }
    }
}
