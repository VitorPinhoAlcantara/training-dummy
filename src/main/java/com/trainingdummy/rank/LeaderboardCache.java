package com.trainingdummy.rank;

import com.trainingdummy.config.CommonConfig;
import com.trainingdummy.persistence.LeaderboardPersistence;
import com.trainingdummy.snapshot.PlayerCombatSnapshot;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

public final class LeaderboardCache {

    private static final int TOP_N = 10;
    private static final long RETRY_AFTER_FAILURE_MILLIS = 30_000L;

    public static final int COMMUNICATION_ERROR = Integer.MIN_VALUE;
    public static final int SKIPPED = Integer.MIN_VALUE + 1;

    private static final Object REFRESH_LOCK = new Object();

    private static volatile LeaderboardService service = new LocalStubLeaderboardService();

    private static volatile List<LeaderboardEntry> localEntries = List.of();
    private static volatile List<LeaderboardEntry> globalEntries = List.of();
    private static volatile boolean globalLoaded = false;

    private static long nextFetchAllowedMillis = 0L;
    private static boolean fetchInFlight = false;
    private static final List<Consumer<List<LeaderboardEntry>>> refreshListeners = new ArrayList<>();

    public static void setService(LeaderboardService newService) {
        service = newService;
        globalLoaded = false;
        synchronized (REFRESH_LOCK) {
            nextFetchAllowedMillis = 0L;
            fetchInFlight = false;
            refreshListeners.clear();
        }
    }

    public static LeaderboardService service() {
        return service;
    }

    public static GlobalLeaderboardState globalState() {
        if (!CommonConfig.GLOBAL_LEADERBOARD_ENABLED.get()) {
            return GlobalLeaderboardState.DISABLED_IN_CONFIG;
        }
        if (CommonConfig.effectiveModpackId().isBlank()) {
            return GlobalLeaderboardState.NO_MODPACK_ID;
        }
        return GlobalLeaderboardState.ENABLED;
    }

    public static boolean globalAvailable() {
        return globalState() == GlobalLeaderboardState.ENABLED;
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

    public static boolean globalLoaded() {
        return globalLoaded;
    }

    public static void refreshGlobalIfStale(MinecraftServer server, Consumer<List<LeaderboardEntry>> onRefreshed) {
        if (!globalAvailable()) {
            return;
        }
        LeaderboardService svc = service;
        String modpackId = CommonConfig.effectiveModpackId();
        long refreshMillis = CommonConfig.LEADERBOARD_REFRESH_SECONDS.get() * 1000L;

        synchronized (REFRESH_LOCK) {
            if (fetchInFlight) {
                if (onRefreshed != null) {
                    refreshListeners.add(onRefreshed);
                }
                return;
            }
            long now = System.currentTimeMillis();
            if (now < nextFetchAllowedMillis) {
                return;
            }
            fetchInFlight = true;
            if (onRefreshed != null) {
                refreshListeners.add(onRefreshed);
            }
        }

        svc.fetchTop10(modpackId).whenComplete((result, error) -> server.execute(() -> {
            List<Consumer<List<LeaderboardEntry>>> listeners;
            boolean ok = error == null && result != null && result.isPresent();
            synchronized (REFRESH_LOCK) {
                if (svc != service) {
                    return;
                }
                fetchInFlight = false;
                listeners = new ArrayList<>(refreshListeners);
                refreshListeners.clear();
                long wait = ok ? refreshMillis : Math.min(refreshMillis, RETRY_AFTER_FAILURE_MILLIS);
                nextFetchAllowedMillis = System.currentTimeMillis() + wait;
            }
            if (!ok) {
                return;
            }
            List<LeaderboardEntry> fresh = result.get();
            boolean changed = !fresh.equals(globalEntries);
            globalEntries = fresh;
            globalLoaded = true;
            if (changed) {
                listeners.forEach(listener -> listener.accept(fresh));
            }
        }));
    }

    public static boolean qualifiesLocal(String playerName, float damage) {
        return qualifies(localEntries, playerName, damage);
    }

    public static boolean qualifiesGlobal(String playerName, float damage) {
        return qualifies(globalEntries, playerName, damage);
    }

    public static synchronized int applyLocalUpdate(String playerName, UUID playerUuid, float damage) {
        UpdateResult result = computeUpdate(localEntries, playerName, playerUuid, damage);
        localEntries = result.entries();
        LeaderboardPersistence.save();
        return result.rank();
    }

    public static synchronized int applyGlobalOptimisticUpdate(String playerName, UUID playerUuid, float damage) {
        UpdateResult result = computeUpdate(globalEntries, playerName, playerUuid, damage);
        globalEntries = result.entries();
        return result.rank();
    }

    public static void confirmGlobalCandidate(MinecraftServer server, PlayerCombatSnapshot snapshot, IntConsumer onResult) {
        if (!globalAvailable()) {
            onResult.accept(SKIPPED);
            return;
        }
        LeaderboardService svc = service;
        svc.submitCandidate(CommonConfig.effectiveModpackId(), snapshot).whenComplete((result, error) ->
                server.execute(() -> onResult.accept(applySubmitResult(snapshot, error == null ? result : null))));
    }

    private static int applySubmitResult(PlayerCombatSnapshot snapshot, SubmitResult result) {
        if (result == null || !result.reachable()) {
            return COMMUNICATION_ERROR;
        }
        result.leaderboard().ifPresent(entries -> {
            globalEntries = entries;
            globalLoaded = true;
        });
        LeaderboardPersistence.save();

        if (result.rank() > 0) {
            return result.rank();
        }
        if (result.accepted()) {
            List<LeaderboardEntry> entries = globalEntries;
            for (int i = 0; i < entries.size(); i++) {
                if (entries.get(i).playerName().equals(snapshot.playerName())) {
                    return i + 1;
                }
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

    private static UpdateResult computeUpdate(List<LeaderboardEntry> current, String playerName, UUID playerUuid, float damage) {
        List<LeaderboardEntry> updated = new ArrayList<>(current);
        updated.removeIf(entry -> entry.playerName().equals(playerName));
        LeaderboardEntry newEntry = new LeaderboardEntry(playerName, playerUuid, damage);
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
