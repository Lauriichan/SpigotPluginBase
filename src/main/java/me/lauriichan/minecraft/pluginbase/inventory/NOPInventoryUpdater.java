package me.lauriichan.minecraft.pluginbase.inventory;

import org.bukkit.event.inventory.InventoryType;

final class NOPInventoryUpdater implements IGuiInventoryUpdater {
    
    public static final NOPInventoryUpdater NOP = new NOPInventoryUpdater();
    
    private NOPInventoryUpdater() {}

    @Override
    public String title() {
        return "";
    }

    @Override
    public IGuiInventoryUpdater title(String title) {
        return this;
    }

    @Override
    public ChestSize chestSize() {
        return ChestSize.GRID_3x9;
    }

    @Override
    public IGuiInventoryUpdater chestSize(ChestSize size) {
        return this;
    }

    @Override
    public InventoryType type() {
        return InventoryType.CHEST;
    }

    @Override
    public IGuiInventoryUpdater type(InventoryType type) {
        return this;
    }

    @Override
    public boolean apply() {
        return false;
    }

    
    
}
