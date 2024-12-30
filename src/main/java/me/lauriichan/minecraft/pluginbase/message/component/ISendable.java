package me.lauriichan.minecraft.pluginbase.message.component;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.lauriichan.laylib.command.Actor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;

public interface ISendable {

    default void send(final Actor<?> actor) {
        final Actor<CommandSender> sender = actor.as(CommandSender.class);
        if (!actor.isValid()) {
            return;
        }
        send(sender.getHandle());
    }

    default void send(final Actor<?> actor, final ChatMessageType type) {
        final Actor<Player> sender = actor.as(Player.class);
        if (!actor.isValid()) {
            return;
        }
        send(sender.getHandle(), type);
    }

    default void send(final CommandSender sender) {
        sender.spigot().sendMessage(buildComponentArray());
    }

    default void send(final Player player, final ChatMessageType type) {
        player.spigot().sendMessage(type, buildComponentArray());
    }

    default void sendConsole() {
        send(Bukkit.getConsoleSender());
    }

    default void sendBroadcast(final World world) {
        sendBroadcast(world, ChatMessageType.CHAT);
    }

    default void sendBroadcast(final World world, final ChatMessageType type) {
        final BaseComponent[] message = buildComponentArray();
        for (final Player player : world.getPlayers()) {
            player.spigot().sendMessage(type, message);
        }
    }

    default void sendBroadcast(final World world, final String permission) {
        sendBroadcast(world, ChatMessageType.CHAT, permission, true);
    }

    default void sendBroadcast(final World world, final ChatMessageType type, final String permission) {
        sendBroadcast(world, type, permission, true);
    }

    default void sendBroadcast(final World world, final String permission, final boolean permissionValue) {
        sendBroadcast(world, ChatMessageType.CHAT, permission, permissionValue);
    }

    default void sendBroadcast(final World world, final ChatMessageType type, final String permission, final boolean permissionValue) {
        final BaseComponent[] message = buildComponentArray();
        for (final Player player : world.getPlayers()) {
            if (player.hasPermission(permission) != permissionValue) {
                continue;
            }
            player.spigot().sendMessage(type, message);
        }
    }

    default void sendBroadcast() {
        BaseComponent[] components = buildComponentArray();
        Bukkit.getConsoleSender().spigot().sendMessage(components);
        Bukkit.getServer().spigot().broadcast(components);
    }

    default void sendBroadcast(String permission) {
        sendBroadcast(permission, true);
    }

    default void sendBroadcast(String permission, boolean permissionValue) {
        BaseComponent[] components = buildComponentArray();
        Bukkit.getConsoleSender().spigot().sendMessage(components);
        for (final Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission(permission) != permissionValue) {
                continue;
            }
            player.spigot().sendMessage(components);
        }
    }

    BaseComponent[] buildComponentArray();

}
