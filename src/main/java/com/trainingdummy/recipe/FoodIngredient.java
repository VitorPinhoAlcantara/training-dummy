package com.trainingdummy.recipe;

import com.mojang.serialization.MapCodec;
import com.trainingdummy.registry.ModIngredientTypes;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

import java.util.stream.Stream;

/**
 * Matches any item that has {@link net.minecraft.world.food.FoodProperties} - i.e. "any food",
 * for the lure bait recipe (the user wants "whatever food you've got" to work, not one fixed
 * item). Used in recipe JSON as {@code {"type": "trainingdummy:food"}}.
 */
public record FoodIngredient() implements ICustomIngredient {

    public static final MapCodec<FoodIngredient> CODEC = MapCodec.unit(FoodIngredient::new);

    @Override
    public boolean test(ItemStack stack) {
        return stack.has(DataComponents.FOOD);
    }

    @Override
    public Stream<Holder<Item>> items() {
        return BuiltInRegistries.ITEM.listElements()
                .map(holder -> (Holder<Item>) holder)
                .filter(holder -> holder.value().getDefaultInstance().has(DataComponents.FOOD));
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
