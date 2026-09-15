package com.trainingdummy.entity;

import com.trainingdummy.config.CommonConfig;
import com.trainingdummy.curios.CuriosCompat;
import com.trainingdummy.item.DummyCurioEntry;
import com.trainingdummy.item.DummyStoredData;
import com.trainingdummy.menu.DummyMenu;
import com.trainingdummy.registry.ModDataComponents;
import com.trainingdummy.registry.ModItems;
import com.trainingdummy.registry.ModSounds;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
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

import java.util.ArrayList;
import java.util.List;

/**
 * A stationary, player-shaped combat dummy. Not a real {@code Player}/FakePlayer - a plain
 * {@link LivingEntity} (same base class ArmorStand uses) rendered with the vanilla player
 * model, so it needs no skin lookups and no server-side "fake account" machinery.
 *
 * <p>Health never drops (see {@link #actuallyHurt}); the only way to remove it from the world
 * is a melee hit with a vanilla stick (see {@link #hurt}). The actual damage-reporting packet is
 * sent from {@link com.trainingdummy.event.DummyCombatEvents} listening to
 * {@code LivingDamageEvent.Post}, not from here - that event fires with the damage already
 * reduced by armor/shield/enchantments, whereas the {@code amount} parameters in this class are
 * still the raw pre-reduction values.
 */
public class DummyEntity extends LivingEntity {

    private static final int BAIT_SCAN_INTERVAL_TICKS = 20;

    public static final Component DEFAULT_NAME = Component.translatable("entity.trainingdummy.dummy");

    private final NonNullList<ItemStack> handItems = NonNullList.withSize(2, ItemStack.EMPTY);
    private final NonNullList<ItemStack> armorItems = NonNullList.withSize(4, ItemStack.EMPTY);

    /**
     * Which "page" of the Curios grid its inventory screen should open on - server-side only,
     * not saved, just so the page-flip buttons (see menu.DummyMenu) can reopen the menu on a
     * different page. Curios slot counts change live (relics/artifacts can grant more), so this
     * is re-clamped to whatever is actually available every time the menu opens.
     */
    private int curiosPage = 0;

    /**
     * Per-dummy override of {@link CommonConfig#MAX_HEALTH}, set from the inventory screen's
     * health field (see network.DummySetMaxHealthPayload) - a negative value means "no override,
     * use the global config default".
     */
    private double maxHealthOverride = -1.0D;

    private DummyDisplayMetric displayMetric = DummyDisplayMetric.TOTAL;
    private boolean displayMetricCustomized = false;

    public DummyEntity(EntityType<? extends DummyEntity> type, Level level) {
        super(type, level);
        this.noPhysics = false;
        this.setNoGravity(false);
        this.applyMaxHealth();
    }

