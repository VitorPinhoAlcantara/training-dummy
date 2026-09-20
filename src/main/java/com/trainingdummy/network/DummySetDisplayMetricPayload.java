package com.trainingdummy.network;

import com.trainingdummy.TrainingDummyMod;
import com.trainingdummy.entity.DummyDisplayMetric;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;


public record DummySetDisplayMetricPayload(int dummyEntityId, DummyDisplayMetric metric)
        implements CustomPacketPayload {

    public static final Type<DummySetDisplayMetricPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(TrainingDummyMod.MODID, "dummy_set_display_metric"));

    public static final StreamCodec<ByteBuf, DummySetDisplayMetricPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, DummySetDisplayMetricPayload::dummyEntityId,
            DummyDisplayMetric.STREAM_CODEC, DummySetDisplayMetricPayload::metric,
            DummySetDisplayMetricPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
