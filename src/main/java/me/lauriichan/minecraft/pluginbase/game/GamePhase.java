package me.lauriichan.minecraft.pluginbase.game;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface GamePhase {

    Class<? extends Game<?>> game();

    /**
     * Execute order is lowest -> highest
     * 
     * @return the order id
     */
    int orderId();

}
