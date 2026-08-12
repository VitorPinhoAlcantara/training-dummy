package com.trainingdummy.client;

import com.trainingdummy.TrainingDummyMod;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class DummyHudOverlay {

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(TrainingDummyMod.MODID, "damage_display");

    public static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        ClientDamageTracker.currentHudMessage().ifPresent(message -> {
            Minecraft mc = Minecraft.getInstance();
            int screenWidth = mc.getWindow().getGuiScaledWidth();
            int screenHeight = mc.getWindow().getGuiScaledHeight();
            int textWidth = mc.font.width(message);

            int x = (screenWidth - textWidth) / 2;
            // A bit above the XP bar (which itself sits right above the hotbar).
            int y = screenHeight - 55;

            graphics.drawString(mc.font, message, x + 1, y + 1, 0x000000, false);
            graphics.drawString(mc.font, message, x, y, 0xFFFF5555, false);
        });
    }

    private DummyHudOverlay() {
    }
}
