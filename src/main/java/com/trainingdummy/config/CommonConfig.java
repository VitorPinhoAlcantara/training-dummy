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

    public static final ModConfigSpec.DoubleValue MAX_HEALTH = BUILDER
            .comment("The dummy's max health. It's kept topped off every tick regardless of this value (see " +
                    "entity.DummyEntity#tick), so even 1 never actually lets a hit kill it - the only way to " +
                    "remove a dummy is still hitting it with a Stick.")
            .defineInRange("maxHealth", 20.0D, 1.0D, 1_000_000_000.0D);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private CommonConfig() {
    }
}
