package me.lauriichan.minecraft.pluginbase.listener;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

import me.lauriichan.minecraft.pluginbase.ConditionConstant;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.extension.ExtensionCondition;
import me.lauriichan.minecraft.pluginbase.inventory.IGuiInventory;

@Extension
@ExtensionCondition(name = ConditionConstant.ENABLE_GUI)
public final class GuiListener implements IListenerExtension {

    private final BukkitScheduler scheduler = Bukkit.getScheduler();
    private final Plugin plugin;

    public GuiListener(final Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onClick(final InventoryClickEvent event) {
        IGuiInventory inventory = inventory(event);
        if (inventory == null || !inventory.hasHandler()) {
            return;
        }
        event.setCancelled(inventory.getHandler().onEventClick(event.getWhoClicked(), inventory, event));
    }

    @EventHandler(ignoreCancelled = true)
    public void onDrag(final InventoryDragEvent event) {
        IGuiInventory inventory = inventory(event);
        if (inventory == null || !inventory.hasHandler()) {
            return;
        }
        event.setCancelled(inventory.getHandler().onEventDrag(event.getWhoClicked(), inventory, event));
    }

    @EventHandler(ignoreCancelled = true)
    public void onClose(final InventoryCloseEvent event) {
        IGuiInventory inventory = inventory(event);
        if (inventory == null || !inventory.hasHandler()) {
            return;
        }
        if (inventory.getHandler().onEventClose(event.getPlayer(), inventory)) {
            scheduler.runTask(plugin, () -> event.getPlayer().openInventory(inventory.getInventory()));
        } else {
            // Clear inventory on close to free up space
            inventory.clear();
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onOpen(final InventoryOpenEvent event) {
        IGuiInventory inventory = inventory(event);
        if (inventory == null || !inventory.hasHandler()) {
            return;
        }
        event.setCancelled(inventory.getHandler().onEventOpen(event.getPlayer(), inventory));
    }
    
    private IGuiInventory inventory(InventoryEvent event) {
        if (!(event.getInventory().getHolder() instanceof IGuiInventory)) {
            return null;
        }
        return (IGuiInventory) event.getInventory().getHolder();
    }

}