package me.lauriichan.minecraft.pluginbase;

import org.bukkit.plugin.Plugin;

public interface IBasePluginAccess extends Plugin {
    
    BasePlugin<?> base();

}
