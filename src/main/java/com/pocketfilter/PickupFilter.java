package com.pocketfilter;

import com.mojang.logging.LogUtils;
import com.pocketfilter.config.CommonConfig;
import com.pocketfilter.data.PickupFilterPlayerData;
import com.pocketfilter.event.PickupFilterEvents;
import com.pocketfilter.network.PickupFilterNetwork;
import com.pocketfilter.registry.PickupFilterMenus;
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
    public static final String MOD_ID = "pocketfilter";
    public static final Logger LOGGER = LogUtils.getLogger();

    @SuppressWarnings("removal")
    public PickupFilter() {
        PickupFilterMenus.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC);
        PickupFilterNetwork.register();
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new PickupFilterEvents());
        LOGGER.info("Pocket Filter initialized");
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        PickupFilterPlayerData.copy(event.getOriginal(), event.getEntity());
    }
}
