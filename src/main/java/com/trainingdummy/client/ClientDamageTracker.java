package com.trainingdummy.client;

import com.trainingdummy.TrainingDummyMod;
import com.trainingdummy.config.ClientConfig;
import com.trainingdummy.entity.DummyDisplayMetric;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

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

    // How long a dummy's tracker state is kept around after its last hit, regardless of the
    // configured display duration - just generous headroom so the map doesn't grow forever
    // while still surviving a brief pause between hits.
    private static final double STATE_EXPIRY_SECONDS = 30.0D;

    private static final Map<Integer, PerDummyState> STATES = new ConcurrentHashMap<>();

    private static int lastActiveDummyId = -1;

    public static void recordHit(int dummyId, float amount, DummyDisplayMetric metric) {
        long now = currentTick();
        double resetTicks = ClientConfig.HIT_RESET_SECONDS.get() * 20.0D;

        PerDummyState state = STATES.computeIfAbsent(dummyId, id -> new PerDummyState());
        if (state.lastHitTick == Long.MIN_VALUE || now - state.lastHitTick > resetTicks) {
            state.streakTotal = 0.0F;
            state.streakStartTick = now;
        }
        state.streakTotal += amount;
        state.lastHitAmount = amount;
        state.lastHitTick = now;
        state.metric = metric;
        state.pendingFlush = true;

        lastActiveDummyId = dummyId;
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        long now = currentTick();

        for (Iterator<Map.Entry<Integer, PerDummyState>> it = STATES.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Integer, PerDummyState> entry = it.next();
            PerDummyState state = entry.getValue();
            if (now - state.lastHitTick > STATE_EXPIRY_SECONDS * 20.0D) {
                it.remove();
                continue;
            }
            if (state.pendingFlush) {
                state.pendingFlush = false;
                state.lastMessage = formatMessage(currentMetricValue(state, now), state.metric);

                if (ClientConfig.DISPLAY_LOCATION.get() == ClientConfig.DisplayLocation.CHAT) {
                    Minecraft mc = Minecraft.getInstance();
                    if (mc.player != null) {
                        mc.player.sendSystemMessage(state.lastMessage);
                    }
                }
            }
        }
    }

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
        DummyDisplayMetric metric = DummyDisplayMetric.TOTAL;
        Component lastMessage;
        boolean pendingFlush;
    }

    private ClientDamageTracker() {
    }
}
