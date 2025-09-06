package me.lauriichan.minecraft.pluginbase.message.component;

import java.awt.Color;

import me.lauriichan.laylib.command.Actor;
import me.lauriichan.laylib.localization.Key;
import me.lauriichan.laylib.localization.MessageProvider;
import me.lauriichan.laylib.logger.util.StringUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Entity;

public abstract class SubComponentBuilder<P extends ComponentBuilder<?, ?>, S extends SubComponentBuilder<P, S>>
    extends ComponentBuilder<P, S> implements ISendable {

    SubComponentBuilder(P parent) {
        super(parent);
    }

    protected abstract BaseComponent component();

    public final S color(Color color) {
        return color(ChatColor.of(color));
    }

    public final S color(ChatColor color) {
        component().setColor(color);
        return self;
    }

    public final ChatColor color() {
        return component().getColor();
    }

    public final S apply(Formatting formatting) {
        formatting.apply(component(), true);
        return self;
    }

    public final S unapply(Formatting formatting) {
        formatting.apply(component(), false);
        return self;
    }

    public final boolean hasFormatting(Formatting formatting) {
        return formatting.isApplied(component());
    }

    public final S clickUrl(final String url, final Object... format) {
        return clickUrl(StringUtil.format(url, format));
    }

    public final S clickFile(final String file, final Object... format) {
        return clickFile(StringUtil.format(file, format));
    }

    public final S clickCopy(final String copy, final Object... format) {
        return clickCopy(StringUtil.format(copy, format));
    }

    public final S clickSuggest(final String suggest, final Object... format) {
        return clickSuggest(StringUtil.format(suggest, format));
    }

    public final S clickRun(final String run, final Object... format) {
        return clickRun(StringUtil.format(run, format));
    }

    public final S clickUrl(final String url) {
        return click(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
    }

    public final S clickFile(final String file) {
        return click(new ClickEvent(ClickEvent.Action.OPEN_FILE, file));
    }

    public final S clickCopy(final String copy) {
        return click(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, copy));
    }

    public final S clickSuggest(final String suggest) {
        return click(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, suggest));
    }

    public final S clickRun(final String run) {
        return click(new ClickEvent(ClickEvent.Action.RUN_COMMAND, run));
    }

    public final S click(ClickEvent event) {
        component().setClickEvent(event);
        return self;
    }

    public final ClickEvent click() {
        return component().getClickEvent();
    }

    public final S hoverEntity(final org.bukkit.entity.Entity entity) {
        if (entity == null) {
            return self;
        }
        final TextComponent component = new TextComponent();
        component.setExtra(ComponentBuilder.create()
            .appendContent(entity.getCustomName() == null ? entity.getName() : entity.getCustomName()).buildComponentList());
        return hover(new HoverEvent(HoverEvent.Action.SHOW_ENTITY,
            new Entity(entity.getType().getKey().toString(), entity.getUniqueId().toString(), component)));
    }

    public final S hoverText(final ComponentBuilder<?, ?> builder) {
        if (builder == null) {
            return self;
        }
        return hover(
            new HoverEvent(HoverEvent.Action.SHOW_TEXT, new net.md_5.bungee.api.chat.hover.content.Text(builder.buildComponentArray())));
    }

    public final S hoverText(final MessageProvider provider, final Actor<?> actor, final Key... placeholders) {
        if (provider == null) {
            return self;
        }
        return hoverText(actor.getTranslatedMessageAsString(provider, placeholders));
    }

    public final S hoverText(final String string) {
        if (string == null) {
            return self;
        }
        return hover(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
            new net.md_5.bungee.api.chat.hover.content.Text(ComponentBuilder.create().appendContent(string).buildComponentArray())));
    }

    public final S hover(HoverEvent event) {
        component().setHoverEvent(event);
        return self;
    }

    public final HoverEvent hover() {
        return component().getHoverEvent();
    }

    public final S copyFrom(SubComponentBuilder<?, ?> component) {
        return copyFrom(component, false);
    }

    public final S copyFrom(SubComponentBuilder<?, ?> component, boolean copyReset) {
        if (component.component().isReset() && !copyReset) {
            return self;
        }
        component().copyFormatting(component.component());
        return self;
    }

    public S loadFrom(SubComponentBuilder<?, ?> component) {
        component().copyFormatting(component.component());
        return self;
    }

    public final P finish() {
        parent.add(this);
        return parent;
    }

}
