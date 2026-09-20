package com.trainingdummy.network;

import com.trainingdummy.TrainingDummyMod;
import com.trainingdummy.entity.DamageCategory;
import com.trainingdummy.entity.DummyDisplayMetric;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;


public record DummyDamagePayload(int dummyEntityId, float amount, DamageCategory category, DummyDisplayMetric metric)
        implements CustomPacketPayload {

    public static final Type<DummyDamagePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(TrainingDummyMod.MODID, "dummy_damage"));

    public static final StreamCodec<ByteBuf, DummyDamagePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, DummyDamagePayload::dummyEntityId,
            ByteBufCodecs.FLOAT, DummyDamagePayload::amount,
            DamageCategory.STREAM_CODEC, DummyDamagePayload::category,
            DummyDisplayMetric.STREAM_CODEC, DummyDamagePayload::metric,
            DummyDamagePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
