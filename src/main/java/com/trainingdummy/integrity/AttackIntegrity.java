package com.trainingdummy.integrity;

import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.Map;
import java.util.Set;


public final class AttackIntegrity {

    private static final double EPSILON = 1.0E-4;

    private static final Map<Holder<Attribute>, Double> EXPECTED_BASE = Map.of(
            Attributes.ATTACK_DAMAGE, 1.0,
            Attributes.ATTACK_SPEED, 4.0,
            Attributes.MAX_HEALTH, 20.0,
            Attributes.ARMOR, 0.0,
            Attributes.ARMOR_TOUGHNESS, 0.0,
            Attributes.KNOCKBACK_RESISTANCE, 0.0,
            Attributes.LUCK, 0.0
    );

    public static Set<Holder<Attribute>> trackedAttributes() {
        return EXPECTED_BASE.keySet();
    }

    public static boolean hasTamperedBase(ServerPlayer player) {
        for (Map.Entry<Holder<Attribute>, Double> expected : EXPECTED_BASE.entrySet()) {
            AttributeInstance instance = player.getAttribute(expected.getKey());
            if (instance != null && Math.abs(instance.getBaseValue() - expected.getValue()) > EPSILON) {
                return true;
            }
        }
        return false;
    }

    private AttackIntegrity() {
    }
}
