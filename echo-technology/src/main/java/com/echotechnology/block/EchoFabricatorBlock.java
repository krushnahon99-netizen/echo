package com.echotechnology.block;

import com.echotechnology.block.entity.EchoFabricatorBlockEntity;
import com.echotechnology.recipe.FabricatorRecipe;
import com.echotechnology.recipe.FabricatorRecipes;
import com.echotechnology.registry.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EchoFabricatorBlock extends MachineBlock {
    public static final MapCodec<EchoFabricatorBlock> CODEC = createCodec(EchoFabricatorBlock::new);

    public EchoFabricatorBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new EchoFabricatorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return world.isClient ? null : validateTicker(type, ModBlockEntities.ECHO_FABRICATOR, EchoFabricatorBlockEntity::serverTick);
    }

    /** Lists the built-in recipe book so players can discover recipes without a recipe viewer. */
    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType options) {
        tooltip.add(Text.translatable("tooltip.echo_technology.fabricator.recipes").formatted(Formatting.DARK_AQUA));
        for (FabricatorRecipe recipe : FabricatorRecipes.ALL) {
            MutableText line = Text.literal(" ")
                    .append(ingredient(recipe.first().item(), recipe.first().count()))
                    .append(" + ")
                    .append(ingredient(recipe.second().item(), recipe.second().count()))
                    .append(" \u2192 ")
                    .append(ingredient(recipe.outputItem(), recipe.outputCount()));
            tooltip.add(line.formatted(Formatting.GRAY));
        }
    }

    private static MutableText ingredient(String itemId, int count) {
        Item item = Registries.ITEM.get(Identifier.of(itemId));
        MutableText name = Text.empty().append(item.getName());
        return count > 1 ? Text.literal(count + "x ").append(name) : name;
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (state.get(LIT) && random.nextInt(2) == 0) {
            world.addParticle(ParticleTypes.ELECTRIC_SPARK,
                    pos.getX() + 0.2 + random.nextDouble() * 0.6, pos.getY() + 1.01, pos.getZ() + 0.2 + random.nextDouble() * 0.6,
                    0.0, 0.02, 0.0);
        }
    }
}
