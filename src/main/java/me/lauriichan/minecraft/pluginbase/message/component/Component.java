package me.lauriichan.minecraft.pluginbase.message.component;

import java.awt.Color;
import java.util.Arrays;

import me.lauriichan.laylib.command.Action;
import me.lauriichan.laylib.command.ActionMessage;
import me.lauriichan.laylib.command.Actor;
import me.lauriichan.laylib.localization.IMessage;
import me.lauriichan.laylib.localization.MessageProvider;
import me.lauriichan.laylib.logger.util.StringUtil;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Entity;
import net.md_5.bungee.api.chat.hover.content.Text;

public final class Component extends SendableComponent {

    public static Component of(final MessageProvider provider, final String language) {
        return of(provider.getMessage(language));
    }

    public static Component of(final MessageProvider provider) {
        return of(provider.getMessage(Actor.DEFAULT_LANGUAGE));
    }

    public static Component of(final IMessage message) {
        if (message == null) {
            return new Component("");
        }
        return new Component(message.value());
    }

    public static Component of(final String message) {
        return new Component(message);
    }

    public static Component of(final ActionMessage message) {
        if (message == null || message.message() == null) {
            return new Component("");
        }
        ClickEvent click = null;
        HoverEvent hover = null;
        if (message.clickAction() != null) {
            final Action clickAction = message.clickAction();
            switch (clickAction.getType()) {
            case CLICK_COPY:
                try {
                    click = new ClickEvent(ClickEvent.Action.valueOf("COPY_TO_CLIPBOARD"), clickAction.getValueAsString());
                } catch (final IllegalArgumentException exp) {
                }
                break;
            case CLICK_FILE:
                click = new ClickEvent(ClickEvent.Action.OPEN_FILE, clickAction.getValueAsString());
                break;
            case CLICK_RUN:
                click = new ClickEvent(ClickEvent.Action.RUN_COMMAND, clickAction.getValueAsString());
                break;
            case CLICK_SUGGEST:
                click = new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, clickAction.getValueAsString());
                break;
            case CLICK_URL:
                click = new ClickEvent(ClickEvent.Action.OPEN_URL, clickAction.getValueAsString());
                break;
            default:
                break;
            }
        }
        if (message.hoverAction() != null) {
            final Action hoverAction = message.hoverAction();
            switch (hoverAction.getType()) {
            case HOVER_TEXT:
                hover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ComponentParser.parse(hoverAction.getValueAsString())));
                break;
            default:
                break;
            }
        }
        return new Component(message.message()).click(click).hover(hover);
    }

    private final String message;

    private ClickEvent click;
    private HoverEvent hover;

    private Color defaultColor = ComponentParser.DEFAULT_COLOR;

    private boolean changed = true;

    private BaseComponent[] componentMessage = EMPTY;

    private Component(final String message) {
        this.message = message;
    }

    public String message() {
        return message;
    }

    public Component defaultColor(Color defaultColor) {
        if (defaultColor == null) {
            defaultColor = ComponentParser.DEFAULT_COLOR;
        }
        if (this.defaultColor == defaultColor) {
            return this;
        }
        this.defaultColor = defaultColor;
        return this;
    }

    public Component clickUrl(final String url, final Object... format) {
        return clickUrl(StringUtil.format(url, format));
    }

    public Component clickFile(final String file, final Object... format) {
        return clickFile(StringUtil.format(file, format));
    }

    public Component clickCopy(final String copy, final Object... format) {
        return clickCopy(StringUtil.format(copy, format));
    }

    public Component clickSuggest(final String suggest, final Object... format) {
        return clickSuggest(StringUtil.format(suggest, format));
    }

    public Component clickRun(final String run, final Object... format) {
        return clickRun(StringUtil.format(run, format));
    }

    public Component clickUrl(final String url) {
        return click(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
    }

    public Component clickFile(final String file) {
        return click(new ClickEvent(ClickEvent.Action.OPEN_FILE, file));
    }

    public Component clickCopy(final String copy) {
        return click(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, copy));
    }

    public Component clickSuggest(final String suggest) {
        return click(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, suggest));
    }

    public Component clickRun(final String run) {
        return click(new ClickEvent(ClickEvent.Action.RUN_COMMAND, run));
    }

    public Component click(final ClickEvent click) {
        if (this.click == click) {
            return this;
        }
        this.click = click;
        this.changed = true;
        return this;
    }

    public Component hoverEntity(final org.bukkit.entity.Entity entity) {
        if (entity == null) {
            return this;
        }
        final TextComponent component = new TextComponent();
        component
            .setExtra(Arrays.asList(ComponentParser.parse(entity.getCustomName() == null ? entity.getName() : entity.getCustomName())));
        return hover(new HoverEvent(HoverEvent.Action.SHOW_ENTITY,
            new Entity(entity.getType().getKey().toString(), entity.getUniqueId().toString(), component)));
    }

    public Component hoverText(final Component component) {
        if (component == null) {
            return this;
        }
        return hover(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new net.md_5.bungee.api.chat.hover.content.Text(component.build())));
    }

    public Component hoverText(final MessageProvider provider, final String language) {
        if (provider == null) {
            return this;
        }
        return hoverText(provider.getMessage(language));
    }

    public Component hoverText(final MessageProvider provider) {
        if (provider == null) {
            return this;
        }
        return hoverText(provider.getMessage(Actor.DEFAULT_LANGUAGE));
    }

    public Component hoverText(final IMessage message) {
        if (message == null) {
            return this;
        }
        return hoverText(message.value());
    }

    public Component hoverText(final String string) {
        if (string == null) {
            return this;
        }
        return hover(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
            new net.md_5.bungee.api.chat.hover.content.Text(ComponentParser.parse(string, defaultColor))));
    }

    public Component hover(final HoverEvent hover) {
        if (this.hover == hover) {
            return this;
        }
        this.hover = hover;
        this.changed = true;
        return this;
    }

    public Component reset() {
        componentMessage = EMPTY;
        changed = true;
        return this;
    }

    @Override
    public BaseComponent[] build() {
        if (!changed) {
            return componentMessage;
        }
        changed = false;
        if (componentMessage == EMPTY) {
            final BaseComponent[] components = ComponentParser.parse(message, defaultColor, click, hover);
            if (components == null) {
                return componentMessage;
            }
            return componentMessage = components;
        }
        for (final BaseComponent component : componentMessage) {
            component.setClickEvent(click);
            component.setHoverEvent(hover);
        }
        return componentMessage;
    }

}