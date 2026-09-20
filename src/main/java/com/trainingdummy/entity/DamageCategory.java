package com.trainingdummy.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;


public enum DamageCategory {
    PHYSICAL,
    FIRE,
    FREEZING,
    EXPLOSION,
    LIGHTNING,
    MAGIC;

    private static final TagKey<DamageType> IS_MAGIC =
            TagKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath("c", "is_magic"));

    private static final DamageCategory[] VALUES = values();

    public static final StreamCodec<io.netty.buffer.ByteBuf, DamageCategory> STREAM_CODEC =
            ByteBufCodecs.idMapper(i -> VALUES[i], DamageCategory::ordinal);

    public static DamageCategory classify(DamageSource source) {
        if (source.is(DamageTypeTags.IS_FIRE)) {
            return FIRE;
        }
        if (source.is(DamageTypeTags.IS_FREEZING)) {
            return FREEZING;
        }
        if (source.is(DamageTypeTags.IS_EXPLOSION)) {
            return EXPLOSION;
        }
        if (source.is(DamageTypeTags.IS_LIGHTNING)) {
            return LIGHTNING;
        }
        if (source.is(IS_MAGIC)) {
            return MAGIC;
        }
        return PHYSICAL;
    }
}
