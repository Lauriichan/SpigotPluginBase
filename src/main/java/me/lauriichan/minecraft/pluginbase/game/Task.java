package me.lauriichan.minecraft.pluginbase.game;

import me.lauriichan.minecraft.pluginbase.extension.ExtensionPoint;
import me.lauriichan.minecraft.pluginbase.extension.IExtension;

@ExtensionPoint
public abstract class Task<G extends Game<G>> implements IExtension {

    protected void onStart(GameState<G> state) {}

    protected void onTick(GameState<G> state, long delta) {}

    protected void onStop(GameState<G> state) {}

}
