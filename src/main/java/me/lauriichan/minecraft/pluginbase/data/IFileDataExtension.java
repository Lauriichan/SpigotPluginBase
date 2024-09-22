package me.lauriichan.minecraft.pluginbase.data;

import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.minecraft.pluginbase.data.IDataHandler.Wrapper;

public interface IFileDataExtension<T> extends IDataExtension<T> {

    default boolean isModified() {
        // Consider data modified by default
        return true;
    }

    default void onPropergate(final ISimpleLogger logger, final Wrapper<T> value) {}

    default void onLoad(final ISimpleLogger logger, final Wrapper<T> value) throws Exception {}

    default void onSave(final ISimpleLogger logger, final Wrapper<T> value) throws Exception {}

}
