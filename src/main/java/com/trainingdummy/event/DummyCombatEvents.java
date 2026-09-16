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
        float amount = event.getNewDamage();
        if (amount <= 0.0F) {
            return;
        }
        if (event.getSource().getEntity() instanceof ServerPlayer attacker) {
            dummy.rememberAttacker(attacker);
        }
        // Not every mod attributes its damage source back to the casting player (some magic mods'
        // indirect/summon-based spells never do), so instead of relying on that, every hit just
        // goes out to whoever has actually hit this dummy recently.
        DummyDamagePayload payload = new DummyDamagePayload(dummy.getId(), amount,
                DamageCategory.classify(event.getSource()), dummy.getDisplayMetric());
        for (ServerPlayer recent : dummy.recentAttackers()) {
            PacketDistributor.sendToPlayer(recent, payload);
        }
    }

    private DummyCombatEvents() {
    }
}
