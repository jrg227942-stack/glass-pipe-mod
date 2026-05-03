package com.glasspipe.network;

import com.glasspipe.GlassPipeTransportMod;
import com.glasspipe.block.entity.FilterPipeBlockEntity;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

public class ModNetworking {

    public static void register() {
        // Register the toggle filter mode packet
        PayloadTypeRegistry.playC2S().register(
                ToggleFilterModePayload.ID,
                ToggleFilterModePayload.CODEC
        );

        ServerPlayNetworking.registerGlobalReceiver(
                ToggleFilterModePayload.ID,
                (payload, context) -> {
                    ServerPlayerEntity player = context.player();
                    BlockPos pos = payload.pos();

                    context.server().execute(() -> {
                        BlockEntity be = player.getWorld().getBlockEntity(pos);
                        if (be instanceof FilterPipeBlockEntity filterPipe) {
                            // Security check: player must be within range
                            if (player.squaredDistanceTo(pos.getX() + 0.5,
                                    pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0) {
                                filterPipe.toggleMode();
                                GlassPipeTransportMod.LOGGER.debug(
                                        "Filter mode toggled at {} by {}",
                                        pos, player.getName().getString());
                            }
                        }
                    });
                }
        );

        GlassPipeTransportMod.LOGGER.debug("Registered networking");
    }

    /**
     * Packet sent from client to server to toggle whitelist/blacklist mode.
     */
    public record ToggleFilterModePayload(BlockPos pos) implements CustomPayload {

        public static final CustomPayload.Id<ToggleFilterModePayload> ID =
                new CustomPayload.Id<>(GlassPipeTransportMod.id("toggle_filter_mode"));

        public static final PacketCodec<PacketByteBuf, ToggleFilterModePayload> CODEC =
                PacketCodec.of(
                        (value, buf) -> buf.writeBlockPos(value.pos),
                        buf -> new ToggleFilterModePayload(buf.readBlockPos())
                );

        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
}
