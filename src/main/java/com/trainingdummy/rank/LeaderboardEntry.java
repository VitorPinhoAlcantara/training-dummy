package com.trainingdummy.rank;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.UUID;

public record LeaderboardEntry(String playerName, UUID playerUuid, float damage) {

    public static final Codec<LeaderboardEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("player_name").forGetter(LeaderboardEntry::playerName),
            UUIDUtil.STRING_CODEC.fieldOf("player_uuid").forGetter(LeaderboardEntry::playerUuid),
            Codec.FLOAT.fieldOf("damage").forGetter(LeaderboardEntry::damage)
    ).apply(instance, LeaderboardEntry::new));

    public static final StreamCodec<ByteBuf, LeaderboardEntry> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, LeaderboardEntry::playerName,
            UUIDUtil.STREAM_CODEC, LeaderboardEntry::playerUuid,
            ByteBufCodecs.FLOAT, LeaderboardEntry::damage,
            LeaderboardEntry::new
    );
}
