package net.geforcemods.securitycraft.network.server;

import java.util.function.Supplier;

import net.geforcemods.securitycraft.blockentities.IMSBlockEntity;
import net.geforcemods.securitycraft.blockentities.IMSBlockEntity.IMSTargetingMode;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

public class SyncIMSTargetingOption {
	private BlockPos pos;
	private IMSTargetingMode targetingMode;

	public SyncIMSTargetingOption() {}

	public SyncIMSTargetingOption(BlockPos pos, IMSTargetingMode targetingMode) {
		this.pos = pos;
		this.targetingMode = targetingMode;
	}

	public static void encode(SyncIMSTargetingOption message, FriendlyByteBuf buf) {
		buf.writeBlockPos(message.pos);
		buf.writeEnum(message.targetingMode);
	}

	public static SyncIMSTargetingOption decode(FriendlyByteBuf buf) {
		SyncIMSTargetingOption message = new SyncIMSTargetingOption();

		message.pos = buf.readBlockPos();
		message.targetingMode = buf.readEnum(IMSTargetingMode.class);
		return message;
	}

	public static void onMessage(SyncIMSTargetingOption message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			BlockPos pos = message.pos;
			Player player = ctx.get().getSender();

			if (player.level.getBlockEntity(pos) instanceof IMSBlockEntity be && be.getOwner().isOwner(player))
				be.setTargetingMode(message.targetingMode);
		});

		ctx.get().setPacketHandled(true);
	}
}
