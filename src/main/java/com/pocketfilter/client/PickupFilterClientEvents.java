package com.pocketfilter.client;

import com.pocketfilter.PickupFilter;
import com.pocketfilter.config.CommonConfig;
import com.pocketfilter.network.PickupFilterNetwork;
import com.pocketfilter.registry.PickupFilterMenus;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public final class PickupFilterClientEvents {
    private static final KeyMapping OPEN_PICKUP_FILTER = new KeyMapping(
            "key.pocketfilter.open",
            InputConstants.UNKNOWN.getType(),
            InputConstants.UNKNOWN.getValue(),
            "key.categories.pocketfilter"
    );

    private PickupFilterClientEvents() {
    }

    @Mod.EventBusSubscriber(modid = PickupFilter.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class ModBusEvents {
        private ModBusEvents() {
        }

        @SubscribeEvent
        public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
            event.register(OPEN_PICKUP_FILTER);
        }

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> MenuScreens.register(PickupFilterMenus.PICKUP_FILTER.get(), PickupFilterScreen::new));
            MinecraftForge.EVENT_BUS.register(ForgeBusEvents.class);
        }
    }

    public static final class ForgeBusEvents {
        private ForgeBusEvents() {
        }

        @SubscribeEvent
        public static void onInventoryScreenInit(ScreenEvent.Init.Post event) {
            if (!CommonConfig.ENABLE_INVENTORY_BUTTON.get()) {
                return;
            }

            if (!(event.getScreen() instanceof InventoryScreen inventoryScreen)) {
                return;
            }

            int x = inventoryScreen.getGuiLeft() + 154;
            int y = inventoryScreen.getGuiTop() + 61;
            Button button = Button.builder(Component.translatable("button.pocketfilter.open"), clickedButton -> PickupFilterNetwork.sendOpenMenu())
                    .bounds(x, y, 18, 18)
                    .tooltip(Tooltip.create(Component.translatable("button.pocketfilter.open.tooltip")))
                    .build(PickupFilterInventoryButton::new);
            event.addListener(button);
        }

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) {
                return;
            }

            Minecraft minecraft = Minecraft.getInstance();
            while (OPEN_PICKUP_FILTER.consumeClick()) {
                if (!CommonConfig.ENABLE_KEYBIND_FALLBACK.get()) {
                    continue;
                }

                if (minecraft.player != null && minecraft.level != null && minecraft.screen == null) {
                    PickupFilterNetwork.sendOpenMenu();
                }
            }
        }
    }
}
