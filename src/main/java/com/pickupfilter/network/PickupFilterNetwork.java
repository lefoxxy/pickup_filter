package com.pickupfilter.network;

import com.pickupfilter.PickupFilter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class PickupFilterNetwork {
    private static final String PROTOCOL_VERSION = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(PickupFilter.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int nextPacketId;

    private PickupFilterNetwork() {
    }

    public static void register() {
        CHANNEL.registerMessage(
                nextPacketId++,
                OpenPickupFilterMenuPacket.class,
                OpenPickupFilterMenuPacket::encode,
                OpenPickupFilterMenuPacket::new,
                OpenPickupFilterMenuPacket::handle
        );
        CHANNEL.registerMessage(
                nextPacketId++,
                ToggleFilterModePacket.class,
                ToggleFilterModePacket::encode,
                ToggleFilterModePacket::new,
                ToggleFilterModePacket::handle
        );
        CHANNEL.registerMessage(
                nextPacketId++,
                ToggleFeedbackPacket.class,
                ToggleFeedbackPacket::encode,
                ToggleFeedbackPacket::new,
                ToggleFeedbackPacket::handle
        );
        CHANNEL.registerMessage(
                nextPacketId++,
                UpdateFilterSlotPacket.class,
                UpdateFilterSlotPacket::encode,
                UpdateFilterSlotPacket::new,
                UpdateFilterSlotPacket::handle
        );
        CHANNEL.registerMessage(
                nextPacketId++,
                ClearFilterSlotPacket.class,
                ClearFilterSlotPacket::encode,
                ClearFilterSlotPacket::new,
                ClearFilterSlotPacket::handle
        );
        CHANNEL.registerMessage(
                nextPacketId++,
                SyncPickupFilterDataPacket.class,
                SyncPickupFilterDataPacket::encode,
                SyncPickupFilterDataPacket::new,
                SyncPickupFilterDataPacket::handle
        );
    }

    public static void sendOpenMenu() {
        CHANNEL.sendToServer(new OpenPickupFilterMenuPacket());
    }

    public static void sendToggleMode() {
        CHANNEL.sendToServer(new ToggleFilterModePacket());
    }

    public static void sendToggleFeedback() {
        CHANNEL.sendToServer(new ToggleFeedbackPacket());
    }

    public static void sendUpdateSlot(int slot, ResourceLocation itemId) {
        CHANNEL.sendToServer(new UpdateFilterSlotPacket(slot, itemId));
    }

    public static void sendClearSlot(int slot) {
        CHANNEL.sendToServer(new ClearFilterSlotPacket(slot));
    }

    public static void syncTo(ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), SyncPickupFilterDataPacket.from(player));
    }
}
