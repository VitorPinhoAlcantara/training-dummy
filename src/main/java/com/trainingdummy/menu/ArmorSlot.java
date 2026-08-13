package com.trainingdummy.menu;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * An equipment slot with a vanilla empty-slot icon (helmet/chestplate/.../shield silhouette,
 * same sprites the survival inventory uses) and a hover-name tooltip when empty. Only accepts
 * items that actually belong in the given {@link EquipmentSlot} (a diamond chestplate can't be
 * shoved into the boots slot) - pass {@code restrictTo} as {@code null} for an unrestricted slot
 * like offhand, which vanilla itself allows to hold anything.
 */
public class ArmorSlot extends Slot implements SlotTooltip {

    private final Component tooltipName;
    private final LivingEntity wearer;
    private final EquipmentSlot restrictTo;
    private final @Nullable Identifier icon;

    public ArmorSlot(Container container, int index, int x, int y, Component tooltipName,
                      @Nullable Identifier icon, LivingEntity wearer, EquipmentSlot restrictTo) {
        super(container, index, x, y);
        this.tooltipName = tooltipName;
        this.wearer = wearer;
        this.restrictTo = restrictTo;
        this.icon = icon;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return this.restrictTo == null || this.wearer.getEquipmentSlotForItem(stack) == this.restrictTo;
    }

    @Override
    public @Nullable Identifier getNoItemIcon() {
        return this.icon;
    }

    @Override
    public Component getTooltipName() {
        return this.tooltipName;
    }
}
