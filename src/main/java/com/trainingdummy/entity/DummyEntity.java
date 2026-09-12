package com.trainingdummy.entity;

import com.trainingdummy.config.CommonConfig;
import com.trainingdummy.curios.CuriosCompat;
import com.trainingdummy.item.DummyCurioEntry;
import com.trainingdummy.item.DummyStoredData;
import com.trainingdummy.menu.DummyMenu;
import com.trainingdummy.registry.ModDataComponents;
import com.trainingdummy.registry.ModItems;
import com.trainingdummy.registry.ModSounds;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForgeMod;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DummyEntity extends LivingEntity {

    private static final int BAIT_SCAN_INTERVAL_TICKS = 20;

    public static final Component DEFAULT_NAME = Component.translatable("entity.trainingdummy.dummy");

    private int curiosPage = 0;

    private double maxHealthOverride = -1.0D;

    private DummyDisplayMetric displayMetric = DummyDisplayMetric.TOTAL;
    private boolean displayMetricCustomized = false;

    public DummyEntity(EntityType<? extends DummyEntity> type, Level level) {
        super(type, level);
        this.noPhysics = false;
        this.setNoGravity(false);
        this.applyMaxHealth();
    }

    private void applyMaxHealth() {
        AttributeInstance maxHealthAttribute = this.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealthAttribute != null) {
            maxHealthAttribute.setBaseValue(this.effectiveMaxHealthConfig());
        }
        this.setHealth(this.getMaxHealth());
    }

    private double effectiveMaxHealthConfig() {
        return this.maxHealthOverride > 0.0D ? this.maxHealthOverride : CommonConfig.MAX_HEALTH.get();
    }

    public double getMaxHealthOverride() {
        return this.maxHealthOverride;
    }

    public void setMaxHealthOverride(double value) {
        this.maxHealthOverride = value > 0.0D ? Math.min(value, 1_000_000_000.0D) : -1.0D;
        this.applyMaxHealth();
    }

    public DummyDisplayMetric getDisplayMetric() {
        return this.displayMetric;
    }

    public boolean isDisplayMetricCustomized() {
        return this.displayMetricCustomized;
    }

    public void setDisplayMetric(DummyDisplayMetric metric) {
        this.displayMetric = metric;
        this.displayMetricCustomized = true;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 1_000_000.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.ATTACK_DAMAGE)
                .add(Attributes.ATTACK_SPEED)
                .add(Attributes.LUCK)
                .add(Attributes.BLOCK_INTERACTION_RANGE)
                .add(Attributes.BLOCK_BREAK_SPEED)
                .add(Attributes.SUBMERGED_MINING_SPEED)
                .add(Attributes.SNEAKING_SPEED)
                .add(Attributes.MINING_EFFICIENCY)
                .add(Attributes.SWEEPING_DAMAGE_RATIO)
                .add(Attributes.WAYPOINT_TRANSMIT_RANGE)
                .add(Attributes.WAYPOINT_RECEIVE_RANGE)
                .add(NeoForgeMod.CREATIVE_FLIGHT);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getHealth() < this.getMaxHealth()) {
            this.setHealth(this.getMaxHealth());
        }
        if (!this.level().isClientSide()) {
            if (!this.isUsingItem() && this.getOffhandItem().is(Items.SHIELD)) {
                this.startUsingItem(InteractionHand.OFF_HAND);
            }
            if (this.tickCount % BAIT_SCAN_INTERVAL_TICKS == 0 && this.getMainHandItem().is(ModItems.LURE_BAIT.get())) {
                this.lureNearbyMobs();
            }
        }
    }

    private void lureNearbyMobs() {
        double radius = CommonConfig.BAIT_RADIUS.get();
        AABB area = this.getBoundingBox().inflate(radius);
        List<Mob> nearby = this.level().getEntitiesOfClass(Mob.class, area,
                mob -> mob.isAlive() && mob instanceof Enemy);
        for (Mob mob : nearby) {
            if (mob.hasLineOfSight(this)) {
                mob.setTarget(this);
                mob.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, this);
            }
            if (mob instanceof AbstractPiglin) {
                mob.getBrain().setMemoryWithExpiry(MemoryModuleType.ANGRY_AT, this.getUUID(), 600L);
            }
        }
    }

    private static boolean isStickHit(DamageSource source) {
        return source.getDirectEntity() instanceof Player player && player.getMainHandItem().is(Items.STICK);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (isStickHit(source)) {
            level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ARMOR_STAND_BREAK,
                    this.getSoundSource(), 1.0F, 1.0F);
            this.spawnLoadedDummySpawner(level);
            this.discard();
            return true;
        }
        return super.hurtServer(level, source, amount);
    }

    private void spawnLoadedDummySpawner(ServerLevel level) {
        List<ItemStack> equipment = new ArrayList<>(EquipmentSlot.VALUES.size());
        for (EquipmentSlot slot : EquipmentSlot.VALUES) {
            equipment.add(this.getItemBySlot(slot).copy());
        }
        List<DummyCurioEntry> curios = CuriosCompat.isLoaded() ? CuriosCompat.captureAll(this) : List.of();
        DummyStoredData stored = new DummyStoredData(this.maxHealthOverride, equipment, curios,
                this.displayMetric, this.displayMetricCustomized);

        ItemStack spawnerStack = new ItemStack(ModItems.DUMMY_SPAWNER.get());
        if (this.getCustomName() != null && !this.getCustomName().equals(DEFAULT_NAME)) {
            spawnerStack.set(DataComponents.CUSTOM_NAME, this.getCustomName());
        }
        if (!stored.isEmpty()) {
            spawnerStack.set(ModDataComponents.DUMMY_DATA.get(), stored);
        }
        this.spawnAtLocation(level, spawnerStack);
    }

    @Override
    protected void actuallyHurt(ServerLevel level, DamageSource source, float amount) {
        super.actuallyHurt(level, source, amount);
        if (this.getHealth() < this.getMaxHealth()) {
            this.setHealth(this.getMaxHealth());
        }
    }

    private static final java.util.Map<String, java.util.function.Supplier<SoundEvent>> NAMED_HURT_SOUNDS = java.util.Map.of(
            "Danrique", ModSounds.DANRIQUE_HURT,
            "MitinhoPlayer", ModSounds.MITINHOPLAYER_HURT,
            "Nofaxu", ModSounds.NOFAXU_HURT,
            "BrunimNeets", ModSounds.BRUNIMNEETS_HURT,
            "mamao170", ModSounds.MAMAO170_HURT,
            "JazaraGamer", ModSounds.JAZARAGAMER_HURT
    );

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        java.util.function.Supplier<SoundEvent> named = NAMED_HURT_SOUNDS.get(this.getSkinName());
        return named != null ? named.get() : ModSounds.DUMMY_HURT.get();
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand, Vec3 location) {
        ItemStack held = player.getItemInHand(hand);
        if (held.is(Items.STICK) && !this.level().isClientSide()) {
            this.openMenuFor(player);
            return InteractionResult.CONSUME;
        }
        if (held.is(Items.NAME_TAG) && held.has(DataComponents.CUSTOM_NAME)) {
            if (!this.level().isClientSide()) {
                this.setCustomName(held.get(DataComponents.CUSTOM_NAME));
                this.setCustomNameVisible(true);
                if (!player.getAbilities().instabuild) {
                    held.shrink(1);
                }
            }
            return InteractionResult.SUCCESS;
        }
        return super.interact(player, hand, location);
    }

    public String getSkinName() {
        return this.getCustomName() != null ? this.getCustomName().getString() : "";
    }

    public void clearNegativeEffects() {
        List<MobEffectInstance> harmful = this.getActiveEffects().stream()
                .filter(effect -> effect.getEffect().value().getCategory() == MobEffectCategory.HARMFUL)
                .collect(Collectors.toList());
        for (MobEffectInstance effect : harmful) {
            this.removeEffect(effect.getEffect());
        }
    }

    public int getCuriosPage() {
        return this.curiosPage;
    }

    public void setCuriosPage(int page) {
        this.curiosPage = Math.max(0, page);
    }

    public void openMenuFor(Player player) {
        player.openMenu(new SimpleMenuProvider(
                (containerId, playerInventory, p) -> new DummyMenu(containerId, playerInventory, this),
                Component.translatable("container.trainingdummy.dummy")
        ), buf -> {
            buf.writeVarInt(this.getId());
            buf.writeVarInt(this.curiosPage);
            buf.writeEnum(this.displayMetric);
            buf.writeBoolean(this.displayMetricCustomized);
        });
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void push(net.minecraft.world.entity.Entity entity) {
    }

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    @Override
    public boolean canBeSeenAsEnemy() {
        return super.canBeSeenAsEnemy() && this.getMainHandItem().is(ModItems.LURE_BAIT.get());
    }

    @Override
    public boolean isAttackable() {
        return true;
    }

    @Override
    public boolean shouldShowName() {
        return true;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (this.maxHealthOverride > 0.0D) {
            output.putDouble("MaxHealthOverride", this.maxHealthOverride);
        }
        if (this.displayMetricCustomized) {
            output.putString("DisplayMetric", this.displayMetric.name());
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.maxHealthOverride = input.getDoubleOr("MaxHealthOverride", -1.0D);
        this.applyMaxHealth();
        Optional<String> savedMetric = input.getString("DisplayMetric");
        this.displayMetricCustomized = savedMetric.isPresent();
        this.displayMetric = savedMetric.map(DummyEntity::parseDisplayMetric).orElse(DummyDisplayMetric.TOTAL);
    }

    private static DummyDisplayMetric parseDisplayMetric(String name) {
        try {
            return DummyDisplayMetric.valueOf(name);
        } catch (IllegalArgumentException e) {
            return DummyDisplayMetric.TOTAL;
        }
    }
}
