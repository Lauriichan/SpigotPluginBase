package me.lauriichan.minecraft.pluginbase.inventory.item;

import org.bukkit.inventory.meta.ItemMeta;

public final class ColoredNameEditor extends ColoredStringEditor<ColoredNameEditor> {

    ColoredNameEditor(final ItemEditor editor) {
        super(editor);
        set(editor.getName());
    }

    public String asPlainString() {
        return content.asPlainString();
    }

    public String asColoredString() {
        return content.asColoredString();
    }
    
    @Override
    public void reset() {
        if (!editor.hasItemMeta()) {
            return;
        }
        content.clear();
        ItemMeta meta = editor.getItemMeta();
        if (meta.hasDisplayName()) {
            content.add(meta.getDisplayName());
        } else {
            content.add(editor.getItemName());
        }
    }

    @Override
    public ItemEditor apply() {
        if (!editor.hasItemMeta()) {
            return editor;
        }
        editor.getItemMeta().setDisplayName(content.asColoredString());
        return editor;
    }

}