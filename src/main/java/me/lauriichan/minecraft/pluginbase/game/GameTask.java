package me.lauriichan.minecraft.pluginbase.game;

public @interface GameTask {

    Class<? extends Game<?>> game();

    int orderId() default 0;

}
