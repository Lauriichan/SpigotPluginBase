package me.lauriichan.minecraft.pluginbase.command.bridge;

import static me.lauriichan.minecraft.pluginbase.command.bridge.BukkitCommandReflection.getCommandMap;
import static me.lauriichan.minecraft.pluginbase.command.bridge.BukkitCommandReflection.getCommands;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.Plugin;

import me.lauriichan.laylib.command.Actor;
import me.lauriichan.laylib.command.CommandManager;
import me.lauriichan.laylib.command.ICommandInjector;
import me.lauriichan.laylib.command.NodeCommand;
import me.lauriichan.laylib.localization.MessageManager;
import me.lauriichan.minecraft.pluginbase.command.BukkitActor;
import me.lauriichan.minecraft.pluginbase.command.BukkitCommand;
import me.lauriichan.minecraft.pluginbase.command.processor.IBukkitCommandProcessor;

public final class BukkitCommandRedirectBridgeInjector<A extends BukkitActor<?>> implements ICommandInjector {

    private final Plugin plugin;
    private final BukkitCommandRedirectBridge<A> bridge;

    private final MessageManager messageManager;

    private final String prefix;

    public BukkitCommandRedirectBridgeInjector(final IBukkitCommandProcessor processor, final CommandManager commandManager,
        final MessageManager messageManager, final Plugin plugin) {
        this(processor, commandManager, messageManager, plugin, null);
    }

    public BukkitCommandRedirectBridgeInjector(final IBukkitCommandProcessor processor, final CommandManager commandManager,
        final MessageManager messageManager, final Plugin plugin,
        final BiFunction<CommandSender, MessageManager, A> actorBuilder) {
        this.plugin = Objects.requireNonNull(plugin);
        this.prefix = plugin.getName().toLowerCase(Locale.ROOT);
        this.bridge = new BukkitCommandRedirectBridge<>(processor, commandManager, messageManager, prefix, actorBuilder);
        if (processor.requiresListener()) {
            plugin.getServer().getPluginManager().registerEvents(new BukkitCommandBridgeListener(commandManager, messageManager), plugin);
        }
        this.messageManager = messageManager;
    }

    public BukkitCommandBridge<A> getBridge() {
        return bridge;
    }

    @Override
    public void inject(final NodeCommand nodeCommand) {
        final SimpleCommandMap commandMap = getCommandMap();
        final BukkitCommand pluginCommand = new BukkitCommand(nodeCommand.getName(), plugin);
        pluginCommand.setAliases(new ArrayList<>(nodeCommand.getAliases()));
        pluginCommand.executor(bridge);
        pluginCommand.completer(bridge);
        final String description = messageManager.translate(nodeCommand.getDescription(), Actor.DEFAULT_LANGUAGE);
        pluginCommand.setDescription(description == null ? nodeCommand.getDescription() : description);
        commandMap.register(prefix, pluginCommand);
    }

    @Override
    public void uninject(final NodeCommand nodeCommand) {
        final SimpleCommandMap commandMap = getCommandMap();
        final Map<String, Command> map = getCommands(commandMap);
        if (map == null) {
            return;
        }
        final ArrayList<String> names = new ArrayList<>();
        names.addAll(nodeCommand.getAliases());
        names.add(nodeCommand.getName());
        for (final String name : names) {
            org.bukkit.command.Command command = map.remove(name);
            if (command instanceof BukkitCommand && ((BukkitCommand) command).getPlugin().equals(plugin)) {
                command.unregister(commandMap);
            }
            command = map.remove(prefix + ':' + name);
            if (command != null) {
                command.unregister(commandMap);
            }
        }
    }

}