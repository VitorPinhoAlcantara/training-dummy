package com.trainingdummy.client;

import com.trainingdummy.TrainingDummyMod;
import com.trainingdummy.config.ClientConfig;
import com.trainingdummy.entity.DamageCategory;
import com.trainingdummy.entity.DummyDisplayMetric;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client-side only. Every hit adds to a running total ("streak"). If more than
 * hitResetSeconds pass without a hit, the next hit starts a fresh streak instead of adding to
 * the old one. displayDurationSeconds controls how long the number stays visible after the last
 * hit. Both are client config so they (and the SCREEN/CHAT presets) can be tuned live. The
 * TOTAL/DPS/PER_HIT metric itself is a per-dummy setting instead (see entity.DummyDisplayMetric),
 * and each dummy's streak is tracked independently so alternating hits between two dummies
 * doesn't mix their totals together.
 */
@EventBusSubscriber(modid = TrainingDummyMod.MODID, value = Dist.CLIENT)
public final class ClientDamageTracker {

    /** "." for the thousands/millions/... groups, "," for the decimal - e.g. 1.234.567,8. */
    private static final DecimalFormat NUMBER_FORMAT;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ROOT);
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        NUMBER_FORMAT = new DecimalFormat("#,##0.0", symbols);
    }

    private static final double MIN_DPS_WINDOW_SECONDS = 1.0D;

    // How long a dummy's tracker state is kept around after its last hit, regardless of the
    // configured display duration - just generous headroom so the map doesn't grow forever
    // while still surviving a brief pause between hits.
    private static final double STATE_EXPIRY_SECONDS = 30.0D;

    private static final Map<Integer, PerDummyState> STATES = new ConcurrentHashMap<>();

    private static int lastActiveDummyId = -1;

    public static void recordHit(int dummyId, float amount, DamageCategory category, DummyDisplayMetric metric) {
        long now = currentTick();
        double resetTicks = ClientConfig.HIT_RESET_SECONDS.get() * 20.0D;

        PerDummyState state = STATES.computeIfAbsent(dummyId, id -> new PerDummyState());
        if (state.lastHitTick == Long.MIN_VALUE || now - state.lastHitTick > resetTicks) {
            state.streakTotal = 0.0F;
            state.streakStartTick = now;
        }
        // A single attack (e.g. a weapon dealing both physical and magic damage) can fire several
        // hits in the same tick - group those into one swing instead of the last one overwriting
        // the rest, both for the combined per-hit amount and its type breakdown.
        if (state.lastHitTick != now) {
            state.currentSwingBreakdown.clear();
            state.lastHitAmount = 0.0F;
        }
        state.currentSwingBreakdown.merge(category, amount, Float::sum);
        state.lastHitAmount += amount;
        state.streakTotal += amount;
        state.lastHitTick = now;
        state.metric = metric;
        state.pendingFlush = true;

        lastActiveDummyId = dummyId;
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        long now = currentTick();

        for (var it = STATES.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Integer, PerDummyState> entry = it.next();
            PerDummyState state = entry.getValue();
            if (now - state.lastHitTick > STATE_EXPIRY_SECONDS * 20.0D) {
                it.remove();
                continue;
            }
            if (state.pendingFlush) {
                state.pendingFlush = false;
                state.lastMessage = state.metric == DummyDisplayMetric.PER_HIT
                        ? formatPerHitMessage(state)
                        : formatMessage(currentMetricValue(state, now), state.metric);

                if (ClientConfig.DISPLAY_LOCATION.get() == ClientConfig.DisplayLocation.CHAT) {
                    Minecraft mc = Minecraft.getInstance();
                    if (mc.player != null) {
                        mc.gui.getChat().addMessage(state.lastMessage);
                    }
                }
            }
        }
    }

    /** Value to show on the HUD overlay this frame, or empty if nothing should be shown. */
    public static Optional<Component> currentHudMessage() {
        if (ClientConfig.DISPLAY_LOCATION.get() != ClientConfig.DisplayLocation.SCREEN || lastActiveDummyId == -1) {
            return Optional.empty();
        }
        PerDummyState state = STATES.get(lastActiveDummyId);
        if (state == null) {
            return Optional.empty();
        }
        long now = currentTick();
        double displayDurationTicks = ClientConfig.DISPLAY_DURATION_SECONDS.get() * 20.0D;
        if (now - state.lastHitTick > displayDurationTicks) {
            return Optional.empty();
        }
        return Optional.ofNullable(state.lastMessage);
    }

    private static double currentMetricValue(PerDummyState state, long now) {
        return switch (state.metric) {
            case DPS -> {
                double elapsedSeconds = Math.max((now - state.streakStartTick) / 20.0D, MIN_DPS_WINDOW_SECONDS);
                yield state.streakTotal / elapsedSeconds;
            }
            case PER_HIT -> state.lastHitAmount;
            case TOTAL -> state.streakTotal;
        };
    }

    /**
     * Physical is "normal" and stays unlabeled, same as before this category breakdown existed.
     * Any other single category gets its name shown, and a swing landing more than one category
     * (e.g. a weapon dealing both physical and magic damage) shows a breakdown of all of them.
     */
    private static Component formatPerHitMessage(PerDummyState state) {
        if (state.currentSwingBreakdown.size() == 1) {
            Map.Entry<DamageCategory, Float> only = state.currentSwingBreakdown.entrySet().iterator().next();
            if (only.getKey() == DamageCategory.PHYSICAL) {
                return formatMessage(state.lastHitAmount, DummyDisplayMetric.PER_HIT);
            }
            return Component.translatable("trainingdummy.display.perhit.typed",
                    Component.translatable(categoryNameKey(only.getKey())).getString(),
                    NUMBER_FORMAT.format(state.lastHitAmount));
        }
        StringBuilder breakdown = new StringBuilder();
        for (DamageCategory category : DamageCategory.values()) {
            Float amount = state.currentSwingBreakdown.get(category);
            if (amount == null) {
                continue;
            }
            if (!breakdown.isEmpty()) {
                breakdown.append(" + ");
            }
            breakdown.append(Component.translatable(categoryNameKey(category)).getString())
                    .append(' ')
                    .append(NUMBER_FORMAT.format(amount));
        }
        return Component.translatable("trainingdummy.display.perhit.breakdown",
                breakdown.toString(), NUMBER_FORMAT.format(state.lastHitAmount));
    }

    private static String categoryNameKey(DamageCategory category) {
        return "trainingdummy.damagecategory." + category.name().toLowerCase(Locale.ROOT);
    }

    private static Component formatMessage(double value, DummyDisplayMetric metric) {
        String formatted = NUMBER_FORMAT.format(value);
        String key = switch (metric) {
            case DPS -> "trainingdummy.display.dps";
            case PER_HIT -> "trainingdummy.display.perhit";
            case TOTAL -> "trainingdummy.display.total";
        };
        return Component.translatable(key, formatted);
    }

    private static long currentTick() {
        Minecraft mc = Minecraft.getInstance();
        return mc.level != null ? mc.level.getGameTime() : 0L;
    }

    private static final class PerDummyState {
        float streakTotal = 0.0F;
        long streakStartTick = Long.MIN_VALUE;
        long lastHitTick = Long.MIN_VALUE;
        float lastHitAmount = 0.0F;
        final Map<DamageCategory, Float> currentSwingBreakdown = new EnumMap<>(DamageCategory.class);
        DummyDisplayMetric metric = DummyDisplayMetric.TOTAL;
        Component lastMessage;
        boolean pendingFlush;
    }

    private ClientDamageTracker() {
    }
}
