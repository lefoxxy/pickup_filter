package com.pocketfilter.data;

import com.pocketfilter.PickupFilter;
import com.pocketfilter.config.CommonConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class PickupFilterPlayerData {
    public static final int SLOT_COUNT = 15;

    private static final String ROOT_KEY = PickupFilter.MOD_ID;
    private static final String SLOTS_KEY = "filterSlots";
    private static final String MODE_KEY = "mode";
    private static final String FEEDBACK_KEY = "feedbackEnabled";

    private PickupFilterPlayerData() {
    }

    public static List<ItemStack> getFilterSlots(Player player) {
        CompoundTag data = getOrCreateData(player);
        ListTag savedSlots = data.getList(SLOTS_KEY, Tag.TAG_STRING);
        List<ItemStack> slots = new ArrayList<>(SLOT_COUNT);

        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            if (slot >= savedSlots.size()) {
                slots.add(ItemStack.EMPTY);
                continue;
            }

            Optional<Item> item = getItem(savedSlots.getString(slot));
            if (item.isEmpty()) {
                slots.add(ItemStack.EMPTY);
                continue;
            }

            slots.add(new ItemStack(item.get(), 1));
        }

        return slots;
    }

    public static void setFilterSlot(Player player, int slot, ItemStack stack) {
        validateSlot(slot);
        CompoundTag data = getOrCreateData(player);
        ListTag savedSlots = getNormalizedSlotList(data);

        String itemId = "";
        if (!stack.isEmpty()) {
            ItemStack reference = stack.copy();
            reference.setCount(1);
            ResourceLocation itemKey = ForgeRegistries.ITEMS.getKey(reference.getItem());
            itemId = itemKey == null ? "" : itemKey.toString();
        }

        savedSlots.set(slot, StringTag.valueOf(itemId));
        data.put(SLOTS_KEY, savedSlots);
    }

    public static FilterMode getMode(Player player) {
        String savedMode = getOrCreateData(player).getString(MODE_KEY);
        try {
            return FilterMode.valueOf(savedMode);
        } catch (IllegalArgumentException ignored) {
            return CommonConfig.DEFAULT_MODE.get();
        }
    }

    public static void setMode(Player player, FilterMode mode) {
        getOrCreateData(player).putString(MODE_KEY, mode.name());
    }

    public static boolean isFeedbackEnabled(Player player) {
        CompoundTag data = getOrCreateData(player);
        if (!data.contains(FEEDBACK_KEY, Tag.TAG_BYTE)) {
            return true;
        }

        return data.getBoolean(FEEDBACK_KEY);
    }

    public static void setFeedbackEnabled(Player player, boolean enabled) {
        getOrCreateData(player).putBoolean(FEEDBACK_KEY, enabled);
    }

    public static boolean matchesItem(Player player, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        boolean listed = getFilterSlots(player).stream()
                .filter(slot -> !slot.isEmpty())
                .anyMatch(slot -> slot.is(stack.getItem()));

        return switch (getMode(player)) {
            case WHITELIST -> listed;
            case BLACKLIST -> !listed;
        };
    }

    public static void copy(Player original, Player target) {
        CompoundTag originalData = original.getPersistentData();
        if (!originalData.contains(ROOT_KEY, Tag.TAG_COMPOUND)) {
            return;
        }

        target.getPersistentData().put(ROOT_KEY, originalData.getCompound(ROOT_KEY).copy());
    }

    private static CompoundTag getOrCreateData(Player player) {
        CompoundTag persistentData = player.getPersistentData();
        if (!persistentData.contains(ROOT_KEY, Tag.TAG_COMPOUND)) {
            CompoundTag data = new CompoundTag();
            data.put(SLOTS_KEY, emptySlotList());
            data.putString(MODE_KEY, CommonConfig.DEFAULT_MODE.get().name());
            data.putBoolean(FEEDBACK_KEY, CommonConfig.DEFAULT_FEEDBACK_ENABLED.get());
            persistentData.put(ROOT_KEY, data);
        }

        CompoundTag data = persistentData.getCompound(ROOT_KEY);
        ensureDefaults(data);
        return data;
    }

    private static void ensureDefaults(CompoundTag data) {
        if (!data.contains(SLOTS_KEY, Tag.TAG_LIST)) {
            data.put(SLOTS_KEY, emptySlotList());
        } else if (data.getList(SLOTS_KEY, Tag.TAG_STRING).size() != SLOT_COUNT) {
            data.put(SLOTS_KEY, getNormalizedSlotList(data));
        }

        if (!data.contains(MODE_KEY, Tag.TAG_STRING)) {
            data.putString(MODE_KEY, CommonConfig.DEFAULT_MODE.get().name());
        }

        if (!data.contains(FEEDBACK_KEY, Tag.TAG_BYTE)) {
            data.putBoolean(FEEDBACK_KEY, CommonConfig.DEFAULT_FEEDBACK_ENABLED.get());
        }
    }

    private static ListTag getNormalizedSlotList(CompoundTag data) {
        ListTag existingSlots = data.getList(SLOTS_KEY, Tag.TAG_STRING);
        ListTag normalizedSlots = new ListTag();

        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            String itemId = slot < existingSlots.size() ? existingSlots.getString(slot) : "";
            normalizedSlots.add(StringTag.valueOf(itemId));
        }

        return normalizedSlots;
    }

    private static ListTag emptySlotList() {
        ListTag slots = new ListTag();
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            slots.add(StringTag.valueOf(""));
        }

        return slots;
    }

    private static Optional<Item> getItem(String itemId) {
        if (itemId.isBlank()) {
            return Optional.empty();
        }

        ResourceLocation location = ResourceLocation.tryParse(itemId);
        if (location == null) {
            return Optional.empty();
        }

        Item item = ForgeRegistries.ITEMS.getValue(location);
        if (item == null || item == Items.AIR) {
            return Optional.empty();
        }

        return Optional.of(item);
    }

    private static void validateSlot(int slot) {
        if (slot < 0 || slot >= SLOT_COUNT) {
            throw new IndexOutOfBoundsException("Pocket Filter slot must be between 0 and " + (SLOT_COUNT - 1));
        }
    }
}
