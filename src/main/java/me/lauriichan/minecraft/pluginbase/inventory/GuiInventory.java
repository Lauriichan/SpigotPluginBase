package me.lauriichan.minecraft.pluginbase.inventory;

import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import me.lauriichan.minecraft.pluginbase.util.BukkitColor;
import me.lauriichan.minecraft.pluginbase.util.attribute.Attributable;

public final class GuiInventory extends Attributable implements InventoryHolder, IGuiInventory {

    private static final HumanEntity[] EMPTY_ENTITIES = {};

    private final ThreadLocal<Boolean> inventoryChanged = ThreadLocal.withInitial(() -> false);

    private volatile IHandler handler;
    private volatile Inventory inventory;

    private String title;

    private int rowSize;
    private int columnAmount;
    private int size;

    private ChestSize chestSize;
    private InventoryType type;

    public GuiInventory(final String title, final InventoryType type) {
        this.type = Objects.requireNonNull(type) == InventoryType.ENDER_CHEST ? InventoryType.CHEST : type;
        if (!type.isCreatable()) {
            throw new IllegalArgumentException("InventoryType '" + type.name() + "' is not creatable!");
        }
        this.title = Objects.requireNonNull(title);
        this.size = type.getDefaultSize();
        this.rowSize = IGuiInventory.getRowSize(type);
        this.columnAmount = size / rowSize;
        this.chestSize = rowSize == 9 ? ChestSize.values()[columnAmount - 1] : null;
        internalUpdate();
    }

    public GuiInventory(final String title, final ChestSize chestSize) {
        this.title = Objects.requireNonNull(title);
        this.chestSize = Objects.requireNonNull(chestSize);
        this.type = InventoryType.CHEST;
        this.size = chestSize.inventorySize();
        this.rowSize = 9;
        this.columnAmount = size / rowSize;
        internalUpdate();
    }

    @Override
    public boolean hasInventoryChanged() {
        return inventoryChanged.get();
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public IHandler getHandler() {
        return handler;
    }

    @Override
    public boolean setHandler(final IHandler handler) {
        if (handler == null || this.handler == handler) {
            return false;
        }
        this.handler = handler;
        handler.onInit(this);
        return true;
    }

    @Override
    public boolean setType(final InventoryType type) {
        if (type == null || this.type == type || !type.isCreatable()) {
            return false;
        }
        this.type = type == InventoryType.ENDER_CHEST ? InventoryType.CHEST : type;
        this.size = type.getDefaultSize();
        this.rowSize = IGuiInventory.getRowSize(type);
        this.columnAmount = size / rowSize;
        this.chestSize = rowSize == 9 ? ChestSize.values()[columnAmount - 1] : null;
        internalUpdate();
        return true;
    }

    @Override
    public InventoryType getType() {
        return type;
    }

    @Override
    public boolean setChestSize(final ChestSize chestSize) {
        if (chestSize == null || this.chestSize == chestSize) {
            return false;
        }
        this.chestSize = chestSize;
        this.size = chestSize.inventorySize();
        this.rowSize = 9;
        this.columnAmount = size / rowSize;
        this.type = InventoryType.CHEST;
        internalUpdate();
        return true;
    }

    @Override
    public ChestSize getChestSize() {
        return chestSize;
    }

    @Override
    public boolean setTitle(final String title) {
        if (title == null || this.title.equals(title)) {
            return false;
        }
        this.title = title;
        internalUpdate();
        return true;
    }

    @Override
    public String getTitle() {
        return title;
    }

    private void internalUpdate() {
        HumanEntity[] entities = EMPTY_ENTITIES;
        if (inventory != null) {
            entities = inventory.getViewers().toArray(HumanEntity[]::new);
        }
        inventory = chestSize != null ? Bukkit.createInventory(this, chestSize.inventorySize(), BukkitColor.apply(title))
            : Bukkit.createInventory(this, type, BukkitColor.apply(title));
        inventoryChanged.set(true);
        try {
            for (final HumanEntity entity : entities) {
                entity.closeInventory();
                entity.openInventory(inventory);
            }
        } finally {
            inventoryChanged.set(false);
        }
        if (handler != null) {
            handler.onUpdate(this, true);
        }
    }

    @Override
    public void update() {
        if (handler != null) {
            handler.onUpdate(this, false);
        }
    }

    @Override
    public int getRowSize() {
        return rowSize;
    }

    @Override
    public int getColumnAmount() {
        return columnAmount;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void clear() {
        inventory.clear();
    }

    @Override
    public ItemStack get(final int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException(index);
        }
        final ItemStack stack = inventory.getItem(index);
        if (stack != null && stack.getType().isAir()) {
            return null;
        }
        return stack;
    }

    @Override
    public void set(final int index, final ItemStack itemStack) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException(index);
        }
        inventory.setItem(index, itemStack);
    }

}