package net.tyrone.squidgame;

import net.fabricmc.api.ModInitializer;

import net.tyrone.squidgame.block.ModBlocks;
import net.tyrone.squidgame.item.ModItemGroups;
import net.tyrone.squidgame.item.ModItems;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Very important comment
public class SquidGame implements ModInitializer {
	public static final String MOD_ID = "tutorialmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModItemGroups.registerItemGroups();

		ModItems.registerModItems();
		ModBlocks.registerModBlocks();
	}
}