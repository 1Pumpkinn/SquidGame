package net.tyrone.squidgame.block;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.tyrone.squidgame.SquidGameMod;
import net.tyrone.squidgame.block.custom.FakeGlassBlock;
import net.tyrone.squidgame.block.custom.SafeGlassBlock;

public class ModBlocks {

    public static final Block SAFE_GLASS = registerBlock("safe_glass", new SafeGlassBlock());
    public static final Block FAKE_GLASS = registerBlock("fake_glass", new FakeGlassBlock());

    private static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return Registry.register(Registries.BLOCK, Identifier.of(SquidGameMod.MOD_ID, name), block);
    }

    private static void registerBlockItem(String name, Block block) {
        Registry.register(Registries.ITEM, Identifier.of(SquidGameMod.MOD_ID, name),
                new BlockItem(block, new Item.Settings()));
    }

    public static void registerModBlocks() {
        SquidGameMod.LOGGER.info("Registering blocks for " + SquidGameMod.MOD_ID);
    }
}