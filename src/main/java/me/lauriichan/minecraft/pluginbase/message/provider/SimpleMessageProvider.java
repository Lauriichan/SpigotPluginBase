package me.lauriichan.minecraft.pluginbase.message.provider;

import java.util.concurrent.ConcurrentHashMap;

import me.lauriichan.laylib.localization.MessageProvider;

public final class SimpleMessageProvider extends MessageProvider implements ISimpleMessageProvider {

    private final ConcurrentHashMap<String, SimpleMessage> messages = new ConcurrentHashMap<>();
    private final String fallback;

    public SimpleMessageProvider(final String id, final String fallback) {
        super(id);
        this.fallback = fallback;
    }

    @Override
    public SimpleMessage getMessage(final String language) {
        SimpleMessage message = messages.get(language);
        if (message != null) {
            return message;
        }
        message = new SimpleMessage(this, language);
        if (language != null && !language.isBlank()) {
            messages.put(language, message);
        }
        return message;
    }

    @Override
    public String getFallback() {
        return fallback;
    }

}