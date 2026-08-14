package com.trainingdummy.client;

import com.trainingdummy.config.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Optional;

/**
 * Client-side only. Every hit adds to a running total ("streak"). If more than
 * hitResetSeconds pass without a hit, the next hit starts a fresh streak instead of adding to
 * the old one. displayDurationSeconds controls how long the number stays visible after the last
 * hit. Both are client config so they (and the SCREEN/CHAT, TOTAL/DPS presets) can be tuned live.
 */
public final class ClientDamageTracker {

    /** "." for the thousands/millions/... groups, "," for the decimal - e.g. 1.234.567,8. */
    private static final DecimalFormat NUMBER_FORMAT;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ROOT);
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        NUMBER_FORMAT = new DecimalFormat("#,##0.0", symbols);
    }

    private static float streakTotal = 0.0F;
    private static long streakStartTick = Long.MIN_VALUE;
    private static long lastHitTick = Long.MIN_VALUE;

    /**
     * Frozen at the moment of the last hit, rather than recomputed every frame - DPS in
     * particular is total/elapsed-time, so recalculating it every single frame while nothing new
     * happens made it drift up and down continuously and was unreadable. It only needs to change
     * when there's actually a new hit to reflect.
     */
    private static Component lastMessage = null;

    public static void recordHit(int dummyId, float amount) {
        long now = currentTick();
        double resetTicks = ClientConfig.HIT_RESET_SECONDS.get() * 20.0D;

        if (lastHitTick == Long.MIN_VALUE || now - lastHitTick > resetTicks) {
            streakTotal = 0.0F;
            streakStartTick = now;
        }
        streakTotal += amount;
        lastHitTick = now;
        lastMessage = formatMessage(currentMetricValue(now));

        if (ClientConfig.DISPLAY_LOCATION.get() == ClientConfig.DisplayLocation.CHAT) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                mc.gui.getChat().addMessage(lastMessage);
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
        return Optional.ofNullable(lastMessage);
    }

    private static double currentMetricValue(long now) {
        if (ClientConfig.DISPLAY_METRIC.get() == ClientConfig.DisplayMetric.DPS) {
            double elapsedSeconds = Math.max((now - streakStartTick) / 20.0D, 0.05D);
            return streakTotal / elapsedSeconds;
        }
        return streakTotal;
    }

    private static Component formatMessage(double value) {
        String formatted = NUMBER_FORMAT.format(value);
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
