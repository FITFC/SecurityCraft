package net.geforcemods.securitycraft.items;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.IModuleInventory;
import net.geforcemods.securitycraft.api.IOwnable;
import net.geforcemods.securitycraft.blocks.DisguisableBlock;
import net.geforcemods.securitycraft.inventory.CustomizeBlockMenu;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkHooks;

public class UniversalBlockModifierItem extends Item {
	public UniversalBlockModifierItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext ctx) {
		Level level = ctx.getLevel();
		BlockPos pos = ctx.getClickedPos();
		BlockEntity be = level.getBlockEntity(pos);
		Player player = ctx.getPlayer();

		if (be instanceof IModuleInventory) {
			if (be instanceof IOwnable ownable && !ownable.getOwner().isOwner(player)) {
				if (!(be.getBlockState().getBlock() instanceof DisguisableBlock db) || (((BlockItem) db.getDisguisedStack(level, pos).getItem()).getBlock() instanceof DisguisableBlock))
					PlayerUtils.sendMessageToPlayer(player, Utils.localize(SCContent.UNIVERSAL_BLOCK_MODIFIER.get().getDescriptionId()), Utils.localize("messages.securitycraft:notOwned", PlayerUtils.getOwnerComponent(ownable.getOwner().getName())), ChatFormatting.RED);
				return InteractionResult.FAIL;
			}
			else if (!ctx.getLevel().isClientSide) {
				NetworkHooks.openScreen((ServerPlayer) player, new MenuProvider() {
					@Override
					public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
						return new CustomizeBlockMenu(windowId, level, pos, inv);
					}

					@Override
					public Component getDisplayName() {
						return Component.translatable(be.getBlockState().getBlock().getDescriptionId());
					}
				}, pos);
			}

			return InteractionResult.SUCCESS;
		}

		return InteractionResult.PASS;
	}
}
