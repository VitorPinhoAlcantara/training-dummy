package com.trainingdummy.client;

import com.trainingdummy.network.DummyDamagePayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ClientPayloadHandler {

    public static void handleDummyDamage(DummyDamagePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientDamageTracker.recordHit(payload.dummyEntityId(), payload.amount()));
    }

    private ClientPayloadHandler() {
    }
}
