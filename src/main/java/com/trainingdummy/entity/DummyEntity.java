package com.trainingdummy.entity;

import com.trainingdummy.config.CommonConfig;
import com.trainingdummy.curios.CuriosCompat;
import com.trainingdummy.menu.DummyMenu;
import com.trainingdummy.registry.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForgeMod;

import java.util.List;

/**
 * A stationary, player-shaped combat dummy. Not a real {@code Player}/FakePlayer - a plain
 * {@link LivingEntity} (same base class ArmorStand uses) rendered with the vanilla player
 * model, so it needs no skin lookups and no server-side "fake account" machinery.
 *
 * <p>Health never drops (see {@link #actuallyHurt}); the only way to remove it from the world
 * is a melee hit with a vanilla stick (see {@link #hurtServer}). The actual damage-reporting
 * packet is sent from {@link com.trainingdummy.event.DummyCombatEvents} listening to
 * {@code LivingDamageEvent.Post}, not from here - that event fires with the damage already
 * reduced by armor/shield/enchantments, whereas the {@code amount} parameters in this class are
 * still the raw pre-reduction values.
 *
 * <p>Unlike the 1.21.1 branch, this class does not keep its own hand/armor item lists or
 * override getItemBySlot/setItemSlot/addAdditionalSaveData/readAdditionalSaveData - as of this
 * Minecraft version LivingEntity has a built-in {@code EntityEquipment} store (same one
 * ArmorStand now relies on) that already handles storage and persistence for every equipment
 * slot, so there's nothing left for this class to do there.
 */
public class DummyEntity extends LivingEntity {

    private static final int BAIT_SCAN_INTERVAL_TICKS = 20;

    /**
     * Which "page" of the Curios grid its inventory screen should open on - server-side only,
     * not saved, just so the page-flip buttons (see menu.DummyMenu) can reopen the menu on a
     * different page. Curios slot counts change live (relics/artifacts can grant more), so this
     * is re-clamped to whatever is actually available every time the menu opens.
     */
    private int curiosPage = 0;

    public DummyEntity(EntityType<? extends DummyEntity> type, Level level) {
        super(type, level);
        this.noPhysics = false;
        this.setNoGravity(false);
    }

