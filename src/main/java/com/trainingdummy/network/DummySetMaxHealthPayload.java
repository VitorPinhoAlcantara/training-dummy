package com.trainingdummy.network;

import com.trainingdummy.TrainingDummyMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record DummySetMaxHealthPayload(int dummyEntityId, double maxHealth) implements CustomPacketPayload {

    public static final Type<DummySetMaxHealthPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(TrainingDummyMod.MODID, "dummy_set_max_health"));

    public static final StreamCodec<ByteBuf, DummySetMaxHealthPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, DummySetMaxHealthPayload::dummyEntityId,
            ByteBufCodecs.DOUBLE, DummySetMaxHealthPayload::maxHealth,
            DummySetMaxHealthPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
