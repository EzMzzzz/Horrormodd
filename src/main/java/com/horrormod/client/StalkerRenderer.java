package com.horrormod.client;

import com.horrormod.StalkerEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class StalkerRenderer extends MobEntityRenderer<StalkerEntity, BipedEntityModel<StalkerEntity>> {
    private static final Identifier TEXTURE = new Identifier("horrormod", "textures/entity/stalker.png");

    public StalkerRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new BipedEntityModel<>(ctx.getPart(EntityModelLayers.ZOMBIE)), 0.5f);
    }

    @Override
    protected void scale(StalkerEntity entity, MatrixStack matrices, float amount) {
        matrices.scale(1.0f, 1.3f, 1.0f); // taller than a player
    }

    @Override
    public Identifier getTexture(StalkerEntity entity) { return TEXTURE; }
}
