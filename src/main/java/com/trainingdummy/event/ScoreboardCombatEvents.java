package com.trainingdummy.event;

import com.trainingdummy.TrainingDummyMod;
import com.trainingdummy.config.CommonConfig;
import com.trainingdummy.entity.ScoreboardDummyEntity;
import com.trainingdummy.integrity.AttackIntegrity;
import com.trainingdummy.rank.LeaderboardCache;
import com.trainingdummy.rank.PendingCandidateTracker;
import com.trainingdummy.snapshot.PlayerCombatSnapshot;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

@EventBusSubscriber(modid = TrainingDummyMod.MODID)
public final class ScoreboardCombatEvents {

    @SubscribeEvent
    static void onLivingDamagePost(LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof ScoreboardDummyEntity)) {
            return;
        }
        if (!(event.getSource().getEntity() instanceof ServerPlayer attacker)) {
            return;
        }
        float amount = event.getNewDamage();
        if (amount <= 0.0F) {
            return;
        }

        if (attacker.isCreative()) {
            return;
        }
        if (CommonConfig.isWeaponBlacklisted(attacker.getMainHandItem().getItem())) {
            return;
        }
        if (AttackIntegrity.hasTamperedBase(attacker)) {
            TrainingDummyMod.LOGGER.warn("Ignoring dummy hit from {} for leaderboard purposes - a combat " +
                    "attribute's base value doesn't match vanilla defaults (possible /attribute tampering)",
                    attacker.getGameProfile().getName());
            return;
        }

        String playerName = attacker.getGameProfile().getName();

        if (LeaderboardCache.qualifiesLocal(playerName, amount)) {
            int localRank = LeaderboardCache.applyLocalUpdate(playerName, attacker.getUUID(), amount);
            attacker.sendSystemMessage(Component.translatable("trainingdummy.record.local", localRank));
        }

        if (LeaderboardCache.globalAvailable() && LeaderboardCache.qualifiesGlobal(playerName, amount)) {
            int globalRank = LeaderboardCache.applyGlobalOptimisticUpdate(playerName, attacker.getUUID(), amount);
            attacker.sendSystemMessage(Component.translatable("trainingdummy.record.achieved", globalRank));

            PlayerCombatSnapshot snapshot = PlayerCombatSnapshot.capture(attacker, amount);
            long debounceTicks = CommonConfig.RECORD_DEBOUNCE_SECONDS.get() * 20L;
            PendingCandidateTracker.offerCandidate(snapshot, attacker.level().getGameTime(), debounceTicks);
        }
    }

    private ScoreboardCombatEvents() {
    }
}
