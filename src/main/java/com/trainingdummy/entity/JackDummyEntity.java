package com.trainingdummy.entity;

import com.trainingdummy.item.DummyStoredData;
import com.trainingdummy.registry.ModDataComponents;
import com.trainingdummy.registry.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
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


public class JackDummyEntity extends PathfinderMob {

    private static final DummyStoredData NO_DATA =
            new DummyStoredData(-1.0D, java.util.List.of(), java.util.List.of(), DummyDisplayMetric.TOTAL, false);

    private static final Component DISPLAY_NAME = Component.literal("Jack");

    private final ServerBossEvent bossEvent =
            new ServerBossEvent(DISPLAY_NAME, BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS);

    private Component originalName;
    private DummyStoredData originalData = NO_DATA;

    public JackDummyEntity(EntityType<? extends JackDummyEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
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
                .add(Attributes.MAX_HEALTH, 1000.0D)
                .add(Attributes.ARMOR, 100.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.23D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
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


    @Override
    protected boolean shouldDropLoot() {
        return false;
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);
        if (this.level() instanceof ServerLevel) {
            this.spawnLoadedDummySpawner();
            this.spawnAtLocation(new ItemStack(Items.JACK_O_LANTERN));
        }
    }

    private void spawnLoadedDummySpawner() {
        ItemStack spawnerStack = new ItemStack(ModItems.DUMMY_SPAWNER.get());
        if (this.originalName != null && !this.originalName.equals(DummyEntity.DEFAULT_NAME)) {
            spawnerStack.set(DataComponents.CUSTOM_NAME, this.originalName);
        }
        if (!this.originalData.isEmpty()) {
            spawnerStack.set(ModDataComponents.DUMMY_DATA.get(), this.originalData);
        }
        this.spawnAtLocation(spawnerStack);
    }
}
