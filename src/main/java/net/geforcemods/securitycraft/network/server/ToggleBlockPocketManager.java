package net.geforcemods.securitycraft.network.server;

import java.util.function.Supplier;

import net.geforcemods.securitycraft.blockentities.BlockPocketManagerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

public class ToggleBlockPocketManager {
	private BlockPos pos;
	private int size;
	private boolean enabling;

	public ToggleBlockPocketManager() {}

	public ToggleBlockPocketManager(BlockPocketManagerBlockEntity te, boolean enabling, int size) {
		pos = te.getBlockPos();
		this.enabling = enabling;
		this.size = size;
	}

	public static void encode(ToggleBlockPocketManager message, FriendlyByteBuf buf) {
		buf.writeLong(message.pos.asLong());
		buf.writeBoolean(message.enabling);
		buf.writeInt(message.size);
	}

	public static ToggleBlockPocketManager decode(FriendlyByteBuf buf) {
		ToggleBlockPocketManager message = new ToggleBlockPocketManager();

		message.pos = BlockPos.of(buf.readLong());
		message.enabling = buf.readBoolean();
		message.size = buf.readInt();
		return message;
	}

	public static void onMessage(ToggleBlockPocketManager message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			Player player = ctx.get().getSender();

			if (player.level.getBlockEntity(message.pos) instanceof BlockPocketManagerBlockEntity be && be.getOwner().isOwner(player)) {
				be.size = message.size;

				if (message.enabling)
					be.enableMultiblock();
				else
					be.disableMultiblock();

				be.setChanged();
			}
		});

		ctx.get().setPacketHandled(true);
	}
}
