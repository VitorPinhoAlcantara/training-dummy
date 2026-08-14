package com.trainingdummy.client;

import com.trainingdummy.config.ClientConfig;
import com.trainingdummy.menu.DummyMenu;
import com.trainingdummy.menu.SlotTooltip;
import com.trainingdummy.network.DummyCuriosPagePayload;
import com.trainingdummy.network.DummySetMaxHealthPayload;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

/**
 * No custom texture asset - the panel is a vanilla-style beveled surface drawn procedurally
 * (raised outer border, recessed slot wells) instead of a hand-authored PNG, following the same
 * light-gray inventory look Minecraft and mods like Curios use, just built out of flat fills.
 */
public class DummyScreen extends AbstractContainerScreen<DummyMenu> {

    private static final int BASE = 0xFFC6C6C6;
    private static final int LIGHT = 0xFFFFFFFF;
    private static final int SHADOW = 0xFF555555;
    private static final int SLOT_WELL = 0xFF8B8B8B;
    private static final int LABEL_COLOR = 0xFF404040;

    private EditBox maxHealthBox;

    public DummyScreen(DummyMenu menu, Inventory playerInventory, Component title) {
        // imageWidth/imageHeight are constructor-only (final) as of this Minecraft version, so
        // the menu's computed size has to go in here instead of being assigned afterward.
        super(menu, playerInventory, title, menu.imageWidth, menu.imageHeight);
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelX = DummyMenu.PLAYER_INV_X;
        this.inventoryLabelY = menu.playerInvY - 10;
    }

    @Override
    protected void init() {
        super.init();
        if (this.menu.curiosTotalPages > 1) {
            int rowY = this.topPos + DummyMenu.TOP_Y - 12;
            int rightEdge = this.leftPos + DummyMenu.CURIOS_X + DummyMenu.CURIOS_COLUMNS * DummyMenu.SLOT_SIZE;
            this.addRenderableWidget(Button.builder(Component.literal("<"), b -> this.requestPage(this.menu.curiosPage - 1))
                    .bounds(rightEdge - 34, rowY, 14, 12).build());
            this.addRenderableWidget(Button.builder(Component.literal(">"), b -> this.requestPage(this.menu.curiosPage + 1))
                    .bounds(rightEdge - 14, rowY, 14, 12).build());
        }

        int controlsX = this.leftPos + DummyMenu.ARMOR_X;
        int controlsY = this.topPos + DummyMenu.CONTROLS_Y;

        this.addRenderableWidget(Button.builder(metricLabel(), b -> {
            ClientConfig.DisplayMetric next = ClientConfig.DISPLAY_METRIC.get() == ClientConfig.DisplayMetric.TOTAL
                    ? ClientConfig.DisplayMetric.DPS : ClientConfig.DisplayMetric.TOTAL;
            ClientConfig.DISPLAY_METRIC.set(next);
            b.setMessage(metricLabel());
        }).bounds(controlsX, controlsY, 80, 16).build());

        this.addRenderableWidget(Button.builder(locationLabel(), b -> {
            ClientConfig.DisplayLocation next = ClientConfig.DISPLAY_LOCATION.get() == ClientConfig.DisplayLocation.CHAT
                    ? ClientConfig.DisplayLocation.SCREEN : ClientConfig.DisplayLocation.CHAT;
            ClientConfig.DISPLAY_LOCATION.set(next);
            b.setMessage(locationLabel());
        }).bounds(controlsX + 84, controlsY, 80, 16).build());

        int fieldY = controlsY + 20;
        this.maxHealthBox = new EditBox(this.font, controlsX, fieldY, 100, 16,
                Component.translatable("trainingdummy.gui.maxHealth"));
        this.maxHealthBox.setMaxLength(15);
        this.maxHealthBox.setFilter(s -> s.isEmpty() || s.matches("[0-9]*\\.?[0-9]*"));
        this.maxHealthBox.setValue(String.valueOf((long) this.menu.getDummy().getMaxHealth()));
        this.addRenderableWidget(this.maxHealthBox);

        this.addRenderableWidget(Button.builder(Component.translatable("trainingdummy.gui.maxHealth.set"),
                b -> this.submitMaxHealth()).bounds(controlsX + 104, fieldY, 60, 16).build());
    }

