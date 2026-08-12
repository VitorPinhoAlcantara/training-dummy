package com.trainingdummy.network;

import com.trainingdummy.TrainingDummyMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Sent server -> attacking client whenever a dummy takes a hit, carrying the exact
 * post-mitigation damage amount. All rolling-window/display timing logic lives purely
 * on the client (see client.ClientDamageTracker) so it can be tuned via the client config.
 */
public record DummyDamagePayload(int dummyEntityId, float amount) implements CustomPacketPayload {

    public static final Type<DummyDamagePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(TrainingDummyMod.MODID, "dummy_damage"));

    public static final StreamCodec<ByteBuf, DummyDamagePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, DummyDamagePayload::dummyEntityId,
            ByteBufCodecs.FLOAT, DummyDamagePayload::amount,
            DummyDamagePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
