package ru.skywatcher_2019.limboqueue.handler;


import net.elytrium.limboapi.api.Limbo;
import net.elytrium.limboapi.api.LimboSessionHandler;
import net.elytrium.limboapi.api.player.LimboPlayer;
import ru.skywatcher_2019.limboqueue.LimboQueue;

public class QueueHandler implements LimboSessionHandler {
    private final LimboQueue plugin;
    private LimboPlayer player;

    public QueueHandler(LimboQueue plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onSpawn(Limbo server, LimboPlayer player) {
        this.player = player;
        this.player.disableFalling();
        this.plugin.queuedPlayers.add(player);
    }

    @Override
    public void onDisconnect() {
        this.plugin.queuedPlayers.remove(this.player);
    }
}
