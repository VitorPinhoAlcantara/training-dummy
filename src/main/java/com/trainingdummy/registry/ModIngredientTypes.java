package com.trainingdummy.registry;

import com.trainingdummy.TrainingDummyMod;
import com.trainingdummy.recipe.FoodIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModIngredientTypes {

    public static final DeferredRegister<IngredientType<?>> INGREDIENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.INGREDIENT_TYPES, TrainingDummyMod.MODID);

    public static final DeferredHolder<IngredientType<?>, IngredientType<FoodIngredient>> FOOD =
            INGREDIENT_TYPES.register("food", () -> new IngredientType<>(FoodIngredient.CODEC));

    private ModIngredientTypes() {
    }
}
