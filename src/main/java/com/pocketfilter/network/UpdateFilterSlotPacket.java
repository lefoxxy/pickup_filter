package com.pocketfilter.network;

import com.pocketfilter.menu.PickupFilterMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public final class UpdateFilterSlotPacket {
    private final int slot;
    private final ResourceLocation itemId;

    public UpdateFilterSlotPacket(int slot, ResourceLocation itemId) {
        this.slot = slot;
        this.itemId = itemId;
    }

    public UpdateFilterSlotPacket(FriendlyByteBuf buffer) {
        this(buffer.readVarInt(), ResourceLocation.tryParse(buffer.readUtf(256)));
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(this.slot);
        buffer.writeUtf(this.itemId == null ? "" : this.itemId.toString());
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || !(player.containerMenu instanceof PickupFilterMenu menu) || !isValidSlot(this.slot)) {
                return;
            }

            ItemStack reference = toReferenceStack(this.itemId);
            if (reference.isEmpty()) {
                return;
            }

            menu.setFilterReferenceFromServer(this.slot, reference);
            PickupFilterNetwork.syncTo(player);
        });
        context.setPacketHandled(true);
    }

    private static boolean isValidSlot(int slot) {
        return slot >= 0 && slot < PickupFilterMenu.FILTER_SLOT_COUNT;
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
