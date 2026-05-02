package com.pocketfilter.menu;

import com.pocketfilter.data.PickupFilterPlayerData;
import com.pocketfilter.data.FilterMode;
import com.pocketfilter.network.PickupFilterNetwork;
import com.pocketfilter.registry.PickupFilterMenus;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class PickupFilterMenu extends AbstractContainerMenu {
    public static final int FILTER_SLOT_COUNT = PickupFilterPlayerData.SLOT_COUNT;

    private static final int PLAYER_INVENTORY_START = FILTER_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_ROWS = 3;
    private static final int PLAYER_INVENTORY_COLUMNS = 9;
    private static final int PLAYER_HOTBAR_SIZE = 9;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_ROWS * PLAYER_INVENTORY_COLUMNS + PLAYER_HOTBAR_SIZE;

    private final Player player;
    private final FilterReferenceContainer referenceContainer;
    private final DataSlot modeData;
    private final DataSlot feedbackData;

    public PickupFilterMenu(int containerId, Inventory inventory) {
        super(PickupFilterMenus.PICKUP_FILTER.get(), containerId);
        this.player = inventory.player;
        this.referenceContainer = new FilterReferenceContainer(this.player);
        this.modeData = addDataSlot(new DataSlot() {
            private int clientValue = FilterMode.BLACKLIST.ordinal();

            @Override
            public int get() {
                if (PickupFilterMenu.this.player.level().isClientSide) {
                    return this.clientValue;
                }

                return PickupFilterPlayerData.getMode(PickupFilterMenu.this.player).ordinal();
            }

            @Override
            public void set(int value) {
                this.clientValue = value;
                if (!PickupFilterMenu.this.player.level().isClientSide && value >= 0 && value < FilterMode.values().length) {
                    PickupFilterPlayerData.setMode(PickupFilterMenu.this.player, FilterMode.values()[value]);
                }
            }
        });
        this.feedbackData = addDataSlot(new DataSlot() {
            private int clientValue = 1;

            @Override
            public int get() {
                if (PickupFilterMenu.this.player.level().isClientSide) {
                    return this.clientValue;
                }

                return PickupFilterPlayerData.isFeedbackEnabled(PickupFilterMenu.this.player) ? 1 : 0;
            }

            @Override
            public void set(int value) {
                this.clientValue = value;
                if (!PickupFilterMenu.this.player.level().isClientSide) {
                    PickupFilterPlayerData.setFeedbackEnabled(PickupFilterMenu.this.player, value != 0);
                }
            }
        });

        addFilterReferenceSlots();
        addPlayerInventorySlots(inventory);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        if (!isPlayerInventorySlot(slotIndex)) {
            return ItemStack.EMPTY;
        }

        ItemStack selectedStack = this.slots.get(slotIndex).getItem();
        if (selectedStack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        setFirstAvailableReference(selectedStack);
        return ItemStack.EMPTY;
    }

    @Override
    public void clicked(int slotIndex, int button, ClickType clickType, Player player) {
        if (isFilterSlot(slotIndex)) {
            handleReferenceSlotClick(slotIndex, button, clickType, player);
            return;
        }

        super.clicked(slotIndex, button, clickType, player);
    }

    @Override
    public boolean stillValid(Player player) {
        return this.referenceContainer.stillValid(player);
    }

    @Override
    public boolean canDragTo(Slot slot) {
        return !(slot instanceof FilterReferenceSlot) && super.canDragTo(slot);
    }

    public FilterMode getMode() {
        int modeIndex = this.modeData.get();
        if (modeIndex < 0 || modeIndex >= FilterMode.values().length) {
            return FilterMode.BLACKLIST;
        }

        return FilterMode.values()[modeIndex];
    }

    public boolean isFeedbackEnabled() {
        return this.feedbackData.get() != 0;
    }

    public void setLocalMode(FilterMode mode) {
        this.modeData.set(mode.ordinal());
    }

    public void setLocalFeedbackEnabled(boolean enabled) {
        this.feedbackData.set(enabled ? 1 : 0);
    }

    public ItemStack getPlayerInventoryItem(int slot) {
        if (slot < 0 || slot >= this.player.getInventory().getContainerSize()) {
            return ItemStack.EMPTY;
        }

        return this.player.getInventory().getItem(slot);
    }

    public void setFilterReferenceFromServer(int slotIndex, ItemStack stack) {
        setReferenceSlot(slotIndex, stack, true);
    }

    public void clearFilterReferenceFromServer(int slotIndex) {
        setReferenceSlot(slotIndex, ItemStack.EMPTY, true);
    }

    public void applyServerState(List<ItemStack> references, FilterMode mode, boolean feedbackEnabled) {
        for (int slot = 0; slot < FILTER_SLOT_COUNT && slot < references.size(); slot++) {
            setLocalFilterReference(slot, references.get(slot));
        }

        setLocalMode(mode);
        setLocalFeedbackEnabled(feedbackEnabled);
    }

    public void setLocalFilterReference(int slotIndex, ItemStack stack) {
        if (!isFilterSlot(slotIndex)) {
            return;
        }

        ItemStack reference = stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
        if (!reference.isEmpty()) {
            reference.setCount(1);
        }

        this.referenceContainer.setLocalReference(slotIndex, reference);
        this.slots.get(slotIndex).setChanged();
    }

    private void addFilterReferenceSlots() {
        int startX = 44;
        int startY = 18;

        for (int slot = 0; slot < FILTER_SLOT_COUNT; slot++) {
            int x = startX + (slot % 5) * 18;
            int y = startY + (slot / 5) * 18;
            this.addSlot(new FilterReferenceSlot(this.referenceContainer, slot, x, y));
        }
    }

    private void addPlayerInventorySlots(Inventory inventory) {
        int startX = 8;
        int startY = 108;

        for (int row = 0; row < PLAYER_INVENTORY_ROWS; row++) {
            for (int column = 0; column < PLAYER_INVENTORY_COLUMNS; column++) {
                int index = column + row * PLAYER_INVENTORY_COLUMNS + PLAYER_HOTBAR_SIZE;
                int x = startX + column * 18;
                int y = startY + row * 18;
                this.addSlot(new Slot(inventory, index, x, y));
            }
        }

        int hotbarY = 166;
        for (int column = 0; column < PLAYER_HOTBAR_SIZE; column++) {
            this.addSlot(new Slot(inventory, column, startX + column * 18, hotbarY));
        }
    }

    private void handleReferenceSlotClick(int slotIndex, int button, ClickType clickType, Player player) {
        if (player != this.player) {
            return;
        }

        switch (clickType) {
            case PICKUP, QUICK_MOVE -> {
                ItemStack carriedStack = getCarried();
                setReferenceSlot(slotIndex, carriedStack);
            }
            case SWAP -> setReferenceSlot(slotIndex, getHotbarStack(button));
            case CLONE -> {
                if (player.getAbilities().instabuild) {
                    setReferenceSlot(slotIndex, getCarried());
                }
            }
            default -> {
            }
        }
    }

    private void setFirstAvailableReference(ItemStack stack) {
        int matchingSlot = findMatchingReferenceSlot(stack);
        if (matchingSlot >= 0) {
            setReferenceSlot(matchingSlot, stack);
            return;
        }

        int emptySlot = findEmptyReferenceSlot();
        if (emptySlot >= 0) {
            setReferenceSlot(emptySlot, stack);
        }
    }

    private int findMatchingReferenceSlot(ItemStack stack) {
        for (int slot = 0; slot < FILTER_SLOT_COUNT; slot++) {
            ItemStack reference = this.referenceContainer.getItem(slot);
            if (!reference.isEmpty() && reference.is(stack.getItem())) {
                return slot;
            }
        }

        return -1;
    }

    private int findEmptyReferenceSlot() {
        for (int slot = 0; slot < FILTER_SLOT_COUNT; slot++) {
            if (this.referenceContainer.getItem(slot).isEmpty()) {
                return slot;
            }
        }

        return -1;
    }

    private ItemStack getHotbarStack(int hotbarSlot) {
        if (hotbarSlot < 0 || hotbarSlot >= PLAYER_HOTBAR_SIZE) {
            return ItemStack.EMPTY;
        }

        return this.player.getInventory().getItem(hotbarSlot);
    }

    private void setReferenceSlot(int slotIndex, ItemStack stack) {
        setReferenceSlot(slotIndex, stack, false);
    }

    private void setReferenceSlot(int slotIndex, ItemStack stack, boolean fromNetworkPacket) {
        if (!isFilterSlot(slotIndex)) {
            return;
        }

        ItemStack reference = stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
        if (!reference.isEmpty()) {
            reference.setCount(1);
        }

        this.referenceContainer.setItem(slotIndex, reference);
        this.slots.get(slotIndex).setChanged();
        this.broadcastChanges();
        if (!this.player.level().isClientSide && !fromNetworkPacket && this.player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            PickupFilterNetwork.syncTo(serverPlayer);
        }
    }

    private static boolean isFilterSlot(int slotIndex) {
        return slotIndex >= 0 && slotIndex < FILTER_SLOT_COUNT;
    }

    private static boolean isPlayerInventorySlot(int slotIndex) {
        return slotIndex >= PLAYER_INVENTORY_START && slotIndex < PLAYER_INVENTORY_START + PLAYER_INVENTORY_SLOT_COUNT;
    }
}
