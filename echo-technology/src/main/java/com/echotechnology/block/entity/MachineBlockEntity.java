package com.echotechnology.block.entity;

import com.echotechnology.block.MachineBlock;
import com.echotechnology.energy.EchoEnergyStorage;
import com.echotechnology.energy.EnergyNode;
import com.echotechnology.item.UpgradeItem;
import com.echotechnology.upgrade.UpgradeModifiers;
import com.echotechnology.upgrade.UpgradeType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;

import java.util.EnumMap;
import java.util.Map;

/**
 * Base class for inventory-holding Echo machines: owns the item list, the
 * energy buffer and the installed-upgrade summary, and persists all of them.
 */
public abstract class MachineBlockEntity extends BlockEntity implements SidedInventory, NamedScreenHandlerFactory, EnergyNode {
    protected final DefaultedList<ItemStack> items;
    protected final EchoEnergyStorage energy;
    private UpgradeModifiers upgrades = UpgradeModifiers.NONE;

    protected MachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int size, EchoEnergyStorage energy) {
        super(type, pos, state);
        this.items = DefaultedList.ofSize(size, ItemStack.EMPTY);
        this.energy = energy;
        this.energy.setChangeListener(this::markDirty);
    }

    protected abstract int baseCapacity();

    protected abstract int[] upgradeSlots();

    public static boolean isUpgrade(ItemStack stack) {
        return stack.getItem() instanceof UpgradeItem;
    }

    /** Registry id of a stack's item, or "" for empty stacks. */
    public static String idOf(ItemStack stack) {
        return stack.isEmpty() ? "" : Registries.ITEM.getId(stack.getItem()).toString();
    }

    /** Re-reads the upgrade slots and applies capacity changes to the energy buffer. */
    protected void refreshUpgrades() {
        Map<UpgradeType, Integer> counts = new EnumMap<>(UpgradeType.class);
        for (int slot : upgradeSlots()) {
            ItemStack stack = items.get(slot);
            if (stack.getItem() instanceof UpgradeItem upgrade) {
                counts.merge(upgrade.getType(), stack.getCount(), Integer::sum);
            }
        }
        upgrades = UpgradeModifiers.fromCounts(counts);
        energy.setCapacity(upgrades.scaleCapacity(baseCapacity()));
    }

    public UpgradeModifiers getUpgrades() {
        return upgrades;
    }

    @Override
    public EchoEnergyStorage getEnergyStorage() {
        return energy;
    }

    protected void setLit(boolean lit) {
        if (world == null) {
            return;
        }
        BlockState state = getCachedState();
        if (state.contains(MachineBlock.LIT) && state.get(MachineBlock.LIT) != lit) {
            world.setBlockState(pos, state.with(MachineBlock.LIT, lit), Block.NOTIFY_ALL);
        }
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable(getCachedState().getBlock().getTranslationKey());
    }

    // --- Persistence ---------------------------------------------------------

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        Inventories.writeNbt(nbt, items, registryLookup);
        nbt.putInt("Energy", energy.getEnergy());
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        for (int i = 0; i < items.size(); i++) {
            items.set(i, ItemStack.EMPTY);
        }
        Inventories.readNbt(nbt, items, registryLookup);
        // Capacity depends on installed upgrades, so restore it before the energy value.
        refreshUpgrades();
        energy.setEnergy(nbt.getInt("Energy"));
    }

    // --- Inventory -----------------------------------------------------------

    @Override
    public int size() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        return items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getStack(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack result = Inventories.splitStack(items, slot, amount);
        if (!result.isEmpty()) {
            markDirty();
        }
        return result;
    }

    @Override
    public ItemStack removeStack(int slot) {
        return Inventories.removeStack(items, slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        items.set(slot, stack);
        stack.capCount(getMaxCount(stack));
        markDirty();
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return Inventory.canPlayerUse(this, player);
    }

    @Override
    public void clear() {
        items.clear();
        markDirty();
    }
}
