package com.trainingdummy.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.world.level.block.entity.SkullBlockEntity;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resolves a dummy's display name to a real player's skin, client-side only. Uses the exact same
 * vanilla machinery player-head blocks use for "give a skull a player's face": name ->
 * {@link SkullBlockEntity#fetchGameProfile}, profile -> {@link net.minecraft.client.resources.SkinManager}.
 * Both hops are async (network calls), so lookups are fire-and-forget - callers get the cached
 * result if it's ready, or nothing yet while it's still in flight/if the name doesn't match a
 * real account (that failure is cached too, so it's not retried every frame).
 */
public final class DummySkinCache {

    private static final Map<String, PlayerSkin> RESOLVED = new ConcurrentHashMap<>();
    private static final Set<String> IN_FLIGHT = ConcurrentHashMap.newKeySet();

    public static Optional<PlayerSkin> getSkin(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        String key = name.toLowerCase(Locale.ROOT);
        PlayerSkin cached = RESOLVED.get(key);
        if (cached != null) {
            return Optional.of(cached);
        }
        if (IN_FLIGHT.add(key)) {
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
