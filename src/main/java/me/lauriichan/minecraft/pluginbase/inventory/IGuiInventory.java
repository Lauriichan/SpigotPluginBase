package me.lauriichan.minecraft.pluginbase.inventory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.lauriichan.minecraft.pluginbase.inventory.item.ItemEditor;
import me.lauriichan.minecraft.pluginbase.util.attribute.IAttributable;
import me.lauriichan.minecraft.pluginbase.util.math.InventoryMath;

public interface IGuiInventory extends IAttributable {

    static int getColumnAmount(final InventoryType type) {
        switch (type) {
        case DISPENSER:
        case DROPPER:
            return 3;
        case HOPPER:
            return 5;
        case CHEST:
        case ENDER_CHEST:
            return 9;
        default:
            return 1;
        }
    }

    /**
     * Gets the bukkit inventory
     * 
     * @return the bukkit {@link Inventory}
     */
    Inventory getInventory();

    /**
     * Gets an inventory updater
     * 
     * @return a new inventory updater instance
     */
    IGuiInventoryUpdater updater();

    /**
     * Checks if an entity with the specified is viewing this inventory
     * 
     * @param  uniqueId the id of the entity in question
     * 
     * @return          @{code true} if an entity with the specified id is viewing
     *                      the inventory otherwise @{code false}
     */
    default boolean isViewing(final UUID uniqueId) {
        final Inventory inventory = getInventory();
        if (inventory == null) {
            return false;
        }
        return inventory.getViewers().stream().anyMatch(entity -> entity.getUniqueId().equals(uniqueId));
    }

    /**
     * Opens the inventory for the specified {@link HumanEntity}
     * 
     * @param entity the entity to open the inventory for
     */
    default void open(final HumanEntity entity) {
        entity.openInventory(getInventory());
    }

    /**
     * Checks if the underlying inventory changed
     * 
     * @return @{code true} if the underlying inventory changed otherwise @{code
     *             false}
     */
    default boolean hasInventoryChanged() {
        return false;
    }

    /**
     * Gets the currently set handler for this inventory
     * 
     * @return the {@link IHandler} if a handler was already set otherwise
     *             {@code null}
     */
    IHandler getHandler();

    /**
     * Checks if this inventory has a handler
     * 
     * @return {@code true} if the inventory has a handler otherwise {@code false}
     */
    default boolean hasHandler() {
        return getHandler() != null;
    }

    /**
     * Sets the handler for this inventory
     * 
     * @param  handler the handler to set
     * 
     * @return         {@code true} if the handler was updated otherwise
     *                     {@code false}
     */
    boolean setHandler(IHandler handler);

    /**
     * Sets the title of the inventory
     * 
     * @param  title the title to set
     * 
     * @return       {@code true} if the inventory was changed otherwise
     *                   {@code false}
     */
    boolean setTitle(String title);

    /**
     * Gets the title of the inventory
     * 
     * @return the title of the inventory or {@code null} if unsupported
     */
    String getTitle();

    /**
     * Sets the generic chest size of the inventory
     * 
     * @param  chestSize the chest size to set
     * 
     * @return           {@code true} if the inventory was changed otherwise
     *                       {@code false}
     */
    boolean setChestSize(ChestSize chestSize);

    /**
     * Gets the chest size of the inventory
     * 
     * @return the {@link ChestSize} or {@code null} if the inventory is not a
     *             generic chest-like container
     */
    ChestSize getChestSize();

    /**
     * Sets the type of the inventory
     * 
     * @param  type the inventory type to set
     * 
     * @return      {@code true} if the inventory was changed otherwise
     *                  {@code false}
     */
    boolean setType(InventoryType type);

    /**
     * Gets the type of the inventory
     * 
     * @return the type of the inventory
     */
    InventoryType getType();

    /**
     * Triggers an handler update for this inventory
     */
    void update();

    /**
     * Gets the amount of columns that the inventory has
     * 
     * @return the amount of columns
     */
    int getColumnAmount();

    /**
     * Gets the amount of rows that the inventory has
     * 
     * @return the amount of rows
     */
    int getRowAmount();

    /**
     * Gets the slot amount of the inventory
     * 
     * @return the slot amount
     */
    int size();

    /**
     * Clears all items from all slots
     */
    void clear();

    /**
     * Clears the item of a slot
     * 
     * @param  index                     the slot index
     * 
     * @throws IndexOutOfBoundsException if slot index is out of bounds
     */
    void clear(int index) throws IndexOutOfBoundsException;

