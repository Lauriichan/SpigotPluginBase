package me.lauriichan.minecraft.pluginbase.message.component;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;

public enum Formatting {
    
    MAGIC(ChatColor.MAGIC, BaseComponent::setObfuscated, BaseComponent::isObfuscated),
    BOLD(ChatColor.BOLD, BaseComponent::setBold, BaseComponent::isBold),
    STRIKETHROUGH(ChatColor.STRIKETHROUGH, BaseComponent::setStrikethrough, BaseComponent::isStrikethrough),
    UNDERLINE(ChatColor.UNDERLINE, BaseComponent::setUnderlined, BaseComponent::isUnderlined),
    ITALIC(ChatColor.ITALIC, BaseComponent::setItalic, BaseComponent::isUnderlined),
    RESET(ChatColor.RESET, BaseComponent::setReset, BaseComponent::isReset);
    
    static final Formatting[] VALUES = Formatting.values();

    private final ChatColor bukkitValue;
    private final BiConsumer<BaseComponent, Boolean> applyFunc;
    private final Predicate<BaseComponent> isAppliedFunc;
    
    private Formatting(ChatColor bukkitValue, BiConsumer<BaseComponent, Boolean> applyFunc, Predicate<BaseComponent> isAppliedFunc) {
        this.bukkitValue = bukkitValue;
        this.applyFunc = applyFunc;
        this.isAppliedFunc = isAppliedFunc;
    }
    
    public ChatColor bukkitValue() {
        return bukkitValue;
    }
    
    public void apply(BaseComponent component, boolean state) { 
        applyFunc.accept(component, state);
    }
    
    public boolean isApplied(BaseComponent component) {
        return isAppliedFunc.test(component);
    }
    
    public static Formatting find(ChatColor bukkitValue) {
        for (Formatting value : VALUES) {
            if (value.bukkitValue == bukkitValue) {
                return value;
            }
        }
        return null;
    }

}
