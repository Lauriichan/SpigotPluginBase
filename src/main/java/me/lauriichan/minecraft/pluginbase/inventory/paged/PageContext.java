package me.lauriichan.minecraft.pluginbase.inventory.paged;

import me.lauriichan.minecraft.pluginbase.inventory.IGuiInventory;

public final class PageContext<H extends IInventoryPageExtension<H, P>, P> {

    private final PagedInventoryHandler<H, P> handler;

    private final P player;
    private final IGuiInventory inventory;

    PageContext(final PagedInventoryHandler<H, P> handler, final P player, final IGuiInventory inventory) {
        this.handler = handler;
        this.player = player;
        this.inventory = inventory;
    }

    public P player() {
        return player;
    }

    public IGuiInventory inventory() {
        return inventory;
    }
    
    public PagedInventoryHandler<H, P> handler() {
        return handler;
    }
    
    public void setPage(final Class<? extends H> page) {
        handler.setPage(inventory, page);
    }

    public void openPage(final Class<? extends H> page) {
        handler.openPage(player, inventory, page);
    }

    public boolean hasPage(final Class<? extends H> page) {
        return handler.hasPage(page);
    }

    public H getPage(final Class<? extends H> page) {
        return handler.getPage(page);
    }

}
