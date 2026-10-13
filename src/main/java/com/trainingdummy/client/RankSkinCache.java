package com.trainingdummy.client;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.ProfileResult;
import com.trainingdummy.rank.LeaderboardEntry;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.level.block.entity.SkullBlockEntity;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

final class RankSkinCache {

    private static final Map<String, CompletableFuture<GameProfile>> RESOLVED_PROFILES = new ConcurrentHashMap<>();

    static PlayerSkin resolve(UUID uuid, String name) {
        boolean byName = LeaderboardEntry.NIL_UUID.equals(uuid);
        UUID fallbackUuid = byName ? UUIDUtil.createOfflinePlayerUUID(name) : uuid;
        String key = byName ? "name:" + name.toLowerCase(Locale.ROOT) : uuid.toString();

        CompletableFuture<GameProfile> future = RESOLVED_PROFILES.computeIfAbsent(key, k -> byName
                ? SkullBlockEntity.fetchGameProfile(name)
                        .thenApply(profile -> profile.orElseGet(() -> new GameProfile(fallbackUuid, name)))
                        .exceptionally(error -> new GameProfile(fallbackUuid, name))
                : CompletableFuture.supplyAsync(() -> {
                    ProfileResult result = Minecraft.getInstance().getMinecraftSessionService().fetchProfile(uuid, false);
                    return result != null ? result.profile() : new GameProfile(uuid, name);
                }, Util.backgroundExecutor()));

        GameProfile profile = future.getNow(null);
        if (profile == null) {
            return DefaultPlayerSkin.get(fallbackUuid);
        }
        return Minecraft.getInstance().getSkinManager().getInsecureSkin(profile);
    }

    private RankSkinCache() {
    }
}
