package me.lauriichan.minecraft.pluginbase.game;

public @interface GamePhase {

    Class<? extends Game> game();

    int orderId();

}
