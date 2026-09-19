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

/**
 * Renders the dummy with the vanilla player model. Defaults to the "Steve" skin, but if it's
 * been renamed (with a name tag) to something matching a real Minecraft account, it wears that
 * player's actual skin instead - including switching between the wide/slim arm models, which is
 * why {@link #model} gets swapped per-frame rather than being fixed at construction like a
 * normal {@link LivingEntityRenderer}.
 */
public class DummyEntityRenderer extends LivingEntityRenderer<DummyEntity, PlayerModel<DummyEntity>> {

    private static final ResourceLocation DEFAULT_SKIN =
            ResourceLocation.withDefaultNamespace("textures/entity/player/wide/steve.png");

    /**
     * Bundled instead of resolved through DummySkinCache like every other nickname - a local
     * texture is lighter than a live skin lookup (two network round trips: Mojang profile by
     * username, then the actual texture download), not heavier, and this nickname shouldn't wear
     * a real player's face anyway.
     */
    private static final String IMMORTAL_NICK = "Immortal";
    private static final ResourceLocation IMMORTAL_SKIN =
            ResourceLocation.fromNamespaceAndPath(TrainingDummyMod.MODID, "textures/entity/immortal_skin.png");

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
        this.model = IMMORTAL_NICK.equals(entity.getSkinName())
                ? this.wideModel
                : DummySkinCache.getSkin(entity.getSkinName())
                        .map(skin -> skin.model() == PlayerSkin.Model.SLIM ? this.slimModel : this.wideModel)
                        .orElse(this.wideModel);
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(DummyEntity entity) {
        if (IMMORTAL_NICK.equals(entity.getSkinName())) {
            return IMMORTAL_SKIN;
        }
        return DummySkinCache.getSkin(entity.getSkinName())
                .map(PlayerSkin::texture)
                .orElse(DEFAULT_SKIN);
    }
}
