package me.lauriichan.minecraft.pluginbase.config;

import me.lauriichan.minecraft.pluginbase.extension.IExtension;

public interface IConfigExtension extends IExtension {
    
    default String name() {
        return getClass().getSimpleName();
    }

    IConfigHandler handler();

    default boolean isModified() {
        return false;
    }
    
    default void onPropergate(final Configuration configuration) throws Exception {}

    default void onLoad(final Configuration configuration) throws Exception {}

    default void onSave(final Configuration configuration) throws Exception {}

}
