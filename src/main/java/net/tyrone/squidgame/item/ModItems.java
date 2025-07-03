package net.tyrone.squidgame.item;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.tyrone.squidgame.SquidGameMod;

public class ModItems {
    // Items go here

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(SquidGameMod.MOD_ID, name), item);
    }

    public static void registerModItems() {
        SquidGameMod.LOGGER.info("Registering Mod Items for " + SquidGameMod.MOD_ID);
    }
}