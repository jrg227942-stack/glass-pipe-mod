package com.glasspipe.screen;

import com.glasspipe.GlassPipeTransportMod;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class ModScreenHandlers {

    public static ScreenHandlerType<FilterPipeScreenHandler> FILTER_PIPE_SCREEN_HANDLER;

    public static void register() {
        FILTER_PIPE_SCREEN_HANDLER = Registry.register(
                Registries.SCREEN_HANDLER,
                Identifier.of(GlassPipeTransportMod.MOD_ID, "filter_pipe"),
                new ExtendedScreenHandlerType<>(FilterPipeScreenHandler::new, BlockPos.PACKET_CODEC)
        );

        GlassPipeTransportMod.LOGGER.debug("Registered screen handlers");
    }
}
