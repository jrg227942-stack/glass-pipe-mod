package com.glasspipe.block.entity;

import com.glasspipe.GlassPipeTransportMod;
import com.glasspipe.block.ModBlocks;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlockEntities {

    public static BlockEntityType<GlassPipeBlockEntity> GLASS_PIPE;
    public static BlockEntityType<FilterPipeBlockEntity> FILTER_PIPE;

    public static void register() {
        GLASS_PIPE = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                Identifier.of(GlassPipeTransportMod.MOD_ID, "glass_pipe"),
                FabricBlockEntityTypeBuilder.create(GlassPipeBlockEntity::new,
                        ModBlocks.GLASS_PIPE).build()
        );

        FILTER_PIPE = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                Identifier.of(GlassPipeTransportMod.MOD_ID, "filter_pipe"),
                FabricBlockEntityTypeBuilder.create(FilterPipeBlockEntity::new,
                        ModBlocks.FILTER_PIPE).build()
        );

        GlassPipeTransportMod.LOGGER.debug("Registered block entities");
    }
}
