package com.trainingdummy.client;

import com.trainingdummy.TrainingDummyMod;
import com.trainingdummy.entity.JackDummyEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;


public class JackDummyEntityRenderer extends HumanoidMobRenderer<JackDummyEntity, PlayerModel<JackDummyEntity>> {

    private static final ResourceLocation SKIN =
            ResourceLocation.fromNamespaceAndPath(TrainingDummyMod.MODID, "textures/entity/immortal_skin.png");

    public JackDummyEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
        this.addLayer(new HumanoidArmorLayer<>(this,
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                context.getModelManager()));
        this.addLayer(new JackPumpkinHatLayer(this, context.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(JackDummyEntity entity) {
        return SKIN;
    }
}
