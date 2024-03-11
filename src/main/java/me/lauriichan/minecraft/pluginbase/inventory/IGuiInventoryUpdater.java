package me.lauriichan.minecraft.pluginbase.inventory;

import org.bukkit.event.inventory.InventoryType;

public interface IGuiInventoryUpdater {

    String title();

    IGuiInventoryUpdater title(String title);

    ChestSize chestSize();

    IGuiInventoryUpdater chestSize(ChestSize size);

    InventoryType type();

    IGuiInventoryUpdater type(InventoryType type);

    /**
     * Applies the update as specified
     * 
     * @return {@code true} if the update was executed otherwise {@code false}
     */
    boolean apply();

}
