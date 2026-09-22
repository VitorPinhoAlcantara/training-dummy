package com.trainingdummy;

import com.trainingdummy.client.DummyEntityRenderer;
import com.trainingdummy.client.DummyHudOverlay;
import com.trainingdummy.client.DummyScreen;
import com.trainingdummy.client.JackDummyEntityRenderer;
import com.trainingdummy.registry.ModEntities;
import com.trainingdummy.registry.ModMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@Mod(value = TrainingDummyMod.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = TrainingDummyMod.MODID, value = Dist.CLIENT)
public class TrainingDummyClient {

    public TrainingDummyClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.DUMMY.get(), DummyEntityRenderer::new);
        event.registerEntityRenderer(ModEntities.JACK.get(), JackDummyEntityRenderer::new);
        event.registerEntityRenderer(ModEntities.SCOREBOARD_DUMMY.get(), DummyEntityRenderer::new);
    }

    @SubscribeEvent
    static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, DummyHudOverlay.ID, DummyHudOverlay::render);
    }

    @SubscribeEvent
    static void onRegisterMenuScreens(net.neoforged.neoforge.client.event.RegisterMenuScreensEvent event) {
        event.register(ModMenus.DUMMY_MENU.get(), DummyScreen::new);
    }
}
