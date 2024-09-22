package me.lauriichan.minecraft.pluginbase.game;

import me.lauriichan.minecraft.pluginbase.extension.ExtensionPoint;
import me.lauriichan.minecraft.pluginbase.extension.IExtension;
import me.lauriichan.minecraft.pluginbase.util.tick.AbstractTickTimer;

@ExtensionPoint
public abstract class Game<G extends Game<G>> implements IExtension {
    
    protected void onStart(GameState<G> state, AbstractTickTimer timer) {}
    
    protected void onTick(GameState<G> state, long delta) {}
    
    protected void onTickPostPhase(GameState<G> state, long delta) {}
    
    protected void onTickPostTask(GameState<G> state, long delta) {}
    
    protected void onStop(GameState<G> state) {}
    
    protected boolean shouldRestart(GameState<G> state) {
        return true;
    }

}
