package me.lauriichan.minecraft.pluginbase.config;

import me.lauriichan.laylib.logger.ISimpleLogger;

public interface IConfigExtension {
    
    default String name() {
        return getClass().getSimpleName();
    }

    IConfigHandler handler();

    default boolean isModified() {
        return false;
    }
    
    default void onPropergate(final ISimpleLogger logger, final Configuration configuration) throws Exception {}

    default void onLoad(final ISimpleLogger logger, final Configuration configuration) throws Exception {}

    default void onSave(final ISimpleLogger logger, final Configuration configuration) throws Exception {}

}
