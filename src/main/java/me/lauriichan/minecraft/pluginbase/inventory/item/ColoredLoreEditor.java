package me.lauriichan.minecraft.pluginbase.inventory.item;

import java.util.List;

import org.bukkit.inventory.meta.ItemMeta;

public final class ColoredLoreEditor extends ColoredStringEditor<ColoredLoreEditor> {

    ColoredLoreEditor(final ItemEditor editor) {
        super(editor);
    }

    public List<String> asPlainList() {
        return content.asPlainList();
    }

    public List<String> asColoredList() {
        return content.asColoredList();
    }

    @Override
    public void reset() {
        if (!editor.hasItemMeta()) {
            return;
        }
        content.clear();
        ItemMeta meta = editor.getItemMeta();
        if (meta.hasLore()) {
            content.addAll(meta.getLore());
        }
    }

    @Override
    public ItemEditor apply() {
        if (!editor.hasItemMeta()) {
            return editor;
        }
        editor.getItemMeta().setLore(content.asColoredList());
        return editor;
    }

}