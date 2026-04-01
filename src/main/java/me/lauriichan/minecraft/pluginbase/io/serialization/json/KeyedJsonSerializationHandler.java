package me.lauriichan.minecraft.pluginbase.io.serialization.json;

import java.util.Objects;

import me.lauriichan.laylib.json.*;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.io.HandlerPoint;

@HandlerPoint
public abstract class KeyedJsonSerializationHandler<J extends IJson<?>, V> extends JsonSerializationHandler<V> {

    public static final JsonType<JsonObject> OBJECT = new JsonType<>() {
        @Override
        public JsonObject from(IJson<?> json) {
            if (json != null && json.isObject()) {
                return json.asJsonObject();
            }
            return null;
        }
    };
    public static final JsonType<JsonArray> ARRAY = new JsonType<>() {
        @Override
        public JsonArray from(IJson<?> json) {
            if (json != null && json.isArray()) {
                return json.asJsonArray();
            }
            return null;
        }
    };
    public static final JsonType<JsonString> STRING = new JsonType<>() {
        @Override
        public JsonString from(IJson<?> json) {
            String string = asString(json);
            if (string == null) {
                return null;
            }
            return new JsonString(string);
        }

        private String asString(IJson<?> json) {
            if (json == null || json.isNull()) {
                return null;
            }
            if (json.isString()) {
                return json.value().toString();
            }
            if (json.isArray()) {
                JsonArray array = json.asJsonArray();
                StringBuilder builder = new StringBuilder();
                for (IJson<?> entry : array) {
                    String string = asString(entry);
                    if (string == null) {
                        continue;
                    }
                    if (!builder.isEmpty()) {
                        builder.append('\n');
                    }
                    builder.append(string);
                }
                return builder.toString();
            }
            if (!json.isPrimitive()) {
                return null;
            }
            return json.value().toString();
        }
    };
    public static final JsonType<JsonBoolean> BOOL = new JsonType<>() {
        @Override
        public JsonBoolean from(IJson<?> json) {
            if (json == null || json.isNull() || !json.isPrimitive()) {
                return null;
            }
            if (json.isBoolean()) {
                return json.asJsonBoolean();
            }
            if (json.isString()) {
                return IJson.of(Boolean.parseBoolean(json.asString()));
            }
            return IJson.of(json.asNumber().intValue() == 1);
        }
    };
    public static final JsonType<IJsonNumber<?>> NUMBER = new JsonType<>() {
        @Override
        public IJsonNumber<?> from(IJson<?> json) {
            if (json == null || json.isNull() || !json.isPrimitive()) {
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

    public static abstract class JsonType<T extends IJson<?>> {
        private JsonType() {}

        public abstract T from(IJson<?> json);
    }

    private final String key;
    private final JsonType<J> jsonType;

    public KeyedJsonSerializationHandler(BasePlugin<?> plugin, String key, JsonType<J> jsonType, Class<V> type) {
        super(plugin, type);
        this.key = Objects.requireNonNull(key).trim();
        if (key.isEmpty()) {
            throw new IllegalArgumentException("Key can't be empty");
        }
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
    public V deserialize(JsonObject buffer) {
        J json = jsonType.from(buffer.get(key));
        if (json == null && !allowNullJson()) {
            return nullDefaultValue();
        }
        return fromJson(json);
    }

    @Override
    protected void serialize(JsonObject buffer, V value) {
        IJson<?> json = toJson(value);
        if (json == null) {
            return;
        }
        buffer.put(key, json);
    }

}
