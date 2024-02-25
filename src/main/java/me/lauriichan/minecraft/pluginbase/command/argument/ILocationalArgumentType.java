package me.lauriichan.minecraft.pluginbase.command.argument;

import org.bukkit.Location;

import me.lauriichan.laylib.command.Actor;
import me.lauriichan.laylib.command.IArgumentType;
import me.lauriichan.laylib.command.Suggestions;

public interface ILocationalArgumentType<E> extends IArgumentType<E> {

    default void suggest(Actor<?> actor, String input, Suggestions suggestions, Location location) {}

}
