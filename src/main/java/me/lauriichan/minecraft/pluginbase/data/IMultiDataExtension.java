package me.lauriichan.minecraft.pluginbase.data;

import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.minecraft.pluginbase.extension.ExtensionPoint;
import me.lauriichan.minecraft.pluginbase.extension.IExtension;

@ExtensionPoint
public interface IMultiDataExtension<K, E, T, D extends IFileDataExtension<T>> extends IExtension {
    
    Class<D> type();
    
    K getDataKey(E element);

    String path(E element);
    
    D create();
    
    default void onLoad(ISimpleLogger logger) {}
    
    default void onSave(ISimpleLogger logger) {}
    
}
