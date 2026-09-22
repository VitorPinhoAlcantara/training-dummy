package com.trainingdummy.client;

import com.trainingdummy.rank.LeaderboardEntry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.MutableComponent;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;


public class ScoreboardScreen extends Screen {

    private static final DecimalFormat DAMAGE_FORMAT = new DecimalFormat("#,##0.0");
    private static final int ROW_HEIGHT = 12;
    private static final int LIST_TOP = 40;
    private static final int WIDGET_WIDTH = 240;

    private static boolean lastViewedGlobal = true;

    private final List<LeaderboardEntry> localEntries;
    private final List<LeaderboardEntry> globalEntries;
    private final boolean globalAvailable;
    private final String modpackDisplayName;
    private final List<AbstractWidget> contentWidgets = new ArrayList<>();

    private boolean showingGlobal;
    private Button toggleButton;

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
        } else {
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
