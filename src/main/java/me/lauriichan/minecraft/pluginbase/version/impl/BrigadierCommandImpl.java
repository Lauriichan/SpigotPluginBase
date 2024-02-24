package me.lauriichan.minecraft.pluginbase.version.impl;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.logging.Level;

import org.bukkit.Server;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import me.lauriichan.minecraft.pluginbase.command.processor.IBrigadierTabCompleter;
import me.lauriichan.minecraft.pluginbase.version.api.IBrigadierCommand;
import net.minecraft.commands.CommandListenerWrapper;

public class BrigadierCommandImpl implements Command<CommandListenerWrapper>, Predicate<CommandListenerWrapper>,
    SuggestionProvider<CommandListenerWrapper>, IBrigadierCommand {

    private final Server server;
    private final org.bukkit.command.PluginCommand command;

    public BrigadierCommandImpl(Server server, org.bukkit.command.PluginCommand command) {
        this.server = server;
        this.command = command;
    }

    @Override
    public boolean test(CommandListenerWrapper wrapper) {
        return command.testPermissionSilent(wrapper.getBukkitSender());
    }

    @Override
    public int run(CommandContext<CommandListenerWrapper> context) throws CommandSyntaxException {
        CommandSender sender = context.getSource().getBukkitSender();

        try {
            String input = context.getInput().substring(1);
            String[] rawArgs = input.split(" ");

            String[] newArgs = new String[rawArgs.length - 1];
            if (newArgs.length != 0) {
                System.arraycopy(rawArgs, 1, newArgs, 0, newArgs.length);
            }
            return command.execute(sender, rawArgs[0], newArgs) ? 1 : 0;
        } catch (CommandException ex) {
            sender.sendMessage(org.bukkit.ChatColor.RED + "An internal error occurred while attempting to perform this command");
            server.getLogger().log(Level.SEVERE, null, ex);
            return 0;
        }
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandListenerWrapper> context, SuggestionsBuilder builder)
        throws CommandSyntaxException {
        TabCompleter completer = command.getTabCompleter();
        if (completer == null) {
            return builder.buildFuture();
        }
        
        if (completer instanceof IBrigadierTabCompleter brigadierCompleter) {
            BrigadierSuggestions suggestions = new BrigadierSuggestions(builder.getInput());
            
            return suggestions.buildFuture();
        }

        // Defaults to sub nodes, but we have just one giant args node, so offset accordingly
        builder = builder.createOffset(builder.getInput().lastIndexOf(' ') + 1);

        for (String s : results) {
            builder.suggest(s);
        }

        return builder.buildFuture();
    }

}
