package com.trainingdummy.entity;

import com.trainingdummy.config.CommonConfig;
import com.trainingdummy.curios.CuriosCompat;
import com.trainingdummy.item.DummyCurioEntry;
import com.trainingdummy.item.DummyStoredData;
import com.trainingdummy.menu.DummyMenu;
import com.trainingdummy.registry.ModDataComponents;
import com.trainingdummy.registry.ModEntities;
import com.trainingdummy.registry.ModItems;
import com.trainingdummy.registry.ModSounds;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;


public class DummyEntity extends LivingEntity {

    private static final int BAIT_SCAN_INTERVAL_TICKS = 20;

    public static final Component DEFAULT_NAME = Component.translatable("entity.trainingdummy.dummy");

    private final NonNullList<ItemStack> handItems = NonNullList.withSize(2, ItemStack.EMPTY);
    private final NonNullList<ItemStack> armorItems = NonNullList.withSize(4, ItemStack.EMPTY);


    private int curiosPage = 0;


    private Vec3 herobrineLevitateAnchor;
    private long herobrineLevitateStartTick = -1L;
    private long herobrineLevitateUntilTick = -1L;


    private double maxHealthOverride = -1.0D;

    private float lastDamageTaken = -1.0F;

    private DummyDisplayMetric displayMetric = DummyDisplayMetric.PER_HIT;
    private boolean displayMetricCustomized = false;


    private static final long RECENT_ATTACKER_EXPIRY_TICKS = 60 * 20L;
    private final Map<UUID, Long> recentAttackers = new HashMap<>();

    public DummyEntity(EntityType<? extends DummyEntity> type, Level level) {
        super(type, level);
        this.noPhysics = false;
        this.setNoGravity(false);
        this.applyMaxHealth();
    }


