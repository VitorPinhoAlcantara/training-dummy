package com.trainingdummy.rank;

import com.trainingdummy.snapshot.PlayerCombatSnapshot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public final class LocalStubLeaderboardService implements LeaderboardService {

    private static final int TOP_N = 10;

    private final Map<String, List<LeaderboardEntry>> byModpack = new ConcurrentHashMap<>();

    @Override
    public CompletableFuture<Optional<List<LeaderboardEntry>>> fetchTop10(String modpackId) {
        return CompletableFuture.completedFuture(Optional.of(this.byModpack.getOrDefault(modpackId, List.of())));
    }

    @Override
    public synchronized CompletableFuture<SubmitResult> submitCandidate(String modpackId, PlayerCombatSnapshot snapshot) {
        List<LeaderboardEntry> current = new ArrayList<>(this.byModpack.getOrDefault(modpackId, List.of()));

        boolean alreadyHasBetter = current.stream()
                .anyMatch(existing -> existing.playerName().equals(snapshot.playerName()) && existing.damage() >= snapshot.damage());
        if (!alreadyHasBetter) {
            current.removeIf(existing -> existing.playerName().equals(snapshot.playerName()));
            current.add(new LeaderboardEntry(snapshot.playerName(), snapshot.playerUuid(), snapshot.damage()));
            current.sort(Comparator.comparingDouble(LeaderboardEntry::damage).reversed());
            List<LeaderboardEntry> trimmed = current.size() > TOP_N ? current.subList(0, TOP_N) : current;
            this.byModpack.put(modpackId, List.copyOf(trimmed));
        }

        List<LeaderboardEntry> board = this.byModpack.getOrDefault(modpackId, List.of());
        int rank = -1;
        for (int i = 0; i < board.size(); i++) {
            if (board.get(i).playerName().equals(snapshot.playerName())) {
                rank = i + 1;
                break;
            }
        }
        return CompletableFuture.completedFuture(new SubmitResult(true, !alreadyHasBetter && rank > 0, rank, Optional.of(board)));
    }

    public Map<String, List<LeaderboardEntry>> exportState() {
        return Map.copyOf(this.byModpack);
    }

    public void importState(Map<String, List<LeaderboardEntry>> state) {
        this.byModpack.clear();
        this.byModpack.putAll(state);
    }
}
