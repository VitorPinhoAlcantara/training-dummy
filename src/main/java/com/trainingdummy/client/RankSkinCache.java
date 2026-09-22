package com.trainingdummy.client;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.ProfileResult;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;


final class RankSkinCache {

    private static final Map<UUID, CompletableFuture<GameProfile>> RESOLVED_PROFILES = new ConcurrentHashMap<>();

    static PlayerSkin resolve(UUID uuid, String name) {
        CompletableFuture<GameProfile> future = RESOLVED_PROFILES.computeIfAbsent(uuid, id ->
                CompletableFuture.supplyAsync(() -> {
                    ProfileResult result = Minecraft.getInstance().getMinecraftSessionService().fetchProfile(id, false);
                    return result != null ? result.profile() : new GameProfile(id, name);
                }, Util.backgroundExecutor()));

        GameProfile profile = future.getNow(null);
        if (profile == null) {
            return DefaultPlayerSkin.get(uuid);
        }
        return Minecraft.getInstance().getSkinManager().getInsecureSkin(profile);
    }

    private RankSkinCache() {
    }
}
