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

    public enum DisplayMetric {
        TOTAL,
        DPS
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
            .defineEnum("displayLocation", DisplayLocation.SCREEN);

    public static final ModConfigSpec.EnumValue<DisplayMetric> DISPLAY_METRIC = BUILDER
            .comment("TOTAL shows the summed damage of the current streak; DPS shows that sum divided by the streak's elapsed time.")
            .defineEnum("displayMetric", DisplayMetric.TOTAL);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private ClientConfig() {
    }
}
