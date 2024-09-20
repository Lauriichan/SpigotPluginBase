package me.lauriichan.minecraft.pluginbase.game;

import me.lauriichan.minecraft.pluginbase.extension.ExtensionPoint;
import me.lauriichan.minecraft.pluginbase.extension.IExtension;
import me.lauriichan.minecraft.pluginbase.util.tick.AbstractTickTimer;

@ExtensionPoint
public abstract class Game implements IExtension {
    
    protected void onStart(GameState<?> state, AbstractTickTimer timer) {}
    
    protected void onTick(GameState<?> state, long tick) {}
    
    protected void onTickPostPhase(GameState<?> state, long tick) {}
    
    protected void onTickPostTask(GameState<?> state, long tick) {}
    
    protected void onStop(GameState<?> state) {}
    
    protected boolean shouldRestart(GameState<?> state) {
        return true;
    }

}
