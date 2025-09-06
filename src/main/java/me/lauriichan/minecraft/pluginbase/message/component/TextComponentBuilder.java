package me.lauriichan.minecraft.pluginbase.message.component;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

public final class TextComponentBuilder<P extends ComponentBuilder<?, ?>> extends SubComponentBuilder<P, TextComponentBuilder<P>> {

    public static TextComponentBuilder<?> parse(String richString) {
        return ComponentBuilder.create().appendContent(richString);
    }

    private final TextComponent component = new TextComponent();

    TextComponentBuilder(P parent) {
        super(parent);
    }

    @Override
    protected TextComponent component() {
        return component;
    }

    public TextComponentBuilder<P> text(String text) {
        component.setText(text);
        return this;
    }

    public TextComponentBuilder<P> appendText(String text) {
        String compText = component.getText();
        if (compText == null) {
            compText = "";
        }
        component.setText(compText + text);
        return this;
    }

    public TextComponentBuilder<P> appendChar(char ch) {
        String compText = component.getText();
        if (compText == null) {
            compText = "";
        }
        component.setText(compText + ch);
        return this;
    }

    public String text() {
        return component.getText();
    }

    @Override
    public TextComponentBuilder<P> loadFrom(SubComponentBuilder<?, ?> component) {
        if (component instanceof TextComponentBuilder<?> otherComponent) {
            this.component.setText(otherComponent.component.getText());
        }
        return super.loadFrom(component);
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty() && (component.getText() == null || component.getText().isEmpty());
    }

    @Override
    public BaseComponent buildComponent() {
        TextComponent output = component.duplicate();
        if (!super.isEmpty()) {
            output.setExtra(buildComponentList());
        }
        return output;
    }

    @Override
    public String asPlainText() {
        if (builders.isEmpty()) {
            return component.getText();
        }
        StringBuilder builder = new StringBuilder(component.getText());
        for (int i = 0; i < builders.size(); i++) {
            builder.append(builders.get(i).asPlainText());
        }
        return builder.toString();
    }

}
