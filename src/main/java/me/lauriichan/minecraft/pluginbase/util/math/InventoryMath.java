package me.lauriichan.minecraft.pluginbase.util.math;

public final class InventoryMath {
    
    public static final int DEFAULT_COLUMN_AMOUNT = 9;

    private InventoryMath() {
        throw new UnsupportedOperationException();
    }

    public static record Slot(int row, int column) {

        public int toSlot(int columnAmount) {
            return InventoryMath.toSlot(column, row, columnAmount);
        }

        public int slotInBounds(int columnAmount, int size) {
            return InventoryMath.slotInBounds(column, row, columnAmount, size);
        }

        public boolean isValid(int columnAmount, int size) {
            return InventoryMath.isValid(column, row, columnAmount, size);
        }

    }

    public static Slot of(int slot, int columnAmount) {
        int column = slot % columnAmount;
        int row = Math.floorDiv(slot - column, columnAmount);
        return new Slot(row, column);
    }

    public static int toSlot(int row, int column, int columnAmount) {
        return row * columnAmount + column;
    }
    
    public static boolean isValid(int row, int column, int columnAmount, int size) {
        return toSlot(row, column, columnAmount) < size;
    }
    
    public static int slotInBounds(int row, int column, int columnAmount, int size) {
        int slot = toSlot(row, column, columnAmount);
        if (slot < size) {
            return slot;
        }
        throw new IndexOutOfBoundsException(slot);
    }

}
