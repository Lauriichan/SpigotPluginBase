package me.lauriichan.minecraft.pluginbase.game;

import me.lauriichan.minecraft.pluginbase.extension.ExtensionPoint;
import me.lauriichan.minecraft.pluginbase.extension.IExtension;

@ExtensionPoint
public abstract class Phase<G extends Game> implements IExtension {
    
    protected void onBegin(GameState<G> state) {}
    
    protected void onTick(GameState<G> state, long tick) {}
    
    protected void onEnd(GameState<G> state) {}
    
    protected abstract boolean nextPhase();

}
