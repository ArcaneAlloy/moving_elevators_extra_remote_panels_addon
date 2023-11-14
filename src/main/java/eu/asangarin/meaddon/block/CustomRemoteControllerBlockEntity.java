package eu.asangarin.meaddon.block;

import com.supermartijn642.movingelevators.blocks.ControllerBlockEntity;
import com.supermartijn642.movingelevators.blocks.ElevatorInputBlockEntity;
import com.supermartijn642.movingelevators.elevator.ElevatorCabinLevel;
import com.supermartijn642.movingelevators.elevator.ElevatorGroup;
import com.supermartijn642.movingelevators.elevator.ElevatorGroupCapability;
import eu.asangarin.meaddon.MovingElevatorsAddon;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class CustomRemoteControllerBlockEntity extends ElevatorInputBlockEntity {
	private Direction facing;
	private BlockPos controllerPos;
	private Direction controllerFacing;
	private boolean isInCabin;
	private int cabinFloorIndex;
	private int groupCheckCounter;
	private ElevatorGroup lastGroup;
	private final boolean flag;

	public CustomRemoteControllerBlockEntity(BlockPos pos, BlockState state) {
		this(pos, state, true);
	}

	public CustomRemoteControllerBlockEntity(BlockPos pos, BlockState state, boolean flag) {
		super(MovingElevatorsAddon.button_tile_custom, pos, state);
		this.facing = Direction.NORTH;
		this.controllerPos = BlockPos.ZERO;
		this.controllerFacing = null;
		this.isInCabin = false;
		this.cabinFloorIndex = -1;
		this.groupCheckCounter = 2;
		this.flag = flag;
	}

	@Override
	public void update() {
		super.update();
		this.groupCheckCounter--;
		if (this.groupCheckCounter <= 0) {
			// Update missing data when a remote elevator panel was placed in an older version
			if (this.controllerFacing == null && this.controllerPos != null) {
				ControllerBlockEntity controller = this.getController();
				if (controller != null) this.controllerFacing = controller.getFacing();
			}

			// Update comparator output if the remote group changed
			ElevatorGroup group = this.getGroup();
			if (group != this.lastGroup && this.level != null) {
				this.level.updateNeighbourForOutputSignal(this.worldPosition, this.getBlockState().getBlock());
				if (group != null) group.addComparatorListener(this.getFloorLevel(), this.worldPosition);
				this.lastGroup = group;
			}

			this.calculateInCabin();
			this.groupCheckCounter = 40;
		}
	}

	public void setValues(Direction facing, BlockPos controllerPos, Direction controllerFacing) {
		this.facing = facing;
		this.controllerPos = controllerPos;
		this.controllerFacing = controllerFacing;
		this.dataChanged();
	}

	private void calculateInCabin() {
		if (this.hasGroup()) {
			ElevatorGroup group = this.getGroup();
			for (int floor = 0; floor < group.getFloorCount(); floor++) {
				int y = group.getFloorYLevel(floor);
				BlockPos min = group.getCageAnchorBlockPos(y);
				if (this.worldPosition.getX() >= min.getX() && this.worldPosition.getX() < min.getX() + group.getCageSizeX() && this.worldPosition.getY() >= min.getY() && this.worldPosition.getY() < min.getY() + group.getCageSizeY() && this.worldPosition.getZ() >= min.getZ() && this.worldPosition.getZ() < min.getZ() + group.getCageSizeZ()) {
					this.isInCabin = true;
					this.cabinFloorIndex = floor;
					return;
				}
			}
		}
		this.isInCabin = false;
	}

	@Override
	protected CompoundTag writeData() {
		CompoundTag compound = super.writeData();
		compound.putInt("facing", this.facing.get3DDataValue());
		compound.putInt("controllerX", this.controllerPos.getX());
		compound.putInt("controllerY", this.controllerPos.getY());
		compound.putInt("controllerZ", this.controllerPos.getZ());
		if (this.controllerFacing != null) compound.putInt("controllerFacing", this.controllerFacing.get2DDataValue());
		this.groupCheckCounter = 2;
		return compound;
	}

	@Override
	protected void readData(CompoundTag compound) {
		super.readData(compound);
		this.facing = Direction.from3DDataValue(compound.getInt("facing"));
		this.controllerPos = new BlockPos(compound.getInt("controllerX"), compound.getInt("controllerY"), compound.getInt("controllerZ"));
		this.controllerFacing = compound.contains("controllerFacing", Tag.TAG_INT) ? Direction.from2DDataValue(compound.getInt("controllerFacing")) : null;
		this.isInCabin = false;
	}

	@Override
	public Direction getFacing() {
		return this.facing;
	}

	public ControllerBlockEntity getController() {
		if (this.level == null || this.controllerPos == null) return null;
		BlockEntity entity = this.level.getBlockEntity(this.controllerPos);
		return entity instanceof ControllerBlockEntity ? (ControllerBlockEntity) entity : null;
	}

	@Override
	public boolean hasGroup() {
		return this.getGroup() != null;
	}

	@Override
	public ElevatorGroup getGroup() {
		if (this.level == null || this.controllerPos == null || this.controllerFacing == null) return null;
		ElevatorGroup group;
		if (this.level instanceof ElevatorCabinLevel) group = ((ElevatorCabinLevel) this.level).getElevatorGroup();
		else {
			ElevatorGroupCapability capability = ElevatorGroupCapability.get(this.level);
			group = capability == null ? null : capability.get(this.controllerPos.getX(), this.controllerPos.getZ(), this.controllerFacing);
		}
		return group != null && group.hasControllerAt(this.controllerPos.getY()) ? group : null;
	}

	@Override
	public String getFloorName() {
		ControllerBlockEntity controller = this.getController();
		return controller == null ? null : controller.getFloorName();
	}

	@Override
	public DyeColor getDisplayLabelColor() {
		ControllerBlockEntity controller = this.getController();
		return controller == null ? null : controller.getDisplayLabelColor();
	}

	@Override
	public int getFloorLevel() {
		if (this.level instanceof ElevatorCabinLevel && this.hasGroup()) {
			ElevatorGroup group = this.getGroup();
			return group.getFloorYLevel(group.getClosestFloorNumber(this.worldPosition.getY()));
		}
		return this.isInCabin && this.hasGroup() ? this.getGroup().getFloorYLevel(this.cabinFloorIndex) : this.controllerPos.getY();
	}

	public BlockPos getControllerPos() {
		return this.controllerPos;
	}

	@Override
	public void setRemoved() {
		ElevatorGroup group = this.getGroup();
		if (group != null) group.removeComparatorListener(this.worldPosition);
		super.setRemoved();
	}

	public boolean provideUpAndDown() {
		return this.flag;
	}

	public boolean provideMiddle() {
		return !this.flag;
	}
}
