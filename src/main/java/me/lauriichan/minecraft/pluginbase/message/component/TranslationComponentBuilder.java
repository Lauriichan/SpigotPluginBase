package me.lauriichan.minecraft.pluginbase.message.component;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import net.md_5.bungee.chat.TranslationRegistry;

public final class TranslationComponentBuilder<P extends ComponentBuilder<?, ?>>
    extends SubComponentBuilder<P, TranslationComponentBuilder<P>> {

    private final TranslatableComponent component = new TranslatableComponent();

    TranslationComponentBuilder(P parent) {
        super(parent);
    }

    @Override
    protected TranslatableComponent component() {
        return component;
    }

    public TranslationComponentBuilder<P> translationId(String translationId) {
        component.setTranslate(translationId);
        return this;
    }

    public String translationId() {
        return component.getTranslate();
    }

    public TranslationComponentBuilder<P> fallback(String fallback) {
        component.setFallback(fallback);
        return this;
    }

    public String fallback() {
        return component.getFallback();
    }

    @Override
    public TranslationComponentBuilder<P> loadFrom(SubComponentBuilder<?, ?> component) {
        if (component instanceof TranslationComponentBuilder<?> otherComponent) {
            this.component.setTranslate(otherComponent.component.getTranslate());
            this.component.setFallback(otherComponent.component.getFallback());
        }
        return super.loadFrom(component);
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty() && (component.getTranslate() == null || component.getTranslate().isEmpty());
    }

    @Override
    public BaseComponent buildComponent() {
        TranslatableComponent output = component.duplicate();
        if (!super.isEmpty()) {
            output.setExtra(buildComponentList());
        }
        return output;
    }

    private String translatedString() {
        String translated = TranslationRegistry.INSTANCE.translate(component.getTranslate());
        if (translated.equals(component.getTranslate()) && component.getFallback() != null) {
            return component.getFallback();
        }
        return translated;
    }

    @Override
    public String asPlainText() {
        if (builders.isEmpty()) {
            return translatedString();
        }
        StringBuilder builder = new StringBuilder(translatedString());
        for (int i = 0; i < builders.size(); i++) {
            builder.append(builders.get(i).asPlainText());
        }
        return builder.toString();
    }

}
