package me.lauriichan.minecraft.pluginbase.config;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;

public final class Configuration {

    private final Object2ObjectLinkedOpenHashMap<String, Object> map = new Object2ObjectLinkedOpenHashMap<>();
    
    /*
     * Keys
     */

    public Set<String> keySet() {
        return map.keySet();
    }
    
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /*
     * Add / Remove
     */

    public void set(final String pathUri, final Object object) {
        if (object == null) {
            remove(pathUri);
            return;
        }
        if (object instanceof Configuration) {
            throw new IllegalStateException("Can't put a configuration into another configuration!");
        }
        if (!pathUri.contains(".")) {
            map.put(pathUri, object);
            return;
        }
        final String[] path = pathUri.split("\\.");
        findConfiguration(path, path.length - 1, true).map.put(path[path.length - 1], object);
    }

    public void remove(final String pathUri) {
        if (!pathUri.contains(".")) {
            map.remove(pathUri);
            return;
        }
        final String[] path = pathUri.split("\\.");
        final Configuration config = findConfiguration(path, path.length - 1, false);
        if (config == null) {
            return;
        }
        config.map.remove(path[path.length - 1]);
    }

    public void clear() {
        map.clear();
    }

    /*
     * Contains
     */

    public boolean contains(final String pathUri) {
        if (!pathUri.contains(".")) {
            return map.containsKey(pathUri);
        }
        final String[] path = pathUri.split("\\.");
        final Configuration config = findConfiguration(path, path.length - 1, false);
        return config != null && config.map.containsKey(path[path.length - 1]);
    }

    public boolean contains(final String pathUri, final Class<?> type) {
        if (!pathUri.contains(".")) {
            final Object object = map.get(pathUri);
            return object != null && type.isAssignableFrom(object.getClass());
        }
        final String[] path = pathUri.split("\\.");
        final Configuration config = findConfiguration(path, path.length - 1, false);
        if (config == null) {
            return false;
        }
        final Object object = map.get(path[path.length - 1]);
        return object != null && type.isAssignableFrom(object.getClass());
    }

    /*
     * Getter
     */

    public Object get(final String pathUri) {
        if (!pathUri.contains(".")) {
            return map.get(pathUri);
        }
        final String[] path = pathUri.split("\\.");
        final Configuration config = findConfiguration(path, path.length - 1, false);
        if (config == null) {
            return null;
        }
        return config.map.get(path[path.length - 1]);
    }

    public <E> E get(final String pathUri, final Class<E> type) {
        if (type.isEnum()) {
            return type.cast(getEnum(pathUri, type.asSubclass(Enum.class)));
        }
        final Object object = get(pathUri);
        if (object == null || !type.isAssignableFrom(object.getClass())) {
            return null;
        }
        return type.cast(object);
    }

    public <E> E get(final String pathUri, final Class<E> type, final E fallback) {
        if (type.isEnum()) {
            E value = type.cast(getEnum(pathUri, type.asSubclass(Enum.class)));
            if (value == null) {
                return fallback;
            }
            return value;
        }
        final Object object = get(pathUri);
        if (object == null || !type.isAssignableFrom(object.getClass())) {
            return fallback;
        }
        return type.cast(object);
    }

    /*
     * Primitive getter
     */

    public boolean getBoolean(final String pathUri) {
        return getBoolean(pathUri, false);
    }

    public boolean getBoolean(final String pathUri, final boolean fallback) {
        final Object object = get(pathUri);
        if (object == null || !(object instanceof Boolean)) {
            return fallback;
        }
        return (Boolean) object;
    }

    public byte getByte(final String pathUri) {
        return getByte(pathUri, (byte) 0);
    }

    public byte getByte(final String pathUri, final byte fallback) {
        final Object object = get(pathUri);
        if (object == null || !(object instanceof Number)) {
            return fallback;
        }
        return ((Number) object).byteValue();
    }

    public short getShort(final String pathUri) {
        return getShort(pathUri, (short) 0);
    }

    public short getShort(final String pathUri, final short fallback) {
        final Object object = get(pathUri);
        if (object == null || !(object instanceof Number)) {
            return fallback;
        }
        return ((Number) object).shortValue();
    }

    public int getInt(final String pathUri) {
        return getInt(pathUri, 0);
    }

