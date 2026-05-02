package com.pocketfilter.config;

import com.pocketfilter.data.FilterMode;
import net.minecraftforge.common.ForgeConfigSpec;

public final class CommonConfig {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.EnumValue<FilterMode> DEFAULT_MODE;
    public static final ForgeConfigSpec.BooleanValue DEFAULT_FEEDBACK_ENABLED;
    public static final ForgeConfigSpec.IntValue FEEDBACK_COOLDOWN_TICKS;
    public static final ForgeConfigSpec.BooleanValue ENABLE_INVENTORY_BUTTON;
    public static final ForgeConfigSpec.BooleanValue ENABLE_KEYBIND_FALLBACK;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("defaults");
        DEFAULT_MODE = builder
                .comment("Default filter mode for players who do not have saved Pocket Filter data yet.")
                .defineEnum("defaultMode", FilterMode.BLACKLIST);
        DEFAULT_FEEDBACK_ENABLED = builder
                .comment("Default actionbar feedback setting for players who do not have saved Pocket Filter data yet.")
                .define("defaultFeedbackEnabled", true);
        builder.pop();

        builder.push("feedback");
        FEEDBACK_COOLDOWN_TICKS = builder
                .comment("Minimum ticks between ignored-pickup feedback messages per player.")
                .defineInRange("feedbackCooldownTicks", 40, 0, 20 * 60);
        builder.pop();

        builder.push("client");
        ENABLE_INVENTORY_BUTTON = builder
                .comment("Adds a small Pocket Filter button to the vanilla player inventory screen.")
                .define("enableInventoryButton", true);
        ENABLE_KEYBIND_FALLBACK = builder
                .comment("Allows the configurable Open Pocket Filter keybind to open the GUI.")
                .define("enableKeybindFallback", true);
        builder.pop();

        SPEC = builder.build();
    }

    private CommonConfig() {
    }
}
