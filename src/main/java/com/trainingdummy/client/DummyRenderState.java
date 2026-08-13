package com.trainingdummy.client;

import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerModelType;

/**
 * Extra per-frame data {@link DummyEntityRenderer} needs on top of the normal humanoid pose data
 * {@code HumanoidMobRenderer.extractHumanoidRenderState(...)} already fills in: which texture to
 * paint on the model, and whether that skin needs the slim (3px) or wide (4px) arm model.
 */
public class DummyRenderState extends HumanoidRenderState {

    public Identifier skinTexture = DummyEntityRenderer.DEFAULT_SKIN;
    public PlayerModelType skinModel = PlayerModelType.WIDE;
}
