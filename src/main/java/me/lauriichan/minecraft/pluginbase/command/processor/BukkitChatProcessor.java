package me.lauriichan.minecraft.pluginbase.command.processor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.Location;

import me.lauriichan.laylib.command.CommandManager;
import me.lauriichan.laylib.command.Node;
import me.lauriichan.laylib.command.NodeCommand;
import me.lauriichan.laylib.command.util.Triple;
import me.lauriichan.minecraft.pluginbase.command.BukkitActor;

final class BukkitChatProcessor implements IBukkitCommandProcessor {

    @Override
    public boolean requiresListener() {
        return true;
    }

    @Override
    public void onCommand(final BukkitActor<?> actor, final CommandManager commandManager, final String commandName,
        final String[] args) {
        commandManager.createProcess(actor, commandName, args);
    }

    @Override
    public List<String> onTabComplete(final BukkitActor<?> actor, final CommandManager commandManager, final String commandName,
        final String[] args, final Location location) {
        final Triple<NodeCommand, Node, String> triple = commandManager.findNode(commandName, args);
        if (triple == null || !triple.getB().hasChildren()) {
            return Collections.emptyList();
        }
        final String[] path = triple.getC().split(" ");
        if (path.length == args.length) {
            return Collections.singletonList(path[path.length - 1]);
        }
        return Arrays.asList(triple.getB().getNames());
    }

}
