package me.lauriichan.minecraft.pluginbase.version.api;

import org.bukkit.Server;
import org.bukkit.command.Command;

public interface IVersionHelper {
    
    IBrigadierCommand createCommand(Server server, Command command);

}
