package com.trainingdummy.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.world.level.block.entity.SkullBlockEntity;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;


public final class DummySkinCache {

    private static final long RETRY_COOLDOWN_MILLIS = 15_000L;

    private static final Map<String, PlayerSkin> RESOLVED = new ConcurrentHashMap<>();
    private static final Map<String, Long> LAST_ATTEMPT = new ConcurrentHashMap<>();

    public static Optional<PlayerSkin> getSkin(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        String key = name.toLowerCase(Locale.ROOT);
        PlayerSkin cached = RESOLVED.get(key);
        if (cached != null) {
            return Optional.of(cached);
        }

        long now = System.currentTimeMillis();
        Long lastAttempt = LAST_ATTEMPT.get(key);
        if (lastAttempt == null || now - lastAttempt > RETRY_COOLDOWN_MILLIS) {
            LAST_ATTEMPT.put(key, now);
            Minecraft mc = Minecraft.getInstance();
            SkullBlockEntity.fetchGameProfile(name).thenAcceptAsync(profile -> {
                if (profile.isPresent()) {
                    mc.getSkinManager().getOrLoad(profile.get())
                            .thenAcceptAsync(skin -> RESOLVED.put(key, skin), mc);
                }
            }, mc);
        }
        return Optional.empty();
    }

    private DummySkinCache() {
    }
}
