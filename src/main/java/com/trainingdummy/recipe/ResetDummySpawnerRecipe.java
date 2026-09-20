package com.trainingdummy.recipe;

import com.trainingdummy.entity.DummyDisplayMetric;
import com.trainingdummy.item.DummyStoredData;
import com.trainingdummy.registry.ModDataComponents;
import com.trainingdummy.registry.ModItems;
import com.trainingdummy.registry.ModRecipeSerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;


public class ResetDummySpawnerRecipe extends CustomRecipe {

    public ResetDummySpawnerRecipe(CraftingBookCategory category) {
        super(category);
    }

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
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
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
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 1;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.RESET_DUMMY_SPAWNER.get();
    }
}
