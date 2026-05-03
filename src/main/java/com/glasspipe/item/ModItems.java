package com.glasspipe.item;

import com.glasspipe.GlassPipeTransportMod;
import com.glasspipe.block.ModBlocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {

    public static final Item GLASS_PIPE = new BlockItem(ModBlocks.GLASS_PIPE,
            new Item.Settings());

    public static final Item FILTER_PIPE = new BlockItem(ModBlocks.FILTER_PIPE,
            new Item.Settings());

    public static final Item SPEED_UPGRADE = new SpeedUpgradeItem(
            new Item.Settings().maxCount(1));

    public static final Item SORTING_UPGRADE = new SortingUpgradeItem(
            new Item.Settings().maxCount(1));

    public static final Item PIPE_WRENCH = new PipeWrenchItem(
            new Item.Settings().maxCount(1));

    public static void register() {
        Registry.register(Registries.ITEM, Identifier.of(GlassPipeTransportMod.MOD_ID, "glass_pipe"),
                GLASS_PIPE);
        Registry.register(Registries.ITEM, Identifier.of(GlassPipeTransportMod.MOD_ID, "filter_pipe"),
                FILTER_PIPE);
        Registry.register(Registries.ITEM, Identifier.of(GlassPipeTransportMod.MOD_ID, "speed_upgrade"),
                SPEED_UPGRADE);
        Registry.register(Registries.ITEM, Identifier.of(GlassPipeTransportMod.MOD_ID, "sorting_upgrade"),
                SORTING_UPGRADE);
        Registry.register(Registries.ITEM, Identifier.of(GlassPipeTransportMod.MOD_ID, "pipe_wrench"),
                PIPE_WRENCH);

        GlassPipeTransportMod.LOGGER.debug("Registered mod items");
    }
}
