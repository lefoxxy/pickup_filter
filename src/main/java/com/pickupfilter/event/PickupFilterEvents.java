package com.pickupfilter.event;

import com.pickupfilter.PickupFilter;
import com.pickupfilter.config.CommonConfig;
import com.pickupfilter.data.PickupFilterPlayerData;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class PickupFilterEvents {
    private static final String LAST_FEEDBACK_TICK_KEY = PickupFilter.MOD_ID + ":lastFeedbackTick";

    @SubscribeEvent
    public void onItemPickup(EntityItemPickupEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        ItemStack pickedStack = event.getItem().getItem();
        if (PickupFilterPlayerData.matchesItem(player, pickedStack)) {
            return;
        }

        event.setCanceled(true);
        sendIgnoredFeedback(player, pickedStack);
    }

    private static void sendIgnoredFeedback(ServerPlayer player, ItemStack stack) {
        if (!PickupFilterPlayerData.isFeedbackEnabled(player) || stack.isEmpty()) {
            return;
        }

        long currentTick = player.serverLevel().getGameTime();
        CompoundTag data = player.getPersistentData();
        boolean hasLastFeedbackTick = data.contains(LAST_FEEDBACK_TICK_KEY, Tag.TAG_LONG);
        long lastFeedbackTick = hasLastFeedbackTick ? data.getLong(LAST_FEEDBACK_TICK_KEY) : 0L;

        if (hasLastFeedbackTick && currentTick - lastFeedbackTick < CommonConfig.FEEDBACK_COOLDOWN_TICKS.get()) {
            return;
        }

        data.putLong(LAST_FEEDBACK_TICK_KEY, currentTick);
        Component message = Component.translatable("message.pickupfilter.ignored", stack.getHoverName())
                .withStyle(ChatFormatting.YELLOW);
        player.displayClientMessage(message, true);
    }
}