    /**
     * Clears the item of a slot
     * 
     * @param  row                       the row index
     * @param  column                    the column index
     * 
     * @throws IndexOutOfBoundsException if converted slot index would be out of
     *                                       bounds
     */
    default void clear(int row, int column) throws IndexOutOfBoundsException {
        clear(InventoryMath.slotInBounds(row, column, getColumnAmount(), size()));
    }

    /**
     * Clears the items of a row
     * 
     * @param  row                       the row index
     * 
     * @throws IndexOutOfBoundsException if row is out of bounds
     */
    default void clearRow(int row) throws IndexOutOfBoundsException {
        int rowAmount = getRowAmount();
        if (row < 0 || row >= rowAmount) {
            throw new IndexOutOfBoundsException("Row not in range: " + row);
        }
        int columnAmount = getColumnAmount();
        int rowIndex = row * columnAmount;
        for (int column = 0; column < columnAmount; column++) {
            clear(rowIndex + column);
        }
    }

    /**
     * Clears the items of a column
     * 
     * @param  column                    the column index
     * 
     * @throws IndexOutOfBoundsException if column is out of bounds
     */
    default void clearColumn(int column) throws IndexOutOfBoundsException {
        int columnAmount = getColumnAmount();
        if (column < 0 || column >= columnAmount) {
            throw new IndexOutOfBoundsException("Column not in range: " + column);
        }
        int rowAmount = getRowAmount();
        for (int row = 0; row < rowAmount; row++) {
            clear(row * columnAmount + column);
        }
    }

    /**
     * Clears the items of a section
     * 
     * @param  start                     the start slot index
     * @param  end                       the end slot index
     * 
     * @throws IndexOutOfBoundsException if column is out of bounds
     */
    default void clearSection(int start, int end) throws IndexOutOfBoundsException {
        if (start < 0 || start >= size()) {
            throw new IndexOutOfBoundsException("Start not in range: " + start);
        } else if (end < 0 || end >= size() || end < start) {
            throw new IndexOutOfBoundsException("End not in range: " + end);
        }
        for (int idx = start; idx <= end; idx++) {
            clear(idx);
        }
    }

    /**
     * Clears the items of a section
     * 
     * @param  startRow                  the start row index
     * @param  startColumn               the start column index
     * @param  endRow                    the end row index
     * @param  endColumn                 the end column index
     * 
     * @throws IndexOutOfBoundsException if column is out of bounds
     */
    default void clearSection(int startRow, int startColumn, int endRow, int endColumn) throws IndexOutOfBoundsException {
        clearSection(InventoryMath.toSlot(startRow, startColumn, getColumnAmount()),
            InventoryMath.toSlot(endRow, endColumn, getColumnAmount()));
    }

    /**
     * Gets the item of a slot
     * 
     * @param  index                     the slot index
     * 
     * @return                           the item at that slot or {@code null} if
     *                                       there is none
     * 
     * @throws IndexOutOfBoundsException if slot index is out of bounds
     */
    ItemStack getItem(int index) throws IndexOutOfBoundsException;

    /**
     * Gets the item of a slot as an editor
     * 
     * @param  index                     the slot index
     * 
     * @return                           the item editor of the item at that slot or
     *                                       {@code null} if there is none
     * 
     * @throws IndexOutOfBoundsException if slot index is out of bounds
     */
    default ItemEditor getEditor(final int index) throws IndexOutOfBoundsException {
        return ItemEditor.ofNullable(getItem(index));
    }

    /**
     * Gets the item of a slot
     * 
     * @param  row                       the row index
     * @param  column                    the column index
     * 
     * @return                           the item at that slot or {@code null} if
     *                                       there is none
     * 
     * @throws IndexOutOfBoundsException if converted slot index would be out of
     *                                       bounds
     */
    default ItemStack getItem(final int row, final int column) throws IndexOutOfBoundsException {
        return getItem(InventoryMath.slotInBounds(row, column, getColumnAmount(), size()));
    }

    /**
     * Gets the item of a slot as an editor
     * 
     * @param  row                       the row index
     * @param  column                    the column index
     * 
     * @return                           the item editor of the item at that slot or
     *                                       {@code null} if there is none
     * 
     * @throws IndexOutOfBoundsException if converted slot index would be out of
     *                                       bounds
     */
    default ItemEditor getEditor(final int row, final int column) throws IndexOutOfBoundsException {
        return ItemEditor.ofNullable(getItem(row, column));
    }

