package com.trainingdummy.event;

import com.trainingdummy.TrainingDummyMod;
import com.trainingdummy.entity.DummyEntity;
import com.trainingdummy.network.DummyDamagePayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Reports the dummy's damage to the attacker. Listens to {@code LivingDamageEvent.Post} instead
 * of reading the {@code amount} parameter inside {@link DummyEntity#hurt}/{@code actuallyHurt} -
 * those are the raw, pre-mitigation damage; armor/shield/enchantment reduction all happen inside
 * vanilla's {@code actuallyHurt} itself (via NeoForge's DamageContainer), so this event is the
 * only place the true final damage is available.
 */
@EventBusSubscriber(modid = TrainingDummyMod.MODID)
public final class DummyCombatEvents {

    @SubscribeEvent
    static void onLivingDamagePost(LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof DummyEntity dummy)) {
            return;
        }
        if (!(event.getSource().getEntity() instanceof ServerPlayer attacker)) {
            return;
        }
        float amount = event.getNewDamage();
        if (amount > 0.0F) {
            PacketDistributor.sendToPlayer(attacker, new DummyDamagePayload(dummy.getId(), amount, dummy.getDisplayMetric()));
        }
    }

    private DummyCombatEvents() {
    }
}
