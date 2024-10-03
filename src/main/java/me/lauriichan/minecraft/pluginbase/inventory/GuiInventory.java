package me.lauriichan.minecraft.pluginbase.inventory;

import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import me.lauriichan.minecraft.pluginbase.util.attribute.Attributable;
import me.lauriichan.minecraft.pluginbase.util.color.BukkitColor;

public final class GuiInventory extends Attributable implements InventoryHolder, IGuiInventory {

    private static final ChestSize[] SIZES = ChestSize.values();
    private static final HumanEntity[] EMPTY_ENTITIES = {};

    private final ThreadLocal<Boolean> inventoryChanged = ThreadLocal.withInitial(() -> false);

    private volatile IHandler handler;
    private volatile Inventory inventory;

    private String title;
    private boolean updateTitleOnOpen = false;

    private int columnAmount, rowAmount;
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
        this.columnAmount = IGuiInventory.getColumnAmount(inventory.getType());
        this.rowAmount = inventory.getSize() / columnAmount;
        this.chestSize = columnAmount == 9 && rowAmount < 7 ? SIZES[rowAmount - 1] : null;
        internalUpdate(false);
    }

    public GuiInventory(final String title, final ChestSize chestSize) {
        this.title = Objects.requireNonNull(title);
        this.chestSize = Objects.requireNonNull(chestSize);
        this.type = InventoryType.CHEST;
        this.size = chestSize.inventorySize();
        this.columnAmount = 9;
        this.rowAmount = size / columnAmount;
        internalUpdate(false);
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
    public IGuiInventoryUpdater updater() {
        return new GuiInventoryUpdater(this);
    }

    final boolean apply(GuiInventoryUpdater updater) {
        boolean title = applyTitle(updater.title());
        boolean type = applyChestSize(updater.chestSize()) || applyType(updater.type());
        if (!title && !type) {
            return false;
        }
        internalUpdate(!type);
        return true;
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
        handler.onSet(this);
        return true;
    }

    @Override
    public boolean setType(final InventoryType type) {
        if (!applyType(type)) {
            return false;
        }
        internalUpdate(false);
        return true;
    }

    private boolean applyType(InventoryType type) {
        if (type == null || this.type == type || !type.isCreatable()) {
            return false;
        }
        this.type = type == InventoryType.ENDER_CHEST ? InventoryType.CHEST : type;
        this.size = type.getDefaultSize();
        this.columnAmount = IGuiInventory.getColumnAmount(type);
        this.rowAmount = size / columnAmount;
        this.chestSize = columnAmount == 9 && rowAmount < 7 ? SIZES[rowAmount - 1] : null;
        return true;
    }

    @Override
    public InventoryType getType() {
        return type;
    }

    @Override
    public boolean setChestSize(final ChestSize chestSize) {
        if (!applyChestSize(chestSize)) {
            return false;
        }
        internalUpdate(false);
        return true;
    }

    private boolean applyChestSize(final ChestSize chestSize) {
        if (chestSize == null || this.chestSize == chestSize) {
            return false;
        }
        this.chestSize = chestSize;
        this.size = chestSize.inventorySize();
        this.columnAmount = IGuiInventory.getColumnAmount(inventory.getType());
        this.rowAmount = inventory.getSize() / columnAmount;
        this.type = InventoryType.CHEST;
        return true;
    }

    @Override
    public ChestSize getChestSize() {
        return chestSize;
    }

    @Override
    public boolean setTitle(final String title) {
        if (!applyTitle(title)) {
            return false;
        }
        internalUpdate(true);
        return true;
    }

    private boolean applyTitle(String title) {
        if (title == null || this.title.equals(title)) {
            return false;
        }
        this.title = title;
        return true;
    }

    @Override
    public String getTitle() {
        return title;
    }

    private void internalUpdate(boolean titleOnlyChanged) {
        updateTitleOnOpen = titleOnlyChanged;
        HumanEntity[] entities = EMPTY_ENTITIES;
        if (inventory != null) {
            entities = inventory.getViewers().toArray(HumanEntity[]::new);
            if (titleOnlyChanged) {
                String updatedTitle = BukkitColor.apply(title);
                for (HumanEntity entity : entities) {
                    GuiInventoryReflection.updateTitle(entity, inventory, updatedTitle);
                }
                return;
            }
        }
        if (chestSize != null) {
            inventory = Bukkit.createInventory(this, chestSize.inventorySize(), BukkitColor.apply(title));
        } else {
            inventory = Bukkit.createInventory(this, type, BukkitColor.apply(title));
        }
        inventoryChanged.set(true);
        try {
            for (final HumanEntity entity : entities) {
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
    public void open(HumanEntity entity) {
        Inventory inv = inventory;
        entity.openInventory(inv);
        if (handler != null && inv != inventory) {
            inventoryChanged.set(true);
            try {
                entity.closeInventory();
                entity.openInventory(inventory);
            } finally {
                inventoryChanged.set(false);
            }
            handler.onUpdate(this, false);
        }
        if (updateTitleOnOpen) {
            GuiInventoryReflection.updateTitle(entity, inv, title);
        }
    }

    @Override
    public int getColumnAmount() {
        return columnAmount;
    }

    @Override
    public int getRowAmount() {
        return rowAmount;
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
    public ItemStack getItem(final int index) {
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

    @Override
    public void clear(int index) throws IndexOutOfBoundsException {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException(index);
        }
        inventory.clear(index);
    }

}