package me.lauriichan.minecraft.pluginbase.game.phased;

import it.unimi.dsi.fastutil.objects.ObjectList;
import me.lauriichan.minecraft.pluginbase.game.Phase;

public interface IPhased<T> {
    
    T get();
    
    boolean isBlacklist();
    
    ObjectList<Class<? extends Phase<?>>> phases();
    
    boolean shouldBeActive(Class<? extends Phase<?>> phase);

}
