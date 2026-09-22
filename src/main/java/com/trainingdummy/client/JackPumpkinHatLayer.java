package com.trainingdummy.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class JackPumpkinHatLayer extends RenderLayer<JackRenderState, HumanoidModel<JackRenderState>> {

    public JackPumpkinHatLayer(RenderLayerParent<JackRenderState, HumanoidModel<JackRenderState>> parent) {
        super(parent);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords,
                        JackRenderState state, float yRot, float xRot) {
        if (state.pumpkinHat.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        HumanoidModel<JackRenderState> parentModel = this.getParentModel();
        parentModel.root().translateAndRotate(poseStack);
        parentModel.translateToHead(poseStack);
        poseStack.translate(0.0F, -0.25F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.scale(0.625F, -0.625F, -0.625F);
        state.pumpkinHat.submit(poseStack, submitNodeCollector, lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
    }
}
