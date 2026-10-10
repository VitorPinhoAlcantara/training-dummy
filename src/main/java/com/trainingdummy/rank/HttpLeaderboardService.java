package com.trainingdummy.rank;

import com.trainingdummy.TrainingDummyMod;
import com.trainingdummy.snapshot.PlayerCombatSnapshot;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class HttpLeaderboardService implements LeaderboardService {

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);
    private static final long FAILURE_LOG_INTERVAL_MILLIS = 5 * 60_000L;

    private final String baseUrl;
    private final String apiKey;
    private final HttpClient client;

    private final AtomicLong lastFailureLogMillis = new AtomicLong();
    private final AtomicInteger suppressedFailures = new AtomicInteger();

    public HttpLeaderboardService(String baseUrl, String apiKey) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.apiKey = apiKey;
        this.client = HttpClient.newBuilder()
                .connectTimeout(REQUEST_TIMEOUT)
                .build();
    }

    @Override
    public CompletableFuture<Optional<List<LeaderboardEntry>>> fetchTop10(String modpackId) {
        URI uri = URI.create(this.baseUrl + "/leaderboard/" + URLEncoder.encode(modpackId, StandardCharsets.UTF_8));
        String label = "GET " + uri;
        long startNanos = this.logRequest(label);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(REQUEST_TIMEOUT)
                .GET()
                .build();

        return this.client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .handle((response, error) -> {
                    if (error != null) {
                        this.logResult(label, startNanos, describe(error));
                        this.logFailure("Leaderboard fetch failed: " + describe(error));
                        return Optional.<List<LeaderboardEntry>>empty();
                    }
                    this.logResult(label, startNanos, "HTTP " + response.statusCode());
                    if (response.statusCode() != 200) {
                        this.logFailure("Leaderboard fetch failed: HTTP " + response.statusCode());
                        return Optional.<List<LeaderboardEntry>>empty();
                    }
                    try {
                        return this.decodeEntries(JsonParser.parseString(response.body()));
                    } catch (RuntimeException e) {
                        this.logFailure("Leaderboard fetch failed: " + describe(e));
                        return Optional.<List<LeaderboardEntry>>empty();
                    }
                });
    }

    @Override
    public CompletableFuture<SubmitResult> submitCandidate(String modpackId, PlayerCombatSnapshot snapshot) {
        JsonObject snapshotJson = new JsonObject();
        snapshotJson.addProperty("player_name", snapshot.playerName());
        snapshotJson.addProperty("damage", snapshot.damage());

        JsonObject body = new JsonObject();
        body.addProperty("modpack_id", modpackId);
        body.add("snapshot", snapshotJson);

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(this.baseUrl + "/submit"))
                .timeout(REQUEST_TIMEOUT)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()));
        if (!this.apiKey.isBlank()) {
            requestBuilder.header("X-Api-Key", this.apiKey);
        }

        String label = "POST " + this.baseUrl + "/submit";
        long startNanos = this.logRequest(label + " body=" + body);
        return this.client.sendAsync(requestBuilder.build(), HttpResponse.BodyHandlers.ofString())
                .handle((response, error) -> {
                    if (error != null) {
                        this.logResult(label, startNanos, describe(error));
                        this.logFailure("Leaderboard submit failed: " + describe(error));
                        return SubmitResult.UNREACHABLE;
                    }
                    this.logResult(label, startNanos, "HTTP " + response.statusCode());
                    if (response.statusCode() != 200) {
                        this.logFailure("Leaderboard submit failed: HTTP " + response.statusCode());
                        return SubmitResult.UNREACHABLE;
                    }
                    try {
                        return this.decodeSubmit(JsonParser.parseString(response.body()));
                    } catch (RuntimeException e) {
                        this.logFailure("Leaderboard submit failed: " + describe(e));
                        return SubmitResult.UNREACHABLE;
                    }
                });
    }

    private SubmitResult decodeSubmit(JsonElement json) {
        if (!json.isJsonObject()) {
            this.logFailure("Leaderboard submit failed: unexpected response");
            return SubmitResult.UNREACHABLE;
        }
        JsonObject object = json.getAsJsonObject();
        boolean accepted = object.has("accepted") && object.get("accepted").getAsBoolean();
        int rank = object.has("rank") && object.get("rank").isJsonPrimitive() ? object.get("rank").getAsInt() : -1;
        Optional<List<LeaderboardEntry>> leaderboard = object.has("leaderboard")
                ? this.decodeEntries(object.get("leaderboard"))
                : Optional.empty();
        return new SubmitResult(true, accepted, rank, leaderboard);
    }

    private Optional<List<LeaderboardEntry>> decodeEntries(JsonElement json) {
        return LeaderboardEntry.CODEC.listOf().parse(JsonOps.INSTANCE, json)
                .resultOrPartial(error -> this.logFailure("Failed to decode leaderboard response: " + error));
    }

    private long logRequest(String label) {
        TrainingDummyMod.LOGGER.info("Leaderboard request: {}", label);
        return System.nanoTime();
    }

    private void logResult(String label, long startNanos, String outcome) {
        TrainingDummyMod.LOGGER.info("Leaderboard response: {} -> {} ({} ms)", label, outcome,
                (System.nanoTime() - startNanos) / 1_000_000L);
    }

    private static String describe(Throwable throwable) {
        Throwable root = throwable;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        return root.getClass().getSimpleName() + (root.getMessage() != null ? ": " + root.getMessage() : "");
    }

    private void logFailure(String message) {
        long now = System.currentTimeMillis();
        long last = this.lastFailureLogMillis.get();
        if (now - last >= FAILURE_LOG_INTERVAL_MILLIS && this.lastFailureLogMillis.compareAndSet(last, now)) {
            int suppressed = this.suppressedFailures.getAndSet(0);
            if (suppressed > 0) {
                TrainingDummyMod.LOGGER.warn("{} ({} similar failures suppressed since the last warning)", message, suppressed);
            } else {
                TrainingDummyMod.LOGGER.warn(message);
            }
        } else {
            this.suppressedFailures.incrementAndGet();
        }
    }
}
