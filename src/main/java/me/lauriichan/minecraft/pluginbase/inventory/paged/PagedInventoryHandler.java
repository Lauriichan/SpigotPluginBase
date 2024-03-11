package me.lauriichan.minecraft.pluginbase.inventory.paged;

import java.util.List;
import java.util.Map;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.inventory.IGuiInventory;

public abstract class PagedInventoryHandler<H extends IInventoryPageExtension<H, P>, P> implements IPagedInventoryHandlerExtension {

    public static final String PAGE_PROPERTY = "inv.page";
    public static final String PLAYER_PROPERTY = "inv.player";

    private final Class<P> playerType;
    private final Class<H> handlerType;

    private final Object2ObjectArrayMap<Class<? extends H>, H> handlers = new Object2ObjectArrayMap<>();
    private final Class<? extends H> defaultPage;

    public PagedInventoryHandler(final BasePlugin<?> plugin, final Class<H> handlerType, final Class<P> playerType) {
        this.playerType = playerType;
        this.handlerType = handlerType;
        final List<H> extensions = plugin.extension(IInventoryPageExtension.class, handlerType, true).extensions();
        H handler;
        Class<? extends H> defaultPage = null, handlerClass;
        for (int i = 0; i < extensions.size(); i++) {
            handler = extensions.get(i);
            handlers.put(handlerClass = handler.getClass().asSubclass(handlerType), handler);
            if (handler.defaultPage()) {
                if (defaultPage != null) {
                    throw new IllegalStateException("Can't have two default pages for handler type '" + handlerType.getName() + "'!");
                }
                defaultPage = handlerClass;
            }
        }
        this.defaultPage = defaultPage;
    }

    protected final PageContext<H, P> context(final P player, final IGuiInventory inventory) {
        return new PageContext<>(this, player, inventory);
    }

    /*
     * Entity handling
     */

    protected abstract P playerFromEntity(HumanEntity entity);

    protected abstract HumanEntity entityFromPlayer(P player);

    protected abstract boolean isSame(P p1, P p2);

    /*
     * Inventory callbacks
     */

    protected void onInventoryOpen(final P player, final IGuiInventory inventory) {}

    protected void onInventoryClose(final P player, final IGuiInventory inventory) {
        inventory.attrClear();
    }

    /*
     * Update callbacks
     */

    protected void onUnknownUpdate(final IGuiInventory inventory, final boolean changed) {}

    protected void onHandledUpdate(final P player, final IGuiInventory inventory, final boolean changed) {}

    protected void onUnhandledUpdate(final P player, final IGuiInventory inventory, final boolean changed) {}

    /*
     * Unhandled callbacks
     */

    protected boolean onUnhandledOpen(final P player, final IGuiInventory inventory) {
        return true;
    }

    protected boolean onUnhandledClose(final P player, final IGuiInventory inventory) {
        return false;
    }

    protected boolean onUnhandledDrag(final P player, final IGuiInventory inventory, final InventoryDragEvent event) {
        return true;
    }

    protected boolean onUnhandledClickClone(final P player, final IGuiInventory inventory, final ItemStack item, final int slot) {
        return true;
    }

    protected boolean onUnhandledClickMove(final P player, final IGuiInventory inventory, final Map<Integer, ItemStack> slots,
        final ItemStack item, final int amount) {
        return true;
    }

    protected boolean onUnhandledClickPickup(final P player, final IGuiInventory inventory, final ItemStack item, final int slot,
        final int amount, final boolean cursor) {
        return true;
    }

    protected boolean onUnhandledClickPlace(final P player, final IGuiInventory inventory, final ItemStack item, final int slot,
        final int amount) {
        return true;
    }

    protected boolean onUnhandledClickSwap(final P player, final IGuiInventory inventory, final ItemStack previous, final ItemStack now,
        final int slot) {
        return true;
    }

    protected boolean onUnhandledClickDrop(final P player, final IGuiInventory inventory, final ItemStack item, final int slot,
        final int amount) {
        return true;
    }

    /*
     * Page management
     */

    public final H getPage(final Class<? extends H> page) {
        return handlers.get(page);
    }

    public final boolean hasPage(final Class<? extends H> page) {
        return handlers.containsKey(page);
    }

    public final boolean hasPage(final H page) {
        return handlers.containsValue(page);
    }

    /*
     * Page handling
     */
    
    public final boolean setPage(final IGuiInventory inventory, final Class<? extends H> page) {
        if (!hasPage(page)) {
            return false;
        }
        inventory.attrClass(PAGE_PROPERTY, page);
        return true;
    }

    protected final H currentPageFor(final P player, final IGuiInventory inventory) {
        final Class<? extends H> currentPage = inventory.attrClass(PAGE_PROPERTY, handlerType);
        if (!handlers.containsKey(currentPage)) {
            if (defaultPage == null) {
                return null;
            }
            openPage(player, inventory, defaultPage);
            return handlers.get(defaultPage);
        }
        return handlers.get(currentPage);
    }

    protected final void openPage(final P player, final IGuiInventory inventory, final Class<? extends H> page) {
        final Class<? extends H> currentPage = inventory.attrClass(PAGE_PROPERTY, handlerType);
        if (currentPage != null) {
            final H pageHandler = handlers.get(currentPage);
            if (pageHandler != null) {
                pageHandler.onPageClose(context(player, inventory));
            }
        }
        inventory.attrSet(PAGE_PROPERTY, page);
        if (player != null) {
            applyPlayer(player, inventory);
            final H pageHandler = currentPageFor(player, inventory);
            pageHandler.onPageOpen(context(player, inventory));
            final HumanEntity entity = entityFromPlayer(player);
            if (!inventory.isViewing(entity.getUniqueId())) {
                inventory.open(entity);
            }
        }
    }

