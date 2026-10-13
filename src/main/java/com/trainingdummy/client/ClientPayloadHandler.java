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
        context.enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (payload.refresh()) {
                if (minecraft.screen instanceof ScoreboardScreen screen) {
                    screen.updateEntries(payload.localEntries(), payload.globalEntries());
                }
                return;
            }
            minecraft.setScreen(new ScoreboardScreen(
                    payload.localEntries(), payload.globalEntries(), payload.globalState(), payload.modpackDisplayName()));
        });
    }

    private ClientPayloadHandler() {
    }
}
