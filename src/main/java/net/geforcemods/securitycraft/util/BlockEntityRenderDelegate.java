package net.geforcemods.securitycraft.util;

import java.util.HashMap;
import java.util.Map;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEntityRenderDelegate {
	private final Map<BlockEntity, DelegateRendererInfo> renderDelegates = new HashMap<>();

	public void putDelegateFor(BlockEntity originalBlockEntity, BlockState delegateState) {
		if (renderDelegates.containsKey(originalBlockEntity)) {
			DelegateRendererInfo delegateInfo = renderDelegates.get(originalBlockEntity);

			//the original be already has a delegate block entity of the same type, just update the state instead of creating a whole new be and renderer
			if (delegateInfo.delegateBlockEntity.getBlockState().getBlock() == delegateState.getBlock()) {
				delegateInfo.delegateBlockEntity.setBlockState(delegateState);
				return;
			}
		}

		if (delegateState != null && delegateState.hasBlockEntity()) {
			Minecraft mc = Minecraft.getInstance();
			BlockEntity delegateBe = ((EntityBlock) delegateState.getBlock()).newBlockEntity(BlockPos.ZERO, delegateState);

			if (delegateBe != null) {
				BlockEntityRenderer<?> delegateBeRenderer;

				delegateBe.setLevel(mc.level);
				delegateBeRenderer = mc.getBlockEntityRenderDispatcher().getRenderer(delegateBe);

				if (delegateBeRenderer != null)
					renderDelegates.put(originalBlockEntity, new DelegateRendererInfo(delegateBe, delegateBeRenderer));
			}
		}
	}

	public void removeDelegateOf(BlockEntity originalBlockEntity) {
		renderDelegates.remove(originalBlockEntity);
	}

	public boolean tryRenderDelegate(BlockEntity originalBlockEntity, float partialTicks, PoseStack pose, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
		DelegateRendererInfo delegateRendererInfo = renderDelegates.get(originalBlockEntity);

		if (delegateRendererInfo != null) {
			delegateRendererInfo.delegateRenderer().render(delegateRendererInfo.delegateBlockEntity(), partialTicks, pose, buffer, combinedLight, combinedOverlay);
			return true;
		}

		return false;
	}

	@SuppressWarnings("rawtypes")
	private static record DelegateRendererInfo(BlockEntity delegateBlockEntity, BlockEntityRenderer delegateRenderer) {}
}
