package me.lauriichan.minecraft.pluginbase.game.phased;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import me.lauriichan.minecraft.pluginbase.game.Phase;

@Target({
    ElementType.TYPE,
    ElementType.METHOD
})
@Retention(RetentionPolicy.RUNTIME)
public @interface Phased {

    boolean blacklist() default false;

    Class<? extends Phase<?>>[] phase() default {};

}
