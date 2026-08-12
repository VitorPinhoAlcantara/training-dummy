package com.trainingdummy.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Server-authoritative gameplay values (as opposed to the purely-cosmetic display settings in
 * {@link ClientConfig}) - these affect what actually happens in the world, so they have to be
 * COMMON, not CLIENT, config.
 */
public final class CommonConfig {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue BAIT_RADIUS = BUILDER
            .comment("How far (in blocks) mobs are pulled from to attack a dummy holding the lure bait in its main hand.")
            .defineInRange("baitRadius", 32.0D, 1.0D, 128.0D);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private CommonConfig() {
    }
}
