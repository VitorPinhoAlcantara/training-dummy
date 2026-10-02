package com.trainingdummy.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.component.ResolvableProfile;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

final class RankSkinCache {

    private static final Map<UUID, Supplier<PlayerSkinRenderCache.RenderInfo>> LOOKUPS = new ConcurrentHashMap<>();

    static PlayerSkin resolve(UUID uuid) {
        return LOOKUPS.computeIfAbsent(uuid, id ->
                Minecraft.getInstance().playerSkinRenderCache().createLookup(ResolvableProfile.createUnresolved(id))
        ).get().playerSkin();
    }

    private RankSkinCache() {
    }
}
