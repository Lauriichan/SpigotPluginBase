package me.lauriichan.minecraft.pluginbase.inventory;

import java.util.Map;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

public interface IHandler {

    default void onSet(final IGuiInventory inventory) {}

    default void onInit(final HumanEntity entity, final IGuiInventory inventory) {}

    default void onUpdate(final IGuiInventory inventory, final boolean changed) {}

    default boolean onEventClose(final HumanEntity entity, final IGuiInventory inventory) {
        return false;
    }

    default boolean onEventOpen(final HumanEntity entity, final IGuiInventory inventory) {
        return false;
    }

    default boolean onEventDrag(final HumanEntity entity, final IGuiInventory inventory, final InventoryDragEvent event) {
        return false;
    }

    default boolean onEventClick(final HumanEntity entity, final IGuiInventory inventory, final InventoryClickEvent event) {
        final InventoryAction action = event.getAction();
        final int slot = event.getSlot();
        ItemStack stack;
        switch (action) {
        case DROP_ALL_SLOT:
            if (event.getClickedInventory() != inventory.getInventory()) {
                return false;
            }
            return onClickDrop(entity, inventory, stack = inventory.getItem(slot), slot, stack.getAmount(), DropType.CTRL_DROP);
        case DROP_ONE_SLOT:
            if (event.getClickedInventory() != inventory.getInventory()) {
                return false;
            }
            return onClickDrop(entity, inventory, inventory.getItem(slot), slot, 1, DropType.DROP);
        case PICKUP_ALL:
            if (event.getClickedInventory() != inventory.getInventory()) {
                return false;
            }
            return onClickPickup(entity, inventory, stack = inventory.getItem(slot), slot, stack.getAmount(), true, ClickType.LEFT);
        case PICKUP_HALF:
            if (event.getClickedInventory() != inventory.getInventory()) {
                return false;
            }
            return onClickPickup(entity, inventory, stack = inventory.getItem(slot), slot, stack.getAmount() / 2, true, ClickType.RIGHT);
        case PICKUP_ONE:
            if (event.getClickedInventory() != inventory.getInventory()) {
                return false;
            }
            return onClickPickup(entity, inventory, stack = inventory.getItem(slot), slot, 1, true, ClickType.LEFT);
        case PICKUP_SOME:
            if (event.getClickedInventory() != inventory.getInventory()) {
                return false;
            }
            int amount = event.getCursor().getMaxStackSize() - event.getCursor().getAmount();
            if (amount > (stack = inventory.getItem(slot)).getAmount()) {
                amount = stack.getAmount();
            }
            return onClickPickup(entity, inventory, stack, slot, amount, true, ClickType.LEFT);
        case PLACE_ALL:
            if (event.getClickedInventory() != inventory.getInventory()) {
                return false;
            }
            return onClickPlace(entity, inventory, event.getCursor(), slot, event.getCursor().getAmount(), ClickType.LEFT);
        case PLACE_ONE:
            if (event.getClickedInventory() != inventory.getInventory()) {
                return false;
            }
            return onClickPlace(entity, inventory, event.getCursor(), slot, 1, ClickType.RIGHT);
        case PLACE_SOME:
            if (event.getClickedInventory() != inventory.getInventory()) {
                return false;
            }
            return onClickPlace(entity, inventory, event.getCursor(), slot,
                (stack = inventory.getItem(slot)).getMaxStackSize() - inventory.getItem(slot).getAmount(), ClickType.LEFT);
        case SWAP_WITH_CURSOR:
            if (event.getClickedInventory() != inventory.getInventory()) {
                return false;
            }
            return onClickSwap(entity, inventory, inventory.getItem(slot), event.getCursor(), slot, ClickType.LEFT);
        case HOTBAR_SWAP:
            if (event.getClickedInventory() != inventory.getInventory()) {
                return false;
            }
            return onClickSwap(entity, inventory, inventory.getItem(slot), event.getCurrentItem(), slot, ClickType.KEYBOARD);
        case MOVE_TO_OTHER_INVENTORY:
            if (event.getClickedInventory() == inventory.getInventory()) {
                return onClickPickup(entity, inventory, event.getCurrentItem(), event.getSlot(), event.getCurrentItem().getAmount(), false, ClickType.SHIFT_LEFT);
            } else {
                return onClickMove(entity, inventory, inventory.findPossibleSlots(event.getCurrentItem()), event.getCurrentItem(),
                    event.getCurrentItem().getAmount());
            }
        case CLONE_STACK:
            if (event.getClickedInventory() != inventory.getInventory()) {
                return false;
            }
            return onClickClone(entity, inventory, event.getCurrentItem(), slot);
        case COLLECT_TO_CURSOR:
            return true;
        default:
            return false;
        }
    }

    default boolean onClickClone(final HumanEntity entity, final IGuiInventory inventory, final ItemStack item, final int slot) {
        return true;
    }

    default boolean onClickMove(final HumanEntity entity, final IGuiInventory inventory, final Map<Integer, ItemStack> slots,
        final ItemStack item, final int amount) {
        return true;
    }

    default boolean onClickPickup(final HumanEntity entity, final IGuiInventory inventory, final ItemStack item, final int slot,
        final int amount, final boolean cursor, final ClickType type) {
        return true;
    }

    default boolean onClickPlace(final HumanEntity entity, final IGuiInventory inventory, final ItemStack item, final int slot,
        final int amount, final ClickType type) {
        return true;
    }

    default boolean onClickSwap(final HumanEntity entity, final IGuiInventory inventory, final ItemStack previous, final ItemStack now,
        final int slot, final ClickType type) {
        return true;
    }

    default boolean onClickDrop(final HumanEntity entity, final IGuiInventory inventory, final ItemStack item, final int slot,
        final int amount, final DropType type) {
        return true;
    }

}