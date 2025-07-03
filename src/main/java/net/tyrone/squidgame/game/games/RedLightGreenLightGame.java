package net.tyrone.squidgame.game.games;

import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.tyrone.squidgame.game.SquidGameManager;

import java.util.*;

public class RedLightGreenLightGame extends SquidGame {
    private MinecraftServer server;
    private Set<UUID> players;
    private boolean isGreenLight = true;
    private final Map<UUID, Vec3d> lastPositions = new HashMap<>();
    private long tickCounter = 0;
    private final long toggleInterval = 80; // Toggle every 4 seconds
    private final long maxGameTime = 2700; // 90 seconds in ticks
    private boolean gameStarted = false;

    private final BlockPos startPos = new BlockPos(0, 100, 0);
    private final BlockPos endPos = new BlockPos(0, 100, 100);

    @Override
    public void setupGame(MinecraftServer server, Set<UUID> players) {
        this.server = server;
        this.players = players;

        for (UUID uuid : players) {
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(uuid);
            if (player != null) {
                // Updated teleportation for 1.21
                player.teleport(player.getServerWorld(),
                        startPos.getX(), startPos.getY(), startPos.getZ(), 0, 0);
                lastPositions.put(uuid, player.getPos());
            }
        }

        gameStarted = true;
        SquidGameManager.broadcastMessage("§aGame: Red Light, Green Light - Reach the goal within 90 seconds!", Formatting.GREEN);
    }

    @Override
    public void startGame() {
        showLightEffectToAll(true); // Start with green light
    }

    @Override
    public void onTick() {
        if (!gameStarted) return;

        tickCounter++;

        // End game if time runs out
        if (tickCounter >= maxGameTime) {
            for (UUID uuid : new HashSet<>(players)) {
                SquidGameManager.eliminatePlayer(uuid, "Time ran out");
            }
            SquidGameManager.completeCurrentGame();
            return;
        }

        // Toggle red/green light
        if (tickCounter % toggleInterval == 0) {
            isGreenLight = !isGreenLight;
            String state = isGreenLight ? "§a§lGREEN LIGHT!" : "§c§lRED LIGHT!";
            SquidGameManager.broadcastMessage(state, isGreenLight ? Formatting.GREEN : Formatting.RED);
            showLightEffectToAll(isGreenLight);
        }

        // Check movement and goal
        for (UUID uuid : new HashSet<>(players)) {
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(uuid);
            if (player != null && !player.isSpectator()) {
                Vec3d currentPos = player.getPos();
                Vec3d lastPos = lastPositions.get(uuid);

                // Eliminate if moved during red light
                if (!isGreenLight && lastPos != null) {
                    double moved = currentPos.squaredDistanceTo(lastPos);
                    if (moved > 0.001) {
                        SquidGameManager.eliminatePlayer(uuid, "Moved during RED light");
                        players.remove(uuid);
                        continue;
                    }
                }

                lastPositions.put(uuid, currentPos);

                // Check if player reached the end
                if (currentPos.distanceTo(Vec3d.ofCenter(endPos)) < 2.0) {
                    SquidGameManager.broadcastMessage("§b" + player.getName().getString() + " reached the goal!", Formatting.AQUA);
                    players.remove(uuid);
                }
            }
        }

        // End game if all players are done
        if (players.isEmpty()) {
            SquidGameManager.completeCurrentGame();
        }
    }

    @Override
    public void endGame() {
        gameStarted = false;
    }

    @Override
    public String getGameName() {
        return "Red Light Green Light";
    }

    private void showLightEffectToAll(boolean green) {
        Text title = Text.literal(green ? "GREEN LIGHT" : "RED LIGHT");
        Text subtitle = Text.literal("Don't move on red!");

        for (UUID uuid : players) {
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(uuid);
            if (player != null) {
                // Send title
                TitleS2CPacket titlePacket = new TitleS2CPacket(title);
                player.networkHandler.sendPacket(titlePacket);

                // Send subtitle
                SubtitleS2CPacket subtitlePacket = new SubtitleS2CPacket(subtitle);
                player.networkHandler.sendPacket(subtitlePacket);

                // Set timing (fade in, stay, fade out)
                TitleFadeS2CPacket timingPacket = new TitleFadeS2CPacket(5, 40, 5);
                player.networkHandler.sendPacket(timingPacket);
            }
        }
    }
}