    /**
     * Sets the item at a slot
     * 
     * @param  index                     the slot index
     * @param  itemStack                 the item to be set
     * 
     * @throws IndexOutOfBoundsException if slot index is out of bounds
     */
    void set(int index, ItemStack itemStack) throws IndexOutOfBoundsException;

    /**
     * Sets the item from an editor at a slot
     * 
     * @param  index                     the slot index
     * @param  editor                    the item editor to retrieve the item from
     *                                       that should be set
     * 
     * @throws IndexOutOfBoundsException if slot index is out of bounds
     */
    default void set(final int index, final ItemEditor editor) throws IndexOutOfBoundsException {
        if (editor == null) {
            clear(index);
            return;
        }
        set(index, editor.asItemStack());
    }

    /**
     * Sets the item at a slot
     * 
     * @param  row                       the row index
     * @param  column                    the column index
     * @param  itemStack                 the item to be set
     * 
     * @throws IndexOutOfBoundsException if converted slot index would be out of
     *                                       bounds
     */
    default void set(final int row, final int column, final ItemStack itemStack) throws IndexOutOfBoundsException {
        set(InventoryMath.slotInBounds(row, column, getColumnAmount(), size()), itemStack);
    }

    /**
     * Sets the item from an editor at a slot
     * 
     * 
     * @param  row                       the row index
     * @param  column                    the column index
     * @param  editor                    the item editor to retrieve the item from
     *                                       that should be set
     * 
     * @throws IndexOutOfBoundsException if converted slot index would be out of
     *                                       bounds
     */
    default void set(final int row, final int column, final ItemEditor editor) throws IndexOutOfBoundsException {
        if (editor == null) {
            clear(row, column);
            return;
        }
        set(row, column, editor.asItemStack());
    }

    /**
     * Fills the inventory with the item
     * 
     * @param itemStack the item editor to retrieve the item from that should be set
     */
    default void fill(final ItemStack itemStack) {
        if (itemStack == null) {
            clear();
            return;
        }
        int size = size();
        for (int i = 0; i < size; i++) {
            set(i, itemStack);
        }
    }

    /**
     * Fills the inventory with the item from an editor
     * 
     * @param editor the item editor to retrieve the item from that should be set
     */
    default void fill(final ItemEditor editor) {
        if (editor == null) {
            clear();
            return;
        }
        fill(editor.asItemStack());
    }

    /**
     * Fills a section with the item
     * 
     * @param  start                     the start slot index
     * @param  end                       the end slot index
     * @param  itemStack                 the item editor to retrieve the item from
     *                                       that should be set
     * 
     * @throws IndexOutOfBoundsException if slot index is out of bounds
     */
    default void fillSection(int start, int end, final ItemStack itemStack) {
        if (start < 0 || start >= size()) {
            throw new IndexOutOfBoundsException("Start not in range: " + start);
        } else if (end < 0 || end >= size() || end < start) {
            throw new IndexOutOfBoundsException("End not in range: " + end);
        }
        for (int idx = start; idx <= end; idx++) {
            set(idx, itemStack);
        }
    }

    /**
     * Fills a section with the item from an editor
     * 
     * @param  start                     the start slot index
     * @param  end                       the end slot index
     * @param  editor                    the item editor to retrieve the item from
     *                                       that should be set
     * 
     * @throws IndexOutOfBoundsException if slot index is out of bounds
     */
    default void fillSection(int start, int end, final ItemEditor editor) {
        if (editor == null) {
            clearSection(start, end);
            return;
        }
        fillSection(start, end, editor.asItemStack());
    }

    /**
     * Fills a section with the item
     * 
     * @param  startRow                  the start row index
     * @param  startColumn               the start column index
     * @param  endRow                    the end row index
     * @param  endColumn                 the end column index
     * @param  itemStack                 the item editor to retrieve the item from
     *                                       that should be set
     * 
     * @throws IndexOutOfBoundsException if converted slot index would be out of
     *                                       bounds
     */
    default void fillSection(int startRow, int startColumn, int endRow, int endColumn, final ItemStack itemStack) {
        fillSection(InventoryMath.toSlot(startRow, startColumn, getColumnAmount()),
            InventoryMath.toSlot(endRow, endColumn, getColumnAmount()), itemStack);
    }

