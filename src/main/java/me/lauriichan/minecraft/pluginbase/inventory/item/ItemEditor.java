package me.lauriichan.minecraft.pluginbase.inventory.item;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import me.lauriichan.minecraft.pluginbase.util.color.BukkitColor;

public final class ItemEditor {

    public static ItemEditor ofNullable(final ItemStack itemStack) {
        if (itemStack == null) {
            return null;
        }
        return new ItemEditor(itemStack);
    }

    public static ItemEditor of(final ItemStack itemStack) {
        return new ItemEditor(itemStack);
    }

    public static ItemEditor of(final Material material) {
        return new ItemEditor(material);
    }

    private final ItemStack itemStack;
    private volatile ItemMeta itemMeta;

    public ItemEditor(final Material material) {
        this(new ItemStack(material));
    }

    public ItemEditor(final ItemStack stack) {
        Objects.requireNonNull(stack, "ItemStack can't be null");
        this.itemStack = stack;
        this.itemMeta = stack.getItemMeta();
    }

    public boolean hasItemMeta() {
        return itemMeta != null;
    }

    public ItemMeta getItemMeta() {
        return itemMeta;
    }

    public ItemEditor applyItemMeta(final Consumer<ItemMeta> consumer) {
        if (itemMeta == null) {
            return this;
        }
        consumer.accept(itemMeta);
        itemStack.setItemMeta(itemMeta);
        return this;
    }

    /*
     * Setter / Getter
     */

    // Name
    public String getName() {
        if (itemMeta == null || itemMeta.hasDisplayName()) {
            return "";
        }
        return itemMeta.getDisplayName();
    }

    public ItemEditor setName(final String name) {
        if (itemMeta == null) {
            return this;
        }
        itemMeta.setDisplayName(BukkitColor.apply(name));
        return this;
    }
    
    public ColoredNameEditor name() {
        return new ColoredNameEditor(this);
    }

    // Lore
    public List<String> getLore() {
        if (itemMeta == null) {
            return Collections.emptyList();
        }
        return itemMeta.getLore();
    }

    public ItemEditor setLore(final List<String> lines) {
        if (itemMeta != null) {
            itemMeta.setLore(lines);
        }
        return this;
    }
    
    public ColoredLoreEditor lore() {
        return new ColoredLoreEditor(this);
    }

    // Texture
    public boolean isHead() {
        return itemMeta instanceof SkullMeta;
    }

    // Enchantment
    public boolean hasEnchantments() {
        return itemMeta != null && itemMeta.hasEnchants();
    }

    public boolean hasEnchantment(final Enchantment enchantment) {
        return itemMeta != null && itemMeta.hasEnchant(enchantment);
    }

    public boolean hasEnchantmentConflict(final Enchantment enchantment) {
        return itemMeta != null && itemMeta.hasConflictingEnchant(enchantment);
    }

    public int getEnchantment(final Enchantment enchantment) {
        if (itemMeta == null) {
            return 0;
        }
        return itemMeta.getEnchantLevel(enchantment);
    }

    public ItemEditor setEnchantment(final Enchantment enchantment, final int level) {
        return setEnchantment(enchantment, level, false);
    }

    public ItemEditor setEnchantment(final Enchantment enchantment, final int level, final boolean ignoreRestriction) {
        if (itemMeta == null) {
            return this;
        }
        if (itemMeta.hasEnchant(enchantment)) {
            itemMeta.removeEnchant(enchantment);
        }
        if (level <= 0) {
            return this;
        }
        itemMeta.addEnchant(enchantment, level, ignoreRestriction);
        return this;
    }

    // Material
    public Material getMaterial() {
        return itemStack.getType();
    }

    public ItemEditor setMaterial(final Material material) {
        if (material == null) {
            return this;
        }
        asItemStack().setType(material);
        itemMeta = itemStack.getItemMeta();
        return this;
    }

    // Amount
    public int getMaxAmount() {
        return itemStack.getMaxStackSize();
    }

    public int getAmount() {
        return itemStack.getAmount();
    }

    public ItemEditor setAmount(final int amount) {
        return setAmount(amount, false);
    }

    public ItemEditor setAmount(final int amount, final boolean ignoreRestriction) {
        if (ignoreRestriction) {
            itemStack.setAmount(Math.max(amount, 0));
            return this;
        }
        itemStack.setAmount(Math.max(Math.min(amount, itemStack.getMaxStackSize()), 0));
        return this;
    }

    /*
     * Actions
     */

    public ItemEditor apply() {
        if (itemMeta != null) {
            itemStack.setItemMeta(itemMeta);
        }
        return this;
    }

    public ItemStack asItemStack() {
        if (itemMeta != null) {
            itemStack.setItemMeta(itemMeta);
        }
        return itemStack;
    }
    
    public ItemStack asRawItemStack() {
        return itemStack;
    }

}
