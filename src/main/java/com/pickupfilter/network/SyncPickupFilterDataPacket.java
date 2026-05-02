package com.pickupfilter.network;

import com.pickupfilter.data.FilterMode;
import com.pickupfilter.data.PickupFilterPlayerData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class SyncPickupFilterDataPacket {
    private final List<ItemStack> slots;
    private final FilterMode mode;
    private final boolean feedbackEnabled;

    public SyncPickupFilterDataPacket(List<ItemStack> slots, FilterMode mode, boolean feedbackEnabled) {
        this.slots = normalizeSlots(slots);
        this.mode = mode;
        this.feedbackEnabled = feedbackEnabled;
    }

    public SyncPickupFilterDataPacket(FriendlyByteBuf buffer) {
        List<ItemStack> decodedSlots = new ArrayList<>(PickupFilterPlayerData.SLOT_COUNT);
        for (int slot = 0; slot < PickupFilterPlayerData.SLOT_COUNT; slot++) {
            String itemId = buffer.readUtf(256);
            decodedSlots.add(toReferenceStack(ResourceLocation.tryParse(itemId)));
        }

        this.slots = decodedSlots;
        this.mode = buffer.readEnum(FilterMode.class);
        this.feedbackEnabled = buffer.readBoolean();
    }

    public static SyncPickupFilterDataPacket from(ServerPlayer player) {
        return new SyncPickupFilterDataPacket(
                PickupFilterPlayerData.getFilterSlots(player),
                PickupFilterPlayerData.getMode(player),
                PickupFilterPlayerData.isFeedbackEnabled(player)
        );
    }

    public void encode(FriendlyByteBuf buffer) {
        for (ItemStack stack : this.slots) {
            ResourceLocation itemId = stack.isEmpty() ? null : ForgeRegistries.ITEMS.getKey(stack.getItem());
            buffer.writeUtf(itemId == null ? "" : itemId.toString());
        }

        buffer.writeEnum(this.mode);
        buffer.writeBoolean(this.feedbackEnabled);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> com.pickupfilter.client.PickupFilterClientPacketHandler.handleSync(this.slots, this.mode, this.feedbackEnabled)
        ));
        context.setPacketHandled(true);
    }

    private static List<ItemStack> normalizeSlots(List<ItemStack> slots) {
        List<ItemStack> normalizedSlots = new ArrayList<>(PickupFilterPlayerData.SLOT_COUNT);
        for (int slot = 0; slot < PickupFilterPlayerData.SLOT_COUNT; slot++) {
            ItemStack stack = slot < slots.size() ? slots.get(slot) : ItemStack.EMPTY;
            if (stack.isEmpty()) {
                normalizedSlots.add(ItemStack.EMPTY);
                continue;
            }

            ItemStack reference = stack.copy();
            reference.setCount(1);
            normalizedSlots.add(reference);
        }

        return normalizedSlots;
    }

    private static ItemStack toReferenceStack(ResourceLocation itemId) {
        if (itemId == null) {
            return ItemStack.EMPTY;
        }

        Item item = ForgeRegistries.ITEMS.getValue(itemId);
        if (item == null || item == Items.AIR) {
            return ItemStack.EMPTY;
        }

        return new ItemStack(item, 1);
    }
}
