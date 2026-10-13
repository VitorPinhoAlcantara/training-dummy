package com.trainingdummy.rank;

import com.trainingdummy.snapshot.PlayerCombatSnapshot;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface LeaderboardService {

    CompletableFuture<Optional<List<LeaderboardEntry>>> fetchTop10(String modpackId);

    CompletableFuture<SubmitResult> submitCandidate(String modpackId, PlayerCombatSnapshot snapshot);
}
