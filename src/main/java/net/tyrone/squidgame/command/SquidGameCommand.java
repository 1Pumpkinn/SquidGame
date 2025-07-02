package net.tyrone.squidgame.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.tyrone.squidgame.game.SquidGameManager;
import net.tyrone.squidgame.game.games.RedLightGreenLightGame;
import net.tyrone.squidgame.game.games.SquidGame;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class SquidGameCommand {

    // Map of available games for testing
    private static final Map<String, Class<? extends SquidGame>> AVAILABLE_GAMES = new HashMap<>();

    static {
        AVAILABLE_GAMES.put("redlightgreenlight", RedLightGreenLightGame.class);
        // Add more games here as they're created
        // AVAILABLE_GAMES.put("tugofwar", TugOfWarGame.class);
        // AVAILABLE_GAMES.put("glassstepstones", GlassStepStonesGame.class);
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("squidgame")
                .requires(source -> source.hasPermissionLevel(2)) // Requires OP level 2
                .then(CommandManager.literal("start")
                        .executes(SquidGameCommand::startGame))
                .then(CommandManager.literal("stop")
                        .executes(SquidGameCommand::stopGame))
                .then(CommandManager.literal("status")
                        .executes(SquidGameCommand::getStatus))
                .then(CommandManager.literal("test")
                        .then(CommandManager.argument("game", StringArgumentType.string())
                                .suggests(SquidGameCommand::suggestGames)
                                .executes(SquidGameCommand::testGame)))
                .then(CommandManager.literal("list")
                        .executes(SquidGameCommand::listGames))
                .then(CommandManager.literal("skip")
                        .executes(SquidGameCommand::skipGame))
                .then(CommandManager.literal("eliminate")
                        .then(CommandManager.argument("player", StringArgumentType.string())
                                .executes(SquidGameCommand::eliminatePlayer)))
        );
    }

    private static int startGame(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();

        if (SquidGameManager.isGameActive()) {
            source.sendFeedback(() -> Text.literal("§cSquid Game is already active!").formatted(Formatting.RED), false);
            return 0;
        }

        // Get player count
        int playerCount = source.getServer().getPlayerManager().getPlayerList().size();
        if (playerCount == 0) {
            source.sendFeedback(() -> Text.literal("§cNo players online to start Squid Game!").formatted(Formatting.RED), false);
            return 0;
        }

        source.sendFeedback(() -> Text.literal("§aStarting Squid Game with " + playerCount + " players...").formatted(Formatting.GREEN), true);

        try {
            SquidGameManager.startGames(source.getServer());
            return 1;
        } catch (Exception e) {
            source.sendFeedback(() -> Text.literal("§cError starting Squid Game: " + e.getMessage()).formatted(Formatting.RED), false);
            e.printStackTrace();
            return 0;
        }
    }

    private static int stopGame(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();

        if (!SquidGameManager.isGameActive()) {
            source.sendFeedback(() -> Text.literal("§cNo Squid Game is currently active!").formatted(Formatting.RED), false);
            return 0;
        }

        source.sendFeedback(() -> Text.literal("§eForcing Squid Game to end...").formatted(Formatting.YELLOW), true);

        try {
            SquidGameManager.endGames();
            source.sendFeedback(() -> Text.literal("§aSquid Game ended successfully.").formatted(Formatting.GREEN), false);
            return 1;
        } catch (Exception e) {
            source.sendFeedback(() -> Text.literal("§cError ending Squid Game: " + e.getMessage()).formatted(Formatting.RED), false);
            e.printStackTrace();
            return 0;
        }
    }

    private static int getStatus(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();

        if (!SquidGameManager.isGameActive()) {
            source.sendFeedback(() -> Text.literal("§7Squid Game is not active.").formatted(Formatting.GRAY), false);
        } else {
            int aliveCount = SquidGameManager.getAlivePlayers().size();
            String currentGameName = SquidGameManager.getCurrentGame() != null ?
                    SquidGameManager.getCurrentGame().getGameName() : "None";

            source.sendFeedback(() -> Text.literal("§aSquid Game Status:").formatted(Formatting.GREEN), false);
            source.sendFeedback(() -> Text.literal("§7- Active: §aYes").formatted(Formatting.GRAY), false);
            source.sendFeedback(() -> Text.literal("§7- Players Alive: §b" + aliveCount).formatted(Formatting.GRAY), false);
            source.sendFeedback(() -> Text.literal("§7- Current Game: §e" + currentGameName).formatted(Formatting.GRAY), false);
        }

        return 1;
    }

    private static CompletableFuture<Suggestions> suggestGames(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        AVAILABLE_GAMES.keySet().forEach(builder::suggest);
        return builder.buildFuture();
    }

    private static int testGame(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        String gameName = StringArgumentType.getString(context, "game").toLowerCase();

        if (SquidGameManager.isGameActive()) {
            source.sendFeedback(() -> Text.literal("§cStop the current game first before testing!").formatted(Formatting.RED), false);
            return 0;
        }

        Class<? extends SquidGame> gameClass = AVAILABLE_GAMES.get(gameName);
        if (gameClass == null) {
            source.sendFeedback(() -> Text.literal("§cGame '" + gameName + "' not found! Use /squidgame list to see available games.").formatted(Formatting.RED), false);
            return 0;
        }

        int playerCount = source.getServer().getPlayerManager().getPlayerList().size();
        if (playerCount == 0) {
            source.sendFeedback(() -> Text.literal("§cNo players online to test the game!").formatted(Formatting.RED), false);
            return 0;
        }

        source.sendFeedback(() -> Text.literal("§aTesting game: " + gameName + " with " + playerCount + " players...").formatted(Formatting.GREEN), true);

        try {
            SquidGameManager.testSingleGame(source.getServer(), gameClass);
            return 1;
        } catch (Exception e) {
            source.sendFeedback(() -> Text.literal("§cError testing game: " + e.getMessage()).formatted(Formatting.RED), false);
            e.printStackTrace();
            return 0;
        }
    }

    private static int listGames(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();

        source.sendFeedback(() -> Text.literal("§aAvailable Games for Testing:").formatted(Formatting.GREEN), false);
        AVAILABLE_GAMES.forEach((name, gameClass) -> {
            try {
                SquidGame instance = gameClass.getDeclaredConstructor().newInstance();
                source.sendFeedback(() -> Text.literal("§7- §e" + name + " §7(" + instance.getGameName() + ")").formatted(Formatting.GRAY), false);
            } catch (Exception e) {
                source.sendFeedback(() -> Text.literal("§7- §e" + name + " §c(Error loading)").formatted(Formatting.GRAY), false);
            }
        });

        return 1;
    }

    private static int skipGame(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();

        if (!SquidGameManager.isGameActive()) {
            source.sendFeedback(() -> Text.literal("§cNo game is currently active!").formatted(Formatting.RED), false);
            return 0;
        }

        if (SquidGameManager.getCurrentGame() == null) {
            source.sendFeedback(() -> Text.literal("§cNo current game to skip!").formatted(Formatting.RED), false);
            return 0;
        }

        source.sendFeedback(() -> Text.literal("§eSkipping current game...").formatted(Formatting.YELLOW), true);

        try {
            SquidGameManager.completeCurrentGame();
            source.sendFeedback(() -> Text.literal("§aGame skipped successfully!").formatted(Formatting.GREEN), false);
            return 1;
        } catch (Exception e) {
            source.sendFeedback(() -> Text.literal("§cError skipping game: " + e.getMessage()).formatted(Formatting.RED), false);
            e.printStackTrace();
            return 0;
        }
    }

    private static int eliminatePlayer(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        String playerName = StringArgumentType.getString(context, "player");

        if (!SquidGameManager.isGameActive()) {
            source.sendFeedback(() -> Text.literal("§cNo game is currently active!").formatted(Formatting.RED), false);
            return 0;
        }

        var player = source.getServer().getPlayerManager().getPlayer(playerName);
        if (player == null) {
            source.sendFeedback(() -> Text.literal("§cPlayer '" + playerName + "' not found!").formatted(Formatting.RED), false);
            return 0;
        }

        if (!SquidGameManager.isPlayerAlive(player.getUuid())) {
            source.sendFeedback(() -> Text.literal("§c" + playerName + " is not alive in the current game!").formatted(Formatting.RED), false);
            return 0;
        }

        source.sendFeedback(() -> Text.literal("§eEliminating " + playerName + "...").formatted(Formatting.YELLOW), true);

        try {
            SquidGameManager.eliminatePlayer(player.getUuid(), "Eliminated by admin");
            source.sendFeedback(() -> Text.literal("§a" + playerName + " has been eliminated!").formatted(Formatting.GREEN), false);
            return 1;
        } catch (Exception e) {
            source.sendFeedback(() -> Text.literal("§cError eliminating player: " + e.getMessage()).formatted(Formatting.RED), false);
            e.printStackTrace();
            return 0;
        }
    }
}