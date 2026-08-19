package com.trainingdummy.network;

import com.trainingdummy.TrainingDummyMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record DummyClearNegativeEffectsPayload(int dummyEntityId) implements CustomPacketPayload {

    public static final Type<DummyClearNegativeEffectsPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(TrainingDummyMod.MODID, "dummy_clear_negative_effects"));

    public static final StreamCodec<ByteBuf, DummyClearNegativeEffectsPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, DummyClearNegativeEffectsPayload::dummyEntityId,
            DummyClearNegativeEffectsPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
