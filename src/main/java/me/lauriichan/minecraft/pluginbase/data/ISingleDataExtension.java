package me.lauriichan.minecraft.pluginbase.data;

import me.lauriichan.minecraft.pluginbase.extension.ExtensionPoint;
import me.lauriichan.minecraft.pluginbase.extension.IExtension;

@ExtensionPoint
public interface ISingleDataExtension<T> extends IFileDataExtension<T>, IExtension {
    
    String path();

}
