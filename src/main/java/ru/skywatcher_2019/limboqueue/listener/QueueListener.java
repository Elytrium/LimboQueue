package ru.skywatcher_2019.limboqueue.listener;

import com.velocitypowered.api.event.Subscribe;
import net.elytrium.limboapi.api.event.LoginLimboRegisterEvent;
import ru.skywatcher_2019.limboqueue.LimboQueue;

public class QueueListener {
    private final LimboQueue plugin;

    public QueueListener(LimboQueue plugin) {
        this.plugin = plugin;
    }

    @Subscribe
    public void onLoginLimboRegister(LoginLimboRegisterEvent event) {
        event.addCallback(() -> this.plugin.queuePlayer(event.getPlayer()));
    }
}
