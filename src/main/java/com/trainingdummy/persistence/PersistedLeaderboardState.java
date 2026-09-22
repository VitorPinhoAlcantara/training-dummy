package com.trainingdummy.persistence;

import com.trainingdummy.rank.LeaderboardEntry;
import com.trainingdummy.snapshot.PlayerCombatSnapshot;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Map;

public record PersistedLeaderboardState(
        List<LeaderboardEntry> local,
        Map<String, List<PlayerCombatSnapshot>> globalByModpack
) {

    public static final Codec<PersistedLeaderboardState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            LeaderboardEntry.CODEC.listOf().fieldOf("local").forGetter(PersistedLeaderboardState::local),
            Codec.unboundedMap(Codec.STRING, PlayerCombatSnapshot.CODEC.listOf())
                    .fieldOf("global_by_modpack").forGetter(PersistedLeaderboardState::globalByModpack)
    ).apply(instance, PersistedLeaderboardState::new));
}
