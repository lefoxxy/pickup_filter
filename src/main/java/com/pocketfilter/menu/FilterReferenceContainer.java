package com.pocketfilter.menu;

import com.pocketfilter.data.PickupFilterPlayerData;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

final class FilterReferenceContainer implements Container {
    private final Player player;
    private final NonNullList<ItemStack> references;

    FilterReferenceContainer(Player player) {
        this.player = player;
        this.references = NonNullList.withSize(PickupFilterPlayerData.SLOT_COUNT, ItemStack.EMPTY);
        List<ItemStack> savedReferences = PickupFilterPlayerData.getFilterSlots(player);

        for (int slot = 0; slot < this.references.size(); slot++) {
            ItemStack stack = savedReferences.get(slot);
            this.references.set(slot, normalizeReference(stack));
        }
    }

    @Override
    public int getContainerSize() {
        return this.references.size();
    }

    @Override
    public boolean isEmpty() {
        return this.references.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.references.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (slot < 0 || slot >= this.references.size()) {
            return ItemStack.EMPTY;
        }

        ItemStack removed = ContainerHelper.removeItem(this.references, slot, amount);
        this.setChanged();
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (slot < 0 || slot >= this.references.size()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = this.references.get(slot);
        this.references.set(slot, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot < 0 || slot >= this.references.size()) {
            return;
        }

        ItemStack reference = normalizeReference(stack);
        this.references.set(slot, reference);
        PickupFilterPlayerData.setFilterSlot(this.player, slot, reference);
    }

    void setLocalReference(int slot, ItemStack stack) {
        if (slot < 0 || slot >= this.references.size()) {
            return;
        }

        this.references.set(slot, normalizeReference(stack));
    }

    @Override
    public void setChanged() {
        for (int slot = 0; slot < this.references.size(); slot++) {
            PickupFilterPlayerData.setFilterSlot(this.player, slot, this.references.get(slot));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return player == this.player && !player.isRemoved();
    }

    @Override
    public void clearContent() {
        for (int slot = 0; slot < this.references.size(); slot++) {
            this.setItem(slot, ItemStack.EMPTY);
        }
    }

    private static ItemStack normalizeReference(ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack reference = stack.copy();
        reference.setCount(1);
        return reference;
    }
}
