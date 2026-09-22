package com.trainingdummy.persistence;

import com.trainingdummy.TrainingDummyMod;
import com.trainingdummy.rank.LeaderboardCache;
import com.trainingdummy.rank.LocalStubLeaderboardService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public final class LeaderboardPersistence {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static Path filePath;
    private static HolderLookup.Provider registries;

    public static void bind(MinecraftServer server) {
        registries = server.registryAccess();
        Path dir = server.getWorldPath(LevelResource.ROOT).resolve("trainingdummy_scoreboard");
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            TrainingDummyMod.LOGGER.error("Could not create trainingdummy_scoreboard data folder", e);
            filePath = null;
            return;
        }
        filePath = dir.resolve("leaderboard.json");
        load();
    }

    public static void unbind() {
        save();
        filePath = null;
        registries = null;
    }

    public static synchronized void save() {
        if (filePath == null) {
            return;
        }
        Map<String, java.util.List<com.trainingdummy.snapshot.PlayerCombatSnapshot>> globalState =
                LeaderboardCache.service() instanceof LocalStubLeaderboardService stub ? stub.exportState() : Map.of();
        PersistedLeaderboardState state = new PersistedLeaderboardState(LeaderboardCache.localEntries(), globalState);

        DynamicOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, registries);
        DataResult<JsonElement> result = PersistedLeaderboardState.CODEC.encodeStart(ops, state);
        result.resultOrPartial(error -> TrainingDummyMod.LOGGER.error("Failed to encode leaderboard data: {}", error))
                .ifPresent(json -> {
                    try (Writer writer = Files.newBufferedWriter(filePath)) {
                        GSON.toJson(json, writer);
                    } catch (IOException e) {
                        TrainingDummyMod.LOGGER.error("Failed to save leaderboard data", e);
                    }
                });
    }

    public static synchronized void load() {
        if (filePath == null || !Files.exists(filePath)) {
            return;
        }
        try (Reader reader = Files.newBufferedReader(filePath)) {
            JsonElement json = JsonParser.parseReader(reader);
            DynamicOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, registries);
            PersistedLeaderboardState.CODEC.parse(ops, json)
                    .resultOrPartial(error -> TrainingDummyMod.LOGGER.error("Failed to decode leaderboard data: {}", error))
                    .ifPresent(state -> {
                        LeaderboardCache.restoreLocalEntries(state.local());
                        if (LeaderboardCache.service() instanceof LocalStubLeaderboardService stub) {
                            stub.importState(state.globalByModpack());
                        }
                    });
        } catch (IOException e) {
            TrainingDummyMod.LOGGER.error("Failed to load leaderboard data", e);
        }
    }

    private LeaderboardPersistence() {
    }
}
