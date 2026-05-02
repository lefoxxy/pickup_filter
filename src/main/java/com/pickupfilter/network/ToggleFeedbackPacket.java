package com.pickupfilter.network;

import com.pickupfilter.data.PickupFilterPlayerData;
import com.pickupfilter.menu.PickupFilterMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class ToggleFeedbackPacket {
    public ToggleFeedbackPacket() {
    }

    public ToggleFeedbackPacket(FriendlyByteBuf buffer) {
    }

    public void encode(FriendlyByteBuf buffer) {
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || !(player.containerMenu instanceof PickupFilterMenu menu)) {
                return;
            }

            boolean nextValue = !PickupFilterPlayerData.isFeedbackEnabled(player);
            PickupFilterPlayerData.setFeedbackEnabled(player, nextValue);
            menu.setLocalFeedbackEnabled(nextValue);
            menu.broadcastChanges();
            PickupFilterNetwork.syncTo(player);
        });
        context.setPacketHandled(true);
    }
}