    protected final void applyPlayer(final P player, final IGuiInventory inventory) {
        final P found = inventory.attr(PLAYER_PROPERTY, playerType);
        if (found != null && isSame(found, player)) {
            return;
        }
        inventory.attrSet(PLAYER_PROPERTY, player);
    }

    protected final P playerOf(final IGuiInventory inventory) {
        return inventory.attr(PLAYER_PROPERTY, playerType);
    }

    /*
     * Update handler
     */

    @Override
    public void onUpdate(final IGuiInventory inventory, final boolean changed) {
        final P player = playerOf(inventory);
        if (player == null) {
            onUnknownUpdate(inventory, changed);
            return;
        }
        final H page = currentPageFor(player, inventory);
        if (page == null) {
            onUnhandledUpdate(player, inventory, changed);
            return;
        }
        page.onPageUpdate(context(player, inventory), changed);
        onHandledUpdate(player, inventory, changed);
    }

    /*
     * Event handlers
     */

    @Override
    public final boolean onEventOpen(final HumanEntity entity, final IGuiInventory inventory) {
        if (inventory.hasInventoryChanged()) {
            return false;
        }
        final P player = playerFromEntity(entity);
        final H page = currentPageFor(null, inventory);
        applyPlayer(player, inventory);
        if (page == null) {
            if (onUnhandledOpen(player, inventory)) {
                return true;
            }
            onInventoryOpen(player, inventory);
            return false;
        }
        if (page.onInventoryOpen(context(player, inventory))) {
            return true;
        }
        page.onPageOpen(context(player, inventory));
        onInventoryOpen(player, inventory);
        return false;
    }

    @Override
    public final boolean onEventClose(final HumanEntity entity, final IGuiInventory inventory) {
        if (inventory.hasInventoryChanged()) {
            return false;
        }
        final P player = playerFromEntity(entity);
        final H page = currentPageFor(null, inventory);
        if (page == null) {
            if (onUnhandledClose(player, inventory)) {
                return true;
            }
            onInventoryClose(player, inventory);
            return false;
        }
        if (page.onInventoryClose(context(player, inventory))) {
            return true;
        }
        page.onPageClose(context(player, inventory));
        onInventoryClose(player, inventory);
        return false;
    }

    @Override
    public final boolean onEventDrag(final HumanEntity entity, final IGuiInventory inventory, final InventoryDragEvent event) {
        final P player = playerFromEntity(entity);
        final H page = currentPageFor(player, inventory);
        if (page == null) {
            return onUnhandledDrag(player, inventory, event);
        }
        return page.onEventDrag(context(player, inventory), event);
    }

    /*
     * Click events
     */

    @Override
    public boolean onClickClone(final HumanEntity entity, final IGuiInventory inventory, final ItemStack item, final int slot) {
        final P player = playerFromEntity(entity);
        final H page = currentPageFor(player, inventory);
        if (page == null) {
            return onUnhandledClickClone(player, inventory, item, slot);
        }
        return page.onClickClone(context(player, inventory), item, slot);
    }

    @Override
    public boolean onClickDrop(final HumanEntity entity, final IGuiInventory inventory, final ItemStack item, final int slot,
        final int amount) {
        final P player = playerFromEntity(entity);
        final H page = currentPageFor(player, inventory);
        if (page == null) {
            return onUnhandledClickDrop(player, inventory, item, slot, amount);
        }
        return page.onClickDrop(context(player, inventory), item, slot, amount);
    }

    @Override
    public boolean onClickMove(final HumanEntity entity, final IGuiInventory inventory, final Map<Integer, ItemStack> slots,
        final ItemStack item, final int amount) {
        final P player = playerFromEntity(entity);
        final H page = currentPageFor(player, inventory);
        if (page == null) {
            return onUnhandledClickMove(player, inventory, slots, item, amount);
        }
        return page.onClickMove(context(player, inventory), slots, item, amount);
    }

    @Override
    public boolean onClickPickup(final HumanEntity entity, final IGuiInventory inventory, final ItemStack item, final int slot,
        final int amount, final boolean cursor) {
        final P player = playerFromEntity(entity);
        final H page = currentPageFor(player, inventory);
        if (page == null) {
            return onUnhandledClickPickup(player, inventory, item, slot, amount, cursor);
        }
        return page.onClickPickup(context(player, inventory), item, slot, amount, cursor);
    }

    @Override
    public boolean onClickPlace(final HumanEntity entity, final IGuiInventory inventory, final ItemStack item, final int slot,
        final int amount) {
        final P player = playerFromEntity(entity);
        final H page = currentPageFor(player, inventory);
        if (page == null) {
            return onUnhandledClickPlace(player, inventory, item, slot, amount);
        }
        return page.onClickPlace(context(player, inventory), item, slot, amount);
    }

    @Override
    public boolean onClickSwap(final HumanEntity entity, final IGuiInventory inventory, final ItemStack previous, final ItemStack now,
        final int slot) {
        final P player = playerFromEntity(entity);
        final H page = currentPageFor(player, inventory);
        if (page == null) {
            return onUnhandledClickSwap(player, inventory, previous, now, slot);
        }
        return page.onClickSwap(context(player, inventory), previous, now, slot);
    }

}
