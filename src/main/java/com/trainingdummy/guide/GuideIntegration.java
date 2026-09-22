package com.trainingdummy.guide;

import com.trainingdummy.TrainingDummyMod;
import guideme.Guide;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;

public final class GuideIntegration {

    private static final String GUIDEME_MODID = "guideme";

    public static boolean isLoaded() {
        return ModList.get().isLoaded(GUIDEME_MODID);
    }

    public static void register() {
        Guide.builder(ResourceLocation.fromNamespaceAndPath(TrainingDummyMod.MODID, "guide")).build();
    }

    private GuideIntegration() {
    }
}
