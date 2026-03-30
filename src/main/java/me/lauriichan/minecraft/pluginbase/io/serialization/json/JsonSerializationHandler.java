package me.lauriichan.minecraft.pluginbase.io.serialization.json;

import me.lauriichan.laylib.json.*;
import me.lauriichan.minecraft.pluginbase.io.HandlerPoint;
import me.lauriichan.minecraft.pluginbase.io.serialization.SerializationHandler;

@HandlerPoint
public abstract class JsonSerializationHandler<V> extends SerializationHandler<JsonObject, V> {

    public JsonSerializationHandler(Class<V> type) {
        super(JsonObject.class, type);
    }

    @Override
    public abstract V deserialize(JsonObject buffer);

    @Override
    public final JsonObject serialize(V value) {
        JsonObject object = new JsonObject();
        serialize(object, value);
        return object;
    }

    protected abstract void serialize(JsonObject buffer, V value);

}
