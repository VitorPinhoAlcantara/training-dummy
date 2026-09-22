package com.trainingdummy.client;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.entity.player.PlayerSkin;

final class RankPreviewPlayer extends RemotePlayer {

    RankPreviewPlayer(ClientLevel level, GameProfile profile) {
        super(level, profile);
        this.noPhysics = true;
    }

    @Override
    public PlayerSkin getSkin() {
        return RankSkinCache.resolve(this.getUUID(), this.getGameProfile().name());
    }
}