    private void applyMaxHealth() {
        net.minecraft.world.entity.ai.attributes.AttributeInstance maxHealthAttribute = this.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealthAttribute != null) {
            maxHealthAttribute.setBaseValue(this.effectiveMaxHealthConfig());
        }
        this.setHealth(this.getMaxHealth());
    }

    private double effectiveMaxHealthConfig() {
        return this.maxHealthOverride > 0.0D ? this.maxHealthOverride : 20.0D;
    }


    public double getMaxHealthOverride() {
        return this.maxHealthOverride;
    }


    public void setMaxHealthOverride(double value) {
        this.maxHealthOverride = value > 0.0D ? Math.min(value, Double.MAX_VALUE) : -1.0D;
        this.applyMaxHealth();
    }

    public float getLastDamageTaken() {
        return this.lastDamageTaken;
    }

    public void setLastDamageTaken(float amount) {
        this.lastDamageTaken = amount;
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

    public void rememberAttacker(ServerPlayer player) {
        this.recentAttackers.put(player.getUUID(), this.level().getGameTime());
    }


    public List<ServerPlayer> recentAttackers() {
        long now = this.level().getGameTime();
        this.recentAttackers.values().removeIf(lastHitTick -> now - lastHitTick > RECENT_ATTACKER_EXPIRY_TICKS);

        MinecraftServer server = this.level().getServer();
        if (server == null) {
            return List.of();
        }
        List<ServerPlayer> players = new ArrayList<>(this.recentAttackers.size());
        for (UUID uuid : this.recentAttackers.keySet()) {
            ServerPlayer player = server.getPlayerList().getPlayer(uuid);
            if (player != null) {
                players.add(player);
            }
        }
        return players;
    }


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
        this.tickHerobrineLevitate();

        if (!this.level().isClientSide) {



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

    private static final String MITINHO_NICKNAME = "MitinhoPlayer";


    public boolean hasMitinhoNickname() {
        return MITINHO_NICKNAME.equals(this.getSkinName());
    }

    private boolean isMorganKill(DamageSource source) {
        if (!this.hasMitinhoNickname()) {
            return false;
        }
        if (!(source.getDirectEntity() instanceof Player player)) {
            return false;
        }
        ResourceLocation weaponId = BuiltInRegistries.ITEM.getKey(player.getMainHandItem().getItem());
        return weaponId != null && weaponId.getNamespace().equals("mahoutsukai") && weaponId.getPath().equals("morgan");
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
        if (!this.level().isClientSide && this.isMorganKill(source)) {


            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), this.getHurtSound(source),
                    this.getSoundSource(), 1.0F, 1.0F);
            this.setHealth(0.0F);
            this.die(source);
            return true;
        }
        return super.hurt(source, amount);
    }


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

    private static final String HEROBRINE_NICKNAME = "Herobrine";


    public boolean hasHerobrineNickname() {
        return HEROBRINE_NICKNAME.equals(this.getSkinName());
    }

    private static final double HEROBRINE_LEVITATE_HEIGHT = 3.0D;


    public void startHerobrineLevitate(int ticks) {
        this.herobrineLevitateAnchor = this.position();
        this.herobrineLevitateStartTick = this.level().getGameTime();
        this.herobrineLevitateUntilTick = this.herobrineLevitateStartTick + ticks;
    }

    private void tickHerobrineLevitate() {
        if (this.herobrineLevitateAnchor == null) {
            return;
        }
        long now = this.level().getGameTime();
        if (now >= this.herobrineLevitateUntilTick) {
            this.herobrineLevitateAnchor = null;
            return;
        }
        double progress = (double) (now - this.herobrineLevitateStartTick)
                / (this.herobrineLevitateUntilTick - this.herobrineLevitateStartTick);
        this.setPos(this.herobrineLevitateAnchor.x,
                this.herobrineLevitateAnchor.y + HEROBRINE_LEVITATE_HEIGHT * progress,
                this.herobrineLevitateAnchor.z);
    }

    private static final String AUTO_DEATH_NICKNAME = "Immortal";


    public boolean hasAutoDeathNickname() {
        return AUTO_DEATH_NICKNAME.equals(this.getSkinName());
    }


    public void dieOnPlacement() {
        this.setHealth(0.0F);
        this.die(this.damageSources().genericKill());
    }


    private static final int IMMORTAL_EXTRA_DEATH_EVENTS = 9;

    private boolean deathLootSpawned = false;

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);
        if (this.deathLootSpawned) {
            return;
        }
        if (!this.level().isClientSide) {
            this.deathLootSpawned = true;
            this.spawnLoadedDummySpawner();
            if (this.hasAutoDeathNickname()) {
                this.emitExtraDeathSouls();
            }
            if (this.hasMitinhoNickname() && this.level().getServer() != null) {
                this.level().getServer().getPlayerList().broadcastSystemMessage(
                        Component.literal("Mitinho died and went into spectator mode.")
                                .withStyle(net.minecraft.ChatFormatting.GRAY, net.minecraft.ChatFormatting.ITALIC),
                        false);
            }
        }
    }


    private void emitExtraDeathSouls() {
        for (int i = 1; i <= IMMORTAL_EXTRA_DEATH_EVENTS; i++) {
            Vec3 nudged = this.position().add(i * 0.01, 0.0D, 0.0D);
            this.level().gameEvent(this, GameEvent.ENTITY_DIE, nudged);
        }
    }


    @Override
    public void handleEntityEvent(byte id) {
        if (id == 60) {
            return;
        }
        super.handleEntityEvent(id);
    }


    @Override
    protected boolean shouldDropLoot() {
        return false;
    }

    @Override
    protected void actuallyHurt(DamageSource source, float amount) {
        super.actuallyHurt(source, amount);
        if (!this.dead && this.getHealth() < this.getMaxHealth()) {
            this.setHealth(this.getMaxHealth());
        }
    }

    private static final java.util.Map<String, java.util.function.Supplier<SoundEvent>> NAMED_HURT_SOUNDS = java.util.Map.of(
            "Danrique", ModSounds.DANRIQUE_HURT,
            "MitinhoPlayer", ModSounds.MITINHOPLAYER_HURT,
            "Nofaxu", ModSounds.NOFAXU_HURT,
            "BrunimNeets", ModSounds.BRUNIMNEETS_HURT,
            "mamao170", ModSounds.MAMAO170_HURT,
            "JazaraGamer", ModSounds.JAZARAGAMER_HURT,
            "MeioElfo", ModSounds.MEIOELFO_HURT,
            "ForeverPlayerG", ModSounds.BRUNIMNEETS_HURT
    );

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        java.util.function.Supplier<SoundEvent> named = findNamedSound(NAMED_HURT_SOUNDS, this.getSkinName());
        return named != null ? named.get() : ModSounds.DUMMY_HURT.get();
    }

    private static final java.util.Map<String, java.util.function.Supplier<SoundEvent>> NAMED_PLACE_SOUNDS = java.util.Map.of(
            "JazaraGamer", ModSounds.JAZARAGAMER_PLACE
    );


    public Optional<SoundEvent> getPlacementSound() {
        java.util.function.Supplier<SoundEvent> named = findNamedSound(NAMED_PLACE_SOUNDS, this.getSkinName());
        return Optional.ofNullable(named).map(java.util.function.Supplier::get);
    }

    private static java.util.function.Supplier<SoundEvent> findNamedSound(
            java.util.Map<String, java.util.function.Supplier<SoundEvent>> sounds, String name) {
        for (java.util.Map.Entry<String, java.util.function.Supplier<SoundEvent>> entry : sounds.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(name)) {
                return entry.getValue();
            }
        }
        return null;
    }


    public void clearEffects() {
        this.removeAllEffects();
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (held.is(Items.JACK_O_LANTERN) && !this.level().isClientSide) {
            this.transformIntoJack(held.copyWithCount(1));
            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }
            return InteractionResult.CONSUME;
        }
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
                if (this.hasHerobrineNickname() && player instanceof ServerPlayer serverPlayer) {
                    com.trainingdummy.event.HerobrinePrank.trigger(this, serverPlayer);
                }
            }
            return InteractionResult.SUCCESS;
        }
        return super.interact(player, hand);
    }


    public String getSkinName() {
        return this.getCustomName() != null ? this.getCustomName().getString() : "";
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
    public boolean canUseSlot(EquipmentSlot slot) {



        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void push(net.minecraft.world.entity.Entity entity) {

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


    private static final float JACK_DEFAULT_ATTACK_DAMAGE = 10.0F;

    private void transformIntoJack(ItemStack pumpkinStack) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        JackDummyEntity jack = ModEntities.JACK.get().create(serverLevel);
        if (jack == null) {
            return;
        }
        jack.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
        jack.setYHeadRot(this.getYHeadRot());

        List<ItemStack> equipment = new ArrayList<>(EquipmentSlot.values().length);
        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            equipment.add(this.getItemBySlot(equipmentSlot).copy());
        }
        List<DummyCurioEntry> curios = CuriosCompat.isLoaded() ? CuriosCompat.captureAll(this) : List.of();
        DummyStoredData stored = new DummyStoredData(this.maxHealthOverride, equipment, curios,
                this.displayMetric, this.displayMetricCustomized);
        jack.initializeFrom(this.getCustomName(), stored);

        boolean hasSword = this.getItemBySlot(EquipmentSlot.MAINHAND).getItem() instanceof SwordItem;

        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            jack.setItemSlot(equipmentSlot, this.getItemBySlot(equipmentSlot).copy());
        }
        jack.setPumpkinHat(pumpkinStack.copy());
        if (!curios.isEmpty()) {
            CuriosCompat.restoreAll(jack, curios);
        }

        jack.applyMaxHealthFrom(this.effectiveMaxHealthConfig());
        if (!hasSword) {
            float fallbackAttack = this.lastDamageTaken >= 0.0F ? this.lastDamageTaken : JACK_DEFAULT_ATTACK_DAMAGE;
            jack.setBaseAttackDamage(fallbackAttack);
        }

        serverLevel.addFreshEntity(jack);
        this.discard();
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
        if (this.lastDamageTaken >= 0.0F) {
            tag.putFloat("LastDamageTaken", this.lastDamageTaken);
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

        this.maxHealthOverride = tag.getDouble("MaxHealthOverride");
        this.applyMaxHealth();
        this.displayMetricCustomized = tag.contains("DisplayMetric");
        this.displayMetric = this.displayMetricCustomized
                ? parseDisplayMetric(tag.getString("DisplayMetric"))
                : DummyDisplayMetric.PER_HIT;
        this.lastDamageTaken = tag.contains("LastDamageTaken") ? tag.getFloat("LastDamageTaken") : -1.0F;
    }

    private static DummyDisplayMetric parseDisplayMetric(String name) {
        try {
            return DummyDisplayMetric.valueOf(name);
        } catch (IllegalArgumentException e) {
            return DummyDisplayMetric.PER_HIT;
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
