package com.trainingdummy.recipe;

import com.mojang.serialization.MapCodec;
import com.trainingdummy.registry.ModIngredientTypes;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

import java.util.stream.Stream;


public record FoodIngredient() implements ICustomIngredient {

    public static final MapCodec<FoodIngredient> CODEC = MapCodec.unit(FoodIngredient::new);

    @Override
    public boolean test(ItemStack stack) {
        return stack.has(DataComponents.FOOD);
    }

    @Override
    public Stream<ItemStack> getItems() {
        return BuiltInRegistries.ITEM.stream()
                .map(Item::getDefaultInstance)
                .filter(stack -> stack.has(DataComponents.FOOD));
    }

    @Override
    public boolean isSimple() {
        return true;
    }

    @Override
    public IngredientType<?> getType() {
        return ModIngredientTypes.FOOD.get();
    }
}
