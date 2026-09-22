package com.trainingdummy.event;

import com.trainingdummy.TrainingDummyMod;
import com.trainingdummy.entity.DummyEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@EventBusSubscriber(modid = TrainingDummyMod.MODID)
public final class HerobrinePrank {

    private static final int SHAKE_TICKS = 20 * 5;
    private static final int LIGHTNING_TAIL_TICKS = 20 * 5;
    private static final int LIGHTNING_TOTAL_TICKS = SHAKE_TICKS + LIGHTNING_TAIL_TICKS;
    private static final int LIGHTNING_MIN_INTERVAL_TICKS = 8;
    private static final int LIGHTNING_MAX_INTERVAL_TICKS = 20;
    private static final double LIGHTNING_RADIUS = 4.0D;

    private static final int MIN_CURSE_DELAY_TICKS = 20 * 60;
    private static final int MAX_CURSE_DELAY_TICKS = 20 * 60 * 5;

    private record ScheduledTask(long fireAtTick, Runnable action) {
    }

    private static final List<ScheduledTask> TASKS = new CopyOnWriteArrayList<>();

    public static void trigger(DummyEntity dummy, @Nullable ServerPlayer namedBy) {
        if (!(dummy.level() instanceof ServerLevel level)) {
            return;
        }
        Vec3 center = dummy.position();
        dummy.startHerobrineLevitate(SHAKE_TICKS);
        strikeLightningRepeatedly(level, center, LIGHTNING_TOTAL_TICKS);

        UUID namedById = namedBy != null ? namedBy.getUUID() : null;
        schedule(level, SHAKE_TICKS, () -> finish(level, dummy, namedById));
    }

    private static void finish(ServerLevel level, DummyEntity dummy, @Nullable UUID namedById) {
        if (!dummy.isRemoved()) {
            dummy.discard();
        }
        level.getServer().getPlayerList().broadcastSystemMessage(
                Component.literal("Herobrine is watching you...").withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
                false);
        if (namedById != null) {
            long delay = MIN_CURSE_DELAY_TICKS + level.getRandom().nextInt(MAX_CURSE_DELAY_TICKS - MIN_CURSE_DELAY_TICKS + 1);
            schedule(level, delay, () -> curse(level, namedById));
        }
    }

    private static void curse(ServerLevel level, UUID playerId) {
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(playerId);
        if (player == null || !player.isAlive()) {
            return;
        }
        float target = Math.max(1.0F, player.getHealth() / 2.0F);
        player.hurtServer(level, player.damageSources().magic(), 1.0F);
        player.setHealth(target);
    }

    private static void strikeLightningRepeatedly(ServerLevel level, Vec3 center, int ticksRemaining) {
        if (ticksRemaining <= 0) {
            return;
        }
        double angle = level.getRandom().nextDouble() * Math.PI * 2.0D;
        double distance = level.getRandom().nextDouble() * LIGHTNING_RADIUS;
        Vec3 pos = center.add(Math.cos(angle) * distance, 0.0D, Math.sin(angle) * distance);
        strikeLightning(level, pos);

        int interval = LIGHTNING_MIN_INTERVAL_TICKS
                + level.getRandom().nextInt(LIGHTNING_MAX_INTERVAL_TICKS - LIGHTNING_MIN_INTERVAL_TICKS + 1);
        schedule(level, interval, () -> strikeLightningRepeatedly(level, center, ticksRemaining - interval));
    }

    private static void strikeLightning(ServerLevel level, Vec3 pos) {
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
        if (bolt == null) {
            return;
        }
        bolt.snapTo(pos.x, pos.y, pos.z);
        bolt.setVisualOnly(true);
        level.addFreshEntity(bolt);
    }

    private static void schedule(ServerLevel level, long delayTicks, Runnable action) {
        TASKS.add(new ScheduledTask(level.getServer().overworld().getGameTime() + delayTicks, action));
    }

    @SubscribeEvent
    static void onServerTick(ServerTickEvent.Post event) {
        if (TASKS.isEmpty()) {
            return;
        }
        long now = event.getServer().overworld().getGameTime();
        for (ScheduledTask task : TASKS) {
            if (now >= task.fireAtTick()) {
                TASKS.remove(task);
                task.action().run();
            }
        }
    }

    private HerobrinePrank() {
    }
}
