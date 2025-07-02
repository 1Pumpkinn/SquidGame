package net.tyrone.squidgame.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.tyrone.squidgame.SquidGameMod;
import net.tyrone.squidgame.block.ModBlocks;


public class ModItemGroups {
    public static final ItemGroup SQUID_GAME = Registry.register(Registries.ITEM_GROUP,
            Identifier.of(SquidGameMod.MOD_ID, "squid_game"),
            FabricItemGroup.builder().icon(() -> new ItemStack(Items.ANDESITE))
                    .displayName(Text.translatable("itemgroup.squidgame.squid_game"))

                    .entries((displayContext, entries) -> {
                    entries.add(ModBlocks.FAKE_GLASS);
                    entries.add(ModBlocks.SAFE_GLASS);


                    }).build());




    public static void registerItemGroups() {
        SquidGameMod.LOGGER.info("Registering Mod Item Groups for " + SquidGameMod.MOD_ID);
    }
}
