package net.tyrone.squidgame.item;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.tyrone.squidgame.SquidGame;

public class ModItems {
// Items go here


    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(SquidGame.MOD_ID, name ), item);
    }

    public static void registerModItems() {
        SquidGame.LOGGER.info("Registering Mod Items for " + SquidGame.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> {


        });
    }
}
