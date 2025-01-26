package me.lauriichan.minecraft.pluginbase.config;

import java.util.Comparator;

public interface IConfigWrapper<T extends IConfigExtension> {

    final Comparator<IConfigWrapper<?>> ORDER_WRAPPER = (a, b) -> Integer.compare(b.order(), a.order());
    
    int order();
    
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
