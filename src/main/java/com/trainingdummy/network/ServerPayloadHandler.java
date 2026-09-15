package com.trainingdummy.network;

import com.trainingdummy.entity.DummyEntity;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ServerPayloadHandler {

    public static void handleCuriosPage(DummyCuriosPagePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer
                    && serverPlayer.level().getEntity(payload.dummyEntityId()) instanceof DummyEntity dummy
                    && dummy.distanceToSqr(serverPlayer) < 64.0D) {
                dummy.setCuriosPage(payload.page());
                dummy.openMenuFor(serverPlayer);
            }
        });
    }

    public static void handleSetMaxHealth(DummySetMaxHealthPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer
                    && serverPlayer.level().getEntity(payload.dummyEntityId()) instanceof DummyEntity dummy
                    && dummy.distanceToSqr(serverPlayer) < 64.0D) {
                dummy.setMaxHealthOverride(payload.maxHealth());
            }
        });
    }

    public static void handleSetDisplayMetric(DummySetDisplayMetricPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer
                    && serverPlayer.level().getEntity(payload.dummyEntityId()) instanceof DummyEntity dummy
                    && dummy.distanceToSqr(serverPlayer) < 64.0D) {
                dummy.setDisplayMetric(payload.metric());
            }
        });
    }

    public static void handleClearEffects(DummyClearEffectsPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer
                    && serverPlayer.level().getEntity(payload.dummyEntityId()) instanceof DummyEntity dummy
                    && dummy.distanceToSqr(serverPlayer) < 64.0D) {
                dummy.clearEffects();
            }
        });
    }

    private ServerPayloadHandler() {
    }
}
