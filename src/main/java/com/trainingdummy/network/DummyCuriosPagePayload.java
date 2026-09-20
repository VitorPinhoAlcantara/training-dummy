package com.trainingdummy.network;

import com.trainingdummy.TrainingDummyMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;


public record DummyCuriosPagePayload(int dummyEntityId, int page) implements CustomPacketPayload {

    public static final Type<DummyCuriosPagePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(TrainingDummyMod.MODID, "dummy_curios_page"));

    public static final StreamCodec<ByteBuf, DummyCuriosPagePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, DummyCuriosPagePayload::dummyEntityId,
            ByteBufCodecs.VAR_INT, DummyCuriosPagePayload::page,
            DummyCuriosPagePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
