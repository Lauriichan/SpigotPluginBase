package me.lauriichan.minecraft.pluginbase.data;

import me.lauriichan.minecraft.pluginbase.extension.ExtensionPoint;
import me.lauriichan.minecraft.pluginbase.extension.IExtension;

@ExtensionPoint
public interface IMultiDataExtension<K, E, T, D extends IFileDataExtension<T>> extends IExtension {
    
    K getDataKey(E element);

    String path(E element);
    
    D create();
    
}
