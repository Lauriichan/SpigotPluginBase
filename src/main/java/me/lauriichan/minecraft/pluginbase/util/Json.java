package me.lauriichan.minecraft.pluginbase.util;

import java.io.IOException;
import java.io.Reader;

import me.lauriichan.laylib.json.IJson;
import me.lauriichan.laylib.json.io.JsonParser;
import me.lauriichan.laylib.json.io.JsonSyntaxException;
import me.lauriichan.laylib.json.io.JsonWriter;

public final class Json {

    private final JsonWriter writer;

    public Json(final JsonWriter writer) {
        this.writer = writer;
    }

    public JsonWriter writer() {
        return writer;
    }

    public IJson<?> asJson(String string) throws IllegalStateException, IllegalArgumentException {
        try {
            return JsonParser.fromString(string);
        } catch (IOException | JsonSyntaxException e) {
            throw new IllegalArgumentException("Failed to parse json element", e);
        }
    }

    public IJson<?> asJson(Reader reader) throws IllegalStateException, IllegalArgumentException {
        try {
            return JsonParser.fromReader(reader);
        } catch (IOException | JsonSyntaxException e) {
            throw new IllegalArgumentException("Failed to parse json element", e);
        }
    }

    public String asString(IJson<?> element) throws IOException {
        try {
            return writer.toString(element);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to serialize json element", e);
        }
    }

}
