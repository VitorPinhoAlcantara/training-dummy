package com.trainingdummy.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.trainingdummy.TrainingDummyMod;
import com.trainingdummy.entity.DummyEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;


public class DummyEntityRenderer extends LivingEntityRenderer<DummyEntity, PlayerModel<DummyEntity>> {

    private static final ResourceLocation DEFAULT_SKIN =
            ResourceLocation.withDefaultNamespace("textures/entity/player/wide/steve.png");


    private static final String IMMORTAL_NICK = "Immortal";
    private static final ResourceLocation IMMORTAL_SKIN =
            ResourceLocation.fromNamespaceAndPath(TrainingDummyMod.MODID, "textures/entity/immortal_skin.png");


    private static final String HEROBRINE_NICK = "Herobrine";
    private static final ResourceLocation HEROBRINE_SKIN =
            ResourceLocation.fromNamespaceAndPath(TrainingDummyMod.MODID, "textures/entity/herobrine_skin.png");

    private final PlayerModel<DummyEntity> wideModel;
    private final PlayerModel<DummyEntity> slimModel;

    public DummyEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
        this.wideModel = this.getModel();
        this.slimModel = new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM), true);
        this.addLayer(new HumanoidArmorLayer<>(this,
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                context.getModelManager()));
    }

    @Override
    public void render(DummyEntity entity, float entityYaw, float partialTicks, PoseStack poseStack,
                        MultiBufferSource buffer, int packedLight) {
        String name = entity.getSkinName();
        this.model = IMMORTAL_NICK.equals(name) || HEROBRINE_NICK.equals(name)
                ? this.wideModel
                : DummySkinCache.getSkin(name)
                        .map(skin -> skin.model() == PlayerSkin.Model.SLIM ? this.slimModel : this.wideModel)
                        .orElse(this.wideModel);
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(DummyEntity entity) {
        String name = entity.getSkinName();
        if (IMMORTAL_NICK.equals(name)) {
            return IMMORTAL_SKIN;
        }
        if (HEROBRINE_NICK.equals(name)) {
            return HEROBRINE_SKIN;
        }
        return DummySkinCache.getSkin(name)
                .map(PlayerSkin::texture)
                .orElse(DEFAULT_SKIN);
    }
}
