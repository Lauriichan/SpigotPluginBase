package me.lauriichan.minecraft.pluginbase.config.startup;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import me.lauriichan.minecraft.pluginbase.config.Configuration;

final class EnumPropertyIO<E extends Enum<E>> implements IPropertyIO<E> {

    private static final Object2ObjectArrayMap<Class<?>, EnumPropertyIO<?>> ENUM_PROPERTY_MAP = new Object2ObjectArrayMap<>();

    public static <T extends Enum<T>> EnumPropertyIO<T> of(Class<T> type) {
        EnumPropertyIO<?> io = ENUM_PROPERTY_MAP.get(type);
        if (io == null) {
            ENUM_PROPERTY_MAP.put(type, io = new EnumPropertyIO<>(type));
        }
        return (EnumPropertyIO<T>) io;
    }

    private final Class<E> type;

    private EnumPropertyIO(Class<E> type) {
        this.type = type;
    }

    @Override
    public E read(Configuration configuration, String path) {
        return configuration.getEnum(path, type);
    }

    @Override
    public void write(Configuration configuration, String path, E value) {
        configuration.set(path, value);
    }

}
