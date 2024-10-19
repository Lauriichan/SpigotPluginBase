package me.lauriichan.minecraft.pluginbase.inventory.paged;

import java.util.Map;

import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import me.lauriichan.minecraft.pluginbase.extension.ExtensionPoint;
import me.lauriichan.minecraft.pluginbase.extension.IExtension;
import me.lauriichan.minecraft.pluginbase.inventory.ClickType;
import me.lauriichan.minecraft.pluginbase.inventory.DropType;

@ExtensionPoint
public interface IInventoryPageExtension<H extends IInventoryPageExtension<H, P>, P> extends IExtension {

    default boolean defaultPage() {
        return false;
    }

    default void onPageOpen(final PageContext<H, P> context) {
        onPageUpdate(context, false);
    }

    default void onPageUpdate(final PageContext<H, P> context, final boolean changed) {}

    default void onPageClose(final PageContext<H, P> context) {}

    default boolean onInventoryOpen(final PageContext<H, P> context) {
        return false;
    }

    default boolean onInventoryClose(final PageContext<H, P> context) {
        return false;
    }

    default boolean onClickClone(final PageContext<H, P> context, final ItemStack item, final int slot) {
        return true;
    }

    default boolean onClickMove(final PageContext<H, P> context, final Map<Integer, ItemStack> slots, final ItemStack item,
        final int amount) {
        return true;
    }

    default boolean onClickPickup(final PageContext<H, P> context, final ItemStack item, final int slot, final int amount,
        final boolean cursor, final ClickType type) {
        return true;
    }

    default boolean onClickPlace(final PageContext<H, P> context, final ItemStack item, final int slot, final int amount,
        final ClickType type) {
        return true;
    }

    default boolean onClickSwap(final PageContext<H, P> context, final ItemStack previous, final ItemStack now, final int slot,
        final ClickType type) {
        return true;
    }

    default boolean onClickDrop(final PageContext<H, P> context, final ItemStack item, final int slot, final int amount,
        final DropType type) {
        return true;
    }

    default boolean onEventDrag(final PageContext<H, P> context, final InventoryDragEvent event) {
        return true;
    }

}
