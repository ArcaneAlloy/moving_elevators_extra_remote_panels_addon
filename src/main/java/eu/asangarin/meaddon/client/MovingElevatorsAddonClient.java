package eu.asangarin.meaddon.client;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.registry.ClientRegistrationHandler;
import com.supermartijn642.movingelevators.blocks.CamoBlockEntity;
import com.supermartijn642.movingelevators.model.CamoBakedModel;
import eu.asangarin.meaddon.MovingElevatorsAddon;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MovingElevatorsAddonClient {
	public static void register() {
		ClientRegistrationHandler handler = ClientRegistrationHandler.get(MovingElevatorsAddon.MODID);
		handler.registerCustomBlockEntityRenderer(() -> MovingElevatorsAddon.button_tile_custom, CustomRemoteControllerBlockEntityRenderer::new);
		handler.registerBlockModelOverwrite(() -> MovingElevatorsAddon.button_block_updown, CamoBakedModel::new);
		handler.registerBlockModelOverwrite(() -> MovingElevatorsAddon.button_block_middle, CamoBakedModel::new);
	}

	@SubscribeEvent
	public static void setup(RegisterColorHandlersEvent.Block e) {
		e.register((state, blockAndTintGetter, pos, p_92570_) -> {
			if (blockAndTintGetter == null || pos == null) return 0;
			BlockEntity entity = blockAndTintGetter.getBlockEntity(pos);
			return entity instanceof CamoBlockEntity && ((CamoBlockEntity) entity).hasCamoState() ? ClientUtils.getMinecraft().getBlockColors()
					.getColor(((CamoBlockEntity) entity).getCamoState(), blockAndTintGetter, pos, p_92570_) : 0;
		}, MovingElevatorsAddon.button_block_updown, MovingElevatorsAddon.button_block_middle);
	}
}
