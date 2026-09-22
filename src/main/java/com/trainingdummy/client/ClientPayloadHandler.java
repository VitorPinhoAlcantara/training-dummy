package com.trainingdummy.client;

import com.trainingdummy.network.DummyDamagePayload;
import com.trainingdummy.network.OpenScoreboardScreenPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ClientPayloadHandler {

    public static void handleDummyDamage(DummyDamagePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientDamageTracker.recordHit(payload.dummyEntityId(), payload.amount(), payload.category(), payload.metric()));
    }

    public static void handleOpenScoreboardScreen(OpenScoreboardScreenPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> Minecraft.getInstance().setScreen(new ScoreboardScreen(
                payload.localEntries(), payload.globalEntries(), payload.globalAvailable(), payload.modpackDisplayName())));
    }

    private ClientPayloadHandler() {
    }
}
