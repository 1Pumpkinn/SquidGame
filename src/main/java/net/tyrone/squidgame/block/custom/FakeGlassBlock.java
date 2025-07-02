package net.tyrone.squidgame.block.custom;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.AbstractBlock;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.tyrone.squidgame.game.SquidGameManager;

import java.util.HashSet;
import java.util.Set;

public class FakeGlassBlock extends Block {
    public FakeGlassBlock() {
        super(AbstractBlock.Settings.create()
                .nonOpaque()
                .strength(0.3F)
                .sounds(BlockSoundGroup.GLASS));
    }

    @Override
    public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity) {
        if (!world.isClient && entity instanceof ServerPlayerEntity player) {
            breakConnectedFakeGlass(world, pos, new HashSet<>());
            SquidGameManager.eliminatePlayer(player.getUuid(), "Stepped on fake glass");
        }
        super.onSteppedOn(world, pos, state, entity);
    }

    private void breakConnectedFakeGlass(World world, BlockPos pos, Set<BlockPos> visited) {
        if (visited.contains(pos)) return;
        visited.add(pos);

        BlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof FakeGlassBlock)) return;

        world.breakBlock(pos, false);

        for (Direction direction : Direction.values()) {
            BlockPos nextPos = pos.offset(direction);
            breakConnectedFakeGlass(world, nextPos, visited);
        }
    }
}
