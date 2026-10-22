package com.echotechnology.item;

import com.echotechnology.config.EchoConfig;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Sonar pulse: costs EE, then reports ores (c:ores tag) within a cube around the
 * player in chat and briefly highlights the nearest ones with particles that only
 * the scanning player can see. Uses an item cooldown to limit server load.
 */
public class EchoScannerItem extends Item implements ChargeableItem {
    private static final int MAX_HIGHLIGHTS = 32;
    private static final int MAX_REPORT_LINES = 5;

    public EchoScannerItem(Settings settings) {
        super(settings);
    }

    @Override
    public int getEnergyCapacity() {
        return EchoConfig.get().scannerCapacity;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (world.isClient) {
            return TypedActionResult.success(stack, true);
        }
        EchoConfig config = EchoConfig.get();
        if (!tryConsume(stack, config.scannerEnergyPerScan)) {
            user.sendMessage(Text.translatable("message.echo_technology.scanner.no_energy", config.scannerEnergyPerScan)
                    .formatted(Formatting.RED), true);
            return TypedActionResult.fail(stack);
        }
        user.getItemCooldownManager().set(this, config.scannerCooldownTicks);
        scan((ServerWorld) world, (ServerPlayerEntity) user, config.scannerRadius);
        return TypedActionResult.success(stack, false);
    }

    private record Hit(BlockPos pos, Block block, double distanceSq) {
    }

    private void scan(ServerWorld world, ServerPlayerEntity player, int radius) {
        BlockPos center = player.getBlockPos();
        List<Hit> hits = new ArrayList<>();
        for (BlockPos pos : BlockPos.iterate(center.add(-radius, -radius, -radius), center.add(radius, radius, radius))) {
            BlockState state = world.getBlockState(pos);
            if (state.isIn(ConventionalBlockTags.ORES)) {
                hits.add(new Hit(pos.toImmutable(), state.getBlock(), pos.getSquaredDistance(center)));
            }
        }
        hits.sort(Comparator.comparingDouble(Hit::distanceSq));

        world.playSound(null, center, SoundEvents.BLOCK_SCULK_SENSOR_CLICKING, SoundCategory.PLAYERS, 1.0f, 0.8f);
        world.spawnParticles(player, ParticleTypes.SONIC_BOOM, true, player.getX(), player.getY() + 1.0, player.getZ(), 1, 0, 0, 0, 0);

        if (hits.isEmpty()) {
            player.sendMessage(Text.translatable("message.echo_technology.scanner.none", radius).formatted(Formatting.GRAY), false);
            return;
        }
        player.sendMessage(Text.translatable("message.echo_technology.scanner.header", hits.size(), radius)
                .formatted(Formatting.AQUA), false);

        // Group by block, keeping the nearest distance for each (hits are already sorted).
        Map<Block, int[]> byBlock = new LinkedHashMap<>();
        for (Hit hit : hits) {
            byBlock.computeIfAbsent(hit.block(), b -> new int[]{0, (int) Math.round(Math.sqrt(hit.distanceSq()))})[0]++;
        }
        byBlock.entrySet().stream().limit(MAX_REPORT_LINES).forEach(entry -> player.sendMessage(
                Text.literal(" \u25C6 ").formatted(Formatting.DARK_AQUA)
                        .append(entry.getKey().getName().copy().formatted(Formatting.WHITE))
                        .append(Text.translatable("message.echo_technology.scanner.entry", entry.getValue()[0], entry.getValue()[1])
                                .formatted(Formatting.GRAY)), false));

        for (Hit hit : hits.subList(0, Math.min(MAX_HIGHLIGHTS, hits.size()))) {
            world.spawnParticles(player, ParticleTypes.GLOW, true,
                    hit.pos().getX() + 0.5, hit.pos().getY() + 0.5, hit.pos().getZ() + 0.5, 6, 0.3, 0.3, 0.3, 0.0);
        }
    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        return energyBarStep(stack);
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        return ENERGY_BAR_COLOR;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        EchoConfig config = EchoConfig.get();
        tooltip.add(energyTooltip(stack));
        tooltip.add(Text.translatable("tooltip.echo_technology.scanner.info", config.scannerRadius).formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("tooltip.echo_technology.scanner.cost", config.scannerEnergyPerScan,
                config.scannerCooldownTicks / 20.0).formatted(Formatting.DARK_GRAY));
    }
}
