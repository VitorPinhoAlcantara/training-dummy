package com.trainingdummy.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.trainingdummy.entity.JackDummyEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;


public class JackPumpkinHatLayer extends RenderLayer<JackDummyEntity, PlayerModel<JackDummyEntity>> {

    private final ItemInHandRenderer itemInHandRenderer;

    public JackPumpkinHatLayer(RenderLayerParent<JackDummyEntity, PlayerModel<JackDummyEntity>> parent,
                                ItemInHandRenderer itemInHandRenderer) {
        super(parent);
        this.itemInHandRenderer = itemInHandRenderer;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, JackDummyEntity entity,
                        float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                        float netHeadYaw, float headPitch) {
        ItemStack pumpkinHat = entity.getPumpkinHat();
        if (pumpkinHat.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        this.getParentModel().getHead().translateAndRotate(poseStack);
        CustomHeadLayer.translateToHead(poseStack, false);
        this.itemInHandRenderer.renderItem(entity, pumpkinHat, ItemDisplayContext.HEAD, false, poseStack, buffer, packedLight);
        poseStack.popPose();
    }
}
