package me.lauriichan.minecraft.pluginbase.inventory;

import java.util.Objects;

import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public final class WrappedInventory implements IGuiInventory {
    
    private static final ChestSize[] SIZES = ChestSize.values();

    private final Inventory inventory;

    private final ChestSize chestSize;

    private final int columnAmount, rowAmount;

    public WrappedInventory(final Inventory inventory) {
        this.inventory = Objects.requireNonNull(inventory);
        this.columnAmount = IGuiInventory.getColumnAmount(inventory.getType());
        this.rowAmount = inventory.getSize() / columnAmount;
        this.chestSize = columnAmount == 9 && rowAmount < 7 ? SIZES[rowAmount - 1] : null;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public IGuiInventoryUpdater updater() {
        return NOPInventoryUpdater.NOP;
    }

    @Override
    public IHandler getHandler() {
        return null;
    }

    @Override
    public boolean setHandler(final IHandler handler) {
        return false;
    }

    @Override
    public boolean setTitle(final String title) {
        return false;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public boolean setChestSize(final ChestSize chestSize) {
        return false;
    }

    @Override
    public ChestSize getChestSize() {
        return chestSize;
    }

    @Override
    public boolean setType(final InventoryType type) {
        return false;
    }

    @Override
    public InventoryType getType() {
        return inventory.getType();
    }

    @Override
    public void update() {}

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
        return inventory.getSize();
    }

    @Override
    public void clear() {
        inventory.clear();
    }

    @Override
    public ItemStack getItem(final int index) {
        if (index < 0 || index >= inventory.getSize()) {
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
        if (index < 0 || index >= inventory.getSize()) {
            throw new IndexOutOfBoundsException(index);
        }
        inventory.setItem(index, itemStack);
    }
    
    @Override
    public void clear(int index) throws IndexOutOfBoundsException {
        if (index < 0 || index >= inventory.getSize()) {
            throw new IndexOutOfBoundsException(index);
        }
        inventory.clear(index);
    }

}