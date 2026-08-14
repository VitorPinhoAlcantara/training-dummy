package com.trainingdummy.client;

import com.trainingdummy.TrainingDummyMod;
import com.trainingdummy.config.ClientConfig;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class DummyHudOverlay {

    public static final Identifier ID =
            Identifier.fromNamespaceAndPath(TrainingDummyMod.MODID, "damage_display");

    public static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        ClientDamageTracker.currentHudMessage().ifPresent(message -> {
            Minecraft mc = Minecraft.getInstance();
            int screenWidth = mc.getWindow().getGuiScaledWidth();
            int screenHeight = mc.getWindow().getGuiScaledHeight();
            int textWidth = mc.font.width(message);

            int x = (screenWidth - textWidth) / 2 + ClientConfig.DISPLAY_OFFSET_X.get();
            int y = screenHeight - ClientConfig.DISPLAY_OFFSET_Y.get();
            int color = ClientConfig.displayColorArgb();

            graphics.text(mc.font, message, x + 1, y + 1, 0x000000, false);
            graphics.text(mc.font, message, x, y, color, false);
        });
    }

    private DummyHudOverlay() {
    }
}
