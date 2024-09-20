package me.lauriichan.minecraft.pluginbase.command.bridge;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;

import me.lauriichan.laylib.reflection.JavaLookup;
import me.lauriichan.minecraft.pluginbase.BasePlugin;

final class BukkitCommandReflection {

    private static final Class<?> CRAFT_SERVER = BasePlugin.getBasePlugin().bukkitReflection()
        .findCraftBukkitClass("CraftServer");

    private static final MethodHandle GET_COMMAND_MAP = JavaLookup.PLATFORM.findMethod(CRAFT_SERVER, "getCommandMap", MethodType.methodType(SimpleCommandMap.class));
    private static final MethodHandle KNOWN_COMMANDS_GETTER = JavaLookup.PLATFORM.findGetter(SimpleCommandMap.class, "knownCommands", Map.class);

    private BukkitCommandReflection() {
        throw new UnsupportedOperationException();
    }

    /*
     * Implementation
     */

    public static SimpleCommandMap getCommandMap() {
        try {
            return (SimpleCommandMap) GET_COMMAND_MAP.invoke(Bukkit.getServer());
        } catch (final Throwable e) {
            return null;
        }
    }

    public static Map<String, Command> getCommands(final SimpleCommandMap map) {
        try {
            return (Map<String, Command>) KNOWN_COMMANDS_GETTER.invoke(map);
        } catch (final Throwable e) {
            return null;
        }
    }

}
