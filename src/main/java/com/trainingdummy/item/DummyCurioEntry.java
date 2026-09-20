package com.trainingdummy.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;


public record DummyCurioEntry(String identifier, int slot, ItemStack stack) {

    public static final Codec<DummyCurioEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("identifier").forGetter(DummyCurioEntry::identifier),
            Codec.INT.fieldOf("slot").forGetter(DummyCurioEntry::slot),
            ItemStack.OPTIONAL_CODEC.fieldOf("stack").forGetter(DummyCurioEntry::stack)
    ).apply(instance, DummyCurioEntry::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, DummyCurioEntry> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, DummyCurioEntry::identifier,
            ByteBufCodecs.VAR_INT, DummyCurioEntry::slot,
            ItemStack.OPTIONAL_STREAM_CODEC, DummyCurioEntry::stack,
            DummyCurioEntry::new
    );
}
