package com.trainingdummy.client;

import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerModelType;

public class DummyRenderState extends HumanoidRenderState {

    public Identifier skinTexture = DummyEntityRenderer.DEFAULT_SKIN;
    public PlayerModelType skinModel = PlayerModelType.WIDE;
}
