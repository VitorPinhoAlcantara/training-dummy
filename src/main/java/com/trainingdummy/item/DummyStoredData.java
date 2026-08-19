package com.trainingdummy.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record DummyStoredData(
        double maxHealthOverride,
        List<ItemStack> equipment,
        List<DummyCurioEntry> curios
) {

    public static final Codec<DummyStoredData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("max_health_override").forGetter(DummyStoredData::maxHealthOverride),
            ItemStack.OPTIONAL_CODEC.listOf().fieldOf("equipment").forGetter(DummyStoredData::equipment),
            DummyCurioEntry.CODEC.listOf().fieldOf("curios").forGetter(DummyStoredData::curios)
    ).apply(instance, DummyStoredData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, DummyStoredData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, DummyStoredData::maxHealthOverride,
            ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list()), DummyStoredData::equipment,
            DummyCurioEntry.STREAM_CODEC.apply(ByteBufCodecs.list()), DummyStoredData::curios,
            DummyStoredData::new
    );

    public boolean isEmpty() {
        return this.maxHealthOverride <= 0.0D
                && this.equipment.stream().allMatch(ItemStack::isEmpty) && this.curios.isEmpty();
    }
}
