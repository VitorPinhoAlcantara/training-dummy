package com.trainingdummy.network;

import com.trainingdummy.TrainingDummyMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record DummyDamagePayload(int dummyEntityId, float amount) implements CustomPacketPayload {

    public static final Type<DummyDamagePayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(TrainingDummyMod.MODID, "dummy_damage"));

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
