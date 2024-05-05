package me.lauriichan.minecraft.pluginbase.config;

import me.lauriichan.minecraft.pluginbase.extension.ExtensionPoint;

@ExtensionPoint
public interface ISingleConfigExtension extends IConfigExtension {

    String path();

}
