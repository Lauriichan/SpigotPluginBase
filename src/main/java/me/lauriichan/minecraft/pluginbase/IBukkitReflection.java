package me.lauriichan.minecraft.pluginbase;

import me.lauriichan.laylib.reflection.ClassUtil;

public interface IBukkitReflection {
    
    String createCraftBukkitPath(String path);
    
    default Class<?> findCraftBukkitClass(String path) {
        return ClassUtil.findClass(createCraftBukkitPath(path));
    }
    
    String createMinecraftPath(String path);
    
    default Class<?> findMinecraftClass(String path) {
        return ClassUtil.findClass(createMinecraftPath(path));
    }

}
