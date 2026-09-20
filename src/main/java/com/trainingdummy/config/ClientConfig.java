package com.trainingdummy.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ClientConfig {

    public enum DisplayLocation {
        SCREEN,
        CHAT
    }

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue HIT_RESET_SECONDS = BUILDER
            .comment("Every hit adds to a running total. If this many seconds pass without a hit, the next hit starts a fresh total.")
            .defineInRange("hitResetSeconds", 2.0D, 0.1D, 600.0D);

    public static final ModConfigSpec.DoubleValue DISPLAY_DURATION_SECONDS = BUILDER
            .comment("How long the damage readout stays on screen/in chat after the last hit, in seconds.")
            .defineInRange("displayDurationSeconds", 1.0D, 0.1D, 60.0D);

    public static final ModConfigSpec.EnumValue<DisplayLocation> DISPLAY_LOCATION = BUILDER
            .comment("Where to show the damage readout: SCREEN (HUD overlay) or CHAT (local chat message).")
            .defineEnum("displayLocation", DisplayLocation.CHAT);

    public static final ModConfigSpec.IntValue DISPLAY_OFFSET_X = BUILDER
            .comment("SCREEN readout only. Horizontal pixel offset from the center of the screen (negative = left, positive = right). Useful if another mod's HUD element sits where the readout would otherwise be.")
            .defineInRange("displayOffsetX", 0, -10000, 10000);

    public static final ModConfigSpec.IntValue DISPLAY_OFFSET_Y = BUILDER
            .comment("SCREEN readout only. Vertical pixel offset up from the bottom of the screen (the default, 90, sits above the XP bar/hotbar).")
            .defineInRange("displayOffsetY", 90, 0, 10000);

    public static final ModConfigSpec.ConfigValue<String> DISPLAY_COLOR = BUILDER
            .comment("SCREEN readout only. Text color as a 6-digit RRGGBB or 8-digit AARRGGBB hex string (no # prefix). Default is a darker red. Falls back to the default on an invalid value.")
            .define("displayColor", "FFCC2222");

    public static final ModConfigSpec SPEC = BUILDER.build();

    public static int displayColorArgb() {
        String hex = DISPLAY_COLOR.get().trim();
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }
        try {
            if (hex.length() == 6) {
                return 0xFF000000 | (int) Long.parseLong(hex, 16);
            } else if (hex.length() == 8) {
                return (int) Long.parseLong(hex, 16);
            }
        } catch (NumberFormatException ignored) {
        }
        return 0xFFCC2222;
    }

    private ClientConfig() {
    }
}
