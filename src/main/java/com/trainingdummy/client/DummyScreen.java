package com.trainingdummy.client;

import com.trainingdummy.menu.DummyMenu;
import com.trainingdummy.menu.SlotTooltip;
import com.trainingdummy.network.DummyCuriosPagePayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.network.PacketDistributor;

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

    public DummyScreen(DummyMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = menu.imageWidth;
        this.imageHeight = menu.imageHeight;
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
    }

    /** Curios slot positions are fixed once built, so a page change reopens the menu on that page - see DummyEntity#openMenuFor. */
    private void requestPage(int page) {
        int clamped = Math.max(0, Math.min(page, this.menu.curiosTotalPages - 1));
        if (clamped != this.menu.curiosPage) {
            PacketDistributor.sendToServer(new DummyCuriosPagePayload(this.menu.getDummy().getId(), clamped));
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        drawRaisedPanel(graphics, x, y, this.imageWidth, this.imageHeight);

        for (Slot slot : this.menu.slots) {
            drawSlotWell(graphics, x + slot.x - 1, y + slot.y - 1);
        }

        if (this.menu.curiosTotalSlotCount > 0) {
            Component label = this.menu.curiosTotalPages > 1
                    ? Component.translatable("gui.trainingdummy.curios").append(" (" + (this.menu.curiosPage + 1) + "/" + this.menu.curiosTotalPages + ")")
                    : Component.translatable("gui.trainingdummy.curios");
            graphics.drawString(this.font, label, x + DummyMenu.CURIOS_X, y + DummyMenu.TOP_Y - 10, LABEL_COLOR, false);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, LABEL_COLOR, false);
        graphics.drawString(this.font, Component.translatable("gui.trainingdummy.inventory"),
                this.inventoryLabelX, this.inventoryLabelY, LABEL_COLOR, false);
    }

    /**
     * Vanilla only shows a tooltip for slots that have an item in them. Curios' own screen adds a
     * name tooltip for empty accessory slots too (e.g. hovering an empty Ring slot shows "Ring") -
     * same idea here, for both the armor paperdoll and any Curios slots.
     */
    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (this.hoveredSlot instanceof SlotTooltip named && !this.hoveredSlot.hasItem()) {
            graphics.renderTooltip(this.font, named.getTooltipName(), mouseX, mouseY);
            return;
        }
        super.renderTooltip(graphics, mouseX, mouseY);
    }

    /** Outer panel with a light top/left edge and dark bottom/right edge, like vanilla's inventory frame. */
    private static void drawRaisedPanel(GuiGraphics graphics, int x, int y, int w, int h) {
        graphics.fill(x, y, x + w, y + h, BASE);
        graphics.fill(x, y, x + w, y + 2, LIGHT);
        graphics.fill(x, y, x + 2, y + h, LIGHT);
        graphics.fill(x, y + h - 2, x + w, y + h, SHADOW);
        graphics.fill(x + w - 2, y, x + w, y + h, SHADOW);
    }

    /** Recessed 18x18 slot well: dark top/left edge, light bottom/right edge, mid-gray fill. */
    private static void drawSlotWell(GuiGraphics graphics, int x, int y) {
        graphics.fill(x, y, x + 18, y + 18, SHADOW);
        graphics.fill(x, y, x + 17, y + 1, 0xFF373737);
        graphics.fill(x, y, x + 1, y + 17, 0xFF373737);
        graphics.fill(x + 1, y + 17, x + 18, y + 18, LIGHT);
        graphics.fill(x + 17, y + 1, x + 18, y + 18, LIGHT);
        graphics.fill(x + 1, y + 1, x + 17, y + 17, SLOT_WELL);
    }
}
