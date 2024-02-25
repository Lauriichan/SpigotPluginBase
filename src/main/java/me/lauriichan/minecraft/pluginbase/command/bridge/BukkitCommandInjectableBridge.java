package me.lauriichan.minecraft.pluginbase.command.bridge;

import static me.lauriichan.minecraft.pluginbase.command.bridge.BukkitCommandReflection.getCommandMap;
import static me.lauriichan.minecraft.pluginbase.command.bridge.BukkitCommandReflection.getCommands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiFunction;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;

import me.lauriichan.laylib.command.Actor;
import me.lauriichan.laylib.command.CommandManager;
import me.lauriichan.laylib.localization.MessageManager;
import me.lauriichan.minecraft.pluginbase.command.BukkitActor;
import me.lauriichan.minecraft.pluginbase.command.BukkitCommand;
import me.lauriichan.minecraft.pluginbase.command.processor.IBukkitCommandProcessor;

public class BukkitCommandInjectableBridge<A extends BukkitActor<?>> extends BukkitCommandBridge<A> {

    public static record CommandDefinition(String prefix, String name, List<String> aliases, String description) {

        public CommandDefinition(final String prefix, final String name, final List<String> aliases, final String description) {
            this.prefix = prefix;
            this.name = name;
            this.aliases = Collections.unmodifiableList(aliases);
            this.description = description;
        }

        public static final Builder of(final String name) {
            return new Builder(name);
        }

        public static final class Builder {

            private final String name;
            private final ArrayList<String> aliases = new ArrayList<>();
            private String description;
            private String prefix;

            private Builder(final String name) {
                this.name = name;
            }

            public Builder prefix(final String prefix) {
                this.prefix = prefix;
                return this;
            }

            public Builder alias(final String alias) {
                if (aliases.contains(alias)) {
                    return this;
                }
                aliases.add(alias);
                return this;
            }

            public Builder description(final String description) {
                this.description = description;
                return this;
            }

            public CommandDefinition build(final Plugin plugin) {
                String prefix = this.prefix;
                if (prefix == null || prefix.isBlank()) {
                    prefix = plugin.getName();
                }
                return new CommandDefinition(prefix.toLowerCase(Locale.ROOT), name, aliases, description);
            }

        }

    }

    private static final String[] EMPTY_ARGS = {};

    private final Plugin plugin;

    private final CommandDefinition definition;

    private final BukkitCommandBridgeListener listener;

    private volatile String fallbackCommand = "help";
    private volatile boolean injected = false;

    public BukkitCommandInjectableBridge(final IBukkitCommandProcessor processor, final CommandManager commandManager,
        final MessageManager messageManager, final Plugin plugin, final CommandDefinition definition) {
        this(processor, commandManager, messageManager, plugin, definition, null);
    }

    public BukkitCommandInjectableBridge(final IBukkitCommandProcessor processor, final CommandManager commandManager,
        final MessageManager messageManager, final Plugin plugin, final CommandDefinition definition,
        final BiFunction<CommandSender, MessageManager, A> actorBuilder) {
        super(processor, commandManager, messageManager, actorBuilder);
        this.plugin = plugin;
        this.definition = definition;
        this.listener = processor.requiresListener() ? new BukkitCommandBridgeListener(commandManager, messageManager) : null;
    }

    @Override
    protected String getCommandName(final String label, final String[] args, final boolean isForSuggestions) {
        if (args.length == 0) {
            return isForSuggestions ? "" : fallbackCommand;
        }
        return args[0];
    }

    @Override
    protected String[] getCommandArguments(final String[] args, final boolean isForSuggestions) {
        if (args.length <= 1) {
            return EMPTY_ARGS;
        }
        final String[] newArgs = new String[args.length - 1];
        System.arraycopy(args, 1, newArgs, 0, newArgs.length);
        return newArgs;
    }

    /*
     * Override default behaviour
     */

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length == 0) {
            return Arrays.asList(commandManager.getCommandNames());
        }
        return super.onTabComplete(sender, command, label, args);
    }

    /*
     * Management
     */

    public BukkitCommandInjectableBridge<A> fallbackCommand(final String fallbackCommand) {
        this.fallbackCommand = fallbackCommand;
        return this;
    }

    public String fallbackCommand() {
        return fallbackCommand;
    }

    public CommandDefinition definition() {
        return definition;
    }

    public boolean injected() {
        return injected;
    }

    public BukkitCommandInjectableBridge<A> inject() {
        if (injected) {
            return this;
        }
        if (listener != null) {
            plugin.getServer().getPluginManager().registerEvents(listener, plugin);
        }
        final SimpleCommandMap commandMap = getCommandMap();
        final BukkitCommand pluginCommand = new BukkitCommand(definition.name(), plugin);
        pluginCommand.setAliases(new ArrayList<>(definition.aliases()));
        pluginCommand.executor(this);
        pluginCommand.completer(this);
        pluginCommand.setDescription(messageManager.translate(definition.description(), Actor.DEFAULT_LANGUAGE));
        commandMap.register(definition.prefix(), pluginCommand);
        return this;
    }

    public BukkitCommandInjectableBridge<A> uninject() {
        if (!injected) {
            return this;
        }
        if (listener != null) {
            HandlerList.unregisterAll(listener);
        }
        final SimpleCommandMap commandMap = getCommandMap();
        final Map<String, org.bukkit.command.Command> map = getCommands(commandMap);
        final ArrayList<String> names = new ArrayList<>();
        names.addAll(definition.aliases());
        names.add(definition.name());
        for (final String name : names) {
            org.bukkit.command.Command command = map.remove(name);
            if (command instanceof PluginCommand && ((PluginCommand) command).getPlugin().equals(plugin)) {
                command.unregister(commandMap);
            }
            command = map.remove(definition.prefix() + ':' + name);
            if (command != null) {
                command.unregister(commandMap);
            }
        }
        return this;
    }

}
