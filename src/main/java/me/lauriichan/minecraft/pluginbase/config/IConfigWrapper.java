package me.lauriichan.minecraft.pluginbase.config;

public interface IConfigWrapper<T extends IConfigExtension> {
    
    Class<T> configType();
    
    default int[] reload() {
        return reload(false, false);
    }
    
    int[] reload(boolean forceReload, boolean wipeAfterLoad);
    
    default int[] save() {
        return save(false);
    }
    
    int[] save(boolean forceSave);

}
