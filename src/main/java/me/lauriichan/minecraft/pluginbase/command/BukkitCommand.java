package me.lauriichan.minecraft.pluginbase.command;

import java.util.List;
import java.util.Objects;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import me.lauriichan.minecraft.pluginbase.command.bridge.BukkitCommandBridge;

public class BukkitCommand extends Command implements PluginIdentifiableCommand {

    private final Plugin plugin;

    private volatile CommandExecutor executor;
    private volatile TabCompleter completer;

    public BukkitCommand(String name, Plugin plugin) {
        super(name);
        this.plugin = Objects.requireNonNull(plugin, "Plugin cannot be null");
    }

    public CommandExecutor executor() {
        return executor;
    }

    public void executor(CommandExecutor executor) {
        this.executor = executor;
    }

    public TabCompleter completer() {
        return completer;
    }

    public void completer(TabCompleter completer) {
        this.completer = completer;
    }

    @Override
    public Plugin getPlugin() {
        return plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!plugin.isEnabled()) {
            throw new CommandException("Cannot execute command '" + commandLabel + "' in plugin " + plugin.getDescription().getFullName()
                + " - plugin is disabled.");
        }
        if (!testPermission(sender)) {
            return true;
        }
        if (executor == null) {
            sender.sendMessage("Command '" + commandLabel + "' is not yet implemented.");
            return true;
        }
        boolean success = false;
        try {
            success = executor.onCommand(sender, this, commandLabel, args);
        } catch (Throwable ex) {
            throw new CommandException(
                "Unhandled exception executing command '" + commandLabel + "' in plugin " + plugin.getDescription().getFullName(), ex);
        }
        if (!success && usageMessage.length() > 0) {
            for (String line : usageMessage.replace("<command>", commandLabel).split("\n")) {
                sender.sendMessage(line);
            }
        }
        return success;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
        return tabComplete(sender, alias, args, null);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args, Location location) throws IllegalArgumentException {
        Preconditions.checkArgument(sender != null, "Sender cannot be null");
        Preconditions.checkArgument(args != null, "Arguments cannot be null");
        Preconditions.checkArgument(alias != null, "Alias cannot be null");

        List<String> suggestions = null;
        try {
            if (completer != null) {
                suggestions = complete(sender, completer, args, location);
            }
            if (suggestions == null && executor instanceof TabCompleter completer) {
                suggestions = complete(sender, completer, args, location);
            }
        } catch (Throwable ex) {
            StringBuilder message = new StringBuilder();
            message.append("Unhandled exception during tab completion for command '/").append(alias).append(' ');
            for (String arg : args) {
                message.append(arg).append(' ');
            }
            message.deleteCharAt(message.length() - 1).append("' in plugin ").append(plugin.getDescription().getFullName());
            throw new CommandException(message.toString(), ex);
        }
        if (suggestions == null) {
            return ImmutableList.of();
        }
        return suggestions;
    }

    private List<String> complete(CommandSender sender, TabCompleter completer, String[] args, Location location) {
        if (completer instanceof BukkitCommandBridge<?> bridge) {
            return bridge.onTabComplete(sender, this, description, args, location);
        }
        return completer.onTabComplete(sender, this, description, args);
    }

}
