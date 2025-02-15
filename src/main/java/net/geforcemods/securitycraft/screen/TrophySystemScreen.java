package net.geforcemods.securitycraft.screen;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.blockentities.TrophySystemBlockEntity;
import net.geforcemods.securitycraft.misc.ModuleType;
import net.geforcemods.securitycraft.util.ClientUtils;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.client.gui.widget.ScrollPanel;

public class TrophySystemScreen extends Screen {
	private static final ResourceLocation BEACON_GUI = new ResourceLocation("textures/gui/container/beacon.png");
	private static final ResourceLocation GUI_TEXTURE = new ResourceLocation(SecurityCraft.MODID, "textures/gui/container/blank.png");
	private final Component projectiles = Utils.localize("gui.securitycraft:trophy_system.targetableProjectiles");
	private final Component moduleRequired = Utils.localize("gui.securitycraft:trophy_system.moduleRequired");
	private final Component toggle = Utils.localize("gui.securitycraft:trophy_system.toggle");
	private final Component moddedProjectiles = Utils.localize("gui.securitycraft:trophy_system.moddedProjectiles");
	private int imageWidth = 176;
	private int imageHeight = 166;
	private int leftPos;
	private int topPos;
	private final boolean isSmart;
	private final List<EntityType<?>> orderedFilterList;
	private TrophySystemBlockEntity be;
	private ProjectileScrollList projectileList;

	public TrophySystemScreen(TrophySystemBlockEntity be) {
		super(Component.translatable(SCContent.TROPHY_SYSTEM.get().getDescriptionId()));

		this.be = be;
		isSmart = be.isModuleEnabled(ModuleType.SMART);
		orderedFilterList = new ArrayList<>(be.getFilters().keySet());
		orderedFilterList.sort((e1, e2) -> {
			//the entry for modded projectiles always shows at the bottom of the list
			if (e1 == EntityType.PIG)
				return 1;
			else if (e2 == EntityType.PIG)
				return -1;
			else
				return e1.getDescription().getString().compareTo(e2.getDescription().getString());
		});
	}

	@Override
	protected void init() {
		super.init();

		leftPos = (width - imageWidth) / 2;
		topPos = (height - imageHeight) / 2;
		addRenderableWidget(projectileList = new ProjectileScrollList(minecraft, imageWidth - 24, imageHeight - 60, topPos + 40, leftPos + 12));
	}


	@Override
	public void render(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		renderBackground(pose);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem._setShaderTexture(0, GUI_TEXTURE);
		blit(pose, leftPos, topPos, 0, 0, imageWidth, imageHeight);
		super.render(pose, mouseX, mouseY, partialTicks);
		font.draw(pose, title, width / 2 - font.width(title) / 2, topPos + 6, 4210752);
		font.draw(pose, projectiles, width / 2 - font.width(projectiles) / 2, topPos + 31, 4210752);
		ClientUtils.renderModuleInfo(pose, ModuleType.SMART, toggle, moduleRequired, isSmart, leftPos + 5, topPos + 5, width, height, mouseX, mouseY);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (projectileList != null)
			projectileList.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);

		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}

	class ProjectileScrollList extends ScrollPanel {
		private final int slotHeight = 12, listLength = orderedFilterList.size();

		public ProjectileScrollList(Minecraft client, int width, int height, int top, int left) {
			super(client, width, height, top, left);
		}

		@Override
		protected int getContentHeight() {
			int height = 50 + (listLength * font.lineHeight);

			if (height < bottom - top - 8)
				height = bottom - top - 8;

			return height;
		}

		@Override
		protected boolean clickPanel(double mouseX, double mouseY, int button) {
			int slotIndex = (int) (mouseY + (border / 2)) / slotHeight;

			if (isSmart && slotIndex >= 0 && mouseY >= 0 && slotIndex < listLength) {
				be.toggleFilter(orderedFilterList.get(slotIndex));
				Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
				return true;
			}

			return false;
		}

		@Override
		protected void drawPanel(PoseStack pose, int entryRight, int relativeY, Tesselator tess, int mouseX, int mouseY) {
			int baseY = top + border - (int) scrollDistance;
			int slotBuffer = slotHeight - 4;
			int mouseListY = (int) (mouseY - top + scrollDistance - (border / 2));
			int slotIndex = mouseListY / slotHeight;

			//highlight hovered slot
			if (isSmart && mouseX >= left && mouseX <= right - 7 && slotIndex >= 0 && mouseListY >= 0 && slotIndex < listLength && mouseY >= top && mouseY <= bottom) {
				int min = left;
				int max = entryRight - 6; //6 is the width of the scrollbar
				int slotTop = baseY + slotIndex * slotHeight;
				BufferBuilder bufferBuilder = tess.getBuilder();

				RenderSystem.enableBlend();
				RenderSystem.disableTexture();
				RenderSystem.defaultBlendFunc();
				bufferBuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
				bufferBuilder.vertex(min, slotTop + slotBuffer + 2, 0).uv(0, 1).color(0x80, 0x80, 0x80, 0xFF).endVertex();
				bufferBuilder.vertex(max, slotTop + slotBuffer + 2, 0).uv(1, 1).color(0x80, 0x80, 0x80, 0xFF).endVertex();
				bufferBuilder.vertex(max, slotTop - 2, 0).uv(1, 0).color(0x80, 0x80, 0x80, 0xFF).endVertex();
				bufferBuilder.vertex(min, slotTop - 2, 0).uv(0, 0).color(0x80, 0x80, 0x80, 0xFF).endVertex();
				bufferBuilder.vertex(min + 1, slotTop + slotBuffer + 1, 0).uv(0, 1).color(0x00, 0x00, 0x00, 0xFF).endVertex();
				bufferBuilder.vertex(max - 1, slotTop + slotBuffer + 1, 0).uv(1, 1).color(0x00, 0x00, 0x00, 0xFF).endVertex();
				bufferBuilder.vertex(max - 1, slotTop - 1, 0).uv(1, 0).color(0x00, 0x00, 0x00, 0xFF).endVertex();
				bufferBuilder.vertex(min + 1, slotTop - 1, 0).uv(0, 0).color(0x00, 0x00, 0x00, 0xFF).endVertex();
				BufferUploader.drawWithShader(bufferBuilder.end());
				RenderSystem.enableTexture();
				RenderSystem.disableBlend();
			}

			int i = 0;

			//draw entry strings and indicators whether the filter is enabled
			for (EntityType<?> projectileType : orderedFilterList) {
				Component projectileName = projectileType == EntityType.PIG ? moddedProjectiles : projectileType.getDescription();
				int yStart = relativeY + (slotHeight * i);

				font.draw(pose, projectileName, left + width / 2 - font.width(projectileName) / 2, yStart, 0xC6C6C6);
				RenderSystem._setShaderTexture(0, BEACON_GUI);
				blit(pose, left, yStart - 3, 14, 14, be.getFilter(projectileType) ? 88 : 110, 219, 21, 22, 256, 256);
				i++;
			}
		}

		@Override
		public NarrationPriority narrationPriority() {
			return NarrationPriority.NONE;
		}

		@Override
		public void updateNarration(NarrationElementOutput narrationElementOutput) {}
	}
}
