package com.glasspipe.client.render;

import com.glasspipe.block.entity.GlassPipeBlockEntity;
import com.glasspipe.transport.TransitItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

import java.util.List;

/**
 * Renders items floating and moving inside glass pipes.
 * Items are rendered as small 3D models at their current progress position
 * along the pipe segment, with a gentle spinning animation.
 */
@Environment(EnvType.CLIENT)
public class GlassPipeBlockEntityRenderer implements BlockEntityRenderer<GlassPipeBlockEntity> {

    private final ItemRenderer itemRenderer;

    /** Scale of the rendered item (0.3 = 30% of normal size) */
    private static final float ITEM_SCALE = 0.3f;

    /** Center of the pipe in block-local coordinates */
    private static final float CENTER = 0.5f;

    public GlassPipeBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        this.itemRenderer = MinecraftClient.getInstance().getItemRenderer();
    }

    @Override
    public void render(GlassPipeBlockEntity entity, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light, int overlay) {

        List<TransitItem> items = entity.clientTransitItems;
        if (items.isEmpty()) return;

        long gameTime = entity.getWorld() != null ? entity.getWorld().getTime() : 0;

        for (TransitItem transitItem : items) {
            ItemStack stack = transitItem.getStack();
            if (stack.isEmpty()) continue;

            // Interpolate position based on progress + tick delta for smooth motion
            float progress = Math.min(transitItem.getProgress() + tickDelta * 0.05f, 1.0f);
            Vec3d renderPos = getItemRenderPosition(transitItem.getTravelDirection(), progress);

            // Gentle bobbing animation
            float bobOffset = (float) Math.sin((gameTime + tickDelta) * 0.15f) * 0.02f;

            matrices.push();

            // Translate to item position within the block
            matrices.translate(renderPos.x, renderPos.y + bobOffset, renderPos.z);

            // Scale down the item
            matrices.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);

            // Rotate item to spin as it travels
            applyDirectionRotation(matrices, transitItem.getTravelDirection(), gameTime, tickDelta);

            // Render the item model
            itemRenderer.renderItem(
                    stack,
                    ModelTransformationMode.GROUND,
                    light,
                    overlay,
                    matrices,
                    vertexConsumers,
                    entity.getWorld(),
                    (int) entity.getPos().asLong()
            );

            matrices.pop();
        }
    }

    /**
     * Calculates the 3D position of the item within the block based on
     * travel direction and progress (0.0 = entry face, 1.0 = exit face).
     */
    private Vec3d getItemRenderPosition(Direction dir, float progress) {
        // Map progress 0→1 to position from entry center to exit center
        // Entry is at 0.1 from the entry face, exit is at 0.1 from the exit face
        float t = 0.1f + progress * 0.8f; // 0.1 to 0.9

        return switch (dir) {
            case NORTH -> new Vec3d(CENTER, CENTER, 1.0f - t);  // Z decreases going north
            case SOUTH -> new Vec3d(CENTER, CENTER, t);          // Z increases going south
            case EAST  -> new Vec3d(t,      CENTER, CENTER);     // X increases going east
            case WEST  -> new Vec3d(1.0f - t, CENTER, CENTER);  // X decreases going west
            case UP    -> new Vec3d(CENTER, t,      CENTER);     // Y increases going up
            case DOWN  -> new Vec3d(CENTER, 1.0f - t, CENTER);  // Y decreases going down
        };
    }

    /**
     * Applies a spin rotation to the item based on travel direction and time.
     */
    private void applyDirectionRotation(MatrixStack matrices, Direction dir,
                                         long gameTime, float tickDelta) {
        float spinAngle = ((gameTime + tickDelta) * 4.0f) % 360.0f;

        switch (dir) {
            case NORTH, SOUTH -> matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(spinAngle));
            case EAST, WEST   -> matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(spinAngle));
            case UP, DOWN     -> matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(spinAngle));
        }
    }

    @Override
    public boolean rendersOutsideBoundingBox(GlassPipeBlockEntity blockEntity) {
        return false;
    }

    @Override
    public int getRenderDistance() {
        return 64;
    }
}
