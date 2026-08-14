package com.trainingdummy.network;

import com.trainingdummy.TrainingDummyMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Sent client -> server when the player edits the max health field in the dummy's inventory
 * screen. {@code maxHealth <= 0} clears the per-dummy override, going back to the global
 * {@code CommonConfig#MAX_HEALTH} default - see entity.DummyEntity#setMaxHealthOverride.
 */
public record DummySetMaxHealthPayload(int dummyEntityId, double maxHealth) implements CustomPacketPayload {

    public static final Type<DummySetMaxHealthPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(TrainingDummyMod.MODID, "dummy_set_max_health"));

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
