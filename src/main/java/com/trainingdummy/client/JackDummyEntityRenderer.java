package com.trainingdummy.client;

import com.trainingdummy.TrainingDummyMod;
import com.trainingdummy.entity.JackDummyEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;

public class JackDummyEntityRenderer extends LivingEntityRenderer<JackDummyEntity, JackRenderState, HumanoidModel<JackRenderState>> {

    private static final Identifier SKIN =
            Identifier.fromNamespaceAndPath(TrainingDummyMod.MODID, "textures/entity/immortal_skin.png");

    public JackDummyEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5F);
        this.addLayer(new HumanoidArmorLayer<>(this,
                ArmorModelSet.bake(ModelLayers.PLAYER_ARMOR, context.getModelSet(), HumanoidModel<JackRenderState>::new),
                context.getEquipmentRenderer()));
        this.addLayer(new JackPumpkinHatLayer(this));
    }

    @Override
    public JackRenderState createRenderState() {
        return new JackRenderState();
    }

    @Override
    public void extractRenderState(JackDummyEntity entity, JackRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        HumanoidMobRenderer.extractHumanoidRenderState(entity, state, partialTicks, this.itemModelResolver);
        this.itemModelResolver.updateForLiving(state.pumpkinHat, entity.getPumpkinHat(), ItemDisplayContext.HEAD, entity);
    }

    @Override
    public Identifier getTextureLocation(JackRenderState state) {
        return SKIN;
    }
}
