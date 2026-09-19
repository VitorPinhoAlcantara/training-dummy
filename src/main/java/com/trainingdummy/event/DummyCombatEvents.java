package com.trainingdummy.event;

import com.trainingdummy.TrainingDummyMod;
import com.trainingdummy.entity.DamageCategory;
import com.trainingdummy.entity.DummyEntity;
import com.trainingdummy.network.DummyDamagePayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Reports the dummy's damage. Listens to {@code LivingDamageEvent.Post} instead of the raw
 * pre-mitigation amount inside {@link DummyEntity#hurtServer}/{@code actuallyHurt} - armor/shield/
 * enchantment reduction all happen inside vanilla's {@code actuallyHurt} itself, so this event is
 * the only place the true final damage is available.
 *
 * <p>Not every mod attributes its damage source back to the casting player (some magic mods'
 * indirect/summon-based spells never do), so instead of relying on that, every hit just goes out
 * to whoever has actually hit this dummy recently (see DummyEntity#recentAttackers).
 */
@EventBusSubscriber(modid = TrainingDummyMod.MODID)
public final class DummyCombatEvents {

    @SubscribeEvent
    static void onLivingDamagePost(LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof DummyEntity dummy)) {
            return;
        }
        float amount = event.getInflictedDamage();
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
