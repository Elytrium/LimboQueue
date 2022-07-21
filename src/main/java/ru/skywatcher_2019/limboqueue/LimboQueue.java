package ru.skywatcher_2019.limboqueue;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerPing;
import com.velocitypowered.api.scheduler.ScheduledTask;
import net.elytrium.java.commons.mc.serialization.Serializer;
import net.elytrium.java.commons.mc.serialization.Serializers;
import net.elytrium.limboapi.api.Limbo;
import net.elytrium.limboapi.api.LimboFactory;
import net.elytrium.limboapi.api.chunk.Dimension;
import net.elytrium.limboapi.api.chunk.VirtualWorld;
import net.elytrium.limboapi.api.player.LimboPlayer;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.ComponentSerializer;
import org.slf4j.Logger;
import ru.skywatcher_2019.limboqueue.commands.LimboQueueCommand;
import ru.skywatcher_2019.limboqueue.handler.QueueHandler;
import ru.skywatcher_2019.limboqueue.listener.QueueListener;

import java.io.File;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Plugin(id = "limboqueue", name = "LimboQueue", version = "1.0-SNAPSHOT", authors = {"skywatcher_2019", "hevav"})
public class LimboQueue {

    @Inject
    private static Logger LOGGER;
    private static Serializer SERIALIZER;
    private final ProxyServer server;
    private final File configFile;
    private final LimboFactory factory;
    private Limbo queueServer;
    public LinkedList<LimboPlayer> queuedPlayers = new LinkedList<>();
    private String queueMessage;
    private Component serverOfflineMessage;
    private int checkInterval;
    private RegisteredServer targetServer;
    private ScheduledTask queueTask, pingTask;
    public boolean isFull, isOffline = false;

    @Inject
    public LimboQueue(Logger logger, ProxyServer server, @DataDirectory Path dataDirectory) {
        setLogger(logger);

        this.server = server;

        File dataDirectoryFile = dataDirectory.toFile();
        this.configFile = new File(dataDirectoryFile, "config.yml");

        this.factory = (LimboFactory) this.server.getPluginManager().getPlugin("limboapi").flatMap(PluginContainer::getInstance).orElseThrow();
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        this.reload();
    }

    public void reload() {
        Config.IMP.reload(this.configFile);
        ComponentSerializer<Component, Component, String> serializer = Serializers.valueOf(Config.IMP.MAIN.SERIALIZER.toUpperCase(Locale.ROOT)).getSerializer();
        if (serializer == null) {
            LOGGER.warn("The specified serializer could not be founded, using default. (LEGACY_AMPERSAND)");
            setSerializer(new Serializer(Objects.requireNonNull(Serializers.LEGACY_AMPERSAND.getSerializer())));
        } else {
            setSerializer(new Serializer(serializer));
        }

        this.queueMessage = Config.IMP.MESSAGES.QUEUE_MESSAGE;
        this.serverOfflineMessage = SERIALIZER.deserialize(Config.IMP.MESSAGES.SERVER_OFFLINE);
        this.checkInterval = Config.IMP.MAIN.CHECK_INTERVAL;

        VirtualWorld queueWorld = this.factory.createVirtualWorld(Dimension.valueOf(Config.IMP.MAIN.WORLD.DIMENSION), 0, 100, 0, (float) 90, (float) 0.0);
        this.queueServer = this.factory.createLimbo(queueWorld).setName("LimboQueue").setWorldTime(6000);
        this.server.getEventManager().register(this, new QueueListener(this));

        CommandManager manager = this.server.getCommandManager();
        manager.unregister("limboqueue");
        manager.register("limboqueue", new LimboQueueCommand(this), "lq", "queue");

        Optional<RegisteredServer> server = this.getServer().getServer(Config.IMP.MAIN.SERVER);
        server.ifPresent(registeredServer -> this.targetServer = registeredServer);
        this.startPingTask();
        this.startQueueTask();
    }

    private static void setSerializer(Serializer serializer) {
        SERIALIZER = serializer;
    }

    private static void setLogger(Logger logger) {
        LOGGER = logger;
    }

    public void queuePlayer(Player player) {
        this.queueServer.spawnPlayer(player, new QueueHandler(this));
    }

    public ProxyServer getServer() {
        return this.server;
    }

    public static Serializer getSerializer() {
        return SERIALIZER;
    }

    private void startQueueTask() {
        if (this.queueTask != null) this.queueTask.cancel();
        this.queueTask = this.getServer().getScheduler().buildTask(this, () -> {
            if (this.isOffline) {
                if (!this.isFull && this.queuedPlayers.size() > 0) {
                    LimboPlayer limboPlayer = this.queuedPlayers.getFirst();
                    limboPlayer.disconnect();
                } else {
                    AtomicInteger i = new AtomicInteger(0);
                    this.queuedPlayers.forEach((p) -> p.getProxyPlayer().sendMessage(SERIALIZER.deserialize(MessageFormat.format(queueMessage, i.incrementAndGet())), MessageType.SYSTEM));
                }
            } else {
                this.queuedPlayers.forEach((p) -> p.getProxyPlayer().sendMessage(serverOfflineMessage, MessageType.SYSTEM));
            }
        }).repeat(checkInterval, TimeUnit.SECONDS).schedule();
    }

    private void startPingTask() {
        if (this.pingTask != null) this.pingTask.cancel();
        this.pingTask = this.getServer().getScheduler().buildTask(this, () -> {
            try {
                ServerPing serverPing = this.targetServer.ping().get();
                if (serverPing.getPlayers().isPresent()) {
                    ServerPing.Players players = serverPing.getPlayers().get();
                    this.isFull = players.getOnline() >= players.getMax();
                    this.isOffline = false;
                }
            } catch (InterruptedException | ExecutionException | NullPointerException ignored) {
                this.isOffline = true;
            }
        }).repeat(checkInterval, TimeUnit.SECONDS).schedule();
    }
}
