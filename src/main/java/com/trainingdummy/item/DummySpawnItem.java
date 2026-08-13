package com.trainingdummy.item;

import com.trainingdummy.entity.DummyEntity;
import com.trainingdummy.registry.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

/**
 * Places a {@link DummyEntity} on the clicked block, mirroring vanilla's ArmorStandItem.
 */
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
        // Face the player who placed it, not the direction the player was facing.
        float facingYaw = context.getRotation() + 180.0F;
        dummy.moveTo(x, y, z, facingYaw, 0.0F);
        dummy.setYHeadRot(facingYaw);
        dummy.setYBodyRot(facingYaw);
        dummy.setCustomName(net.minecraft.network.chat.Component.translatable("entity.trainingdummy.dummy"));
        dummy.setCustomNameVisible(true);

        if (!serverLevel.noCollision(dummy, dummy.getBoundingBox())) {
            return InteractionResult.FAIL;
        }

        serverLevel.addFreshEntity(dummy);
        serverLevel.gameEvent(context.getPlayer(), net.minecraft.world.level.gameevent.GameEvent.ENTITY_PLACE, placePos);

        if (context.getPlayer() != null) {
            context.getPlayer().awardStat(Stats.ITEM_USED.get(this));
        }
        context.getItemInHand().shrink(1);
        return InteractionResult.SUCCESS;
    }
}
