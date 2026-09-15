package com.trainingdummy.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Client-only config: every time value and display preset is here (not COMMON), because the
 * damage window/duration/format are purely client-side decisions - the server just streams raw
 * hit amounts (see network.DummyDamagePayload) and the client decides how to fold and show them.
 * Standard NeoForge ModConfigSpec, so it gets a native config screen and works with Configured.
 */
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

    /** Parsed {@link #DISPLAY_COLOR}, ARGB packed into an int - falls back to the default color on a malformed config value. */
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
            // Falls through to the default below.
        }
        return 0xFFCC2222;
    }

    private ClientConfig() {
    }
}
