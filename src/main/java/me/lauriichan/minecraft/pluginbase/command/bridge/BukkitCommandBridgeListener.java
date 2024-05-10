package me.lauriichan.minecraft.pluginbase.command.bridge;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import me.lauriichan.laylib.command.Actor;
import me.lauriichan.laylib.command.CommandManager;
import me.lauriichan.laylib.command.CommandProcess;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.command.BukkitActor;

final class BukkitCommandBridgeListener implements Listener {

    public static final String CANCEL_COMMAND = "/cancel";
    public static final String SKIP_COMMAND = "/skip";
    public static final String SUGGESTION_COMMAND = "/suggestion";

    private final CommandManager commandManager;
    private final BasePlugin<?> plugin;

    public BukkitCommandBridgeListener(final CommandManager commandManager, final BasePlugin<?> plugin) {
        this.commandManager = commandManager;
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChat(final AsyncPlayerChatEvent event) {
        final Player player = event.getPlayer();
        final CommandProcess process = commandManager.getProcess(player.getUniqueId());
        if (process == null) {
            return;
        }
        event.setCancelled(true);
        commandManager.handleProcessInput(new BukkitActor<>(player, plugin), process, event.getMessage(), false);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPreProcess(final PlayerCommandPreprocessEvent event) {
        final Player player = event.getPlayer();
        final CommandProcess process = commandManager.getProcess(player.getUniqueId());
        if (process == null) {
            return;
        }
        event.setCancelled(true);
        final BukkitActor<Player> actor = new BukkitActor<>(player, plugin);
        final String[] args = event.getMessage().split(" ");
        if (CANCEL_COMMAND.equalsIgnoreCase(args[0])) {
            commandManager.cancelProcess(actor);
            return;
        }
        if (SKIP_COMMAND.equalsIgnoreCase(args[0])) {
            commandManager.handleProcessSkip(actor, process);
            return;
        }
        if (args.length > 1 && SUGGESTION_COMMAND.equalsIgnoreCase(args[0])) {
            commandManager.handleProcessInput(actor, process,
                Arrays.stream(args).skip(1).filter(Predicate.not(String::isBlank)).collect(Collectors.joining(" ")), true);
            return;
        }
        commandManager.handleProcessInput(actor, process, event.getMessage());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onServerCommand(final ServerCommandEvent event) {
        final CommandProcess process = commandManager.getProcess(Actor.IMPL_ID);
        if (process == null) {
            return;
        }
        final BukkitActor<CommandSender> actor = new BukkitActor<>(event.getSender(), plugin);
        event.setCancelled(true);
        final String[] args = event.getCommand().split(" ");
        if (CANCEL_COMMAND.equalsIgnoreCase(args[0])) {
            commandManager.cancelProcess(actor);
            return;
        }
        if (SKIP_COMMAND.equalsIgnoreCase(args[0])) {
            commandManager.handleProcessSkip(actor, process);
            return;
        }
        if (args.length > 1 && SUGGESTION_COMMAND.equalsIgnoreCase(args[0])) {
            commandManager.handleProcessInput(actor, process,
                Arrays.stream(args).skip(1).filter(Predicate.not(String::isBlank)).collect(Collectors.joining(" ")), true);
            return;
        }
        commandManager.handleProcessInput(actor, process, event.getCommand());
    }

}