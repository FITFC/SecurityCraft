package net.geforcemods.securitycraft.entity;

import net.geforcemods.securitycraft.ConfigHandler;
import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.util.BlockUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;

public class BouncingBetty extends Entity {
	/** How long the fuse is */
	public int fuse;

	public BouncingBetty(EntityType<BouncingBetty> type, Level level) {
		super(SCContent.BOUNCING_BETTY_ENTITY.get(), level);
	}

	public BouncingBetty(Level level, double x, double y, double z) {
		this(SCContent.BOUNCING_BETTY_ENTITY.get(), level);
		setPos(x, y, z);
		float f = (float) (Math.random() * Math.PI * 2.0D);
		setDeltaMovement(-((float) Math.sin(f)) * 0.02F, 0.20000000298023224D, -((float) Math.cos(f)) * 0.02F);
		fuse = 80;
		xo = x;
		yo = y;
		zo = z;
	}

	@Override
	protected void defineSynchedData() {}

	@Override
	protected MovementEmission getMovementEmission() {
		return MovementEmission.NONE;
	}

	@Override
	public boolean isPickable() {
		return !isRemoved();
	}

	@Override
	public void tick() {
		xo = getX();
		yo = getY();
		zo = getZ();
		setDeltaMovement(getDeltaMovement().add(0, -0.03999999910593033D, 0));
		move(MoverType.SELF, getDeltaMovement());
		setDeltaMovement(getDeltaMovement().multiply(0.9800000190734863D, 0.9800000190734863D, 0.9800000190734863D));

		if (onGround)
			setDeltaMovement(getDeltaMovement().multiply(0.699999988079071D, 0.699999988079071D, -0.5D));

		if (!level.isClientSide && fuse-- <= 0) {
			discard();
			explode();
		}
		else if (level.isClientSide)
			level.addParticle(ParticleTypes.SMOKE, false, getX(), getY() + 0.5D, getZ(), 0.0D, 0.0D, 0.0D);
	}

	private void explode() {
		level.explode(this, getX(), getY(), getZ(), ConfigHandler.SERVER.smallerMineExplosion.get() ? 3.0F : 6.0F, ConfigHandler.SERVER.shouldSpawnFire.get(), BlockUtils.getExplosionMode());
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
		tag.putByte("Fuse", (byte) fuse);
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
		fuse = tag.getByte("Fuse");
	}

	@Override
	public Packet<?> getAddEntityPacket() {
		return new ClientboundAddEntityPacket(this);
	}
}
