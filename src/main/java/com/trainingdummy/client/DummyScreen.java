package com.trainingdummy.client;

import com.trainingdummy.config.ClientConfig;
import com.trainingdummy.entity.DummyDisplayMetric;
import com.trainingdummy.menu.DummyMenu;
import com.trainingdummy.menu.SlotTooltip;
import com.trainingdummy.network.DummyClearEffectsPayload;
import com.trainingdummy.network.DummyCuriosPagePayload;
import com.trainingdummy.network.DummySetDisplayMetricPayload;
import com.trainingdummy.network.DummySetMaxHealthPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.network.PacketDistributor;


public class DummyScreen extends AbstractContainerScreen<DummyMenu> {

    private static final int BASE = 0xFFC6C6C6;
    private static final int LIGHT = 0xFFFFFFFF;
    private static final int SHADOW = 0xFF555555;
    private static final int SLOT_WELL = 0xFF8B8B8B;
    private static final int LABEL_COLOR = 0xFF404040;

    private EditBox maxHealthBox;
    private DummyDisplayMetric displayMetric;

    public DummyScreen(DummyMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = menu.imageWidth;
        this.imageHeight = menu.imageHeight;
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelX = DummyMenu.PLAYER_INV_X;
        this.inventoryLabelY = menu.playerInvY - 10;
        this.displayMetric = menu.displayMetric;
    }


    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
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

        this.addRenderableWidget(Button.builder(this.metricLabel(), b -> {
            this.displayMetric = this.displayMetric.next();
            PacketDistributor.sendToServer(
                    new DummySetDisplayMetricPayload(this.menu.getDummy().getId(), this.displayMetric));
            b.setMessage(this.metricLabel());
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

        int effectsY = fieldY + 20;
        this.addRenderableWidget(Button.builder(Component.translatable("trainingdummy.gui.clearEffects"),
                b -> PacketDistributor.sendToServer(new DummyClearEffectsPayload(this.menu.getDummy().getId())))
                .bounds(controlsX, effectsY, 164, 16).build());
    }

    private Component metricLabel() {
        String key = switch (this.displayMetric) {
            case DPS -> "trainingdummy.display.dps.name";
            case PER_HIT -> "trainingdummy.display.perhit.name";
            case TOTAL -> "trainingdummy.display.total.name";
        };
        return Component.translatable(key);
    }

    private static Component locationLabel() {
        return Component.translatable(ClientConfig.DISPLAY_LOCATION.get() == ClientConfig.DisplayLocation.CHAT
                ? "trainingdummy.gui.location.chat" : "trainingdummy.gui.location.screen");
    }


    private void submitMaxHealth() {
        String text = this.maxHealthBox.getValue().trim();
        double value = text.isEmpty() ? 0.0D : parseOrZero(text);
        PacketDistributor.sendToServer(new DummySetMaxHealthPayload(this.menu.getDummy().getId(), value));
    }

    private static double parseOrZero(String text) {
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return 0.0D;
        }
    }


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

        if (this.menu.curiosTotalPages > 1) {
            String pageText = "(" + (this.menu.curiosPage + 1) + "/" + this.menu.curiosTotalPages + ")";
            graphics.drawString(this.font, pageText, x + DummyMenu.CURIOS_X, y + DummyMenu.TOP_Y - 10, LABEL_COLOR, false);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, LABEL_COLOR, false);
        graphics.drawString(this.font, Component.translatable("gui.trainingdummy.inventory"),
                this.inventoryLabelX, this.inventoryLabelY, LABEL_COLOR, false);
    }


    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (this.hoveredSlot instanceof SlotTooltip named && !this.hoveredSlot.hasItem()) {
            graphics.renderTooltip(this.font, named.getTooltipName(), mouseX, mouseY);
            return;
        }
        super.renderTooltip(graphics, mouseX, mouseY);
    }


    private static void drawRaisedPanel(GuiGraphics graphics, int x, int y, int w, int h) {
        graphics.fill(x, y, x + w, y + h, BASE);
        graphics.fill(x, y, x + w, y + 2, LIGHT);
        graphics.fill(x, y, x + 2, y + h, LIGHT);
        graphics.fill(x, y + h - 2, x + w, y + h, SHADOW);
        graphics.fill(x + w - 2, y, x + w, y + h, SHADOW);
    }


    private static void drawSlotWell(GuiGraphics graphics, int x, int y) {
        graphics.fill(x, y, x + 18, y + 18, SHADOW);
        graphics.fill(x, y, x + 17, y + 1, 0xFF373737);
        graphics.fill(x, y, x + 1, y + 17, 0xFF373737);
        graphics.fill(x + 1, y + 17, x + 18, y + 18, LIGHT);
        graphics.fill(x + 17, y + 1, x + 18, y + 18, LIGHT);
        graphics.fill(x + 1, y + 1, x + 17, y + 17, SLOT_WELL);
    }
}
