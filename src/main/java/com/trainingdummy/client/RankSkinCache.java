package com.trainingdummy.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.component.ResolvableProfile;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

final class RankSkinCache {

    private static final Map<String, Supplier<PlayerSkinRenderCache.RenderInfo>> LOOKUPS = new ConcurrentHashMap<>();

    static PlayerSkin resolve(UUID uuid, String name) {
        boolean byName = com.trainingdummy.rank.LeaderboardEntry.NIL_UUID.equals(uuid);
        String key = byName ? "name:" + name.toLowerCase(Locale.ROOT) : uuid.toString();
        return LOOKUPS.computeIfAbsent(key, k -> Minecraft.getInstance().playerSkinRenderCache().createLookup(
                byName ? ResolvableProfile.createUnresolved(name) : ResolvableProfile.createUnresolved(uuid))
        ).get().playerSkin();
    }

    private RankSkinCache() {
    }
}
