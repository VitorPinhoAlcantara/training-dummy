package com.trainingdummy.client;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.PlayerSkin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

final class RankSkinCache {

    private static final Map<UUID, Supplier<PlayerSkin>> LOOKUPS = new ConcurrentHashMap<>();

    static PlayerSkin resolve(UUID uuid, String name) {
        return LOOKUPS.computeIfAbsent(uuid, id ->
                Minecraft.getInstance().getSkinManager().createLookup(new GameProfile(id, name), false)
        ).get();
    }

    private RankSkinCache() {
    }
}
