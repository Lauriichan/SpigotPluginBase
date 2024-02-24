package me.lauriichan.minecraft.pluginbase.command.processor;

import me.lauriichan.laylib.command.CommandManager;
import me.lauriichan.minecraft.pluginbase.command.BukkitActor;
import me.lauriichan.minecraft.pluginbase.version.api.IBrigadierSuggestions;

public interface IBrigadierTabCompleter {
    
    void onTabComplete(IBrigadierSuggestions suggestions, BukkitActor<?> actor, CommandManager commandManager, String commandName, String[] args);

}
