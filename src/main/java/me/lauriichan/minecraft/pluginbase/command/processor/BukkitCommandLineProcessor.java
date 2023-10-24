package me.lauriichan.minecraft.pluginbase.command.processor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import me.lauriichan.laylib.command.Actor;
import me.lauriichan.laylib.command.CommandManager;
import me.lauriichan.laylib.command.CommandProcess;
import me.lauriichan.laylib.command.Node;
import me.lauriichan.laylib.command.NodeAction;
import me.lauriichan.laylib.command.NodeArgument;
import me.lauriichan.laylib.command.NodeCommand;
import me.lauriichan.laylib.command.Suggestions;
import me.lauriichan.laylib.command.util.Triple;
import me.lauriichan.laylib.localization.Key;
import me.lauriichan.laylib.reflection.ClassUtil;
import me.lauriichan.minecraft.pluginbase.command.BukkitActor;
import me.lauriichan.minecraft.pluginbase.message.CommandManagerMessage;
import me.lauriichan.minecraft.pluginbase.util.StringReader;

final class BukkitCommandLineProcessor implements IBukkitCommandProcessor {

    private final int suggestionAmount;

    public BukkitCommandLineProcessor(final int suggestionAmount) {
        this.suggestionAmount = Math.max(1, suggestionAmount);
    }

    @Override
    public boolean requiresListener() {
        return false;
    }

    /*
     * Command execution
     */

    @Override
    public void onCommand(final BukkitActor<?> actor, final CommandManager commandManager, final String commandName,
        final String[] args) {
        final Triple<NodeCommand, Node, String> triple = commandManager.findNode(commandName, args);
        if (triple == null) {
            actor.sendTranslatedMessage("command.process.create.no-command", Key.of("label", commandName));
            return;
        }
        final NodeCommand command = triple.getA();
        if (command.isRestricted() && !actor.hasPermission(command.getPermission())) {
            actor.sendTranslatedMessage("command.process.not-permitted", Key.of("permission", command.getPermission()));
            return;
        }
        final Node node = triple.getB();
        final String commandPath = triple.getC().substring(1);
        if (node.getAction() == null) {
            actor.sendTranslatedMessage("command.process.create.no-action", Key.of("command", commandPath));
            return;
        }
        final NodeAction action = node.getAction();
        if (action.isRestricted() && !actor.hasPermission(action.getPermission())) {
            actor.sendTranslatedMessage("command.process.not-permitted", Key.of("permission", action.getPermission()));
            return;
        }
        final CommandProcess process = new CommandProcess(triple.getC(), action, command.getInstance());
        NodeArgument argument = process.findNext(actor);
        if (argument == null) {
            commandManager.executeProcess(actor, process);
            return;
        }
        final int space = countSpace(commandPath);
        int argIdx = 0;
        for (int index = 0; index < space; index++) {
            while (args[argIdx++].isEmpty()) {
            }
        }
        final StringBuilder string = new StringBuilder();
        for (int index = argIdx; index < args.length; index++) {
            string.append(args[index]);
            if (index + 1 != args.length) {
                string.append(' ');
            }
        }
        final StringReader reader = new StringReader(string.toString());
        String data;
        while (argument != null) {
            if (!reader.skipWhitespace().hasNext()) {
                if (argument.isOptional()) {
                    process.skip(actor);
                    argument = process.findNext(actor);
                    continue;
                }
                actor.sendTranslatedMessage(CommandManagerMessage.INPUT_FAILED.id(),
                    Key.of("argument.type", ClassUtil.getClassName(argument.getArgumentType())), Key.of("error", "Argument not provided!"));
                return;
            }
            data = reader.read();
            try {
                process.provide(actor, data);
            } catch (final IllegalArgumentException exp) {
                actor.sendTranslatedMessage(CommandManagerMessage.INPUT_FAILED.id(),
                    Key.of("argument.type", ClassUtil.getClassName(argument.getArgumentType())), Key.of("error", exp.getMessage()));
                return;
            }
            argument = process.findNext(actor);
        }
        commandManager.executeProcess(actor, process);
    }

    private int countSpace(final String path) {
        int count = 0;
        for (int i = 0; i < path.length(); i++) {
            if (path.charAt(i) == ' ') {
                count++;
            }
        }
        return count;
    }

    /*
     * Tab completion
     */

    @Override
    public List<String> onTabComplete(final BukkitActor<?> actor, final CommandManager commandManager, final String commandName,
        final String[] args) {
        if (commandName.isBlank() || args.length == 0) {
            return Arrays.asList(commandManager.getCommandNames());
        }
        final Triple<NodeCommand, Node, String> triple = commandManager.findNode(commandName, args);
        if (triple == null) {
            return Collections.emptyList();
        }
        final NodeCommand nodeCommand = triple.getA();
        if (nodeCommand.isRestricted() && actor.hasPermission(nodeCommand.getPermission())) {
            return Collections.emptyList();
        }
        String[] tmpPath = triple.getC().split(" ");
        final String[] path = new String[tmpPath.length - 1];
        System.arraycopy(tmpPath, 1, path, 0, path.length);
        tmpPath = null;
        if (path.length == args.length) {
            return Collections.singletonList(path[path.length - 1]);
        }
        final Node node = triple.getB();
        if (node.hasChildren() && args.length - path.length == 1) {
            return Arrays.asList(triple.getB().getNames());
        }
        final NodeAction action = node.getAction();
        if (action == null) {
            return Collections.emptyList();
        }
        final CommandProcess process = new CommandProcess(triple.getC(), action, nodeCommand.getInstance());
        NodeArgument argument = process.findNext(actor);
        if (argument == null) {
            return Collections.emptyList();
        }
        int argIdx = 0;
        for (int index = 0; index <= path.length; index++) {
            while (args[argIdx++].isEmpty() && argIdx < args.length) {
            }
        }
        final StringBuilder string = new StringBuilder();
        for (int index = argIdx; index < args.length; index++) {
            string.append(args[index]);
            if (index + 1 != args.length) {
                string.append(' ');
            }
        }
        final StringReader reader = new StringReader(string.toString());
        if (!reader.skipWhitespace().hasNext()) {
            return createSuggestions(actor, "", argument);
        }
        String data = "";
        while (argument != null) {
            if (!reader.hasNext()) {
                return Collections.singletonList(data);
            }
            if (!reader.skipWhitespace().hasNext()) {
                return createSuggestions(actor, "", argument);
            }
            data = reader.read();
            try {
                process.provide(actor, data);
            } catch (final IllegalArgumentException exp) {
                return createSuggestions(actor, data, argument);
            }
            argument = process.findNext(actor);
        }
        return Collections.emptyList();
    }

    private List<String> createSuggestions(final Actor<?> actor, final String data, final NodeArgument argument) {
        final Suggestions suggestions = new Suggestions();
        argument.getType().suggest(actor, data, suggestions);
        if (suggestions.hasSuggestions()) {
            final Map.Entry<String, Double>[] entries = suggestions.getSuggestions(suggestionAmount);
            final ArrayList<String> list = new ArrayList<>();
            for (final Map.Entry<String, Double> entry : entries) {
                list.add(entry.getKey());
            }
            return list;
        }
        return Collections.emptyList();
    }

}
