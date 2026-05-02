package com.pickupfilter.client;

import com.pickupfilter.data.FilterMode;
import com.pickupfilter.menu.PickupFilterMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class PickupFilterClientPacketHandler {
    private PickupFilterClientPacketHandler() {
    }

    public static void handleSync(List<ItemStack> slots, FilterMode mode, boolean feedbackEnabled) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || !(minecraft.player.containerMenu instanceof PickupFilterMenu menu)) {
            return;
        }

        menu.applyServerState(slots, mode, feedbackEnabled);
    }
}
