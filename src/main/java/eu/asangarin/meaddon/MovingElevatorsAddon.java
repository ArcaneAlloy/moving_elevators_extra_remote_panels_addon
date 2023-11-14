package eu.asangarin.meaddon;

import com.supermartijn642.core.CommonUtils;
import com.supermartijn642.core.block.BaseBlockEntityType;
import com.supermartijn642.core.block.BlockProperties;
import com.supermartijn642.core.item.ItemProperties;
import com.supermartijn642.core.registry.RegistrationHandler;
import com.supermartijn642.core.registry.RegistryEntryAcceptor;
import com.supermartijn642.movingelevators.MovingElevators;
import com.supermartijn642.movingelevators.blocks.RemoteControllerBlockItem;
import eu.asangarin.meaddon.block.CustomRemoteControllerBlock;
import eu.asangarin.meaddon.block.CustomRemoteControllerBlockEntity;
import eu.asangarin.meaddon.client.MovingElevatorsAddonClient;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.fml.common.Mod;

import java.util.function.Supplier;

@Mod(MovingElevatorsAddon.MODID)
public class MovingElevatorsAddon {
	public static final String MODID = "meaddon";

	@RegistryEntryAcceptor(namespace = MODID, identifier = "button_block_updown", registry = RegistryEntryAcceptor.Registry.BLOCKS)
	public static CustomRemoteControllerBlock button_block_updown;
	@RegistryEntryAcceptor(namespace = MODID, identifier = "button_block_middle", registry = RegistryEntryAcceptor.Registry.BLOCKS)
	public static CustomRemoteControllerBlock button_block_middle;
	@RegistryEntryAcceptor(namespace = MODID, identifier = "button_tile_custom", registry = RegistryEntryAcceptor.Registry.BLOCK_ENTITY_TYPES)
	public static BaseBlockEntityType<CustomRemoteControllerBlockEntity> button_tile_custom;

	public MovingElevatorsAddon() {
		RegistrationHandler handler = RegistrationHandler.get(MODID);
		// Blocks
		Supplier<BlockProperties> properties = () -> BlockProperties.create(Material.STONE, MaterialColor.COLOR_GRAY).sound(SoundType.METAL).destroyTime(1.5f)
				.explosionResistance(6);
		handler.registerBlock("button_block_updown", () -> new CustomRemoteControllerBlock(properties.get(), true));
		handler.registerBlock("button_block_middle", () -> new CustomRemoteControllerBlock(properties.get(), false));
		// Block entities
		handler.registerBlockEntityType("button_tile_custom",
				() -> BaseBlockEntityType.create(CustomRemoteControllerBlockEntity::new, button_block_updown, button_block_middle));
		// Items
		handler.registerItem("button_block_updown",
				() -> new RemoteControllerBlockItem(button_block_updown, ItemProperties.create().group(MovingElevators.GROUP)));
		handler.registerItem("button_block_middle",
				() -> new RemoteControllerBlockItem(button_block_middle, ItemProperties.create().group(MovingElevators.GROUP)));

		if (CommonUtils.getEnvironmentSide().isClient()) MovingElevatorsAddonClient.register();
	}
}
