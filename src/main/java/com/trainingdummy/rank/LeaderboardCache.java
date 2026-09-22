package com.trainingdummy.rank;

import com.trainingdummy.config.CommonConfig;
import com.trainingdummy.persistence.LeaderboardPersistence;
import com.trainingdummy.snapshot.PlayerCombatSnapshot;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;


public final class LeaderboardCache {

    private static final int TOP_N = 10;

    public static final int COMMUNICATION_ERROR = Integer.MIN_VALUE;

    private static LeaderboardService service = new LocalStubLeaderboardService();

    private static volatile List<LeaderboardEntry> localEntries = List.of();
    private static volatile List<LeaderboardEntry> globalEntries = List.of();
    private static long lastFetchGameTime = Long.MIN_VALUE;

    public static void setService(LeaderboardService newService) {
        service = newService;
        lastFetchGameTime = Long.MIN_VALUE;
    }

    public static LeaderboardService service() {
        return service;
    }

    public static boolean globalAvailable() {
        return !CommonConfig.effectiveModpackId().isBlank();
    }

    public static List<LeaderboardEntry> localEntries() {
        return localEntries;
    }

    public static void restoreLocalEntries(List<LeaderboardEntry> entries) {
        localEntries = List.copyOf(entries);
    }

    public static List<LeaderboardEntry> globalEntries() {
        return globalEntries;
    }

    public static void refreshGlobalIfStale(Level level) {
        if (!globalAvailable()) {
            return;
        }
        long now = level.getGameTime();
        long refreshTicks = CommonConfig.LEADERBOARD_REFRESH_SECONDS.get() * 20L;
        if (lastFetchGameTime != Long.MIN_VALUE && now - lastFetchGameTime < refreshTicks) {
            return;
        }
        lastFetchGameTime = now;

        service.fetchTop10(CommonConfig.effectiveModpackId()).ifPresent(entries -> globalEntries = entries);
    }

    public static boolean qualifiesLocal(String playerName, float damage) {
        return qualifies(localEntries, playerName, damage);
    }

    public static boolean qualifiesGlobal(String playerName, float damage) {
        return qualifies(globalEntries, playerName, damage);
    }

    public static synchronized int applyLocalUpdate(String playerName, float damage) {
        UpdateResult result = computeUpdate(localEntries, playerName, damage);
        localEntries = result.entries();
        LeaderboardPersistence.save();
        return result.rank();
    }

    public static synchronized int applyGlobalOptimisticUpdate(String playerName, float damage) {
        UpdateResult result = computeUpdate(globalEntries, playerName, damage);
        globalEntries = result.entries();
        return result.rank();
    }

    public static synchronized int confirmGlobalCandidate(PlayerCombatSnapshot snapshot, HolderLookup.Provider registries) {
        String modpackId = CommonConfig.effectiveModpackId();

        Optional<List<LeaderboardEntry>> beforeSubmit = service.fetchTop10(modpackId);
        if (beforeSubmit.isEmpty()) {
            return COMMUNICATION_ERROR;
        }
        globalEntries = beforeSubmit.get();
        if (!qualifiesGlobal(snapshot.playerName(), snapshot.damage())) {
            return -1;
        }

        if (!service.submitCandidate(modpackId, snapshot, registries)) {
            return COMMUNICATION_ERROR;
        }

        Optional<List<LeaderboardEntry>> afterSubmit = service.fetchTop10(modpackId);
        if (afterSubmit.isEmpty()) {
            return COMMUNICATION_ERROR;
        }
        globalEntries = afterSubmit.get();
        LeaderboardPersistence.save();
        for (int i = 0; i < globalEntries.size(); i++) {
            if (globalEntries.get(i).playerName().equals(snapshot.playerName())) {
                return i + 1;
            }
        }
        return -1;
    }

    private static boolean qualifies(List<LeaderboardEntry> entries, String playerName, float damage) {
        for (LeaderboardEntry entry : entries) {
            if (entry.playerName().equals(playerName)) {
                return damage > entry.damage();
            }
        }
        return entries.size() < TOP_N || damage > entries.get(entries.size() - 1).damage();
    }

    private static UpdateResult computeUpdate(List<LeaderboardEntry> current, String playerName, float damage) {
        List<LeaderboardEntry> updated = new ArrayList<>(current);
        updated.removeIf(entry -> entry.playerName().equals(playerName));
        LeaderboardEntry newEntry = new LeaderboardEntry(playerName, damage);
        updated.add(newEntry);
        updated.sort(Comparator.comparingDouble(LeaderboardEntry::damage).reversed());
        List<LeaderboardEntry> trimmed = updated.size() > TOP_N ? updated.subList(0, TOP_N) : updated;
        List<LeaderboardEntry> result = List.copyOf(trimmed);
        return new UpdateResult(result, result.indexOf(newEntry) + 1);
    }

    private record UpdateResult(List<LeaderboardEntry> entries, int rank) {
    }

    private LeaderboardCache() {
    }
}
