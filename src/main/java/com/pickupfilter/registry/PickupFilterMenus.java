package com.pickupfilter.registry;

import com.pickupfilter.PickupFilter;
import com.pickupfilter.menu.PickupFilterMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class PickupFilterMenus {
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, PickupFilter.MOD_ID);

    public static final RegistryObject<MenuType<PickupFilterMenu>> PICKUP_FILTER = MENUS.register(
            "pickup_filter",
            () -> IForgeMenuType.create((containerId, inventory, buffer) -> new PickupFilterMenu(containerId, inventory))
    );

    private PickupFilterMenus() {
    }

    public static void register(IEventBus modEventBus) {
        MENUS.register(modEventBus);
    }
}
