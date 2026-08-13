package com.trainingdummy.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/** No behavior of its own - entity.DummyEntity checks for this item in the dummy's main hand. */
public class LureBaitItem extends Item {

    public LureBaitItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("item.trainingdummy.lure_bait.tooltip")
                .withStyle(ChatFormatting.GRAY));
    }
}
