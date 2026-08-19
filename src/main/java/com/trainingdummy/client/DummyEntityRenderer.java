package com.trainingdummy.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.trainingdummy.entity.DummyEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.component.ResolvableProfile;

import java.util.Optional;

public class DummyEntityRenderer extends LivingEntityRenderer<DummyEntity, DummyRenderState, HumanoidModel<DummyRenderState>> {

    public static final Identifier DEFAULT_SKIN = Identifier.withDefaultNamespace("textures/entity/player/wide/steve.png");

    private final HumanoidModel<DummyRenderState> wideModel;
    private final HumanoidModel<DummyRenderState> slimModel;
    private final PlayerSkinRenderCache skinRenderCache;

    public DummyEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5F);
        this.wideModel = this.getModel();
        this.slimModel = new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM));
        this.skinRenderCache = context.getPlayerSkinRenderCache();
        this.addLayer(new HumanoidArmorLayer<>(this,
                ArmorModelSet.bake(ModelLayers.PLAYER_ARMOR, context.getModelSet(), HumanoidModel<DummyRenderState>::new),
                context.getEquipmentRenderer()));
    }

    @Override
    public DummyRenderState createRenderState() {
        return new DummyRenderState();
    }

    @Override
    public void extractRenderState(DummyEntity entity, DummyRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        HumanoidMobRenderer.extractHumanoidRenderState(entity, state, partialTicks, this.itemModelResolver);

        String name = entity.getSkinName();
        if (!name.isBlank()) {
            Optional<PlayerSkinRenderCache.RenderInfo> info =
                    this.skinRenderCache.lookup(ResolvableProfile.createUnresolved(name)).getNow(Optional.empty());
            if (info.isPresent()) {
                PlayerSkin skin = info.get().playerSkin();
                state.skinTexture = skin.body().texturePath();
                state.skinModel = skin.model();
                return;
            }
        }
        state.skinTexture = DEFAULT_SKIN;
        state.skinModel = PlayerModelType.WIDE;
    }

    @Override
    public void submit(DummyRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        this.model = state.skinModel == PlayerModelType.SLIM ? this.slimModel : this.wideModel;
        super.submit(state, poseStack, submitNodeCollector, camera);
    }

    @Override
    public Identifier getTextureLocation(DummyRenderState state) {
        return state.skinTexture;
    }
}
