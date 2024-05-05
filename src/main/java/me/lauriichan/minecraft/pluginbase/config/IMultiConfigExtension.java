package me.lauriichan.minecraft.pluginbase.config;

import me.lauriichan.minecraft.pluginbase.extension.ExtensionPoint;
import me.lauriichan.minecraft.pluginbase.extension.IExtension;

@ExtensionPoint
public interface IMultiConfigExtension<K, T, C extends IConfigExtension> extends IExtension {
    
    K getConfigKey(T element);

    String path(T element);
    
    C create();
    
}
