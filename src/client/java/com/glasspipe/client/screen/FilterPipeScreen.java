package com.glasspipe.client.screen;

import com.glasspipe.GlassPipeTransportMod;
import com.glasspipe.network.ModNetworking;
import com.glasspipe.screen.FilterPipeScreenHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Client-side screen for the filter pipe GUI.
 * Shows a 3x3 grid of filter slots and a whitelist/blacklist toggle button.
 */
@Environment(EnvType.CLIENT)
public class FilterPipeScreen extends HandledScreen<FilterPipeScreenHandler> {

    private static final Identifier TEXTURE =
            Identifier.of(GlassPipeTransportMod.MOD_ID, "textures/gui/filter_pipe.png");

    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 166;

    private ButtonWidget modeButton;
    private boolean whitelistMode = true;

    public FilterPipeScreen(FilterPipeScreenHandler handler, PlayerInventory inventory,
                             Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = GUI_WIDTH;
        this.backgroundHeight = GUI_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();

        // Mode toggle button (whitelist / blacklist)
        modeButton = ButtonWidget.builder(getModeText(), button -> {
            // Send toggle packet to server
            if (handler.getBlockEntity() != null) {
                ClientPlayNetworking.send(
                        new ModNetworking.ToggleFilterModePayload(
                                handler.getBlockEntity().getPos()
                        )
                );
                whitelistMode = !whitelistMode;
                button.setMessage(getModeText());
            }
        })
        .dimensions(x + 100, y + 17, 68, 20)
        .build();

        addDrawableChild(modeButton);

        // Initialize mode from handler
        whitelistMode = handler.isWhitelistMode();
        modeButton.setMessage(getModeText());
    }

    private Text getModeText() {
        return whitelistMode
                ? Text.translatable("gui.glass_pipe_transport.filter_pipe.whitelist")
                : Text.translatable("gui.glass_pipe_transport.filter_pipe.blacklist");
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        // Draw the GUI background
        context.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight);

        // Draw mode indicator
        int modeColor = whitelistMode ? 0xFF00AA00 : 0xFFAA0000;
        context.fill(x + 100, y + 40, x + 168, y + 42, modeColor);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        // Title
        context.drawText(textRenderer,
                Text.translatable("block.glass_pipe_transport.filter_pipe"),
                8, 6, 0x404040, false);

        // "Filter:" label
        context.drawText(textRenderer,
                Text.translatable("gui.glass_pipe_transport.filter_pipe.filter_label"),
                8, 17, 0x404040, false);

        // Player inventory label
        context.drawText(textRenderer, playerInventoryTitle, playerInventoryTitleX,
                playerInventoryTitleY, 0x404040, false);

        // Mode description
        String modeDesc = whitelistMode
                ? "Only listed items pass"
                : "Listed items blocked";
        context.drawText(textRenderer, Text.literal(modeDesc), 100, 44, 0x606060, false);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }
}
