package me.lauriichan.minecraft.pluginbase.config.handler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.List;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.lauriichan.laylib.json.IJson;
import me.lauriichan.laylib.json.JsonArray;
import me.lauriichan.laylib.json.JsonObject;
import me.lauriichan.laylib.json.io.JsonWriter;
import me.lauriichan.minecraft.pluginbase.config.Configuration;
import me.lauriichan.minecraft.pluginbase.config.IConfigHandler;
import me.lauriichan.minecraft.pluginbase.resource.source.IDataSource;
import me.lauriichan.minecraft.pluginbase.util.Json;

public final class JsonConfigHandler implements IConfigHandler {

    public static final JsonConfigHandler JSON = new JsonConfigHandler();

    private final Json json = new Json(new JsonWriter().setPretty(true).setSpaces(true).setIndent(4));

    private JsonConfigHandler() {}

    public Json json() {
        return json;
    }

    @Override
    public void load(final Configuration configuration, final IDataSource source) throws Exception {
        IJson<?> element;
        try (BufferedReader reader = source.openReader()) {
            element = json.asJson(reader);
        }
        if (!element.isObject()) {
            throw new IllegalStateException("Config source doesn't contain a JsonObject");
        }
        loadToConfig(element.asJsonObject(), configuration);
    }

    private void loadToConfig(final JsonObject object, final Configuration configuration) {
        for (final String key : object.keySet()) {
            final IJson<?> element = object.get(key);
            if (element.isNull()) {
                continue;
            }
            if (element.isObject()) {
                loadToConfig(element.asJsonObject(), configuration.getConfiguration(key, true));
                continue;
            }
            if (element.isArray()) {
                configuration.set(key, deserialize(element.asJsonArray()));
                continue;
            }
            configuration.set(key, deserialize(element));
        }
    }

    @SuppressWarnings({
        "rawtypes",
        "unchecked"
    })
    private List deserialize(final JsonArray array) {
        final ObjectArrayList list = new ObjectArrayList();
        for (final IJson<?> arrayElement : array) {
            if (arrayElement.isObject() || arrayElement.isNull()) {
                continue;
            }
            if (arrayElement.isArray()) {
                list.add(deserialize(arrayElement.asJsonArray()));
                continue;
            }
            list.add(deserialize(arrayElement));
        }
        return list;
    }

    private Object deserialize(final IJson<?> primitive) {
        if (primitive.isBoolean()) {
            return primitive.asJsonBoolean().value();
        }
        if (primitive.isNumber()) {
            return primitive.asJsonNumber().value();
        }
        return primitive.asJsonString().value();
    }

    @Override
    public void save(final Configuration configuration, final IDataSource source) throws Exception {
        final JsonObject root = new JsonObject();
        saveToObject(root, configuration);
        try (BufferedWriter writer = source.openWriter()) {
            writer.write(json.asString(root));
        }
    }

    private void saveToObject(final JsonObject object, final Configuration configuration) {
        IJson<?> json;
        for (final String key : configuration.keySet()) {
            if (configuration.isConfiguration(key)) {
                final JsonObject child = new JsonObject();
                saveToObject(child, configuration.getConfiguration(key));
                object.put(key, child);
                continue;
            }
            json = serialize(configuration.get(key));
            if (json == null) {
                continue;
            }
            object.put(key, json);
        }
    }

    private IJson<?> serialize(final Object object) {
        if (object instanceof final List<?> list) {
            final JsonArray array = new JsonArray();
            for (final Object elem : list) {
                array.add(serialize(elem));
            }
            return array;
        }
        if (object instanceof Enum<?> enumObject) {
            return IJson.of(enumObject.toString());
        }
        try {
            return IJson.of(object);
        } catch (IllegalArgumentException e) {
            return null; // Ignore unsupported types
        }
    }

}
