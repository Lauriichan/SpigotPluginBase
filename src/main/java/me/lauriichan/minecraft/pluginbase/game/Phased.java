package me.lauriichan.minecraft.pluginbase.game;

public @interface Phased {

    boolean blacklist() default false;

    Class<? extends Phase<?>>[] phase() default {};

}
