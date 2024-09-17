package me.lauriichan.minecraft.pluginbase.io.serialization.json;

import java.util.Objects;

import me.lauriichan.laylib.json.*;
import me.lauriichan.minecraft.pluginbase.io.HandlerPoint;
import me.lauriichan.minecraft.pluginbase.io.serialization.SerializationHandler;

@HandlerPoint
@SuppressWarnings("rawtypes")
public abstract class JsonSerializationHandler<J extends IJson<?>, V> extends SerializationHandler<IJson, V> {

    protected static final JsonType<JsonObject> OBJECT = new JsonType<>() {
        @Override
        protected JsonObject from(IJson<?> json) {
            if (json.isObject()) {
                return json.asJsonObject();
            }
            return null;
        }
    };
    protected static final JsonType<JsonArray> ARRAY = new JsonType<>() {
        @Override
        protected JsonArray from(IJson<?> json) {
            if (json.isArray()) {
                return json.asJsonArray();
            }
            return null;
        }
    };
    protected static final JsonType<JsonString> STRING = new JsonType<>() {
        @Override
        protected JsonString from(IJson<?> json) {
            if (json.isString()) {
                return json.asJsonString();
            }
            if (json.isNull() || !json.isPrimitive()) {
                return null;
            }
            return new JsonString(json.value().toString());
        }
    };
    protected static final JsonType<JsonBoolean> BOOL = new JsonType<>() {
        @Override
        protected JsonBoolean from(IJson<?> json) {
            if (json.isBoolean()) {
                return json.asJsonBoolean();
            }
            if (json.isNull() || !json.isPrimitive()) {
                return null;
            }
            if (json.isString()) {
                return IJson.of(Boolean.parseBoolean(json.asString()));
            }
            return IJson.of(json.asNumber().intValue() == 1);
        }
    };
    protected static final JsonType<IJsonNumber<?>> NUMBER = new JsonType<>() {
        @Override
        protected IJsonNumber<?> from(IJson<?> json) {
            if (json.isNull() || !json.isPrimitive()) {
                return null;
            }
            if (json.isNumber()) {
                return json.asJsonNumber();
            }
            if (json.isBoolean()) {
                return IJson.of(json.asBoolean() ? 1 : 0);
            }
            try {
                return IJson.of(Double.valueOf(json.asString()));
            } catch (NumberFormatException nfe) {
                return null;
            }
        }
    };

    protected static abstract class JsonType<T extends IJson<?>> {
        private JsonType() {}

        protected abstract T from(IJson<?> json);
    }

    private final JsonType<J> jsonType;

    public JsonSerializationHandler(JsonType<J> jsonType, Class<V> type) {
        super(IJson.class, type);
        this.jsonType = Objects.requireNonNull(jsonType);
    }

    protected boolean allowNullJson() {
        return false;
    }

    protected V nullDefaultValue() {
        return null;
    }

    public abstract J toJson(V value);

    public abstract V fromJson(J json);

    @Override
    public V deserialize(IJson buffer) {
        J json = jsonType.from(buffer);
        if (json == null) {
            return nullDefaultValue();
        }
        return fromJson(json);
    }

    @Override
    public IJson serialize(V value) {
        return toJson(value);
    }

}
