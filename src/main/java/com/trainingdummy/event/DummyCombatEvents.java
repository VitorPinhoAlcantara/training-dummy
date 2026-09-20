package com.trainingdummy.event;

import com.trainingdummy.TrainingDummyMod;
import com.trainingdummy.entity.DamageCategory;
import com.trainingdummy.entity.DummyEntity;
import com.trainingdummy.network.DummyDamagePayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.network.PacketDistributor;



@EventBusSubscriber(modid = TrainingDummyMod.MODID)
public final class DummyCombatEvents {

    @SubscribeEvent
    static void onLivingDamagePost(LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof DummyEntity dummy)) {
            return;
        }
        float amount = event.getInflictedDamage();
        if (Float.isInfinite(amount) || Float.isNaN(amount) || amount == Float.MAX_VALUE) {
            amount = Float.MAX_VALUE;
            if (event.getSource().getEntity() instanceof ServerPlayer attacker) {
                attacker.sendSystemMessage(Component.translatable("trainingdummy.combat.damageCapped"));
            }
        }
        if (amount <= 0.0F) {
            return;
        }
        if (event.getSource().getEntity() instanceof ServerPlayer attacker) {
            dummy.rememberAttacker(attacker);
        }
        DummyDamagePayload payload = new DummyDamagePayload(dummy.getId(), amount,
                DamageCategory.classify(event.getSource()), dummy.getDisplayMetric());
        for (ServerPlayer recent : dummy.recentAttackers()) {
            PacketDistributor.sendToPlayer(recent, payload);
        }
    }

    private DummyCombatEvents() {
    }
}
