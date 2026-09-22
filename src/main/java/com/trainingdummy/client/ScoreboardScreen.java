package com.trainingdummy.client;

import com.mojang.authlib.GameProfile;
import com.trainingdummy.rank.LeaderboardEntry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ScoreboardScreen extends Screen {

    private static final DecimalFormat DAMAGE_FORMAT = new DecimalFormat("#,##0.0");
    private static final int ROW_HEIGHT = 12;
    private static final int LIST_TOP = 40;
    private static final int WIDGET_WIDTH = 240;

    private static final int TOP_PREVIEW_WIDTH = 160;
    private static final int TOP_PREVIEW_HEIGHT = 240;
    private static final int TOP_PREVIEW_SCALE = 90;
    private static final float PREVIEW_ROTATION_SENSITIVITY = -1.0F;

    private static boolean lastViewedGlobal = true;

    private final List<LeaderboardEntry> localEntries;
    private final List<LeaderboardEntry> globalEntries;
    private final boolean globalAvailable;
    private final String modpackDisplayName;
    private final List<AbstractWidget> contentWidgets = new ArrayList<>();

    private boolean showingGlobal;
    private Button toggleButton;
    private LivingEntity topPreviewEntity;
    private UUID topPreviewUuid;
    private String topPreviewName = "";
    private float previewYaw = 180.0F;
    private boolean rotatingPreview;

    public ScoreboardScreen(List<LeaderboardEntry> localEntries, List<LeaderboardEntry> globalEntries,
                             boolean globalAvailable, String modpackDisplayName) {
        super(Component.translatable("gui.trainingdummy.title"));
        this.localEntries = localEntries;
        this.globalEntries = globalEntries;
        this.globalAvailable = globalAvailable;
        this.modpackDisplayName = modpackDisplayName;
        this.showingGlobal = globalAvailable && lastViewedGlobal;
    }

    @Override
    protected void init() {
        super.init();

        if (this.globalAvailable) {
            this.toggleButton = this.addRenderableWidget(Button.builder(this.modeLabel(), b -> this.toggleMode())
                    .bounds(this.width / 2 - 50, this.height - 55, 100, 20).build());
        }

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> this.onClose())
                .bounds(this.width / 2 - 50, this.height - 30, 100, 20).build());

        this.rebuildContent();
    }

    private Component modeLabel() {
        return Component.translatable(this.showingGlobal ? "gui.trainingdummy.mode.global" : "gui.trainingdummy.mode.local");
    }

    private void toggleMode() {
        this.showingGlobal = !this.showingGlobal;
        lastViewedGlobal = this.showingGlobal;
        this.toggleButton.setMessage(this.modeLabel());
        this.rebuildContent();
    }

    private void rebuildContent() {
        for (AbstractWidget widget : this.contentWidgets) {
            this.removeWidget(widget);
        }
        this.contentWidgets.clear();

        List<LeaderboardEntry> entries = this.showingGlobal ? this.globalEntries : this.localEntries;
        Component title = this.showingGlobal ? this.globalTitle() : Component.translatable("gui.trainingdummy.title.local");
        this.contentWidgets.add(this.addCenteredLine(title, 16));

        if (entries.isEmpty()) {
            this.contentWidgets.add(this.addCenteredLine(Component.translatable("gui.trainingdummy.empty"), LIST_TOP));
            this.topPreviewEntity = null;
            this.topPreviewUuid = null;
            this.topPreviewName = "";
        } else {
            this.updateTopPreviewEntity(entries.get(0));
            for (int i = 0; i < entries.size(); i++) {
                LeaderboardEntry entry = entries.get(i);
                MutableComponent name = Component.literal(entry.playerName());
                ChatFormatting rankColor = rankColor(i);
                if (rankColor != null) {
                    name = name.withStyle(rankColor);
                }
                Component line = Component.translatable("gui.trainingdummy.entry",
                        i + 1, name, DAMAGE_FORMAT.format(entry.damage()));
                this.contentWidgets.add(this.addCenteredLine(line, LIST_TOP + i * ROW_HEIGHT));
            }
        }
    }

    private void updateTopPreviewEntity(LeaderboardEntry topEntry) {
        this.topPreviewName = topEntry.playerName();
        if (this.minecraft == null || this.minecraft.level == null) {
            return;
        }
        if (topEntry.playerUuid().equals(this.topPreviewUuid)) {
            return;
        }
        this.topPreviewUuid = topEntry.playerUuid();
        this.topPreviewEntity = new RankPreviewPlayer(this.minecraft.level,
                new GameProfile(topEntry.playerUuid(), topEntry.playerName()));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);

        if (this.topPreviewEntity != null) {
            int[] bounds = this.previewBounds();
            int x0 = bounds[0];
            int y0 = bounds[1];
            int x1 = bounds[2];
            int y1 = bounds[3];

            EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
            EntityRenderer<? super LivingEntity, ?> renderer = dispatcher.getRenderer(this.topPreviewEntity);
            EntityRenderState renderState = renderer.createRenderState(this.topPreviewEntity, partialTick);
            renderState.shadowPieces.clear();
            renderState.outlineColor = 0;

            if (renderState instanceof LivingEntityRenderState livingState) {
                livingState.bodyRot = this.previewYaw;
                livingState.yRot = 0.0F;
                livingState.xRot = 0.0F;
                livingState.boundingBoxWidth = livingState.boundingBoxWidth / livingState.scale;
                livingState.boundingBoxHeight = livingState.boundingBoxHeight / livingState.scale;
                livingState.scale = 1.0F;
            }

            Vector3f translation = new Vector3f(0.0F, renderState.boundingBoxHeight / 2.0F + 0.0625F, 0.0F);
            Quaternionf rotation = new Quaternionf().rotateZ((float) Math.PI);

            graphics.entity(renderState, TOP_PREVIEW_SCALE, translation, rotation, null, x0, y0, x1, y1);

            Component nameLabel = Component.literal(this.topPreviewName).withStyle(ChatFormatting.GREEN);
            graphics.centeredText(this.font, nameLabel, (x0 + x1) / 2, y0 - 12, 0xFFFFFF);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (super.mouseClicked(event, doubleClick)) {
            return true;
        }
        if (event.button() == 0 && this.topPreviewEntity != null && this.isOverTopPreview(event.x(), event.y())) {
            this.rotatingPreview = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (this.rotatingPreview) {
            this.previewYaw += (float) dragX * PREVIEW_ROTATION_SENSITIVITY;
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == 0) {
            this.rotatingPreview = false;
        }
        return super.mouseReleased(event);
    }

    private boolean isOverTopPreview(double mouseX, double mouseY) {
        int[] bounds = this.previewBounds();
        return mouseX >= bounds[0] && mouseX < bounds[2] && mouseY >= bounds[1] && mouseY < bounds[3];
    }

    private int[] previewBounds() {
        int x1 = this.width / 2 - WIDGET_WIDTH / 2 - 8;
        int x0 = Math.max(4, x1 - TOP_PREVIEW_WIDTH);
        int y0 = Math.max(20, this.height / 2 - TOP_PREVIEW_HEIGHT / 2);
        int y1 = y0 + TOP_PREVIEW_HEIGHT;
        return new int[] {x0, y0, x1, y1};
    }

    private Component globalTitle() {
        if (this.modpackDisplayName.isBlank()) {
            return Component.translatable("gui.trainingdummy.title.global");
        }
        return Component.translatable("gui.trainingdummy.title.global.named", this.modpackDisplayName);
    }

    private static ChatFormatting rankColor(int index) {
        return switch (index) {
            case 0 -> ChatFormatting.GREEN;
            case 1, 2 -> ChatFormatting.YELLOW;
            default -> null;
        };
    }

    private AbstractWidget addCenteredLine(Component text, int y) {
        int textWidth = Math.min(WIDGET_WIDTH, this.font.width(text));
        int x = (this.width - textWidth) / 2;
        return this.addRenderableWidget(new StringWidget(x, y, textWidth, 12, text, this.font));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
