package com.echotechnology.item;

import com.echotechnology.config.EchoConfig;
import com.echotechnology.registry.ModComponents;
import net.minecraft.block.BlockState;
import net.minecraft.component.type.ToolComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.List;

/**
 * Energy-powered pickaxe/shovel. Each block mined costs EE; without enough
 * energy it mines at bare-hand speed and yields no drops. Sneak + use toggles
 * 3x3 area mode, which halves mining speed and charges EE for every block.
 */
public class EchoDrillItem extends Item implements ChargeableItem {
    private static final float MINING_SPEED = 9.0f;
    /** Prevents area-mined blocks from recursively triggering more area mining. */
    private static boolean breakingArea;

    public EchoDrillItem(Settings settings) {
        super(settings);
    }

    public static ToolComponent createToolComponent() {
        return new ToolComponent(List.of(
                ToolComponent.Rule.ofNeverDropping(BlockTags.INCORRECT_FOR_DIAMOND_TOOL),
                ToolComponent.Rule.ofAlwaysDropping(BlockTags.PICKAXE_MINEABLE, MINING_SPEED),
                ToolComponent.Rule.ofAlwaysDropping(BlockTags.SHOVEL_MINEABLE, MINING_SPEED)),
                1.0f, 0);
    }

    @Override
    public int getEnergyCapacity() {
        return EchoConfig.get().drillCapacity;
    }

    private static int energyPerBlock() {
        return EchoConfig.get().drillEnergyPerBlock;
    }

    public static boolean isAreaMode(ItemStack stack) {
        return stack.getOrDefault(ModComponents.AREA_MODE, false);
    }

    private boolean hasEnergyForBlock(ItemStack stack) {
        return getEnergy(stack) >= energyPerBlock();
    }

    @Override
    public float getMiningSpeed(ItemStack stack, BlockState state) {
        if (!hasEnergyForBlock(stack)) {
            return 1.0f;
        }
        float speed = super.getMiningSpeed(stack, state);
        return isAreaMode(stack) ? Math.max(1.0f, speed * 0.5f) : speed;
    }

    @Override
    public boolean isCorrectForDrops(ItemStack stack, BlockState state) {
        return hasEnergyForBlock(stack) && super.isCorrectForDrops(stack, state);
    }

    @Override
    public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
        if (world.isClient) {
            return true;
        }
        if (state.getHardness(world, pos) != 0.0f) {
            tryConsume(stack, energyPerBlock());
        }
        if (!breakingArea && isAreaMode(stack) && miner instanceof ServerPlayerEntity player && !player.isSneaking()) {
            breakArea(stack, world, state, pos, player);
        }
        return true;
    }

    /** Breaks the 8 blocks surrounding {@code origin} in the plane facing the player. */
    private void breakArea(ItemStack stack, World world, BlockState originState, BlockPos origin, ServerPlayerEntity player) {
        Direction.Axis axis = player.getPitch() > 45 || player.getPitch() < -45
                ? Direction.Axis.Y : player.getHorizontalFacing().getAxis();
        float originHardness = originState.getHardness(world, origin);
        breakingArea = true;
        try {
            for (int a = -1; a <= 1; a++) {
                for (int b = -1; b <= 1; b++) {
                    if (a == 0 && b == 0) {
                        continue;
                    }
                    if (!hasEnergyForBlock(stack)) {
                        return;
                    }
                    BlockPos target = switch (axis) {
                        case X -> origin.add(0, a, b);
                        case Y -> origin.add(a, 0, b);
                        case Z -> origin.add(a, b, 0);
                    };
                    BlockState targetState = world.getBlockState(target);
                    float hardness = targetState.getHardness(world, target);
                    // Skip air, unbreakable blocks and blocks much harder than the one mined.
                    if (targetState.isAir() || hardness < 0 || hardness > originHardness + 1.5f
                            || !super.isCorrectForDrops(stack, targetState)) {
                        continue;
                    }
                    // Goes through the normal break path: respects spawn protection, drops, XP and postMine (energy cost).
                    player.interactionManager.tryBreakBlock(target);
                }
            }
        } finally {
            breakingArea = false;
        }
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!user.isSneaking()) {
            return TypedActionResult.pass(stack);
        }
        if (!world.isClient) {
            boolean area = !isAreaMode(stack);
            stack.set(ModComponents.AREA_MODE, area);
            user.sendMessage(Text.translatable(area ? "message.echo_technology.drill.area" : "message.echo_technology.drill.single")
                    .formatted(Formatting.AQUA), true);
            world.playSound(null, user.getBlockPos(), SoundEvents.BLOCK_SCULK_SENSOR_CLICKING, SoundCategory.PLAYERS, 0.7f, area ? 1.4f : 0.9f);
        }
        return TypedActionResult.success(stack, world.isClient);
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
        tooltip.add(energyTooltip(stack));
        tooltip.add(Text.translatable(isAreaMode(stack) ? "tooltip.echo_technology.drill.mode_area" : "tooltip.echo_technology.drill.mode_single")
                .formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("tooltip.echo_technology.drill.cost", energyPerBlock()).formatted(Formatting.DARK_GRAY));
        tooltip.add(Text.translatable("tooltip.echo_technology.drill.toggle").formatted(Formatting.DARK_GRAY));
    }
}
