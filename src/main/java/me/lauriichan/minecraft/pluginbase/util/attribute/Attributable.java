package me.lauriichan.minecraft.pluginbase.util.attribute;

import java.util.Set;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import me.lauriichan.laylib.reflection.ClassUtil;

public abstract class Attributable implements IAttributable {

    private final Object2ObjectArrayMap<String, Object> attributes = new Object2ObjectArrayMap<>();

    @Override
    public final Object attr(final String key) {
        return attributes.get(key);
    }

    @Override
    public final <T> T attr(final String key, final Class<T> type) {
        return attrOrDefault(key, type, null);
    }

    @Override
    public final <T> T attrOrDefault(final String key, final Class<T> type, final T fallback) {
        final Object obj = attributes.get(key);
        if (obj == null || !ClassUtil.toComplexType(type).isAssignableFrom(obj.getClass())) {
            return fallback;
        }
        if (ClassUtil.isPrimitiveType(type)) {
            return (T) toPrimitive(obj, type);
        }
        return type.cast(obj);
    }

    @Override
    public Class<?> attrClass(final String key) {
        Object object = attributes.get(key);
        if (object instanceof Class) {
            return (Class<?>) object;
        }
        return null;
    }

    @Override
    public <T> Class<? extends T> attrClass(final String key, final Class<T> type) {
        return attrClassOrDefault(key, type, null);
    }

    @Override
    public <T> Class<? extends T> attrClassOrDefault(final String key, final Class<T> type, final Class<? extends T> fallback) {
        Object object = attributes.get(key);
        if (object instanceof Class) {
            Class<?> clazz = (Class<?>) object;
            if (type.isAssignableFrom(clazz)) {
                return clazz.asSubclass(type);
            }
        }
        return fallback;
    }

    @Override
    public final boolean attrHas(final String key) {
        return attributes.containsKey(key);
    }

    @Override
    public final boolean attrHas(final String key, final Class<?> type) {
        final Object obj = attributes.get(key);
        return obj != null && ClassUtil.toComplexType(type).isAssignableFrom(obj.getClass());
    }

    @Override
    public final void attrSet(final String key, final Object object) {
        if (object == null) {
            attributes.remove(key);
            return;
        }
        attributes.put(key, object);
    }

    @Override
    public final Object attrUnset(final String key) {
        return attributes.remove(key);
    }

    @Override
    public <T> T attrUnset(final String key, final Class<T> type) {
        return attrUnsetOrDefault(key, type, null);
    }

    @Override
    public <T> T attrUnsetOrDefault(final String key, final Class<T> type, final T fallback) {
        final Object obj = attributes.remove(key);
        if (obj == null || !ClassUtil.toComplexType(type).isAssignableFrom(obj.getClass())) {
            return fallback;
        }
        if (ClassUtil.isPrimitiveType(type)) {
            return (T) toPrimitive(obj, type);
        }
        return type.cast(obj);
    }

    @Override
    public final void attrClear() {
        attributes.clear();
    }

    @Override
    public final int attrAmount() {
        return attributes.size();
    }

    @Override
    public final Set<String> attrKeys() {
        return attributes.keySet();
    }
    
    private Object toPrimitive(Object object, Class<?> type) {
        if (type == byte.class) {
            return ((Byte) object).byteValue();
        }
        if (type == short.class) {
            return ((Short) object).shortValue();
        }
        if (type == int.class) {
            return ((Integer) object).intValue();
        }
        if (type == long.class) {
            return ((Long) object).longValue();
        }
        if (type == float.class) {
            return ((Float) object).floatValue();
        }
        if (type == double.class) {
            return ((Double) object).doubleValue();
        }
        if (type == boolean.class) {
            return ((Boolean) object).booleanValue();
        }
        return object;
    }

}