package eu.asangarin.meaddon.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.supermartijn642.movingelevators.MovingElevatorsClient;
import com.supermartijn642.movingelevators.blocks.ElevatorInputBlockEntityRenderer;
import eu.asangarin.meaddon.block.CustomRemoteControllerBlockEntity;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;

public class CustomRemoteControllerBlockEntityRenderer extends ElevatorInputBlockEntityRenderer<CustomRemoteControllerBlockEntity> {
	@SuppressWarnings("ConstantConditions")
	@Override
	public void render(CustomRemoteControllerBlockEntity entity, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
		if (entity.getFacing() != null) {
			VertexConsumer buffer = bufferSource.getBuffer(RenderType.cutout());
			Direction facing = entity.getFacing();
			combinedLight = LevelRenderer.getLightColor(entity.getLevel(), entity.getBlockPos().relative(facing));
			poseStack.pushPose();
			poseStack.translate(0.5, 0.5, 0.5);
			poseStack.mulPose(new Quaternion(0.0F, 180.0F - facing.toYRot(), 0.0F, true));
			poseStack.translate(-0.5, -0.5, -0.51);
			boolean canReceiveInput = entity.canReceiveInput();
			boolean renderUp = canReceiveInput && entity.canMoveUp();
			boolean renderDown = canReceiveInput && entity.canMoveDown();
			if (entity.provideMiddle()) this.drawOverlayPart(poseStack, buffer, combinedLight, combinedOverlay, facing, 23, canReceiveInput ? 64 : 87);
			if (entity.provideUpAndDown()) {
				this.drawOverlayPart(poseStack, buffer, combinedLight, combinedOverlay, facing, 0, renderUp ? 64 : 87);
				this.drawOverlayPart(poseStack, buffer, combinedLight, combinedOverlay, facing, 46, renderDown ? 64 : 87);
			}
			poseStack.popPose();
		}
	}

	private void drawOverlayPart(PoseStack poseStack, VertexConsumer buffer, int combinedLight, int combinedOverlay, Direction facing, int tX, int tY) {
		Matrix4f matrix = poseStack.last().pose();
		Matrix3f normalMatrix = poseStack.last().normal();
		float minU = MovingElevatorsClient.OVERLAY_SPRITE.getU(((float) tX / 8.0F));
		float maxU = MovingElevatorsClient.OVERLAY_SPRITE.getU(((float) (tX + 23) / 8.0F));
		float minV = MovingElevatorsClient.OVERLAY_SPRITE.getV((float) tY / 8.0F);
		float maxV = MovingElevatorsClient.OVERLAY_SPRITE.getV(((float) (tY + 23) / 8.0F));
		buffer.vertex(matrix, (float) 0.0, (float) 0.0 + (float) 1.0, 0.0F).color(255, 255, 255, 255).uv(maxU, minV).uv2(combinedLight)
				.normal(normalMatrix, (float) facing.getStepX(), (float) facing.getStepY(), (float) facing.getStepZ()).overlayCoords(combinedOverlay)
				.endVertex();
		buffer.vertex(matrix, (float) 0.0 + (float) 1.0, (float) 0.0 + (float) 1.0, 0.0F).color(255, 255, 255, 255).uv(minU, minV).uv2(combinedLight)
				.normal(normalMatrix, (float) facing.getStepX(), (float) facing.getStepY(), (float) facing.getStepZ()).overlayCoords(combinedOverlay)
				.endVertex();
		buffer.vertex(matrix, (float) 0.0 + (float) 1.0, (float) 0.0, 0.0F).color(255, 255, 255, 255).uv(minU, maxV).uv2(combinedLight)
				.normal(normalMatrix, (float) facing.getStepX(), (float) facing.getStepY(), (float) facing.getStepZ()).overlayCoords(combinedOverlay)
				.endVertex();
		buffer.vertex(matrix, (float) 0.0, (float) 0.0, 0.0F).color(255, 255, 255, 255).uv(maxU, maxV).uv2(combinedLight)
				.normal(normalMatrix, (float) facing.getStepX(), (float) facing.getStepY(), (float) facing.getStepZ()).overlayCoords(combinedOverlay)
				.endVertex();
	}
}
