package me.lauriichan.minecraft.pluginbase.util.instance;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

public class SharedInstances<E> {

    private final Object2ObjectOpenHashMap<Class<?>, E> instances = new Object2ObjectOpenHashMap<>();
    private final IInstanceInvoker invoker;
    
    public SharedInstances() {
        this(IInstanceInvoker.DEFAULT);
    }

    public SharedInstances(final IInstanceInvoker invoker) {
        this.invoker = invoker;
    }

    public final void remove(final ClassLoader loader) {
        ObjectIterator<Object2ObjectOpenHashMap.Entry<Class<?>, E>> iterator = instances.object2ObjectEntrySet().fastIterator();
        while (iterator.hasNext()) {
            if (loader.equals(iterator.next().getKey().getClassLoader())) {
                iterator.remove();
            }
        }
    }

    public final void remove(final Class<?> clazz) {
        instances.remove(clazz);
    }

    public final <T> T get(final Class<T> clazz) {
        E extension = instances.get(clazz);
        if (extension == null) {
            return null;
        }
        return clazz.cast(extension);
    }

    public final <T extends E> T get(final Class<T> clazz, final Object... arguments) throws Throwable {
        if (clazz.getAnnotation(Shared.class) == null) {
            return invoker.invoke(clazz, arguments);
        }
        E extensionRaw = instances.get(clazz);
        if (extensionRaw != null) {
            return clazz.cast(extensionRaw);
        }
        T extension = invoker.invoke(clazz, arguments);
        instances.put(clazz, extension);
        return extension;
    }

}
