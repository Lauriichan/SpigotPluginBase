package me.lauriichan.minecraft.pluginbase.io;

import me.lauriichan.minecraft.pluginbase.extension.ExtensionPoint;
import me.lauriichan.minecraft.pluginbase.extension.IExtension;

@ExtensionPoint
public interface IIOHandler<B, V> extends IExtension {
    
    Class<B> bufferType();
    
    Class<V> valueType();

}
