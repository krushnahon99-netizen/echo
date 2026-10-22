package com.echotechnology.block.entity;

import com.echotechnology.config.EchoConfig;
import com.echotechnology.energy.EchoEnergyStorage;
import com.echotechnology.machine.FabricatorCore;
import com.echotechnology.recipe.FabricatorRecipe;
import com.echotechnology.recipe.FabricatorRecipes;
import com.echotechnology.registry.ModBlockEntities;
import com.echotechnology.screen.FabricatorScreenHandler;
import com.echotechnology.screen.SyncedInts;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class EchoFabricatorBlockEntity extends MachineBlockEntity {
    public static final int INPUT_A = FabricatorCore.INPUT_A;
    public static final int INPUT_B = FabricatorCore.INPUT_B;
    public static final int OUTPUT = FabricatorCore.OUTPUT;
    public static final int[] UPGRADE_SLOTS = {3, 4};
    public static final int SLOT_COUNT = 5;
    public static final int PROPERTY_COUNT = 8;
    private static final int[] INPUT_SLOTS = {INPUT_A, INPUT_B};
    private static final int[] OUTPUT_SLOTS = {OUTPUT};

    private final FabricatorCore core = new FabricatorCore();
    private int duration;
    private int energyPerTick;

    private final FabricatorCore.Slots slotView = new FabricatorCore.Slots() {
        @Override
        public String itemId(int slot) {
            ItemStack stack = items.get(slot);
            // A customised output stack (renamed, enchanted...) must not be merged into.
            if (slot == OUTPUT && !stack.isEmpty()
                    && !ItemStack.areItemsAndComponentsEqual(stack, new ItemStack(stack.getItem()))) {
                return "!" + idOf(stack);
            }
            return idOf(stack);
        }

        @Override
        public int count(int slot) {
            return items.get(slot).getCount();
        }

        @Override
        public int maxStackSize(String itemId) {
            return itemOf(itemId).getMaxCount();
        }

        @Override
        public void remove(int slot, int amount) {
            items.get(slot).decrement(amount);
        }

        @Override
        public void addToOutput(String itemId, int amount) {
            ItemStack output = items.get(OUTPUT);
            if (output.isEmpty()) {
                items.set(OUTPUT, new ItemStack(itemOf(itemId), amount));
            } else {
                output.increment(amount);
            }
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
                case 4 -> core.progress();
                case 5 -> duration;
                case 6 -> core.lastStatus().ordinal();
                case 7 -> energyPerTick;
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

    public EchoFabricatorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ECHO_FABRICATOR, pos, state, SLOT_COUNT,
                new EchoEnergyStorage(EchoConfig.get().fabricatorCapacity, EchoConfig.get().fabricatorInputPerTick, 0));
    }

    private static Item itemOf(String itemId) {
        return Registries.ITEM.get(Identifier.of(itemId));
    }

    public static boolean isIngredient(ItemStack stack) {
        String id = idOf(stack);
        return !id.isEmpty() && FabricatorRecipes.ALL.stream()
                .anyMatch(r -> r.first().item().equals(id) || r.second().item().equals(id));
    }

    public static void serverTick(World world, BlockPos pos, BlockState state, EchoFabricatorBlockEntity fabricator) {
        fabricator.refreshUpgrades();
        EchoConfig config = EchoConfig.get();
        FabricatorCore.Status status = fabricator.core.tick(fabricator.slotView, fabricator.energy,
                fabricator.getUpgrades(), config.fabricatorSpeedMultiplier, config.fabricatorEnergyMultiplier);

        Optional<FabricatorRecipe> recipe = FabricatorRecipes.byId(fabricator.core.activeRecipe());
        fabricator.duration = recipe.map(r -> FabricatorCore.duration(r, fabricator.getUpgrades(), config.fabricatorSpeedMultiplier)).orElse(0);
        fabricator.energyPerTick = recipe.map(r -> FabricatorCore.energyPerTick(r, fabricator.getUpgrades(),
                config.fabricatorSpeedMultiplier, config.fabricatorEnergyMultiplier)).orElse(0);

        if (status == FabricatorCore.Status.COMPLETED) {
            fabricator.markDirty();
            world.playSound(null, pos, SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.BLOCKS, 0.8f, 1.4f);
            if (world instanceof ServerWorld serverWorld) {
                serverWorld.spawnParticles(ParticleTypes.SCULK_CHARGE_POP, pos.getX() + 0.5, pos.getY() + 1.05, pos.getZ() + 0.5,
                        6, 0.25, 0.05, 0.25, 0.01);
            }
        } else if (status == FabricatorCore.Status.WORKING) {
            fabricator.markDirty();
        }
        fabricator.setLit(status == FabricatorCore.Status.WORKING || status == FabricatorCore.Status.COMPLETED);
    }

    @Override
    protected int baseCapacity() {
        return EchoConfig.get().fabricatorCapacity;
    }

    @Override
    protected int[] upgradeSlots() {
        return UPGRADE_SLOTS;
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        if (slot == INPUT_A || slot == INPUT_B) {
            return isIngredient(stack);
        }
        return slot != OUTPUT && isUpgrade(stack);
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return side == Direction.DOWN ? OUTPUT_SLOTS : INPUT_SLOTS;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        if ((slot != INPUT_A && slot != INPUT_B) || !isIngredient(stack)) {
            return false;
        }
        // Keep the two inputs distinct so hoppers can feed a two-ingredient recipe.
        ItemStack other = items.get(slot == INPUT_A ? INPUT_B : INPUT_A);
        ItemStack current = items.get(slot);
        return !current.isEmpty() || other.isEmpty() || !ItemStack.areItemsEqual(other, stack);
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return slot == OUTPUT && dir == Direction.DOWN;
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        nbt.putString("Recipe", core.activeRecipe());
        nbt.putInt("Progress", core.progress());
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        core.load(nbt.getString("Recipe"), nbt.getInt("Progress"));
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new FabricatorScreenHandler(syncId, playerInventory, this, properties);
    }
}
