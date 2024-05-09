package me.lauriichan.minecraft.pluginbase.message.component;

import java.awt.Color;
import java.util.Objects;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import me.lauriichan.laylib.command.ActionMessage;
import me.lauriichan.laylib.command.Actor;
import me.lauriichan.laylib.localization.IMessage;
import me.lauriichan.laylib.localization.MessageProvider;
import me.lauriichan.minecraft.pluginbase.util.LinearColor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

public abstract class ComponentBuilder<P extends ComponentBuilder<?, ?>, S extends ComponentBuilder<P, S>> implements ISendable {

    private static class ComponentBuilderImpl extends ComponentBuilder<ComponentBuilderImpl, ComponentBuilderImpl> {}

    public static ComponentBuilder<?, ?> create() {
        return new ComponentBuilderImpl();
    }

    protected final ObjectArrayList<SubComponentBuilder<?>> builders = new ObjectArrayList<>();
    protected final P parent;

    @SuppressWarnings("unchecked")
    protected final S self = (S) this;

    @SuppressWarnings("unchecked")
    public ComponentBuilder() {
        this.parent = (P) this;
    }

    public ComponentBuilder(final P parent) {
        this.parent = parent;
    }

    protected final void add(SubComponentBuilder<?> builder) {
        if (builders.contains(builder) || builder.parent != this) {
            return;
        }
        builders.add(builder);
    }

    public SubComponentBuilder<S> newComponent() {
        return new SubComponentBuilder<>(self);
    }

    public TextAppender<S> newText() {
        return new TextAppender<>(self);
    }

    public SubComponentBuilder<S> appendContent(final String richString) {
        if (richString == null || richString.isEmpty()) {
            return newComponent();
        }
        if (richString.isBlank()) {
            return newComponent().text(richString);
        }
        return ComponentBuilderUtils.append(newComponent(), richString);
    }

    public SubComponentBuilder<S> appendContent(final ActionMessage message) {
        if (message == null) {
            throw new NullPointerException("ActionMessage can't be null");
        }
        if (message.message() == null || message.message().isEmpty()) {
            throw new IllegalArgumentException("Message content can't be empty");
        }
        return ComponentBuilderUtils.appendAction(self, message);
    }

    public SubComponentBuilder<S> appendContent(final IMessage message) {
        if (message == null) {
            throw new NullPointerException("Message can't be null");
        }
        return appendContent(message.value());
    }

    public SubComponentBuilder<S> appendContent(final MessageProvider provider, final Actor<?> actor) {
        return appendContent(provider, actor.getLanguage());
    }

    public SubComponentBuilder<S> appendContent(final MessageProvider provider) {
        return appendContent(provider, Actor.DEFAULT_LANGUAGE);
    }

    public SubComponentBuilder<S> appendContent(final MessageProvider provider, final String language) {
        if (provider == null) {
            throw new NullPointerException("Provider can't be null");
        }
        return appendContent(provider.getMessage(language));
    }

    public SubComponentBuilder<S> appendContent(ComponentBuilder<?, ?> builder) {
        if (builder == null || builder.isEmpty()) {
            throw new NullPointerException("Builder can't be null");
        }
        if (builder.isEmpty()) {
            throw new IllegalArgumentException("Empty builder, nothing to append");
        }
        SubComponentBuilder<S> append = newComponent();
        if (builder instanceof SubComponentBuilder<?> subBuilder) {
            append.copyFrom(subBuilder);
            append.text(subBuilder.text());
        }
        for (SubComponentBuilder<?> other : builder.builders) {
            append.newComponent().appendContent(other).finish();
        }
        return append;
    }

    public boolean isEmpty() {
        return builders.isEmpty();
    }

    public final ObjectList<BaseComponent> buildComponentList() {
        ObjectArrayList<BaseComponent> list = new ObjectArrayList<>(builders.size());
        for (SubComponentBuilder<?> builder : builders) {
            list.add(builder.buildComponent());
        }
        return list;
    }

    @Override
    public final BaseComponent[] buildComponentArray() {
        BaseComponent[] array = new BaseComponent[builders.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = builders.get(i).buildComponent();
        }
        return array;
    }

    public BaseComponent buildComponent() {
        return new TextComponent(buildComponentArray());
    }

    /*
     * Helper
     */

    public static final class TextAppender<P extends ComponentBuilder<?, ?>> {

        private final P parent;

        private TextAppender(final P parent) {
            this.parent = parent;
        }

        private String text;
        private Color start, end;
        private int colorAmount = -1;

        public TextAppender<P> text(String text) {
            this.text = text;
            return this;
        }

        public TextAppender<P> color(Color color) {
            return startColor(color);
        }

        public TextAppender<P> color(ChatColor color) {
            return startColor(color);
        }

        public Color color() {
            return start;
        }

        public TextAppender<P> startColor(Color color) {
            this.start = color;
            return this;
        }

        public TextAppender<P> startColor(ChatColor color) {
            return startColor(color.getColor());
        }

        public Color startColor() {
            return start;
        }

        public TextAppender<P> endColor(Color color) {
            this.end = color;
            return this;
        }

        public TextAppender<P> endColor(ChatColor color) {
            return endColor(color.getColor());
        }

        public Color endColor() {
            return end;
        }

        public TextAppender<P> colorAmount(int colorAmount) {
            this.colorAmount = colorAmount;
            return this;
        }

        public int colorAmount() {
            return colorAmount;
        }

        public P finish() {
            if (text == null) {
                return parent;
            }
            if (text.isBlank() || (start == null && end == null)) {
                parent.newComponent().text(text).finish();
                return parent;
            }
            if (Objects.equals(start, end) || (start != null && end == null) || (start == null && end != null)) {
                parent.newComponent().text(text).color(start == null ? end : start).finish();
                return parent;
            }
            int colorAmount = this.colorAmount;
            if (colorAmount == 1) {
                parent.newComponent().text(text).color(start == null ? end : start).finish();
                return parent;
            }
            if (colorAmount <= 0) {
                colorAmount = text.length();
            }
            LinearColor start = new LinearColor(this.start);
            LinearColor interpolation = start.calcInterpolationDifference(this.end);
            int characters = text.replaceAll("\\s+", "").length();
            int charsPerStep = Math.floorDiv(characters, colorAmount - 1);
            int remainingCharacters = characters - (charsPerStep * colorAmount);
            int currentChar = 0;
            double charMax = characters;
            char[] chars = text.toCharArray();
            if (charsPerStep == 1 && remainingCharacters == 0) {
                SubComponentBuilder<?> builder = parent.newComponent();
                for (int i = 0; i < chars.length; i++) {
                    char ch = chars[i];
                    if (Character.isWhitespace(ch)) {
                        builder.appendText(Character.toString(ch));
                        continue;
                    }
                    builder = builder.text(Character.toString(ch)).color(interpolation.toInterpolatedColor(start, ++currentChar / charMax))
                        .finish().newComponent();
                }
                if (!builder.isEmpty()) {
                    builder.finish();
                }
                return parent;
            }
            throw new IllegalStateException("Found remaining characters: " + remainingCharacters);
        }

    }

}
