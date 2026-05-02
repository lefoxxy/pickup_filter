package com.pickupfilter.network;

import com.pickupfilter.menu.PickupFilterMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class ClearFilterSlotPacket {
    private final int slot;

    public ClearFilterSlotPacket(int slot) {
        this.slot = slot;
    }

    public ClearFilterSlotPacket(FriendlyByteBuf buffer) {
        this(buffer.readVarInt());
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(this.slot);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || !(player.containerMenu instanceof PickupFilterMenu menu) || !isValidSlot(this.slot)) {
                return;
            }

            menu.clearFilterReferenceFromServer(this.slot);
            PickupFilterNetwork.syncTo(player);
        });
        context.setPacketHandled(true);
    }

    private static boolean isValidSlot(int slot) {
        return slot >= 0 && slot < PickupFilterMenu.FILTER_SLOT_COUNT;
    }
}
