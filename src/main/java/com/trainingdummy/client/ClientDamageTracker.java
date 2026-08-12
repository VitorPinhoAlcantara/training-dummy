package com.trainingdummy.client;

import com.trainingdummy.config.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.Locale;
import java.util.Optional;

/**
 * Client-side only. Every hit adds to a running total ("streak"). If more than
 * hitResetSeconds pass without a hit, the next hit starts a fresh streak instead of adding to
 * the old one. displayDurationSeconds controls how long the number stays visible after the last
 * hit. Both are client config so they (and the SCREEN/CHAT, TOTAL/DPS presets) can be tuned live.
 */
public final class ClientDamageTracker {

    private static float streakTotal = 0.0F;
    private static long streakStartTick = Long.MIN_VALUE;
    private static long lastHitTick = Long.MIN_VALUE;

    public static void recordHit(int dummyId, float amount) {
        long now = currentTick();
        double resetTicks = ClientConfig.HIT_RESET_SECONDS.get() * 20.0D;

        if (lastHitTick == Long.MIN_VALUE || now - lastHitTick > resetTicks) {
            streakTotal = 0.0F;
            streakStartTick = now;
        }
        streakTotal += amount;
        lastHitTick = now;

        if (ClientConfig.DISPLAY_LOCATION.get() == ClientConfig.DisplayLocation.CHAT) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                mc.gui.getChat().addMessage(formatMessage(currentMetricValue(now)));
            }
        }
    }

    /** Value to show on the HUD overlay this frame, or empty if nothing should be shown. */
    public static Optional<Component> currentHudMessage() {
        if (ClientConfig.DISPLAY_LOCATION.get() != ClientConfig.DisplayLocation.SCREEN || lastHitTick == Long.MIN_VALUE) {
            return Optional.empty();
        }
        long now = currentTick();
        double displayDurationTicks = ClientConfig.DISPLAY_DURATION_SECONDS.get() * 20.0D;
        if (now - lastHitTick > displayDurationTicks) {
            return Optional.empty();
        }
        return Optional.of(formatMessage(currentMetricValue(now)));
    }

    private static double currentMetricValue(long now) {
        if (ClientConfig.DISPLAY_METRIC.get() == ClientConfig.DisplayMetric.DPS) {
            double elapsedSeconds = Math.max((now - streakStartTick) / 20.0D, 0.05D);
            return streakTotal / elapsedSeconds;
        }
        return streakTotal;
    }

    private static Component formatMessage(double value) {
        String formatted = String.format(Locale.ROOT, "%.1f", value);
        String key = ClientConfig.DISPLAY_METRIC.get() == ClientConfig.DisplayMetric.DPS
                ? "trainingdummy.display.dps"
                : "trainingdummy.display.total";
        return Component.translatable(key, formatted);
    }

    private static long currentTick() {
        Minecraft mc = Minecraft.getInstance();
        return mc.level != null ? mc.level.getGameTime() : 0L;
    }

    private ClientDamageTracker() {
    }
}