    private static Component metricLabel() {
        return Component.translatable(ClientConfig.DISPLAY_METRIC.get() == ClientConfig.DisplayMetric.DPS
                ? "trainingdummy.display.dps.name" : "trainingdummy.display.total.name");
    }

    private static Component locationLabel() {
        return Component.translatable(ClientConfig.DISPLAY_LOCATION.get() == ClientConfig.DisplayLocation.CHAT
                ? "trainingdummy.gui.location.chat" : "trainingdummy.gui.location.screen");
    }

    /** Blank or 0 clears the per-dummy override, going back to the global config default - see network.DummySetMaxHealthPayload. */
    private void submitMaxHealth() {
        String text = this.maxHealthBox.getValue().trim();
        double value = text.isEmpty() ? 0.0D : parseOrZero(text);
        ClientPacketDistributor.sendToServer(new DummySetMaxHealthPayload(this.menu.getDummy().getId(), value));
    }

    private static double parseOrZero(String text) {
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return 0.0D;
        }
    }

    /** Curios slot positions are fixed once built, so a page change reopens the menu on that page - see DummyEntity#openMenuFor. */
    private void requestPage(int page) {
        int clamped = Math.max(0, Math.min(page, this.menu.curiosTotalPages - 1));
        if (clamped != this.menu.curiosPage) {
            ClientPacketDistributor.sendToServer(new DummyCuriosPagePayload(this.menu.getDummy().getId(), clamped));
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int x = this.leftPos;
        int y = this.topPos;

        drawRaisedPanel(graphics, x, y, this.imageWidth, this.imageHeight);

        for (Slot slot : this.menu.slots) {
            drawSlotWell(graphics, x + slot.x - 1, y + slot.y - 1);
        }

        if (this.menu.curiosTotalPages > 1) {
            String pageText = "(" + (this.menu.curiosPage + 1) + "/" + this.menu.curiosTotalPages + ")";
            graphics.text(this.font, pageText, x + DummyMenu.CURIOS_X, y + DummyMenu.TOP_Y - 10, LABEL_COLOR, false);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(this.font, this.title, this.titleLabelX, this.titleLabelY, LABEL_COLOR, false);
        graphics.text(this.font, Component.translatable("gui.trainingdummy.inventory"),
                this.inventoryLabelX, this.inventoryLabelY, LABEL_COLOR, false);
    }

    /**
     * Vanilla only shows a tooltip for slots that have an item in them. Curios' own screen adds a
     * name tooltip for empty accessory slots too (e.g. hovering an empty Ring slot shows "Ring") -
     * same idea here, for both the armor paperdoll and any Curios slots.
     */
    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (this.hoveredSlot instanceof SlotTooltip named && !this.hoveredSlot.hasItem()) {
            graphics.setTooltipForNextFrame(this.font, named.getTooltipName(), mouseX, mouseY);
            return;
        }
        super.extractTooltip(graphics, mouseX, mouseY);
    }

    /** Outer panel with a light top/left edge and dark bottom/right edge, like vanilla's inventory frame. */
    private static void drawRaisedPanel(GuiGraphicsExtractor graphics, int x, int y, int w, int h) {
        graphics.fill(x, y, x + w, y + h, BASE);
        graphics.fill(x, y, x + w, y + 2, LIGHT);
        graphics.fill(x, y, x + 2, y + h, LIGHT);
        graphics.fill(x, y + h - 2, x + w, y + h, SHADOW);
        graphics.fill(x + w - 2, y, x + w, y + h, SHADOW);
    }

    /** Recessed 18x18 slot well: dark top/left edge, light bottom/right edge, mid-gray fill. */
    private static void drawSlotWell(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.fill(x, y, x + 18, y + 18, SHADOW);
        graphics.fill(x, y, x + 17, y + 1, 0xFF373737);
        graphics.fill(x, y, x + 1, y + 17, 0xFF373737);
        graphics.fill(x + 1, y + 17, x + 18, y + 18, LIGHT);
        graphics.fill(x + 17, y + 1, x + 18, y + 18, LIGHT);
        graphics.fill(x + 1, y + 1, x + 17, y + 17, SLOT_WELL);
    }
}