    public int getInt(final String pathUri, final int fallback) {
        final Object object = get(pathUri);
        if (object == null || !(object instanceof Number)) {
            return fallback;
        }
        return ((Number) object).intValue();
    }

    public long getLong(final String pathUri) {
        return getLong(pathUri, 0L);
    }

    public long getLong(final String pathUri, final long fallback) {
        final Object object = get(pathUri);
        if (object == null || !(object instanceof Number)) {
            return fallback;
        }
        return ((Number) object).longValue();
    }

    public float getFloat(final String pathUri) {
        return getFloat(pathUri, 0f);
    }

    public float getFloat(final String pathUri, final float fallback) {
        final Object object = get(pathUri);
        if (object == null || !(object instanceof Number)) {
            return fallback;
        }
        return ((Number) object).floatValue();
    }

    public double getDouble(final String pathUri) {
        return getDouble(pathUri, 0d);
    }

    public double getDouble(final String pathUri, final double fallback) {
        final Object object = get(pathUri);
        if (object == null || !(object instanceof Number)) {
            return fallback;
        }
        return ((Number) object).doubleValue();
    }

    /*
     * Special getter
     */

    public <E extends Enum<E>> E getEnum(final String pathUri, final Class<E> enumClazz) {
        return getEnum(pathUri, enumClazz, null);
    }

    public <E extends Enum<E>> E getEnum(final String pathUri, final Class<E> enumClazz, final E fallback) {
        final Object object = get(pathUri);
        if (object == null) {
            return fallback;
        }
        if (object.getClass() == enumClazz) {
            return enumClazz.cast(object);
        }
        if (!(object instanceof String)) {
            return fallback;
        }
        String string = (String) object;
        try {
            return Enum.valueOf(enumClazz, string);
        } catch (IllegalArgumentException exp1) {
            try {
                return Enum.valueOf(enumClazz, string.toUpperCase());
            } catch(IllegalArgumentException exp2) {
                try {
                    return Enum.valueOf(enumClazz, string.toLowerCase());
                } catch(IllegalArgumentException exp3) {
                    return fallback;
                }
            }
        }
    }

    public Number getNumber(final String pathUri) {
        return getNumber(pathUri, 0);
    }

    public Number getNumber(final String pathUri, final Number fallback) {
        final Object object = get(pathUri);
        if (object == null || !(object instanceof Number)) {
            return fallback;
        }
        return (Number) object;
    }

    public boolean isConfiguration(final String pathUri) {
        return contains(pathUri, Configuration.class);
    }

    public Configuration getConfiguration(final String pathUri) {
        return getConfiguration(pathUri, false);
    }

    public Configuration getConfiguration(final String pathUri, final boolean createIfNotExists) {
        return findConfiguration(pathUri.split("\\."), createIfNotExists);
    }

    public <E> List<E> getList(final String pathUri, final Class<E> type) {
        final Object object = get(pathUri);
        if (object == null || !(object instanceof List)) {
            return Collections.emptyList();
        }
        try {
            return (List<E>) object;
        } catch (final ClassCastException e) {
            return Collections.emptyList();
        }
    }

    public <K, V> Map<K, V> getMap(final String pathUri, final Class<K> keyType, final Class<V> valueType) {
        final Object object = get(pathUri);
        if (object == null || !(object instanceof Map)) {
            return Collections.emptyMap();
        }
        try {
            return (Map<K, V>) map;
        } catch (final ClassCastException e) {
            return Collections.emptyMap();
        }
    }

    /*
     * Helper
     */

    private Configuration findConfiguration(final String[] path, final boolean createIfNotExists) {
        return findConfiguration(path, path.length, createIfNotExists);
    }

    private Configuration findConfiguration(final String[] path, int length, final boolean createIfNotExists) {
        Configuration current = this;
        String part;
        length = Math.min(length, path.length);
        for (int index = 0; index < length; index++) {
            Object obj = current.map.get(part = path[index]);
            if (obj == null || !(obj instanceof Configuration)) {
                if (!createIfNotExists) {
                    return null;
                }
                final Configuration tmp = new Configuration();
                current.map.put(part, tmp);
                current = tmp;
            } else {
                current = (Configuration) obj;
            }
        }
        return current;
    }

}
