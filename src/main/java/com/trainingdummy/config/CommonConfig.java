package com.trainingdummy.config;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;
import java.util.Locale;


public final class CommonConfig {

    private static final String DEFAULT_MODPACK_ID = WorkerSecrets.DEFAULT_MODPACK_ID;
    private static final String DEFAULT_MODPACK_API_KEY = WorkerSecrets.DEFAULT_MODPACK_API_KEY;
    private static final String DEFAULT_WORKER_BASE_URL = WorkerSecrets.DEFAULT_WORKER_BASE_URL;


    private static final List<String> HARDCODED_WEAPON_BLACKLIST = List.of(
            "mahoutsukai:morgan", "occultism_extra_spirits:forbidden_butcher_knife", "avaritia:*", "modern_industrialization:*", "infinity", "quantum");

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue BAIT_RADIUS = BUILDER
            .comment("How far (in blocks) mobs are pulled from to attack a dummy holding the lure bait in its main hand.")
            .defineInRange("baitRadius", 128.0D, 1.0D, 128.0D);

    public static final ModConfigSpec.ConfigValue<String> MODPACK_ID = BUILDER
            .comment("Identifies this server's modpack to the scoreboard dummy's leaderboard worker. Every server " +
                    "meant to share the same leaderboard must use the same value here. Leave blank to use the " +
                    "mod's built-in default shared leaderboard instead of a dedicated one.")
            .define("modpackId", "");

    public static final ModConfigSpec.ConfigValue<String> MODPACK_DISPLAY_NAME = BUILDER
            .comment("Friendly name shown in the global leaderboard's title (e.g. \"ATM 11\"). Purely " +
                    "cosmetic - has no effect on which leaderboard is used, that's still modpackId. " +
                    "Leave blank to just show a generic title.")
            .define("modpackDisplayName", "");

    public static final ModConfigSpec.IntValue LEADERBOARD_REFRESH_SECONDS = BUILDER
            .comment("Minimum time between re-fetching the global leaderboard from the worker. The server " +
                    "only fetches it on demand, when a player opens the scoreboard dummy's screen and the " +
                    "cached copy is older than this - with nobody opening the screen, no fetch ever happens. " +
                    "Only used while globalLeaderboardEnabled is on.")
            .defineInRange("leaderboardRefreshSeconds", 600, 10, 86_400);

    public static final ModConfigSpec.IntValue RECORD_DEBOUNCE_SECONDS = BUILDER
            .comment("After a hit qualifies for the top 10, how long to wait (without an even bigger " +
                    "qualifying hit from the same player) before actually submitting it. Prevents " +
                    "sending a record that's immediately beaten by the same player's next swing.")
            .defineInRange("recordDebounceSeconds", 20, 5, 3600);

    public static final ModConfigSpec.ConfigValue<String> WORKER_BASE_URL = BUILDER
            .comment("Base URL of the leaderboard worker (e.g. http://127.0.0.1:8787 for a local " +
                    "`wrangler dev` you're running yourself). Leave blank to use the mod's built-in worker.")
            .define("workerBaseUrl", "");

    public static final ModConfigSpec.ConfigValue<String> WORKER_API_KEY = BUILDER
            .comment("Sent as the X-Api-Key header on every submission, if set. Must match modpackId's " +
                    "own key on the worker. Leave blank to use the mod's built-in default modpack's key.")
            .define("workerApiKey", "");

    public static final ModConfigSpec.BooleanValue GLOBAL_LEADERBOARD_ENABLED = BUILDER
            .comment("OFF by default (opt-in). Whether the scoreboard dummy uses the cross-server global " +
                    "leaderboard - fetching it, showing it in the screen, and submitting records to it. " +
                    "When on, the ONLY data this server sends to the leaderboard worker is the player's " +
                    "Minecraft nickname and the damage of a record-breaking hit (plus modpackId), " +
                    "sent to workerBaseUrl at most once per qualifying record after recordDebounceSeconds. No UUID, " +
                    "inventory, equipment, effects or any other player data is sent. The worker address is " +
                    "workerBaseUrl and the worker's source is open (the 'worker' folder of the mod's repository). " +
                    "When off, no network request is ever made; the local (this server only) leaderboard " +
                    "is unaffected either way.")
            .define("globalLeaderboardEnabled", false);

    public static final ModConfigSpec.ConfigValue<List<? extends String>> WEAPON_BLACKLIST = BUILDER
            .comment("Extra item IDs that can never score a leaderboard hit, ADDED ON TOP of the mod's own " +
                    "built-in blacklist (mahoutsukai:morgan, occultism_extra_spirits:forbidden_butcher_knife, every Avaritia and Modern Industrialization item, " +
                    "and anything with \"infinity\" or \"quantum\" in its ID). A hit dealt with a blacklisted item " +
                    "in the player's main hand is ignored entirely, both locally and globally, as if it never " +
                    "happened. Three formats: an exact item ID (\"minecraft:netherite_sword\"), a whole mod's " +
                    "namespace with \"modid:*\" (\"avaritia:*\" blocks every Avaritia item), or - for anything " +
                    "with no \":\" at all, a case-insensitive substring match against every item's ID, so " +
                    "\"sword\" blocks any item from any mod whose ID contains \"sword\". Leave empty to add " +
                    "nothing beyond the built-in blacklist.")
            .defineListAllowEmpty("weaponBlacklist", List.of(),
                    () -> "minecraft:netherite_sword", o -> o instanceof String);

    public static final ModConfigSpec SPEC = BUILDER.build();

    public static String effectiveModpackId() {
        String configured = MODPACK_ID.get();
        return configured.isBlank() ? DEFAULT_MODPACK_ID : configured;
    }

    public static String effectiveWorkerBaseUrl() {
        String configured = WORKER_BASE_URL.get();
        return configured.isBlank() ? DEFAULT_WORKER_BASE_URL : configured;
    }

    public static String effectiveWorkerApiKey() {
        String configured = WORKER_API_KEY.get();
        return configured.isBlank() ? DEFAULT_MODPACK_API_KEY : configured;
    }

    public static boolean isWeaponBlacklisted(Item weapon) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(weapon);
        String idString = id.toString();
        for (String entry : HARDCODED_WEAPON_BLACKLIST) {
            if (matchesBlacklistEntry(entry, id, idString)) {
                return true;
            }
        }
        for (String entry : WEAPON_BLACKLIST.get()) {
            if (matchesBlacklistEntry(entry, id, idString)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matchesBlacklistEntry(String entry, ResourceLocation id, String idString) {
        if (!entry.contains(":")) {
            return idString.toLowerCase(Locale.ROOT).contains(entry.toLowerCase(Locale.ROOT));
        }
        if (entry.equals(idString)) {
            return true;
        }
        return entry.endsWith(":*") && entry.substring(0, entry.length() - 2).equals(id.getNamespace());
    }

    private CommonConfig() {
    }
}
