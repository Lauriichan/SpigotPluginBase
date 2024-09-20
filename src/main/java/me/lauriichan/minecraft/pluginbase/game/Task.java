package me.lauriichan.minecraft.pluginbase.game;

import me.lauriichan.minecraft.pluginbase.extension.ExtensionPoint;
import me.lauriichan.minecraft.pluginbase.extension.IExtension;

@ExtensionPoint
public abstract class Task<G extends Game> implements IExtension {
    
    protected abstract void onTick(GameState<G> state, long tick);

}
