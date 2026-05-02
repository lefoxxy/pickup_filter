package com.pocketfilter.network;

import com.pocketfilter.data.FilterMode;
import com.pocketfilter.data.PickupFilterPlayerData;
import com.pocketfilter.menu.PickupFilterMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class ToggleFilterModePacket {
    public ToggleFilterModePacket() {
    }

    public ToggleFilterModePacket(FriendlyByteBuf buffer) {
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

            FilterMode nextMode = PickupFilterPlayerData.getMode(player) == FilterMode.WHITELIST
                    ? FilterMode.BLACKLIST
                    : FilterMode.WHITELIST;
            PickupFilterPlayerData.setMode(player, nextMode);
            menu.setLocalMode(nextMode);
            menu.broadcastChanges();
            PickupFilterNetwork.syncTo(player);
        });
        context.setPacketHandled(true);
    }
}
