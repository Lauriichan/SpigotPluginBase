package me.lauriichan.minecraft.pluginbase.config;

public interface IConfigWrapper<T extends IConfigExtension> {
    
    Class<T> configType();
    
    int[] reload(boolean wipeAfterLoad);
    
    int[] save(boolean forceSave);

}
