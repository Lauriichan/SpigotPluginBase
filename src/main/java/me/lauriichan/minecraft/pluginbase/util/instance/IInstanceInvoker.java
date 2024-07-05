package me.lauriichan.minecraft.pluginbase.util.instance;

import me.lauriichan.minecraft.pluginbase.util.ReflectionUtil;

public interface IInstanceInvoker {
    
    public static final IInstanceInvoker DEFAULT = ReflectionUtil::createInstanceThrows;
    
    <T> T invoke(final Class<T> clazz, final Object... arguments) throws Throwable;

}
