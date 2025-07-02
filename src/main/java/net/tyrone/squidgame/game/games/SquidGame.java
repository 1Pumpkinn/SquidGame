package net.tyrone.squidgame.game.games;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Set;
import java.util.UUID;

public abstract class SquidGame {
    protected MinecraftServer server;
    protected Set<UUID> players;
    protected boolean gameRunning = false;
    protected int gameTicks = 0;

    public abstract String getGameName();
    public abstract void setupGame(MinecraftServer server, Set<UUID> players);
    public abstract void startGame();
    public abstract void endGame();
    public abstract void onTick();

    protected void broadcastToPlayers(String message, Formatting color) {
        if (server != null) {
            server.getPlayerManager().broadcast(Text.literal(message).formatted(color), false);
        }
    }

    protected void sendMessageToPlayer(UUID playerId, String message, Formatting color) {
        ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerId);
        if (player != null) {
            player.sendMessage(Text.literal(message).formatted(color), false);
        }
    }

    protected ServerPlayerEntity getPlayer(UUID playerId) {
        return server.getPlayerManager().getPlayer(playerId);
    }

    protected void eliminatePlayer(UUID playerId, String reason) {
        net.tyrone.squidgame.game.SquidGameManager.eliminatePlayer(playerId, reason);
    }

    protected void completeGame() {
        endGame();
        net.tyrone.squidgame.game.SquidGameManager.completeCurrentGame();
    }
}