package net.geforcemods.securitycraft.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.inventory.BlockReinforcerMenu;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class BlockReinforcerScreen extends AbstractContainerScreen<BlockReinforcerMenu> {
	private static final ResourceLocation TEXTURE = new ResourceLocation(SecurityCraft.MODID + ":textures/gui/container/universal_block_reinforcer.png");
	private static final ResourceLocation TEXTURE_LVL1 = new ResourceLocation(SecurityCraft.MODID + ":textures/gui/container/universal_block_reinforcer_lvl1.png");
	private final Component ubr = Utils.localize("gui.securitycraft:blockReinforcer.title");
	private final Component output = Utils.localize("gui.securitycraft:blockReinforcer.output");
	private final boolean isLvl1;

	public BlockReinforcerScreen(BlockReinforcerMenu menu, Inventory inv, Component title) {
		super(menu, inv, title);

		this.isLvl1 = menu.isLvl1;
	}

	@Override
	public void render(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		super.render(pose, mouseX, mouseY, partialTicks);

		if (getSlotUnderMouse() != null && !getSlotUnderMouse().getItem().isEmpty())
			renderTooltip(pose, getSlotUnderMouse().getItem(), mouseX, mouseY);
	}

	@Override
	protected void renderLabels(PoseStack pose, int mouseX, int mouseY) {
		NonNullList<ItemStack> inv = menu.getItems();

		font.draw(pose, ubr, (imageWidth - font.width(ubr)) / 2, 5, 4210752);
		font.draw(pose, Utils.INVENTORY_TEXT, 8, imageHeight - 96 + 2, 4210752);

		if (!inv.get(36).isEmpty()) {
			font.draw(pose, output, 50, 25, 4210752);
			minecraft.getItemRenderer().renderAndDecorateItem(menu.reinforcingSlot.getOutput(), 116, 20);
			minecraft.getItemRenderer().renderGuiItemDecorations(minecraft.font, menu.reinforcingSlot.getOutput(), 116, 20, null);

			if (mouseX >= leftPos + 114 && mouseX < leftPos + 134 && mouseY >= topPos + 17 && mouseY < topPos + 39)
				renderTooltip(pose, menu.reinforcingSlot.getOutput(), mouseX - leftPos, mouseY - topPos);
		}

		if (!isLvl1 && !inv.get(37).isEmpty()) {
			font.draw(pose, output, 50, 50, 4210752);
			minecraft.getItemRenderer().renderAndDecorateItem(menu.unreinforcingSlot.getOutput(), 116, 46);
			minecraft.getItemRenderer().renderGuiItemDecorations(minecraft.font, menu.unreinforcingSlot.getOutput(), 116, 46, null);

			if (mouseX >= leftPos + 114 && mouseX < leftPos + 134 && mouseY >= topPos + 43 && mouseY < topPos + 64)
				renderTooltip(pose, menu.unreinforcingSlot.getOutput(), mouseX - leftPos, mouseY - topPos);
		}
	}

	@Override
	protected void renderBg(PoseStack pose, float partialTicks, int mouseX, int mouseY) {
		renderBackground(pose);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem._setShaderTexture(0, isLvl1 ? TEXTURE_LVL1 : TEXTURE);
		blit(pose, leftPos, topPos, 0, 0, imageWidth, imageHeight);
	}
}
