package me.lauriichan.minecraft.pluginbase.message.component.helper;

import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.hover.content.Entity;
import net.md_5.bungee.api.chat.hover.content.Item;

public abstract class ComponentHelper {

    private static volatile ComponentHelper instance;
    
    public static ComponentHelper get() {
        return instance;
    }
    
    public ComponentHelper() {
        if (instance != null) {
            throw new UnsupportedOperationException();
        }
        instance = this;
    }
    
    public abstract ChatColor transform(ChatColor color);
    
    public abstract Item hoverItem(ItemStack itemStack);
    
    public abstract Entity hoverEntity(org.bukkit.entity.Entity entity);
    
    public abstract void send(CommandSender sender, BaseComponent[] components);
    
    public abstract void send(CommandSender sender, ChatMessageType type, BaseComponent[] components);
    
}
