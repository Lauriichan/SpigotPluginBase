package me.lauriichan.minecraft.pluginbase.config;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(SOURCE)
@Target({FIELD})
public @interface ConfigValue {

    String value();

}
