package com.pickupfilter;

import com.mojang.logging.LogUtils;
import com.pickupfilter.config.CommonConfig;
import com.pickupfilter.data.PickupFilterPlayerData;
import com.pickupfilter.event.PickupFilterEvents;
import com.pickupfilter.network.PickupFilterNetwork;
import com.pickupfilter.registry.PickupFilterMenus;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(PickupFilter.MOD_ID)
public final class PickupFilter {
    public static final String MOD_ID = "pickupfilter";
    public static final Logger LOGGER = LogUtils.getLogger();

    @SuppressWarnings("removal")
    public PickupFilter() {
        PickupFilterMenus.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC);
        PickupFilterNetwork.register();
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new PickupFilterEvents());
        LOGGER.info("Pickup Filter initialized");
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        PickupFilterPlayerData.copy(event.getOriginal(), event.getEntity());
    }
}