    /**
     * Fills a section with the item from an editor
     * 
     * @param  startRow                  the start row index
     * @param  startColumn               the start column index
     * @param  endRow                    the end row index
     * @param  endColumn                 the end column index
     * @param  editor                    the item editor to retrieve the item from
     *                                       that should be set
     * 
     * @throws IndexOutOfBoundsException if converted slot index would be out of
     *                                       bounds
     */
    default void fillSection(int startRow, int startColumn, int endRow, int endColumn, final ItemEditor editor) {
        if (editor == null) {
            clearSection(InventoryMath.toSlot(startRow, startColumn, getColumnAmount()),
                InventoryMath.toSlot(endRow, endColumn, getColumnAmount()));
            return;
        }
        fillSection(InventoryMath.toSlot(startRow, startColumn, getColumnAmount()),
            InventoryMath.toSlot(endRow, endColumn, getColumnAmount()), editor.asItemStack());
    }

    /**
     * Fills a row with the item
     * 
     * @param  row                       the row index
     * @param  itemStack                 the item editor to retrieve the item from
     *                                       that should be set
     * 
     * @throws IndexOutOfBoundsException if row is out of bounds
     */
    default void fillRow(final int row, final ItemStack itemStack) throws IndexOutOfBoundsException {
        int rowAmount = getRowAmount();
        if (row < 0 || row >= rowAmount) {
            throw new IndexOutOfBoundsException("Row not in range: " + row);
        }
        int columnAmount = getColumnAmount();
        int rowIndex = row * columnAmount;
        for (int column = 0; column < columnAmount; column++) {
            set(rowIndex + column, itemStack);
        }
    }

    /**
     * Fills a row with the item from an editor
     * 
     * @param  row                       the row index
     * @param  editor                    the item editor to retrieve the item from
     *                                       that should be set
     * 
     * @throws IndexOutOfBoundsException if row is out of bounds
     */
    default void fillRow(final int row, final ItemEditor editor) throws IndexOutOfBoundsException {
        if (editor == null) {
            clearRow(row);
            return;
        }
        fillRow(row, editor.asItemStack());
    }

    /**
     * Fills a column with the item
     * 
     * @param  column                    the column index
     * @param  itemStack                 the item editor to retrieve the item from
     *                                       that should be set
     * 
     * @throws IndexOutOfBoundsException if column is out of bounds
     */
    default void fillColumn(final int column, final ItemStack itemStack) throws IndexOutOfBoundsException {
        int columnAmount = getColumnAmount();
        if (column < 0 || column >= columnAmount) {
            throw new IndexOutOfBoundsException("Column not in range: " + column);
        }
        int rowAmount = getRowAmount();
        for (int row = 0; row < rowAmount; row++) {
            set(row * columnAmount + column, itemStack);
        }
    }

    /**
     * Fills a column with the item from an editor
     * 
     * @param  column                    the column index
     * @param  editor                    the item editor to retrieve the item from
     *                                       that should be set
     * 
     * @throws IndexOutOfBoundsException if column is out of bounds
     */
    default void fillColumn(final int column, final ItemEditor editor) throws IndexOutOfBoundsException {
        if (editor == null) {
            clearColumn(column);
            return;
        }
        fillColumn(column, editor.asItemStack());
    }

    /**
     * Finds all slots that are similar the the provided {@link ItemStack}
     *
     * @param  itemStack the item to find slots for
     * 
     * @return           the slots as map
     */
    default Map<Integer, ItemStack> findSimilarSlots(final ItemStack itemStack) {
        if (itemStack == null) {
            return Collections.emptyMap();
        }
        final HashMap<Integer, ItemStack> map = new HashMap<>();
        final int size = size();
        for (int index = 0; index < size; index++) {
            final ItemStack current = getItem(index);
            if (current == null || !current.isSimilar(itemStack)) {
                continue;
            }
            map.put(index, current);
        }
        return map;
    }

    /**
     * Finds all slots in that the provided {@link ItemStack} can fit in
     *
     * @param  itemStack the item to check for
     * 
     * @return           the possible slots as map
     */
    default Map<Integer, ItemStack> findPossibleSlots(final ItemStack itemStack) {
        if (itemStack == null) {
            return Collections.emptyMap();
        }
        final HashMap<Integer, ItemStack> map = new HashMap<>();
        final int size = size();
        for (int index = 0; index < size; index++) {
            final ItemStack current = getItem(index);
            if (current != null && !current.isSimilar(itemStack) && !ItemHelper.isAir(current.getType())) {
                continue;
            }
            map.put(index, current);
        }
        return map;
    }

}