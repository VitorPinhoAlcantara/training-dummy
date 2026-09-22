package com.trainingdummy.entity;

import com.trainingdummy.item.DummyStoredData;
import com.trainingdummy.registry.ModDataComponents;
import com.trainingdummy.registry.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class JackDummyEntity extends PathfinderMob {

    private static final DummyStoredData NO_DATA =
            new DummyStoredData(-1.0D, java.util.List.of(), java.util.List.of(), DummyDisplayMetric.TOTAL, false);

    private static final Component DISPLAY_NAME = Component.literal("Jack");

    private static final EntityDataAccessor<ItemStack> DATA_PUMPKIN_HAT =
            SynchedEntityData.defineId(JackDummyEntity.class, EntityDataSerializers.ITEM_STACK);

    private final ServerBossEvent bossEvent =
            new ServerBossEvent(this.getUUID(), DISPLAY_NAME, BossEvent.BossBarColor.YELLOW, BossEvent.BossBarOverlay.PROGRESS);

    private Component originalName;
    private DummyStoredData originalData = NO_DATA;
    private boolean deathLootSpawned = false;

    public JackDummyEntity(EntityType<? extends JackDummyEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_PUMPKIN_HAT, ItemStack.EMPTY);
    }

    public ItemStack getPumpkinHat() {
        return this.entityData.get(DATA_PUMPKIN_HAT);
    }

    public void setPumpkinHat(ItemStack stack) {
        this.entityData.set(DATA_PUMPKIN_HAT, stack);
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 100.0D)
                .add(Attributes.ARMOR, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.4D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, false));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public void initializeFrom(Component originalName, DummyStoredData data) {
        this.originalName = originalName;
        this.originalData = data;
        this.setCustomName(DISPLAY_NAME);
        this.setCustomNameVisible(true);
    }

    public void applyMaxHealthFrom(double maxHealth) {
        AttributeInstance maxHealthAttribute = this.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealthAttribute != null) {
            maxHealthAttribute.setBaseValue(maxHealth);
        }
        this.setHealth(this.getMaxHealth());
    }

    public void setBaseAttackDamage(float attackDamage) {
        AttributeInstance attackDamageAttribute = this.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamageAttribute != null) {
            attackDamageAttribute.setBaseValue(attackDamage);
        }
    }

    @Override
    protected boolean shouldDropLoot(ServerLevel level) {
        return false;
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);
        if (this.deathLootSpawned) {
            return;
        }
        if (this.level() instanceof ServerLevel serverLevel) {
            this.deathLootSpawned = true;
            this.spawnLoadedDummySpawner(serverLevel);
            ItemStack pumpkinHat = this.getPumpkinHat();
            this.spawnAtLocation(serverLevel, !pumpkinHat.isEmpty() ? pumpkinHat.copy() : new ItemStack(Items.JACK_O_LANTERN));
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        ItemStack pumpkinHat = this.getPumpkinHat();
        if (!pumpkinHat.isEmpty()) {
            output.store("PumpkinHat", ItemStack.CODEC, pumpkinHat);
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setPumpkinHat(input.read("PumpkinHat", ItemStack.CODEC).orElse(ItemStack.EMPTY));
    }

    private void spawnLoadedDummySpawner(ServerLevel level) {
        ItemStack spawnerStack = new ItemStack(ModItems.DUMMY_SPAWNER.get());
        if (this.originalName != null && !this.originalName.equals(DummyEntity.DEFAULT_NAME)) {
            spawnerStack.set(DataComponents.CUSTOM_NAME, this.originalName);
        }
        if (!this.originalData.isEmpty()) {
            spawnerStack.set(ModDataComponents.DUMMY_DATA.get(), this.originalData);
        }
        this.spawnAtLocation(level, spawnerStack);
    }
}