    /**
     * Mirrors {@code Player.createAttributes()} (base living attributes + every attribute Player
     * adds on top) rather than just the handful this class actually reads itself. Curio/relic
     * mods (Relics' Piglin Mask crashed us this way on the 1.21.1 branch) assume any wearer has a
     * full player-like attribute set and call {@code getAttribute(...)} on it without a null
     * check - so a dummy missing an attribute they touch is a live crash, not just a shrug.
     *
     * <p>{@code LivingEntity.createLivingAttributes()} itself grew several attributes since
     * 1.21.1 (armor, armor toughness and entity interaction range are now part of the base set
     * rather than something Player/Mob add individually), so this only needs to add what
     * {@code Player.createAttributes()} still adds on top of that base, plus this dummy's own
     * overrides (max health, knockback resistance, movement speed).
     */
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
                .add(Attributes.WAYPOINT_TRANSMIT_RANGE, 6.0E7)
                .add(Attributes.WAYPOINT_RECEIVE_RANGE, 6.0E7)
                .add(NeoForgeMod.CREATIVE_FLIGHT);
    }

    @Override
    public void tick() {
        super.tick();
        // Keep it topped off even outside of combat (regen, potions, etc. should never matter).
        if (this.getHealth() < this.getMaxHealth()) {
            this.setHealth(this.getMaxHealth());
        }
        if (!this.level().isClientSide) {
            // The dummy has no AI to "hold right-click", so if it's holding a shield, force it
            // into the same isUsingItem()/BLOCK state a player gets from actually raising one -
            // otherwise isBlocking() never returns true and the shield never reduces damage.
            if (!this.isUsingItem() && this.getOffhandItem().is(Items.SHIELD)) {
                this.startUsingItem(InteractionHand.OFF_HAND);
            }
            if (this.tickCount % BAIT_SCAN_INTERVAL_TICKS == 0 && this.getMainHandItem().is(ModItems.LURE_BAIT.get())) {
                this.lureNearbyMobs();
            }
        }
    }

    /**
     * Makes any hostile mob within range that can see this dummy (and isn't already busy fighting
     * something) attack it. Only actually-hostile mobs ({@link Enemy}, e.g. zombies/skeletons/
     * creepers) - wolves, foxes and bees are {@code Mob}s too but they're neutral/passive by
     * nature and shouldn't be forced to attack just because the bait is out.
     */
    private void lureNearbyMobs() {
        double radius = CommonConfig.BAIT_RADIUS.get();
        AABB area = this.getBoundingBox().inflate(radius);
        // The dummy is a LivingEntity, not a Mob, so it can never show up in this list itself.
        List<Mob> nearby = this.level().getEntitiesOfClass(Mob.class, area,
                mob -> mob.isAlive() && mob instanceof Enemy);
        for (Mob mob : nearby) {
            LivingEntity currentTarget = mob.getTarget();
            if ((currentTarget == null || !currentTarget.isAlive()) && mob.hasLineOfSight(this)) {
                mob.setTarget(this);
                // Older goal-based mobs (zombies, skeletons, spiders...) act on setTarget() alone.
                // Newer brain-based mobs (piglins, breezes, wardens...) decide who to fight from
                // this memory instead and mostly ignore the legacy target field, so both need to
                // be set for the bait to work on the full mob roster - harmless no-op for mobs
                // that don't use their brain for combat. (Phantoms are a known exception either
                // way - their attack goal is hard-coded to only ever target an actual Player, so
                // nothing short of replacing that vanilla goal would bait them.)
                mob.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, this);
            }
            // Piglins/Piglin Brutes re-validate ATTACK_TARGET every tick against their own
            // whitelist (nearest visible player/hoglin/zombified ally) and erase anything else
            // immediately - that's the "targets for an instant then gives up" loop. The only
            // target they'll actually keep chasing outside that whitelist is whoever they're
            // "angry at", the same memory vanilla sets when they get hurt by something, so we
            // set that directly (with the same 600-tick/30s expiry vanilla uses) to bypass it.
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
            this.dropAllEquipment(level);
            this.discard();
            return true;
        }
        return super.hurtServer(level, source, amount);
    }

    /**
     * Gives back everything it was wearing/holding (and any Curios), plus a spawner item for
     * itself - same courtesy vanilla's ArmorStand gives when broken. Each slot is cleared right
     * after dropping (rather than just handed a live reference into the equipment list) so there
     * is no window where the entity is mid-removal with equipment still "equipped".
     */
    private void dropAllEquipment(ServerLevel level) {
        for (EquipmentSlot slot : EquipmentSlot.VALUES) {
            ItemStack stack = this.getItemBySlot(slot);
            if (!stack.isEmpty()) {
                this.spawnAtLocation(level, stack.copy());
                this.setItemSlot(slot, ItemStack.EMPTY);
            }
        }
        if (CuriosCompat.isLoaded()) {
            CuriosCompat.dropAll(this, level);
        }
        this.spawnAtLocation(level, new ItemStack(ModItems.DUMMY_SPAWNER.get()));
    }

    @Override
    protected void actuallyHurt(ServerLevel level, DamageSource source, float amount) {
        super.actuallyHurt(level, source, amount);
        if (this.getHealth() < this.getMaxHealth()) {
            this.setHealth(this.getMaxHealth());
        }
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand, Vec3 location) {
        ItemStack held = player.getItemInHand(hand);
        if (held.is(Items.STICK) && !this.level().isClientSide) {
            this.openMenuFor(player);
            return InteractionResult.CONSUME;
        }
        if (held.is(Items.NAME_TAG) && held.has(DataComponents.CUSTOM_NAME)) {
            if (!this.level().isClientSide) {
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

    /**
     * Whatever name the dummy is showing (renamed with a name tag, or the default) - the client
     * renderer uses this same string to look up a matching player skin, so renaming doubles as
     * "change skin" per the user's request.
     */
    public String getSkinName() {
        return this.getCustomName() != null ? this.getCustomName().getString() : "";
    }

    public int getCuriosPage() {
        return this.curiosPage;
    }

    public void setCuriosPage(int page) {
        this.curiosPage = Math.max(0, page);
    }

    /** Also called by {@link com.trainingdummy.network.DummyCuriosPagePayload}'s handler to reopen on a new page. */
    public void openMenuFor(Player player) {
        player.openMenu(new SimpleMenuProvider(
                (containerId, playerInventory, p) -> new DummyMenu(containerId, playerInventory, this),
                Component.translatable("container.trainingdummy.dummy")
        ), buf -> {
            buf.writeVarInt(this.getId());
            buf.writeVarInt(this.curiosPage);
        });
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void push(net.minecraft.world.entity.Entity entity) {
        // Stationary target - never gets shoved around by other entities.
    }

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    @Override
    public boolean canBeSeenAsEnemy() {
        // Was `false` (copied from ArmorStand, which is never meant to be attacked). That broke
        // the lure bait: several hostile mobs' own targeting AI re-checks canBeSeenAsEnemy() every
        // tick and drops a target that fails it, so only mobs with simpler AI kept attacking.
        // Which mobs the bait targets at all is filtered separately in lureNearbyMobs().
        return true;
    }

    @Override
    public boolean isAttackable() {
        return true;
    }

    @Override
    public boolean shouldShowName() {
        return true;
    }
}
