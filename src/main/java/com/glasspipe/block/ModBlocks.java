package com.glasspipe.block;

import com.glasspipe.GlassPipeTransportMod;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlocks {

    public static final GlassPipeBlock GLASS_PIPE = new GlassPipeBlock(
            FabricBlockSettings.copyOf(Blocks.GLASS)
                    .nonOpaque()
                    .strength(0.3f)
                    .luminance(state -> 2)
    );

    public static final FilterPipeBlock FILTER_PIPE = new FilterPipeBlock(
            FabricBlockSettings.copyOf(Blocks.GLASS)
                    .nonOpaque()
                    .strength(0.3f)
                    .luminance(state -> 2)
    );

    public static void register() {
        Registry.register(Registries.BLOCK,
                Identifier.of(GlassPipeTransportMod.MOD_ID, "glass_pipe"), GLASS_PIPE);
        Registry.register(Registries.BLOCK,
                Identifier.of(GlassPipeTransportMod.MOD_ID, "filter_pipe"), FILTER_PIPE);

        GlassPipeTransportMod.LOGGER.debug("Registered mod blocks");
    }
}
