package com.trainingdummy.recipe;

import com.mojang.serialization.MapCodec;
import com.trainingdummy.entity.DummyDisplayMetric;
import com.trainingdummy.item.DummyStoredData;
import com.trainingdummy.registry.ModDataComponents;
import com.trainingdummy.registry.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;


public class ResetDummySpawnerRecipe extends CustomRecipe {

    public static final ResetDummySpawnerRecipe INSTANCE = new ResetDummySpawnerRecipe();
    public static final MapCodec<ResetDummySpawnerRecipe> MAP_CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, ResetDummySpawnerRecipe> STREAM_CODEC = StreamCodec.unit(INSTANCE);
    public static final RecipeSerializer<ResetDummySpawnerRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    private static ItemStack findOnlyStack(CraftingInput input) {
        ItemStack only = ItemStack.EMPTY;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (!only.isEmpty()) {
                return ItemStack.EMPTY;
            }
            only = stack;
        }
        return only;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        ItemStack only = findOnlyStack(input);
        return !only.isEmpty() && only.is(ModItems.DUMMY_SPAWNER.get())
                && only.has(ModDataComponents.DUMMY_DATA.get());
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        ItemStack only = findOnlyStack(input);
        if (only.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack result = only.copyWithCount(1);
        result.remove(DataComponents.CUSTOM_NAME);
        DummyStoredData stored = result.get(ModDataComponents.DUMMY_DATA.get());
        if (stored != null) {
            DummyStoredData reset = new DummyStoredData(-1.0D, stored.equipment(), stored.curios(),
                    DummyDisplayMetric.TOTAL, false);
            if (reset.isEmpty()) {
                result.remove(ModDataComponents.DUMMY_DATA.get());
            } else {
                result.set(ModDataComponents.DUMMY_DATA.get(), reset);
            }
        }
        return result;
    }

    @Override
    public RecipeSerializer<ResetDummySpawnerRecipe> getSerializer() {
        return SERIALIZER;
    }

    private ResetDummySpawnerRecipe() {
    }
}
