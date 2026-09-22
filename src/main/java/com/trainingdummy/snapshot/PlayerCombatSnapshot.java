package com.trainingdummy.snapshot;

import com.trainingdummy.curios.CuriosCompat;
import com.trainingdummy.integrity.AttackIntegrity;
import com.trainingdummy.item.DummyCurioEntry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record PlayerCombatSnapshot(
        String playerName,
        UUID playerUuid,
        float damage,
        long gameTime,
        List<ItemStack> inventory,
        List<ItemStack> armor,
        ItemStack mainHand,
        ItemStack offHand,
        List<DummyCurioEntry> curios,
        List<MobEffectInstance> effects,
        List<CapturedAttribute> attributes
) {

    public record CapturedAttribute(String id, double base) {
        public static final Codec<CapturedAttribute> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("id").forGetter(CapturedAttribute::id),
                Codec.DOUBLE.fieldOf("base").forGetter(CapturedAttribute::base)
        ).apply(instance, CapturedAttribute::new));
    }

    public static final Codec<PlayerCombatSnapshot> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("player_name").forGetter(PlayerCombatSnapshot::playerName),
            UUIDUtil.STRING_CODEC.fieldOf("player_uuid").forGetter(PlayerCombatSnapshot::playerUuid),
            Codec.FLOAT.fieldOf("damage").forGetter(PlayerCombatSnapshot::damage),
            Codec.LONG.fieldOf("game_time").forGetter(PlayerCombatSnapshot::gameTime),
            ItemStack.OPTIONAL_CODEC.listOf().fieldOf("inventory").forGetter(PlayerCombatSnapshot::inventory),
            ItemStack.OPTIONAL_CODEC.listOf().fieldOf("armor").forGetter(PlayerCombatSnapshot::armor),
            ItemStack.OPTIONAL_CODEC.fieldOf("main_hand").forGetter(PlayerCombatSnapshot::mainHand),
            ItemStack.OPTIONAL_CODEC.fieldOf("off_hand").forGetter(PlayerCombatSnapshot::offHand),
            DummyCurioEntry.CODEC.listOf().fieldOf("curios").forGetter(PlayerCombatSnapshot::curios),
            MobEffectInstance.CODEC.listOf().fieldOf("effects").forGetter(PlayerCombatSnapshot::effects),
            CapturedAttribute.CODEC.listOf().fieldOf("attributes").forGetter(PlayerCombatSnapshot::attributes)
    ).apply(instance, PlayerCombatSnapshot::new));

    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    public static PlayerCombatSnapshot capture(ServerPlayer player, float damage) {
        List<ItemStack> inventory = new ArrayList<>();

        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            if (!stack.isEmpty()) {
                inventory.add(stack.copy());
            }
        }

        List<ItemStack> armor = new ArrayList<>(ARMOR_SLOTS.length);
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            armor.add(player.getItemBySlot(slot).copy());
        }

        List<DummyCurioEntry> curios = CuriosCompat.isLoaded()
                ? CuriosCompat.captureAll(player)
                : List.of();

        List<CapturedAttribute> attributes = new ArrayList<>();
        for (Holder<Attribute> attribute : AttackIntegrity.trackedAttributes()) {
            AttributeInstance instance = player.getAttribute(attribute);
            if (instance != null) {
                String id = BuiltInRegistries.ATTRIBUTE.getKey(attribute.value()).toString();
                attributes.add(new CapturedAttribute(id, instance.getBaseValue()));
            }
        }

        return new PlayerCombatSnapshot(
                player.getGameProfile().name(),
                player.getUUID(),
                damage,
                player.level().getGameTime(),
                inventory,
                armor,
                player.getItemBySlot(EquipmentSlot.MAINHAND).copy(),
                player.getItemBySlot(EquipmentSlot.OFFHAND).copy(),
                curios,
                List.copyOf(player.getActiveEffects()),
                attributes
        );
    }
}
