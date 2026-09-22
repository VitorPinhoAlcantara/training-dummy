package com.trainingdummy.rank;

import com.trainingdummy.snapshot.PlayerCombatSnapshot;
import net.minecraft.core.HolderLookup;

import java.util.List;
import java.util.Optional;


public interface LeaderboardService {

    Optional<List<LeaderboardEntry>> fetchTop10(String modpackId);

    boolean submitCandidate(String modpackId, PlayerCombatSnapshot snapshot, HolderLookup.Provider registries);
}
