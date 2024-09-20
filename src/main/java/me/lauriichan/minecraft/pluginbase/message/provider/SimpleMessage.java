package me.lauriichan.minecraft.pluginbase.message.provider;

import me.lauriichan.laylib.localization.IMessage;

public final class SimpleMessage implements IMessage, ISimpleMessage {

    private final SimpleMessageProvider provider;
    private final String language;

    private String translation;

    public SimpleMessage(final SimpleMessageProvider provider, final String language) {
        this.provider = provider;
        this.language = language;
    }

    @Override
    public void setTranslation(final String translation) {
        if (translation == null || translation.isBlank()) {
            this.translation = null;
            return;
        }
        this.translation = translation;
    }

    @Override
    public String id() {
        return provider.getId();
    }

    @Override
    public String language() {
        return language;
    }

    @Override
    public String value() {
        if (translation == null) {
            return provider.getFallback();
        }
        return translation;
    }

    @Override
    public String fallback() {
        return provider.getFallback();
    }

}