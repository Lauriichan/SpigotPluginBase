package me.lauriichan.minecraft.pluginbase.message.component;

import java.awt.Color;

import it.unimi.dsi.fastutil.objects.ObjectList;
import me.lauriichan.laylib.command.Actor;
import me.lauriichan.laylib.localization.IMessage;
import me.lauriichan.laylib.localization.MessageProvider;
import me.lauriichan.laylib.logger.util.StringUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Entity;

public final class SubComponentBuilder<P extends ComponentBuilder<?, ?>> extends ComponentBuilder<P, SubComponentBuilder<P>> {

    private final TextComponent component = new TextComponent();

    SubComponentBuilder(P parent) {
        super(parent);
    }

    @SuppressWarnings({
        "unchecked",
        "rawtypes"
    })
    @Override
    public SubComponentBuilder<SubComponentBuilder<P>> newComponent() {
        return new SubComponentBuilder(this);
    }

    public SubComponentBuilder<P> color(Color color) {
        return color(ChatColor.of(color));
    }

    public SubComponentBuilder<P> color(ChatColor color) {
        component.setColor(color);
        return this;
    }

    public ChatColor color() {
        return component.getColor();
    }

    public SubComponentBuilder<P> apply(Formatting formatting) {
        formatting.apply(component, true);
        return this;
    }

    public SubComponentBuilder<P> unapply(Formatting formatting) {
        formatting.apply(component, false);
        return this;
    }

    public boolean hasFormatting(Formatting formatting) {
        return formatting.isApplied(component);
    }

    public SubComponentBuilder<P> text(String text) {
        component.setText(text);
        return this;
    }

    public SubComponentBuilder<P> appendText(String text) {
        String compText = component.getText();
        if (compText == null) {
            compText = "";
        }
        component.setText(compText + text);
        return this;
    }

    public String text() {
        return component.getText();
    }

    public SubComponentBuilder<P> clickUrl(final String url, final Object... format) {
        return clickUrl(StringUtil.format(url, format));
    }

    public SubComponentBuilder<P> clickFile(final String file, final Object... format) {
        return clickFile(StringUtil.format(file, format));
    }

    public SubComponentBuilder<P> clickCopy(final String copy, final Object... format) {
        return clickCopy(StringUtil.format(copy, format));
    }

    public SubComponentBuilder<P> clickSuggest(final String suggest, final Object... format) {
        return clickSuggest(StringUtil.format(suggest, format));
    }

    public SubComponentBuilder<P> clickRun(final String run, final Object... format) {
        return clickRun(StringUtil.format(run, format));
    }

    public SubComponentBuilder<P> clickUrl(final String url) {
        return click(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
    }

    public SubComponentBuilder<P> clickFile(final String file) {
        return click(new ClickEvent(ClickEvent.Action.OPEN_FILE, file));
    }

    public SubComponentBuilder<P> clickCopy(final String copy) {
        return click(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, copy));
    }

    public SubComponentBuilder<P> clickSuggest(final String suggest) {
        return click(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, suggest));
    }

    public SubComponentBuilder<P> clickRun(final String run) {
        return click(new ClickEvent(ClickEvent.Action.RUN_COMMAND, run));
    }

    public SubComponentBuilder<P> click(ClickEvent event) {
        component.setClickEvent(event);
        return this;
    }

    public SubComponentBuilder<P> hoverEntity(final org.bukkit.entity.Entity entity) {
        if (entity == null) {
            return this;
        }
        final TextComponent component = new TextComponent();
        component.setExtra(ComponentBuilder.create()
            .appendContent(entity.getCustomName() == null ? entity.getName() : entity.getCustomName()).buildComponentList());
        return hover(new HoverEvent(HoverEvent.Action.SHOW_ENTITY,
            new Entity(entity.getType().getKey().toString(), entity.getUniqueId().toString(), component)));
    }

    public SubComponentBuilder<P> hoverText(final ComponentBuilder<?, ?> builder) {
        if (builder == null) {
            return this;
        }
        return hover(
            new HoverEvent(HoverEvent.Action.SHOW_TEXT, new net.md_5.bungee.api.chat.hover.content.Text(builder.buildComponentArray())));
    }

    public SubComponentBuilder<P> hoverText(final MessageProvider provider, final String language) {
        if (provider == null) {
            return this;
        }
        return hoverText(provider.getMessage(language));
    }

    public SubComponentBuilder<P> hoverText(final MessageProvider provider) {
        if (provider == null) {
            return this;
        }
        return hoverText(provider.getMessage(Actor.DEFAULT_LANGUAGE));
    }

    public SubComponentBuilder<P> hoverText(final IMessage message) {
        if (message == null) {
            return this;
        }
        return hoverText(message.value());
    }

    public SubComponentBuilder<P> hoverText(final String string) {
        if (string == null) {
            return this;
        }
        return hover(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
            new net.md_5.bungee.api.chat.hover.content.Text(ComponentBuilder.create().appendContent(string).buildComponentArray())));
    }

    public SubComponentBuilder<P> hover(HoverEvent event) {
        component.setHoverEvent(event);
        return this;
    }

    public SubComponentBuilder<P> copyFrom(SubComponentBuilder<?> component) {
        this.component.copyFormatting(component.component);
        return this;
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty() && (component.getText() == null || component.getText().isEmpty());
    }

    @Override
    public BaseComponent buildComponent() {
        TextComponent output = component.duplicate();
        ObjectList<BaseComponent> list = buildComponentList();
        if (!list.isEmpty()) {
            output.setExtra(list);
        }
        return output;
    }

    public P finish() {
        parent.add(this);
        return parent;
    }

}
