package net.tyrone.squidgame.game.games;

import net.minecraft.server.MinecraftServer;
import java.util.Set;
import java.util.UUID;

public abstract class SquidGame {
    protected MinecraftServer server;
    protected Set<UUID> alivePlayers;
    private boolean active = false;

    public abstract String getGameName();

    public void setupGame(MinecraftServer server, Set<UUID> players) {
        this.server = server;
        this.alivePlayers = players;
        this.active = false;
    }

    public void startGame() {
        this.active = true;
    }

    public abstract void onTick();

    public void endGame() {
        this.active = false;
    }

    public boolean isActive() {
        return active;
    }

    public Set<UUID> getAlivePlayers() {
        return alivePlayers;
    }
}