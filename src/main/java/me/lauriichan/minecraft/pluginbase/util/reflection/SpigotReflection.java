package me.lauriichan.minecraft.pluginbase.util.reflection;

import org.bukkit.Bukkit;

import me.lauriichan.minecraft.pluginbase.IBukkitReflection;

public final class SpigotReflection implements IBukkitReflection {

    public static final String PACKAGE_VERSION = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
    public static final String CRAFTBUKKIT_PACKAGE = String.format("org.bukkit.craftbukkit.%s.%s", PACKAGE_VERSION, "%s");

    private static final String MINECRAFT_PACKAGE_PRE_MAPPED = String.format("net.minecraft.server.%s.%s", PACKAGE_VERSION, "%s");
    private static final String MINECRAFT_PACKAGE_POST_MAPPED = "net.minecraft.%s";

    public static final boolean IS_PRE_MAPPED = isMinecraftPackagePreMapped();
    public static final String MINECRAFT_PACKAGE = IS_PRE_MAPPED ? MINECRAFT_PACKAGE_PRE_MAPPED : MINECRAFT_PACKAGE_POST_MAPPED;

    private static boolean isMinecraftPackagePreMapped() {
        try {
            Class.forName(String.format(MINECRAFT_PACKAGE_PRE_MAPPED, "MinecraftServer"));
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public String createCraftBukkitPath(String path) {
        return String.format(CRAFTBUKKIT_PACKAGE, path);
    }

    @Override
    public String createMinecraftPath(String path) {
        if (IS_PRE_MAPPED) {
            int index = path.lastIndexOf('.');
            if (index != -1) {
                path = path.substring(index, path.length());
            }
        }
        return String.format(MINECRAFT_PACKAGE, path);
    }

}
