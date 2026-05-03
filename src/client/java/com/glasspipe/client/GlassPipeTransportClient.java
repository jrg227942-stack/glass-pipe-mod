package com.glasspipe.client;

import com.glasspipe.block.entity.ModBlockEntities;
import com.glasspipe.client.render.GlassPipeBlockEntityRenderer;
import com.glasspipe.client.screen.FilterPipeScreen;
import com.glasspipe.screen.ModScreenHandlers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import com.glasspipe.block.ModBlocks;

@Environment(EnvType.CLIENT)
public class GlassPipeTransportClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Register block entity renderers
        BlockEntityRendererFactories.register(ModBlockEntities.GLASS_PIPE,
                GlassPipeBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(ModBlockEntities.FILTER_PIPE,
                GlassPipeBlockEntityRenderer::new);

        // Register screen
        HandledScreens.register(ModScreenHandlers.FILTER_PIPE_SCREEN_HANDLER,
                FilterPipeScreen::new);

        // Set render layers for transparent blocks
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.GLASS_PIPE, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.FILTER_PIPE, RenderLayer.getTranslucent());
    }
}
