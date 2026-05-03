package com.glasspipe;

import com.glasspipe.block.ModBlocks;
import com.glasspipe.block.entity.ModBlockEntities;
import com.glasspipe.item.ModItems;
import com.glasspipe.network.ModNetworking;
import com.glasspipe.screen.ModScreenHandlers;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlassPipeTransportMod implements ModInitializer {

    public static final String MOD_ID = "glass_pipe_transport";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final ItemGroup ITEM_GROUP = FabricItemGroup.builder()
            .icon(() -> new ItemStack(ModItems.GLASS_PIPE))
            .displayName(Text.translatable("itemGroup.glass_pipe_transport.main"))
            .entries((context, entries) -> {
                entries.add(ModItems.GLASS_PIPE);
                entries.add(ModItems.FILTER_PIPE);
                entries.add(ModItems.SPEED_UPGRADE);
                entries.add(ModItems.SORTING_UPGRADE);
                entries.add(ModItems.PIPE_WRENCH);
            })
            .build();

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Glass Pipe Transport System");

        ModBlocks.register();
        ModItems.register();
        ModBlockEntities.register();
        ModScreenHandlers.register();
        ModNetworking.register();

        Registry.register(Registries.ITEM_GROUP,
                Identifier.of(MOD_ID, "main"), ITEM_GROUP);

        LOGGER.info("Glass Pipe Transport System initialized successfully!");
    }

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }
}
