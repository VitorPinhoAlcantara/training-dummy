package com.trainingdummy.client;

import com.trainingdummy.TrainingDummyMod;
import com.trainingdummy.config.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Optional;

@EventBusSubscriber(modid = TrainingDummyMod.MODID, value = Dist.CLIENT)
public final class ClientDamageTracker {

    private static final DecimalFormat NUMBER_FORMAT;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ROOT);
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        NUMBER_FORMAT = new DecimalFormat("#,##0.0", symbols);
    }

    private static final double MIN_DPS_WINDOW_SECONDS = 1.0D;

    private static float streakTotal = 0.0F;
    private static long streakStartTick = Long.MIN_VALUE;
    private static long lastHitTick = Long.MIN_VALUE;

    private static Component lastMessage = null;

    private static boolean pendingFlush = false;

    public static void recordHit(int dummyId, float amount) {
        long now = currentTick();
        double resetTicks = ClientConfig.HIT_RESET_SECONDS.get() * 20.0D;

        if (lastHitTick == Long.MIN_VALUE || now - lastHitTick > resetTicks) {
            streakTotal = 0.0F;
            streakStartTick = now;
        }
        streakTotal += amount;
        lastHitTick = now;
        pendingFlush = true;
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        if (!pendingFlush) {
            return;
        }
        pendingFlush = false;
        lastMessage = formatMessage(currentMetricValue(currentTick()));

        if (ClientConfig.DISPLAY_LOCATION.get() == ClientConfig.DisplayLocation.CHAT) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                mc.player.sendSystemMessage(lastMessage);
            }
        }
    }

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
            double elapsedSeconds = Math.max((now - streakStartTick) / 20.0D, MIN_DPS_WINDOW_SECONDS);
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
