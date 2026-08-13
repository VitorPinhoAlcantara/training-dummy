package com.trainingdummy.entity;

import com.trainingdummy.config.CommonConfig;
import com.trainingdummy.curios.CuriosCompat;
import com.trainingdummy.menu.DummyMenu;
import com.trainingdummy.registry.ModItems;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
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

    private final NonNullList<ItemStack> handItems = NonNullList.withSize(2, ItemStack.EMPTY);
    private final NonNullList<ItemStack> armorItems = NonNullList.withSize(4, ItemStack.EMPTY);

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
    public boolean hurt(DamageSource source, float amount) {
        if (!this.level().isClientSide && isStickHit(source)) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ARMOR_STAND_BREAK,
                    this.getSoundSource(), 1.0F, 1.0F);
            this.dropAllEquipment();
            this.discard();
            return true;
        }
        return super.hurt(source, amount);
    }

    /**
     * Gives back everything it was wearing/holding (and any Curios), plus a spawner item for
     * itself - same courtesy vanilla's ArmorStand gives when broken. Each slot is cleared right
     * after dropping (rather than just handed a live reference into the equipment list) so there
     * is no window where the entity is mid-removal with equipment still "equipped" - matches
     * ArmorStand's own drop-then-clear pattern exactly.
     */
    private void dropAllEquipment() {
        for (int i = 0; i < this.handItems.size(); i++) {
            ItemStack stack = this.handItems.get(i);
            if (!stack.isEmpty()) {
                this.spawnAtLocation(stack.copy());
                this.handItems.set(i, ItemStack.EMPTY);
            }
        }
        for (int i = 0; i < this.armorItems.size(); i++) {
            ItemStack stack = this.armorItems.get(i);
            if (!stack.isEmpty()) {
                this.spawnAtLocation(stack.copy());
                this.armorItems.set(i, ItemStack.EMPTY);
            }
        }
        if (CuriosCompat.isLoaded()) {
            CuriosCompat.dropAll(this);
        }
        this.spawnAtLocation(new ItemStack(ModItems.DUMMY_SPAWNER.get()));
    }

    @Override
    protected void actuallyHurt(DamageSource source, float amount) {
        super.actuallyHurt(source, amount);
        if (this.getHealth() < this.getMaxHealth()) {
            this.setHealth(this.getMaxHealth());
        }
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
