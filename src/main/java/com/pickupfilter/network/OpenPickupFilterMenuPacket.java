package com.pickupfilter.network;

import com.pickupfilter.menu.PickupFilterMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;

import java.util.function.Supplier;

public final class OpenPickupFilterMenuPacket {
    public OpenPickupFilterMenuPacket() {
    }

    public OpenPickupFilterMenuPacket(FriendlyByteBuf buffer) {
    }

    public void encode(FriendlyByteBuf buffer) {
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }

            NetworkHooks.openScreen(player, new SimpleMenuProvider(
                    (containerId, inventory, menuPlayer) -> new PickupFilterMenu(containerId, inventory),
                    Component.translatable("screen.pickupfilter.title")
            ));
            PickupFilterNetwork.syncTo(player);
        });
        context.setPacketHandled(true);
    }
}
