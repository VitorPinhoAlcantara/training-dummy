package com.trainingdummy.item;

import com.trainingdummy.curios.CuriosCompat;
import com.trainingdummy.entity.DummyEntity;
import com.trainingdummy.registry.ModDataComponents;
import com.trainingdummy.registry.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Consumer;

public class DummySpawnItem extends Item {

    public DummySpawnItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                 Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        tooltip.accept(Component.translatable("item.trainingdummy.dummy_spawner.tooltip.configure")
                .withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("item.trainingdummy.dummy_spawner.tooltip.remove")
                .withStyle(ChatFormatting.GRAY));
        if (stack.has(ModDataComponents.DUMMY_DATA.get())) {
            tooltip.accept(Component.translatable("item.trainingdummy.dummy_spawner.tooltip.loaded")
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        Direction clickedFace = context.getClickedFace();
        BlockPos placePos = clickedFace == Direction.UP ? clickedPos.above() : clickedPos;

        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        DummyEntity dummy = ModEntities.DUMMY.get().create(serverLevel, EntitySpawnReason.SPAWN_ITEM_USE);
        if (dummy == null) {
            return InteractionResult.FAIL;
        }

        double x = placePos.getX() + 0.5D;
        double y = placePos.getY();
        double z = placePos.getZ() + 0.5D;
        float facingYaw = context.getRotation() + 180.0F;
        dummy.snapTo(x, y, z, facingYaw, 0.0F);
        dummy.setYHeadRot(facingYaw);
        dummy.setYBodyRot(facingYaw);

        ItemStack spawnerStack = context.getItemInHand();
        DummyStoredData stored = spawnerStack.get(ModDataComponents.DUMMY_DATA.get());
        Component itemName = spawnerStack.get(DataComponents.CUSTOM_NAME);
        dummy.setCustomName(itemName != null ? itemName : DummyEntity.DEFAULT_NAME);
        dummy.setCustomNameVisible(true);

        if (!serverLevel.noCollision(dummy, dummy.getBoundingBox())) {
            return InteractionResult.FAIL;
        }

        serverLevel.addFreshEntity(dummy);
        serverLevel.gameEvent(context.getPlayer(), net.minecraft.world.level.gameevent.GameEvent.ENTITY_PLACE, placePos);

        if (stored != null) {
            dummy.setMaxHealthOverride(stored.maxHealthOverride());
            List<ItemStack> equipment = stored.equipment();
            List<EquipmentSlot> slots = EquipmentSlot.VALUES;
            for (int i = 0; i < slots.size() && i < equipment.size(); i++) {
                if (!equipment.get(i).isEmpty()) {
                    dummy.setItemSlot(slots.get(i), equipment.get(i).copy());
                }
            }
            if (!stored.curios().isEmpty() && CuriosCompat.isLoaded()) {
                CuriosCompat.restoreAll(dummy, stored.curios(), serverLevel);
            }
        }

        if (context.getPlayer() != null) {
            context.getPlayer().awardStat(Stats.ITEM_USED.get(this));
        }
        spawnerStack.shrink(1);
        return InteractionResult.SUCCESS;
    }
}
