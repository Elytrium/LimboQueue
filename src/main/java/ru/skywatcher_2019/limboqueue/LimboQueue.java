package ru.skywatcher_2019.limboqueue;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerPing;
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
    public LinkedList<LimboPlayer> QueuedPlayers = new LinkedList<>();
    private String queueMessage;

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
        queueMessage = Config.IMP.MESSAGES.QUEUEMESSAGE;
        Optional<RegisteredServer> server1 = this.getServer().getServer(Config.IMP.MAIN.SERVER);
        this.getServer().getScheduler().buildTask(this, () -> {
            ServerPing serverPing;
            if (server1.isPresent()) {
                try {
                    serverPing = server1.get().ping().get();
                    if (serverPing.getPlayers().isPresent()) {
                        ServerPing.Players players = serverPing.getPlayers().get();
                        if (players.getOnline() < players.getMax() && this.QueuedPlayers.size() > 0) {
                            LimboPlayer limboPlayer = this.QueuedPlayers.getFirst();
                            limboPlayer.disconnect();
                            this.QueuedPlayers.poll();
                        } else {
                            AtomicInteger i = new AtomicInteger(0);
                            this.QueuedPlayers.forEach((p) -> p.getProxyPlayer().sendMessage(SERIALIZER.deserialize(MessageFormat.format(queueMessage, i.incrementAndGet())), MessageType.SYSTEM));
                        }
                    }
                } catch (InterruptedException | ExecutionException ignored) {
                    this.QueuedPlayers.forEach((p) -> p.getProxyPlayer().sendMessage(SERIALIZER.deserialize(Config.IMP.MESSAGES.OFFLINESERVER), MessageType.SYSTEM));
                }
            } else {
                this.QueuedPlayers.forEach((p) -> p.getProxyPlayer().sendMessage(SERIALIZER.deserialize(Config.IMP.MESSAGES.OFFLINESERVER), MessageType.SYSTEM));
            }

        }).repeat(2, TimeUnit.SECONDS).schedule();
    }

    private void reload() {
        Config.IMP.reload(this.configFile);
        ComponentSerializer<Component, Component, String> serializer = Serializers.valueOf(Config.IMP.MAIN.SERIALIZER.toUpperCase(Locale.ROOT)).getSerializer();
        if (serializer == null) {
            LOGGER.warn("The specified serializer could not be founded, using default. (LEGACY_AMPERSAND)");
            setSerializer(new Serializer(Objects.requireNonNull(Serializers.LEGACY_AMPERSAND.getSerializer())));
        } else {
            setSerializer(new Serializer(serializer));
        }

        VirtualWorld authWorld = this.factory.createVirtualWorld(Dimension.OVERWORLD, 0, 100, 0, (float) 90, (float) 0.0);
        this.queueServer = this.factory.createLimbo(authWorld).setName("LimboQueue").setWorldTime(6000);
        this.server.getEventManager().register(this, new QueueListener(this));
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

}
