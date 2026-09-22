package com.trainingdummy.rank;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;


public record LeaderboardEntry(String playerName, float damage) {

    public static final Codec<LeaderboardEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("player_name").forGetter(LeaderboardEntry::playerName),
            Codec.FLOAT.fieldOf("damage").forGetter(LeaderboardEntry::damage)
    ).apply(instance, LeaderboardEntry::new));

    public static final StreamCodec<ByteBuf, LeaderboardEntry> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, LeaderboardEntry::playerName,
            ByteBufCodecs.FLOAT, LeaderboardEntry::damage,
            LeaderboardEntry::new
    );
}
