package me.lauriichan.minecraft.pluginbase.io.serialization;

import me.lauriichan.minecraft.pluginbase.io.IIOHandler;

public abstract class SerializationHandler<B, V> implements IIOHandler<B, V> {

    protected final Class<B> bufferType;
    protected final Class<V> valueType;

    public SerializationHandler(final Class<B> bufferType, final Class<V> valueType) {
        this.bufferType = bufferType;
        this.valueType = valueType;
    }

    @Override
    public final Class<B> bufferType() {
        return bufferType;
    }

    @Override
    public final Class<V> valueType() {
        return valueType;
    }

    public abstract B serialize(V value);
    
    public final B serializeAny(Object object) {
        if (object == null || !valueType.isAssignableFrom(object.getClass())) {
            return null;
        }
        return serialize(valueType.cast(object));
    }

    public abstract V deserialize(B buffer);
    
    public final V deserializeAny(Object object) {
        if (object == null || !bufferType.isAssignableFrom(object.getClass())) {
            return null;
        }
        return deserialize(bufferType.cast(object));
    }

}
