package net.tyrone.squidgame.block.custom;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSetType;
import net.minecraft.block.Blocks;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.block.AbstractBlock;

public class SafeGlassBlock extends Block {
    public SafeGlassBlock() {
        super(AbstractBlock.Settings.create()
                .nonOpaque()
                .strength(0.3F)
                .sounds(BlockSoundGroup.GLASS)
                .burnable()); // Optional: makes blocks burnable
    }
}
