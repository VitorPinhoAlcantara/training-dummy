package com.trainingdummy.rank;

import java.util.List;
import java.util.Optional;

public record SubmitResult(boolean reachable, boolean accepted, int rank, Optional<List<LeaderboardEntry>> leaderboard) {

    public static final SubmitResult UNREACHABLE = new SubmitResult(false, false, -1, Optional.empty());
}
