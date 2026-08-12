package com.trainingdummy.menu;

import com.trainingdummy.curios.CuriosCompat;
import com.trainingdummy.entity.DummyEntity;
import com.trainingdummy.registry.ModMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Shows the dummy's armor/offhand slots (plus Curios slots, if that mod is loaded) alongside
 * the opening player's own inventory for easy drag-and-drop gearing. Layout mirrors vanilla:
 * an armor "paperdoll" column on the left (helmet on top, like the survival inventory), an
 * optional Curios grid to its right, and the player's own inventory below - all the position
 * constants live here so {@link com.trainingdummy.client.DummyScreen} can draw section labels
 * and panel backgrounds that line up with them.
 */
public class DummyMenu extends AbstractContainerMenu {

    private static final EquipmentSlot[] EQUIPMENT_ORDER = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET,
            EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND
    };
    private static final ResourceLocation[] EQUIPMENT_ICONS = {
            InventoryMenu.EMPTY_ARMOR_SLOT_HELMET, InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE,
            InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS, InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS,
            null, InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD
    };
    private static final String[] EQUIPMENT_NAME_KEYS = {"head", "chest", "legs", "feet", "mainhand", "offhand"};
    private static final int HAND_SLOTS_START = 4;

    public static final int SLOT_SIZE = 18;
    public static final int MARGIN = 8;
    public static final int TOP_Y = 22;
    public static final int ARMOR_X = MARGIN;
    private static final int ARMOR_OFFHAND_GAP = 6;
    public static final int ARMOR_HEIGHT = 4 * SLOT_SIZE + ARMOR_OFFHAND_GAP + 2 * SLOT_SIZE;

    public static final int CURIOS_X = ARMOR_X + SLOT_SIZE + MARGIN;
    public static final int CURIOS_COLUMNS = 8;
    /** Curios slot counts are unbounded (relics/artifacts can grant more) - cap the grid and page through the rest. */
    public static final int MAX_CURIOS_ROWS = 5;
    public static final int CURIOS_PAGE_CAPACITY = CURIOS_COLUMNS * MAX_CURIOS_ROWS;

    public static final int PLAYER_INV_X = MARGIN;
    public static final int MIN_CONTENT_WIDTH = PLAYER_INV_X + 9 * SLOT_SIZE + MARGIN;

    public final int curiosTotalSlotCount;
    public final int curiosPageSlotCount;
    public final int curiosTotalPages;
    public final int curiosPage;
    public final int playerInvY;
    public final int imageWidth;
    public final int imageHeight;

    private final DummyEntity dummy;
    private final EquipmentContainer equipmentContainer;

    public DummyMenu(int containerId, Inventory playerInventory, DummyEntity dummy) {
        this(containerId, playerInventory, dummy, dummy.getCuriosPage());
    }

    public DummyMenu(int containerId, Inventory playerInventory, DummyEntity dummy, int requestedPage) {
        super(ModMenus.DUMMY_MENU.get(), containerId);
        this.dummy = dummy;
        this.equipmentContainer = new EquipmentContainer(dummy);

        // Slot count is read fresh on every open/page-change, so a dummy whose curio count
        // changed (a relic granting/removing slots) is picked up correctly next time the menu
        // is (re)built - see network.DummyCuriosPagePayload for how page changes trigger that.
        this.curiosTotalSlotCount = CuriosCompat.isLoaded() ? CuriosCompat.slotCount(dummy) : 0;
        int totalRows = this.curiosTotalSlotCount == 0 ? 0
                : (this.curiosTotalSlotCount + CURIOS_COLUMNS - 1) / CURIOS_COLUMNS;
        this.curiosTotalPages = Math.max(1, (totalRows + MAX_CURIOS_ROWS - 1) / MAX_CURIOS_ROWS);
        this.curiosPage = Math.min(Math.max(0, requestedPage), this.curiosTotalPages - 1);

        int pageStart = this.curiosPage * CURIOS_PAGE_CAPACITY;
        this.curiosPageSlotCount = Math.max(0, Math.min(CURIOS_PAGE_CAPACITY, this.curiosTotalSlotCount - pageStart));
        int pageRows = this.curiosPageSlotCount == 0 ? 0
                : (this.curiosPageSlotCount + CURIOS_COLUMNS - 1) / CURIOS_COLUMNS;

        for (int i = 0; i < EQUIPMENT_ORDER.length; i++) {
            int gap = i >= HAND_SLOTS_START ? ARMOR_OFFHAND_GAP : 0;
            Component name = Component.translatable("trainingdummy.slot." + EQUIPMENT_NAME_KEYS[i]);
            // Main hand and offhand stay unrestricted (vanilla allows anything there too),
            // armor slots only accept the matching piece.
            EquipmentSlot restrictTo = i >= HAND_SLOTS_START ? null : EQUIPMENT_ORDER[i];
            this.addSlot(new ArmorSlot(this.equipmentContainer, i, ARMOR_X, TOP_Y + i * SLOT_SIZE + gap,
                    name, InventoryMenu.BLOCK_ATLAS, EQUIPMENT_ICONS[i], dummy, restrictTo));
        }

        if (this.curiosPageSlotCount > 0) {
            CuriosCompat.addCurioSlots(this, dummy, CURIOS_X, TOP_Y, CURIOS_COLUMNS, pageStart, this.curiosPageSlotCount);
        }

        int contentHeight = Math.max(ARMOR_HEIGHT, pageRows * SLOT_SIZE);
        this.playerInvY = TOP_Y + contentHeight + 4 + 14;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, PLAYER_INV_X + col * SLOT_SIZE, this.playerInvY + row * SLOT_SIZE));
            }
        }
        int hotbarY = this.playerInvY + 3 * SLOT_SIZE + 4;
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, PLAYER_INV_X + col * SLOT_SIZE, hotbarY));
        }

        int curiosWidth = this.curiosTotalSlotCount > 0 ? CURIOS_X + CURIOS_COLUMNS * SLOT_SIZE + MARGIN : ARMOR_X + SLOT_SIZE + MARGIN;
        this.imageWidth = Math.max(MIN_CONTENT_WIDTH, curiosWidth);
        this.imageHeight = hotbarY + SLOT_SIZE + MARGIN;
    }

    public DummyMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory, resolveDummy(playerInventory, buf.readVarInt()), buf.readVarInt());
    }

    private static DummyEntity resolveDummy(Inventory playerInventory, int entityId) {
        Entity entity = playerInventory.player.level().getEntity(entityId);
        if (entity instanceof DummyEntity dummyEntity) {
            return dummyEntity;
        }
        throw new IllegalStateException("Dummy entity " + entityId + " is not loaded on the client");
    }

    public DummyEntity getDummy() {
        return this.dummy;
    }

    /** Public passthrough so {@link CuriosCompat} (a different package) can add curio slots. */
    public void addCurioSlot(Slot slot) {
        this.addSlot(slot);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack original = slot.getItem();
        ItemStack result = original.copy();
        int dummySlotCount = this.equipmentContainer.getContainerSize() + this.curiosPageSlotCount;

        if (index < dummySlotCount) {
            if (!this.moveItemStackTo(original, dummySlotCount, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!this.moveItemStackTo(original, 0, dummySlotCount, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (original.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.dummy.isAlive() && this.dummy.distanceToSqr(player) < 64.0D;
    }

    /**
     * Adapts the dummy's armor/offhand equipment slots to a vanilla {@link Container} so plain
     * {@link Slot} instances can read/write them directly.
     */
    private static final class EquipmentContainer implements Container {

        private final DummyEntity dummy;

        private EquipmentContainer(DummyEntity dummy) {
            this.dummy = dummy;
        }

        @Override
        public int getContainerSize() {
            return EQUIPMENT_ORDER.length;
        }

        @Override
        public boolean isEmpty() {
            for (EquipmentSlot slot : EQUIPMENT_ORDER) {
                if (!this.dummy.getItemBySlot(slot).isEmpty()) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public ItemStack getItem(int index) {
            return this.dummy.getItemBySlot(EQUIPMENT_ORDER[index]);
        }

        @Override
        public ItemStack removeItem(int index, int count) {
            ItemStack current = this.getItem(index);
            ItemStack split = current.split(count);
            if (!split.isEmpty()) {
                this.setChanged();
            }
            return split;
        }

        @Override
        public ItemStack removeItemNoUpdate(int index) {
            ItemStack current = this.getItem(index);
            this.dummy.setItemSlot(EQUIPMENT_ORDER[index], ItemStack.EMPTY);
            return current;
        }

        @Override
        public void setItem(int index, ItemStack stack) {
            this.dummy.setItemSlot(EQUIPMENT_ORDER[index], stack);
        }

        @Override
        public void setChanged() {
            // No extra bookkeeping needed - the dummy's equipment is the source of truth.
        }

        @Override
        public boolean stillValid(Player player) {
            return this.dummy.isAlive();
        }

        @Override
        public void clearContent() {
            for (EquipmentSlot slot : EQUIPMENT_ORDER) {
                this.dummy.setItemSlot(slot, ItemStack.EMPTY);
            }
        }
    }
}
