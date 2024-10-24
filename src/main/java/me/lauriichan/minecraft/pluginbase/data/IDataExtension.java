package me.lauriichan.minecraft.pluginbase.data;

import me.lauriichan.minecraft.pluginbase.extension.IExtension;

public interface IDataExtension<T> extends IExtension {

    default String name() {
        return getClass().getSimpleName();
    }

    IDataHandler<T> handler();

}
