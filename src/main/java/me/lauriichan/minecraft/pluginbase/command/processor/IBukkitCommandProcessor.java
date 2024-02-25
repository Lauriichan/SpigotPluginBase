package me.lauriichan.minecraft.pluginbase.command.processor;

import java.util.List;

import org.bukkit.Location;

import me.lauriichan.laylib.command.CommandManager;
import me.lauriichan.minecraft.pluginbase.command.BukkitActor;

public interface IBukkitCommandProcessor {

    IBukkitCommandProcessor CHAT = new BukkitChatProcessor();
    IBukkitCommandProcessor COMMAND_LINE_DEFAULT = new BukkitCommandLineProcessor(100);

    static IBukkitCommandProcessor chat() {
        return CHAT;
    }

    static IBukkitCommandProcessor commandLine() {
        return COMMAND_LINE_DEFAULT;
    }

    static IBukkitCommandProcessor commandLine(final int suggestionAmount) {
        return new BukkitCommandLineProcessor(suggestionAmount);
    }

    boolean requiresListener();

    void onCommand(BukkitActor<?> actor, CommandManager commandManager, String commandName, String[] args);

    List<String> onTabComplete(BukkitActor<?> actor, CommandManager commandManager, String commandName, String[] args, Location location);

}
