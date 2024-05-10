package me.lauriichan.minecraft.pluginbase.command.bridge;

import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import me.lauriichan.laylib.command.CommandManager;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.command.BukkitActor;
import me.lauriichan.minecraft.pluginbase.command.processor.IBukkitCommandProcessor;

public abstract class BukkitCommandBridge<A extends BukkitActor<?>> implements CommandExecutor, TabCompleter {

    protected final IBukkitCommandProcessor processor;

    protected final CommandManager commandManager;
    protected final BasePlugin<?> plugin;

    private final BiFunction<CommandSender, BasePlugin<?>, A> actorBuilder;

    public BukkitCommandBridge(final IBukkitCommandProcessor processor, final CommandManager commandManager,
        final BasePlugin<?> plugin, final BiFunction<CommandSender, BasePlugin<?>, A> actorBuilder) {
        this.processor = Objects.requireNonNull(processor);
        this.commandManager = Objects.requireNonNull(commandManager);
        this.plugin = Objects.requireNonNull(plugin);
        this.actorBuilder = Objects.requireNonNull(actorBuilder, "Need Actor builder!");
    }

    public final IBukkitCommandProcessor getProcessor() {
        return processor;
    }

    public final CommandManager getCommandManager() {
        return commandManager;
    }

    public final BasePlugin<?> getMessageManager() {
        return plugin;
    }

    public final BiFunction<CommandSender, BasePlugin<?>, A> getActorBuilder() {
        return actorBuilder;
    }

    protected abstract String getCommandName(String label, String[] args, boolean isForSuggestions);

    protected abstract String[] getCommandArguments(String[] args, boolean isForSuggestions);

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        processor.onCommand(actorBuilder.apply(sender, plugin), commandManager, getCommandName(label, args, false),
            getCommandArguments(args, false));
        return true;
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
        return processor.onTabComplete(actorBuilder.apply(sender, plugin), commandManager, getCommandName(label, args, true),
            getCommandArguments(args, true), null);
    }

    public List<String> onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args, final Location location) {
        return processor.onTabComplete(actorBuilder.apply(sender, plugin), commandManager, getCommandName(label, args, true),
            getCommandArguments(args, true), location);
    }

}
