package net.geforcemods.securitycraft.blockentities;

import java.util.ArrayList;

import net.geforcemods.securitycraft.ClientHandler;
import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.api.LinkableBlockEntity;
import net.geforcemods.securitycraft.api.LinkedAction;
import net.geforcemods.securitycraft.api.Option;
import net.geforcemods.securitycraft.api.Option.BooleanOption;
import net.geforcemods.securitycraft.api.Option.DisabledOption;
import net.geforcemods.securitycraft.api.Owner;
import net.geforcemods.securitycraft.blocks.DisguisableBlock;
import net.geforcemods.securitycraft.blocks.LaserBlock;
import net.geforcemods.securitycraft.items.ModuleItem;
import net.geforcemods.securitycraft.misc.ModuleType;
import net.geforcemods.securitycraft.models.DisguisableDynamicBakedModel;
import net.geforcemods.securitycraft.network.client.RefreshDisguisableModel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.network.PacketDistributor;

public class LaserBlockBlockEntity extends LinkableBlockEntity {
	private DisabledOption disabled = new DisabledOption(false) {
		@Override
		public void toggle() {
			setValue(!get());

			toggleLaser(this);
		}
	};

	public LaserBlockBlockEntity(BlockPos pos, BlockState state) {
		super(SCContent.LASER_BLOCK_BLOCK_ENTITY.get(), pos, state);
	}

	private void toggleLaser(BooleanOption option) {
		if (option.get())
			((LaserBlock) getBlockState().getBlock()).setLaser(level, worldPosition);
		else
			LaserBlock.destroyAdjacentLasers(level, worldPosition);
	}

	@Override
	protected void onLinkedBlockAction(LinkedAction action, Object[] parameters, ArrayList<LinkableBlockEntity> excludedBEs) {
		if (action == LinkedAction.OPTION_CHANGED) {
			Option<?> option = (Option<?>) parameters[0];

			disabled.copy(option);
			toggleLaser((BooleanOption) option);
		}
		else if (action == LinkedAction.MODULE_INSERTED) {
			ItemStack module = (ItemStack) parameters[0];
			boolean toggled = (boolean) parameters[2];

			insertModule(module, toggled);

			if (((ModuleItem) module.getItem()).getModuleType() == ModuleType.DISGUISE)
				onInsertDisguiseModule(module, toggled);
		}
		else if (action == LinkedAction.MODULE_REMOVED) {
			ModuleType module = (ModuleType) parameters[1];
			ItemStack moduleStack = getModule(module);
			boolean toggled = (boolean) parameters[2];

			removeModule(module, toggled);

			if (module == ModuleType.DISGUISE)
				onRemoveDisguiseModule(moduleStack, toggled);
		}
		else if (action == LinkedAction.OWNER_CHANGED) {
			Owner owner = (Owner) parameters[0];

			setOwner(owner.getUUID(), owner.getName());
		}

		excludedBEs.add(this);
		createLinkedBlockAction(action, parameters, excludedBEs);
	}

	@Override
	public void onModuleInserted(ItemStack stack, ModuleType module, boolean toggled) {
		super.onModuleInserted(stack, module, toggled);

		if (module == ModuleType.DISGUISE)
			onInsertDisguiseModule(stack, toggled);
	}

	@Override
	public void onModuleRemoved(ItemStack stack, ModuleType module, boolean toggled) {
		super.onModuleRemoved(stack, module, toggled);

		if (module == ModuleType.DISGUISE)
			onRemoveDisguiseModule(stack, toggled);
	}

	private void onInsertDisguiseModule(ItemStack stack, boolean toggled) {
		BlockState state = getBlockState();

		if (!level.isClientSide) {
			SecurityCraft.channel.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(worldPosition)), new RefreshDisguisableModel(worldPosition, true, stack, toggled));

			if (state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED)) {
				level.scheduleTick(worldPosition, Fluids.WATER, Fluids.WATER.getTickDelay(level));
				level.updateNeighborsAt(worldPosition, state.getBlock());
			}
		}
		else {
			ClientHandler.putDisguisedBeRenderer(this, stack);

			if (state.getLightEmission(level, worldPosition) > 0)
				level.getChunkSource().getLightEngine().checkBlock(worldPosition);
		}
	}

	private void onRemoveDisguiseModule(ItemStack stack, boolean toggled) {
		if (!level.isClientSide) {
			BlockState state = getBlockState();

			SecurityCraft.channel.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(worldPosition)), new RefreshDisguisableModel(worldPosition, false, stack, toggled));

			if (state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED)) {
				level.scheduleTick(worldPosition, Fluids.WATER, Fluids.WATER.getTickDelay(level));
				level.updateNeighborsAt(worldPosition, state.getBlock());
			}
		}
		else {
			ClientHandler.DISGUISED_BLOCK_RENDER_DELEGATE.removeDelegateOf(this);
			DisguisableBlock.getDisguisedBlockStateFromStack(stack).ifPresent(disguisedState -> {
				if (disguisedState.getLightEmission(level, worldPosition) > 0)
					level.getChunkSource().getLightEngine().checkBlock(worldPosition);
			});
		}
	}

	@Override
	public void handleUpdateTag(CompoundTag tag) {
		super.handleUpdateTag(tag);

		if (level != null && level.isClientSide) {
			ItemStack stack = getModule(ModuleType.DISGUISE);

			if (!stack.isEmpty())
				ClientHandler.putDisguisedBeRenderer(this, stack);
			else
				ClientHandler.DISGUISED_BLOCK_RENDER_DELEGATE.removeDelegateOf(this);
		}
	}

	@Override
	public void readOptions(CompoundTag tag) {
		if (tag.contains("enabled"))
			tag.putBoolean("disabled", !tag.getBoolean("enabled")); //legacy support

		for (Option<?> option : customOptions()) {
			option.readFromNBT(tag);
		}
	}

	@Override
	public void setRemoved() {
		super.setRemoved();

		if (level.isClientSide)
			ClientHandler.DISGUISED_BLOCK_RENDER_DELEGATE.removeDelegateOf(this);
	}

	@Override
	public ModuleType[] acceptedModules() {
		return new ModuleType[] {
				ModuleType.HARMING, ModuleType.ALLOWLIST, ModuleType.DISGUISE
		};
	}

	@Override
	public Option<?>[] customOptions() {
		return new Option[] {
				disabled
		};
	}

	@Override
	public ModelData getModelData() {
		BlockState disguisedState = DisguisableBlock.getDisguisedStateOrDefault(getBlockState(), level, worldPosition);

		return ModelData.builder().with(DisguisableDynamicBakedModel.DISGUISED_STATE, disguisedState).build();
	}

	public boolean isEnabled() {
		return !disabled.get();
	}
}
