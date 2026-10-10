package com.trainingdummy.snapshot;

import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public record PlayerCombatSnapshot(String playerName, UUID playerUuid, float damage) {

    public static PlayerCombatSnapshot capture(ServerPlayer player, float damage) {
        return new PlayerCombatSnapshot(player.getGameProfile().name(), player.getUUID(), damage);
    }
}
