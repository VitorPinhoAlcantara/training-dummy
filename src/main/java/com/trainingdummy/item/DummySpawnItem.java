package com.trainingdummy.item;

import com.trainingdummy.curios.CuriosCompat;
import com.trainingdummy.entity.DummyDisplayMetric;
import com.trainingdummy.entity.DummyEntity;
import com.trainingdummy.event.HerobrinePrank;
import com.trainingdummy.registry.ModDataComponents;
import com.trainingdummy.registry.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.List;


public class DummySpawnItem extends Item {

    public DummySpawnItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("item.trainingdummy.dummy_spawner.tooltip.configure")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.trainingdummy.dummy_spawner.tooltip.remove")
                .withStyle(ChatFormatting.GRAY));

        DummyStoredData stored = stack.get(ModDataComponents.DUMMY_DATA.get());
        if (stored == null) {
            return;
        }
        tooltip.add(Component.translatable("item.trainingdummy.dummy_spawner.tooltip.loaded")
                .withStyle(ChatFormatting.GRAY));

        if (Screen.hasControlDown()) {
            appendCustomizationDetails(stored, tooltip);
        } else {
            tooltip.add(Component.translatable("item.trainingdummy.dummy_spawner.tooltip.holdCtrl")
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }
    }

    private static void appendCustomizationDetails(DummyStoredData stored, List<Component> tooltip) {
        if (stored.maxHealthOverride() > 0.0D) {
            tooltip.add(Component.translatable("item.trainingdummy.dummy_spawner.tooltip.maxHealth",
                            String.valueOf((long) stored.maxHealthOverride()))
                    .withStyle(ChatFormatting.AQUA));
        }
        if (stored.displayMetricCustomized()) {
            tooltip.add(Component.translatable("item.trainingdummy.dummy_spawner.tooltip.displayMetric",
                            Component.translatable(metricNameKey(stored.displayMetric())))
                    .withStyle(ChatFormatting.AQUA));
        }

        List<Component> armorNames = stored.equipment().stream()
                .filter(item -> !item.isEmpty())
                .map(ItemStack::getHoverName)
                .toList();
        if (!armorNames.isEmpty()) {
            tooltip.add(Component.translatable("item.trainingdummy.dummy_spawner.tooltip.equipment",
                            ComponentUtils.formatList(armorNames, Component.literal(", ")))
                    .withStyle(ChatFormatting.AQUA));
        }

        if (!stored.curios().isEmpty()) {
            List<Component> curioNames = stored.curios().stream()
                    .map(entry -> entry.stack().getHoverName())
                    .toList();
            tooltip.add(Component.translatable("item.trainingdummy.dummy_spawner.tooltip.curios",
                            ComponentUtils.formatList(curioNames, Component.literal(", ")))
                    .withStyle(ChatFormatting.AQUA));
        }
    }

    private static String metricNameKey(DummyDisplayMetric metric) {
        return switch (metric) {
            case DPS -> "trainingdummy.display.dps.name";
            case PER_HIT -> "trainingdummy.display.perhit.name";
            case TOTAL -> "trainingdummy.display.total.name";
        };
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

        DummyEntity dummy = ModEntities.DUMMY.get().create(serverLevel);
        if (dummy == null) {
            return InteractionResult.FAIL;
        }

        double x = placePos.getX() + 0.5D;
        double y = placePos.getY();
        double z = placePos.getZ() + 0.5D;

        float facingYaw = context.getRotation() + 180.0F;
        dummy.moveTo(x, y, z, facingYaw, 0.0F);
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
            if (stored.displayMetricCustomized()) {
                dummy.setDisplayMetric(stored.displayMetric());
            }
            List<ItemStack> equipment = stored.equipment();
            List<EquipmentSlot> slots = List.of(EquipmentSlot.values());
            for (int i = 0; i < slots.size() && i < equipment.size(); i++) {
                if (!equipment.get(i).isEmpty()) {
                    dummy.setItemSlot(slots.get(i), equipment.get(i).copy());
                }
            }
            if (!stored.curios().isEmpty() && CuriosCompat.isLoaded()) {
                CuriosCompat.restoreAll(dummy, stored.curios());
            }
        }

        if (context.getPlayer() != null) {
            context.getPlayer().awardStat(Stats.ITEM_USED.get(this));
        }
        spawnerStack.shrink(1);

        if (dummy.hasAutoDeathNickname()) {
            dummy.dieOnPlacement();
        }
        dummy.getPlacementSound().ifPresent(sound -> serverLevel.playSound(null, dummy.getX(), dummy.getY(), dummy.getZ(),
                sound, dummy.getSoundSource(), 1.0F, 1.0F));
        if (dummy.hasHerobrineNickname() && context.getPlayer() instanceof ServerPlayer serverPlayer) {
            HerobrinePrank.trigger(dummy, serverPlayer);
        }
        return InteractionResult.SUCCESS;
    }
}
