package net.tyrone.squidgame.game;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameMode;
import net.tyrone.squidgame.game.games.*;

import java.util.*;

public class SquidGameManager {
    private static boolean gameActive = false;
    private static final Set<UUID> alivePlayers = new HashSet<>();
    private static final Set<UUID> eliminatedPlayers = new HashSet<>();
    private static int currentGameIndex = 0;
    private static SquidGame currentGame = null;
    private static MinecraftServer server;

    // List of games in order
    private static final List<Class<? extends SquidGame>> GAMES = Arrays.asList(
            RedLightGreenLightGame.class
            // Add more games here
    );


    public static void startGames(MinecraftServer mcServer) {
        server = mcServer;
        gameActive = true;
        currentGameIndex = 0;
        alivePlayers.clear();
        eliminatedPlayers.clear();

        // Add all players to the game
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            alivePlayers.add(player.getUuid());
            player.changeGameMode(GameMode.ADVENTURE);
            player.setHealth(player.getMaxHealth());
            player.getInventory().clear();
        }

        broadcastMessage("§6§l=== SQUID GAME BEGINS ===", Formatting.GOLD);
        broadcastMessage("§7Welcome to the Squid Game. You will compete in 6 games.", Formatting.GRAY);
        broadcastMessage("§cElimination means death. Only one can survive.", Formatting.RED);

        startNextGame();
    }

    public static void startNextGame() {
        if (!gameActive || currentGameIndex >= GAMES.size()) {
            endGames();
            return;
        }

        if (alivePlayers.size() <= 1) {
            endGames();
            return;
        }

        try {
            currentGame = GAMES.get(currentGameIndex).getDeclaredConstructor().newInstance();
            currentGameIndex++;

            broadcastMessage("§a§l=== GAME " + currentGameIndex + ": " + currentGame.getGameName() + " ===", Formatting.GREEN);
            broadcastMessage("§7Players remaining: " + alivePlayers.size(), Formatting.GRAY);

            currentGame.setupGame(server, new HashSet<>(alivePlayers));
            currentGame.startGame();

        } catch (Exception e) {
            broadcastMessage("§cError starting game: " + e.getMessage(), Formatting.RED);
            e.printStackTrace();
        }
    }



    public static void eliminatePlayer(UUID playerId, String reason) {
        if (!alivePlayers.contains(playerId)) return;

        alivePlayers.remove(playerId);
        eliminatedPlayers.add(playerId);

        ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerId);
        if (player != null) {
            player.changeGameMode(GameMode.SPECTATOR);
            player.sendMessage(Text.literal("§c§lYOU HAVE BEEN ELIMINATED").formatted(Formatting.RED, Formatting.BOLD), false);
            player.sendMessage(Text.literal("§7Reason: " + reason).formatted(Formatting.GRAY), false);

            broadcastMessage("§c" + player.getName().getString() + " has been eliminated. (" + reason + ")", Formatting.RED);
        }

        // Check if game should end
        if (alivePlayers.size() <= 1) {
            if (currentGame != null) {
                currentGame.endGame();
            }
        }
    }

    public static void completeCurrentGame() {
        if (currentGame != null) {
            currentGame.endGame();
            currentGame = null;
        }

        if (alivePlayers.size() > 1) {
            broadcastMessage("§a§lGame " + currentGameIndex + " completed!", Formatting.GREEN);
            broadcastMessage("§7Survivors: " + alivePlayers.size(), Formatting.GRAY);

            // Wait 5 seconds before next game
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    startNextGame();
                }
            }, 5000);
        } else {
            endGames();
        }
    }

    public static void endGames() {
        gameActive = false;

        if (currentGame != null) {
            currentGame.endGame();
            currentGame = null;
        }

        if (alivePlayers.size() == 1) {
            UUID winnerId = alivePlayers.iterator().next();
            ServerPlayerEntity winner = server.getPlayerManager().getPlayer(winnerId);
            if (winner != null) {
                broadcastMessage("§6§l=== SQUID GAME COMPLETE ===", Formatting.GOLD);
                broadcastMessage("§a§lWINNER: " + winner.getName().getString(), Formatting.GREEN);
                winner.sendMessage(Text.literal("§6§lCONGRATULATIONS! YOU WON THE SQUID GAME!").formatted(Formatting.GOLD, Formatting.BOLD), false);
            }
        } else {
            broadcastMessage("§c§lALL PLAYERS ELIMINATED - NO WINNER", Formatting.RED);
        }

        // Reset all players
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            player.changeGameMode(GameMode.ADVENTURE);
            player.setHealth(player.getMaxHealth());
            player.getInventory().clear();
        }

        alivePlayers.clear();
        eliminatedPlayers.clear();
        currentGameIndex = 0;
    }

    public static void onServerTick(MinecraftServer server) {
        if (currentGame != null && gameActive) {
            currentGame.onTick();
        }
    }

    public static boolean isGameActive() {
        return gameActive;
    }

    public static boolean isPlayerAlive(UUID playerId) {
        return alivePlayers.contains(playerId);
    }

    public static Set<UUID> getAlivePlayers() {
        return new HashSet<>(alivePlayers);
    }

    public static SquidGame getCurrentGame() {
        return currentGame;
    }

    public static void broadcastMessage(String message, Formatting color) {
        if (server != null) {
            server.getPlayerManager().broadcast(Text.literal(message).formatted(color), false);
        }
    }

    public static void teleportPlayersToSpawn() {
        for (UUID playerId : alivePlayers) {
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerId);
            if (player != null) {
                // Updated teleportation for 1.21
                player.teleport(player.getServerWorld(), 0, 100, 0, 0, 0);
            }
        }
    }
    public static void testSingleGame(MinecraftServer mcServer, Class<? extends SquidGame> gameClass) {
        server = mcServer;
        gameActive = true;
        currentGameIndex = 1; // Set to 1 for testing display
        alivePlayers.clear();
        eliminatedPlayers.clear();

        // Add all players to the game
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            alivePlayers.add(player.getUuid());
            player.changeGameMode(GameMode.ADVENTURE);
            player.setHealth(player.getMaxHealth());
            player.getInventory().clear();
        }

        try {
            currentGame = gameClass.getDeclaredConstructor().newInstance();

            broadcastMessage("§6§l=== TESTING GAME: " + currentGame.getGameName() + " ===", Formatting.GOLD);
            broadcastMessage("§7This is a test game. Players: " + alivePlayers.size(), Formatting.GRAY);

            currentGame.setupGame(server, new HashSet<>(alivePlayers));
            currentGame.startGame();

        } catch (Exception e) {
            broadcastMessage("§cError starting test game: " + e.getMessage(), Formatting.RED);
            e.printStackTrace();
            gameActive = false;
        }
    }
}