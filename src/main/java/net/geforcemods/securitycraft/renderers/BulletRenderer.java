package net.geforcemods.securitycraft.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import net.geforcemods.securitycraft.ClientHandler;
import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.entity.Bullet;
import net.geforcemods.securitycraft.models.BulletModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class BulletRenderer extends EntityRenderer<Bullet> {
	private static final ResourceLocation TEXTURE = new ResourceLocation(SecurityCraft.MODID + ":textures/entity/bullet.png");
	private final BulletModel model;

	public BulletRenderer(EntityRendererProvider.Context ctx) {
		super(ctx);

		model = new BulletModel(ctx.bakeLayer(ClientHandler.BULLET_LOCATION));
	}

	@Override
	public void render(Bullet entity, float entityYaw, float partialTicks, PoseStack pose, MultiBufferSource buffer, int packedLight) {
		pose.mulPose(new Quaternion(Vector3f.YP, entity.getYRot(), true));
		model.renderToBuffer(pose, buffer.getBuffer(RenderType.entitySolid(getTextureLocation(entity))), packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
	}

	@Override
	public ResourceLocation getTextureLocation(Bullet entity) {
		return TEXTURE;
	}
}
