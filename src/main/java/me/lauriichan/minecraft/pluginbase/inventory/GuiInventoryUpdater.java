package me.lauriichan.minecraft.pluginbase.inventory;

import org.bukkit.event.inventory.InventoryType;

public final class GuiInventoryUpdater implements IGuiInventoryUpdater {

    private final GuiInventory inventory;

    private ChestSize size;
    private InventoryType type;

    private String title;

    public GuiInventoryUpdater(final GuiInventory inventory) {
        this.inventory = inventory;
        this.size = inventory.getChestSize();
        this.type = inventory.getType();
        this.title = inventory.getTitle();
    }

    @Override
    public String title() {
        return title;
    }

    @Override
    public GuiInventoryUpdater title(String title) {
        this.title = title;
        return this;
    }

    @Override
    public ChestSize chestSize() {
        return size;
    }

    @Override
    public GuiInventoryUpdater chestSize(ChestSize size) {
        this.size = size;
        this.type = null;
        return this;
    }

    @Override
    public InventoryType type() {
        return type;
    }

    @Override
    public GuiInventoryUpdater type(InventoryType type) {
        this.size = null;
        this.type = type;
        return this;
    }

    @Override
    public boolean apply() {
        return inventory.apply(this);
    }

}
