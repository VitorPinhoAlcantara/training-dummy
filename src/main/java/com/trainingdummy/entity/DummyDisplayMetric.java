package com.trainingdummy.entity;

import com.mojang.serialization.Codec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;


public enum DummyDisplayMetric implements StringRepresentable {
    TOTAL,
    DPS,
    PER_HIT;

    public static final DummyDisplayMetric[] VALUES = values();

    public static final Codec<DummyDisplayMetric> CODEC = StringRepresentable.fromEnum(DummyDisplayMetric::values);

    public static final StreamCodec<io.netty.buffer.ByteBuf, DummyDisplayMetric> STREAM_CODEC =
            ByteBufCodecs.idMapper(i -> VALUES[i], DummyDisplayMetric::ordinal);

    public DummyDisplayMetric next() {
        return VALUES[(this.ordinal() + 1) % VALUES.length];
    }

    @Override
    public String getSerializedName() {
        return this.name().toLowerCase(Locale.ROOT);
    }
}
