package eu.asangarin.meaddon.block;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.block.BlockProperties;
import com.supermartijn642.movingelevators.blocks.CamoBlock;
import com.supermartijn642.movingelevators.blocks.CamoBlockEntity;
import com.supermartijn642.movingelevators.blocks.ControllerBlockEntity;
import com.supermartijn642.movingelevators.blocks.ElevatorInputBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class CustomRemoteControllerBlock extends CamoBlock {
	private final boolean flag;

	public CustomRemoteControllerBlock(BlockProperties properties, boolean flag) {
		super(properties, (pos, state) -> new CustomRemoteControllerBlockEntity(pos, state, flag));
		this.flag = flag;
	}

	protected boolean onRightClick(BlockState state, Level level, CamoBlockEntity blockEntity, BlockPos pos, Player player, InteractionHand hand, Direction hitSide, Vec3 hitLocation) {
		if (onInputClick(state, level, blockEntity, pos, player, hand, hitSide, hitLocation)) {
			return true;
		} else if (blockEntity instanceof CustomRemoteControllerBlockEntity) {
			if (level.isClientSide) {
				BlockPos controllerPos = ((CustomRemoteControllerBlockEntity) blockEntity).getControllerPos();
				Component x = TextComponents.number(controllerPos.getX()).color(ChatFormatting.GOLD).get();
				Component y = TextComponents.number(controllerPos.getY()).color(ChatFormatting.GOLD).get();
				Component z = TextComponents.number(controllerPos.getZ()).color(ChatFormatting.GOLD).get();
				player.displayClientMessage(TextComponents.translation("movingelevators.remote_controller.controller_location", new Object[]{x, y, z}).get(),
						true);
			}

			return true;
		} else {
			return false;
		}
	}

	protected boolean onInputClick(BlockState state, Level level, CamoBlockEntity blockEntity, BlockPos pos, Player player, InteractionHand hand, Direction hitSide, Vec3 hitLocation) {
		if (blockEntity instanceof ElevatorInputBlockEntity inputEntity) {
			if (inputEntity.getFacing() == hitSide && inputEntity.hasGroup()) {
				if (!level.isClientSide) {
					double y = hitLocation.y - pos.getY();

					boolean flag = false;
					boolean up = y > 2 / 3D;
					boolean down = y < 1 / 3D;

					if (up || down) {
						if (provideUpAndDown()) flag = true;
					} else if (provideMiddle()) flag = true;

					if (flag) inputEntity.getGroup().onButtonPress(up, down, inputEntity.getFloorLevel());
				}
				return true;
			}
		}
		return super.onRightClick(state, level, blockEntity, pos, player, hand, hitSide, hitLocation);
	}

	@Override
	@SuppressWarnings("deprecation")
	public void neighborChanged(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Block block, @NotNull BlockPos fromPos, boolean isMoving) {
		BlockEntity entity = level.getBlockEntity(pos);
		if (entity instanceof ElevatorInputBlockEntity)
			((ElevatorInputBlockEntity) entity).redstone = level.hasNeighborSignal(pos) || level.hasNeighborSignal(pos.above());
	}

	@Override
	public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction side) {
		return true;
	}

	public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
		BlockEntity entity = level.getBlockEntity(pos);
		if (entity instanceof CustomRemoteControllerBlockEntity) {
			CompoundTag compound = stack.getTag();
			if (compound == null || !compound.contains("controllerDim")) {
				return;
			}

			Direction dir;
			if (placer != null) dir = placer.getDirection();
			else dir = Direction.NORTH;
			((CustomRemoteControllerBlockEntity) entity).setValues(dir.getOpposite(),
					new BlockPos(compound.getInt("controllerX"), compound.getInt("controllerY"), compound.getInt("controllerZ")),
					compound.contains("controllerFacing", 3) ? Direction.from2DDataValue(compound.getInt("controllerFacing")) : null);
		}

	}

	protected void appendItemInformation(ItemStack stack, @Nullable BlockGetter level, Consumer<Component> info, boolean advanced) {
		CompoundTag tag = stack.getTag();
		if (tag != null && tag.contains("controllerDim")) {
			Component x = TextComponents.number(tag.getInt("controllerX")).color(ChatFormatting.GOLD).get();
			Component y = TextComponents.number(tag.getInt("controllerY")).color(ChatFormatting.GOLD).get();
			Component z = TextComponents.number(tag.getInt("controllerZ")).color(ChatFormatting.GOLD).get();
			Component dimension = TextComponents.dimension(
					ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(tag.getString("controllerDim")))).color(ChatFormatting.GOLD).get();
			info.accept(TextComponents.translation("movingelevators.remote_controller.tooltip.bound", new Object[]{x, y, z, dimension}).get());
		} else {
			info.accept(TextComponents.translation("movingelevators.remote_controller.tooltip").color(ChatFormatting.AQUA).get());
		}

	}

	@SuppressWarnings("deprecation")
	public boolean hasAnalogOutputSignal(@NotNull BlockState state) {
		return true;
	}

	@SuppressWarnings("deprecation")
	public int getAnalogOutputSignal(@NotNull BlockState state, Level level, @NotNull BlockPos pos) {
		BlockEntity entity = level.getBlockEntity(pos);
		if (entity instanceof CustomRemoteControllerBlockEntity) {
			ControllerBlockEntity be = ((CustomRemoteControllerBlockEntity) entity).getController();
			if (be != null && be.hasGroup() && (be).getGroup().isCageAvailableAt(be)) {
				return 15;
			}
		}

		return 0;
	}

	public boolean provideUpAndDown() {
		return this.flag;
	}

	public boolean provideMiddle() {
		return !this.flag;
	}
}