    /**
     * Applied here (rather than baked into createAttributes()) since that method runs once at
     * mod-construction time, before configs are guaranteed to be loaded - reading the config
     * per-instance, once an entity actually exists, is always safe and also means changing
     * maxHealth takes effect for newly spawned dummies without needing a restart.
     */
    private void applyMaxHealth() {
        net.minecraft.world.entity.ai.attributes.AttributeInstance maxHealthAttribute = this.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealthAttribute != null) {
            maxHealthAttribute.setBaseValue(this.effectiveMaxHealthConfig());
        }
        this.setHealth(this.getMaxHealth());
    }

    private double effectiveMaxHealthConfig() {
        return this.maxHealthOverride > 0.0D ? this.maxHealthOverride : CommonConfig.MAX_HEALTH.get();
    }

    /** -1 if this dummy is using the global {@link CommonConfig#MAX_HEALTH} default, otherwise its own override. */
    public double getMaxHealthOverride() {
        return this.maxHealthOverride;
    }

    /** Same range as {@link CommonConfig#MAX_HEALTH}; a value {@code <= 0} clears the override back to the global default. */
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

    /**
     * Mirrors {@code Player.createAttributes()} (base living attributes + every attribute Player
     * adds on top) rather than just the handful this class actually reads itself. Curio/relic
     * mods (Relics' Piglin Mask crashed us this way) assume any wearer has a full player-like
     * attribute set and call {@code getAttribute(...)} on it without a null check - so a dummy
     * missing an attribute they touch is a live crash, not just a shrug.
     */
    public static AttributeSupplier.Builder createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 1_000_000.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.ARMOR)
                .add(Attributes.ARMOR_TOUGHNESS)
                .add(Attributes.ATTACK_DAMAGE)
                .add(Attributes.ATTACK_SPEED)
                .add(Attributes.LUCK)
                .add(Attributes.BLOCK_INTERACTION_RANGE)
                .add(Attributes.ENTITY_INTERACTION_RANGE)
                .add(Attributes.BLOCK_BREAK_SPEED)
                .add(Attributes.SUBMERGED_MINING_SPEED)
                .add(Attributes.SNEAKING_SPEED)
                .add(Attributes.MINING_EFFICIENCY)
                .add(Attributes.SWEEPING_DAMAGE_RATIO);
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
            // Force retargeting onto the dummy even if the mob is already fighting something else
            // (the player, most likely) - the bait should take priority, not just fill in when a
            // mob happens to have no target at all.
            if (mob.hasLineOfSight(this)) {
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
    public boolean hurt(DamageSource source, float amount) {
        if (!this.level().isClientSide && isStickHit(source)) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ARMOR_STAND_BREAK,
                    this.getSoundSource(), 1.0F, 1.0F);
            this.spawnLoadedDummySpawner();
            this.discard();
            return true;
        }
        return super.hurt(source, amount);
    }

    /**
     * Folds this dummy's customization (name, health override, equipment, curios) into a spawner
     * item dropped at its feet - same courtesy vanilla's ArmorStand gives when broken, but
     * remembering everything instead of scattering the gear on the ground, so placing the item
     * back down recreates the dummy just as it was. Negative effects are not saved.
     */
    private void spawnLoadedDummySpawner() {
        List<ItemStack> equipment = new ArrayList<>(EquipmentSlot.values().length);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
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
        this.spawnAtLocation(spawnerStack);
    }

    @Override
    protected void actuallyHurt(DamageSource source, float amount) {
        super.actuallyHurt(source, amount);
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

    /** Removes every currently active potion effect from the dummy - see network.DummyClearEffectsPayload. */
    public void clearEffects() {
        this.removeAllEffects();
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
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
        return super.interact(player, hand);
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
            buf.writeEnum(this.displayMetric);
            buf.writeBoolean(this.displayMetricCustomized);
        });
    }

    @Override
    public boolean canUseSlot(EquipmentSlot slot) {
        // LivingEntity's default is `false`, which silently breaks
        // getEquipmentSlotForItem() (it falls back to MAINHAND for every armor piece since the
        // canUseSlot check inside it fails) - Mob/Player override this for the same reason.
        return true;
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
    public Iterable<ItemStack> getArmorSlots() {
        return this.armorItems;
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot slot) {
        return switch (slot.getType()) {
            case HAND -> this.handItems.get(slot.getIndex());
            case HUMANOID_ARMOR -> this.armorItems.get(slot.getIndex());
            default -> ItemStack.EMPTY;
        };
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
        switch (slot.getType()) {
            case HAND -> this.handItems.set(slot.getIndex(), stack);
            case HUMANOID_ARMOR -> this.armorItems.set(slot.getIndex(), stack);
            default -> {
            }
        }
    }

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("HandItems", saveItemList(this.handItems, this.registryAccess()));
        tag.put("ArmorItems", saveItemList(this.armorItems, this.registryAccess()));
        if (this.maxHealthOverride > 0.0D) {
            tag.putDouble("MaxHealthOverride", this.maxHealthOverride);
        }
        if (this.displayMetricCustomized) {
            tag.putString("DisplayMetric", this.displayMetric.name());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("HandItems", 9)) {
            loadItemList(tag.getList("HandItems", 10), this.handItems, this.registryAccess());
        }
        if (tag.contains("ArmorItems", 9)) {
            loadItemList(tag.getList("ArmorItems", 10), this.armorItems, this.registryAccess());
        }
        // getDouble() already returns 0.0 (treated as "no override") when the tag is absent.
        this.maxHealthOverride = tag.getDouble("MaxHealthOverride");
        this.applyMaxHealth();
        this.displayMetricCustomized = tag.contains("DisplayMetric");
        this.displayMetric = this.displayMetricCustomized
                ? parseDisplayMetric(tag.getString("DisplayMetric"))
                : DummyDisplayMetric.TOTAL;
    }

    private static DummyDisplayMetric parseDisplayMetric(String name) {
        try {
            return DummyDisplayMetric.valueOf(name);
        } catch (IllegalArgumentException e) {
            return DummyDisplayMetric.TOTAL;
        }
    }

    private static ListTag saveItemList(NonNullList<ItemStack> items, net.minecraft.core.HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (int i = 0; i < items.size(); i++) {
            ItemStack stack = items.get(i);
            if (!stack.isEmpty()) {
                CompoundTag entry = new CompoundTag();
                entry.putByte("Slot", (byte) i);
                entry.put("Item", stack.save(registries, new CompoundTag()));
                list.add(entry);
            }
        }
        return list;
    }

    private static void loadItemList(ListTag list, NonNullList<ItemStack> items, net.minecraft.core.HolderLookup.Provider registries) {
        int size = items.size();
        for (int i = 0; i < size; i++) {
            items.set(i, ItemStack.EMPTY);
        }
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            int slot = entry.getByte("Slot");
            if (slot >= 0 && slot < size) {
                items.set(slot, ItemStack.parseOptional(registries, entry.getCompound("Item")));
            }
        }
    }

    @Override
    public boolean canBeSeenAsEnemy() {
        // Was hardcoded `true` at one point (having been hardcoded `false`, copied from
        // ArmorStand, before that - which broke the lure bait since several hostile mobs'
        // targeting AI drops a target that fails this check). Hardcoding it to `true`
        // unconditionally went too far the other way: it made the dummy a valid target forever,
        // even after being discarded or with the bait long gone.
        //
        // Gating on "holding bait" isn't just for correctness after death - it's also what makes
        // mobs let go the instant the bait is pulled back out, for free. Every hostile mob's own
        // AI (goal-based and brain-based alike) already re-reads this on essentially every tick
        // to decide whether to keep its current target - Mob#getTarget() itself returns null the
        // moment canAttack(target) fails, which folds in canBeSeenAsEnemy() - so flipping this to
        // false makes the whole mob roster drop the dummy on their own via their own existing
        // target-revalidation, without this class having to track or reach into any of them
        // itself. (Symmetric: putting the bait back doesn't need a reason to fail either.)
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
}
