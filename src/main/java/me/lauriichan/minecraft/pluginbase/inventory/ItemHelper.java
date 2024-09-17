package me.lauriichan.minecraft.pluginbase.inventory;

import org.bukkit.Material;

public final class ItemHelper {
    
    private ItemHelper() {
        throw new UnsupportedOperationException();
    }
    
    public static boolean isAir(Material material) {
        return material.name().endsWith("_AIR") || material == Material.AIR;
    }

}
