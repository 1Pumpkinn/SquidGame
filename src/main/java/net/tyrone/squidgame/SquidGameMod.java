package net.tyrone.squidgame;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.tyrone.squidgame.block.ModBlocks;
import net.tyrone.squidgame.command.SquidGameCommand;
import net.tyrone.squidgame.game.SquidGameManager;
import net.tyrone.squidgame.item.ModItemGroups;
import net.tyrone.squidgame.item.ModItems;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SquidGameMod implements ModInitializer {
	public static final String MOD_ID = "squidgame";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModItemGroups.registerItemGroups();

		// Register blocks and items
		ModBlocks.registerModBlocks();
		ModItems.registerModItems();


		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			SquidGameCommand.register(dispatcher);
		});

		// Register server tick event for game updates
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			SquidGameManager.onServerTick(server);
		});
	}
}



