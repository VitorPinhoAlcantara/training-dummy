package com.trainingdummy.registry;

import com.trainingdummy.TrainingDummyMod;
import com.trainingdummy.recipe.ResetDummySpawnerRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModRecipeSerializers {

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, TrainingDummyMod.MODID);

    public static final DeferredHolder<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<ResetDummySpawnerRecipe>> RESET_DUMMY_SPAWNER =
            RECIPE_SERIALIZERS.register("reset_dummy_spawner",
                    () -> new SimpleCraftingRecipeSerializer<>(ResetDummySpawnerRecipe::new));

    private ModRecipeSerializers() {
    }
}
