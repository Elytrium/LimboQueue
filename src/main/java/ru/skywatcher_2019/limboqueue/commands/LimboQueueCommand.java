package ru.skywatcher_2019.limboqueue.commands;

import com.google.common.collect.ImmutableList;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import net.elytrium.java.commons.mc.serialization.Serializer;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.text.Component;
import ru.skywatcher_2019.limboqueue.Config;
import ru.skywatcher_2019.limboqueue.LimboQueue;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LimboQueueCommand implements SimpleCommand {
    private static final Map<String, Component> SUBCOMMANDS = Map.of(
            "reload", Component.textOfChildren(
                    Component.text("  /limboqueue reload"),
                    Component.text(" - "),
                    Component.text("Reload config.")
            )
    );
    private final LimboQueue plugin;
    private final Component reload;
    private final Component reloadFailed;

    public LimboQueueCommand(LimboQueue plugin) {
        this.plugin = plugin;
        Serializer serializer = LimboQueue.getSerializer();
        this.reload = serializer.deserialize(Config.IMP.MESSAGES.RELOAD);
        this.reloadFailed = serializer.deserialize(Config.IMP.MESSAGES.RELOAD_FAILED);
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (args.length == 0) {
            return SUBCOMMANDS.keySet().stream()
                    .filter(command -> source.hasPermission("limboqueue." + command))
                    .collect(Collectors.toList());
        } else if (args.length == 1) {
            String argument = args[0];
            return SUBCOMMANDS.keySet().stream()
                    .filter(command -> source.hasPermission("limbofilter." + command))
                    .filter(command -> command.regionMatches(true, 0, argument, 0, argument.length()))
                    .collect(Collectors.toList());
        } else {
            return ImmutableList.of();
        }
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (args.length == 1) {
            String command = args[0];
            if (command.equalsIgnoreCase("reload") && source.hasPermission("limboqueue.reload")) {
                try {
                    this.plugin.reload();
                    source.sendMessage(this.reload, MessageType.SYSTEM);
                } catch (Exception e) {
                    e.printStackTrace();
                    source.sendMessage(this.reloadFailed, MessageType.SYSTEM);
                }
            }
        }
    }
}
