package com.horrormod.client;

import com.horrormod.HorrorMod;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class HorrorModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(HorrorMod.STALKER, StalkerRenderer::new);
    }
}
