package me.lauriichan.minecraft.pluginbase.message.component;

import java.awt.Color;

import me.lauriichan.laylib.command.Action;
import me.lauriichan.laylib.command.ActionMessage;
import me.lauriichan.minecraft.pluginbase.message.component.ComponentBuilder.TextAppender;
import me.lauriichan.minecraft.pluginbase.util.color.ColorParser;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;

final class ComponentBuilderUtils {

    private static record ColorResult(Color color, int length) {}

    private ComponentBuilderUtils() {
        throw new UnsupportedOperationException();
    }

    public static <S extends ComponentBuilder<?, ?>> SubComponentBuilder<S> append(SubComponentBuilder<S> builder, String content) {
        SubComponentBuilder<?> component = builder.newComponent();
        TextAppender<?> appender = null;
        int last = 0;
        for (int i = content.indexOf('&'); i != -1; i = content.indexOf('&', i + 1)) {
            if (appender != null) {
                appender.text(content.substring(last, i)).finish();
                appender = null;
                component = component.finish().newComponent().copyFrom(component);
            } else {
                component.appendText(content.substring(last, i));
            }
            if (content.charAt(i + 1) != '#') {
                // Apply formatting
                ChatColor format = ChatColor.getByChar(content.charAt(i + 1));
                if (format == null) {
                    continue;
                }
                Formatting formatting = Formatting.find(format);
                last = i + 2;
                if (!component.isEmpty()) {
                    component = component.finish().newComponent().copyFrom(component);
                }
                if (formatting != null) {
                    component.apply(formatting);
                } else {
                    component.color(format);
                }
                continue;
            }
            if (content.charAt(i + 2) != '[') {
                // Apply hex color
                ColorResult result = parseColor(content, i + 2);
                if (result == null) {
                    continue;
                }
                last = i + result.length();
                if (!component.isEmpty()) {
                    component = component.finish().newComponent().copyFrom(component);
                }
                component.color(result.color());
                continue;
            }
            // Apply hex gradient
            ColorResult start = parseColor(content, i + 3);
            if (start == null || content.charAt(i + 3 + start.length()) != '-') {
                continue;
            }
            ColorResult end = parseColor(content, i + 4 + start.length());
            if (end == null) {
                continue;
            }
            int colorEnd = end.length() + i + 4 + start.length;
            int colorAmount = -1;
            int offsetIdx = 0;
            if (content.charAt(colorEnd) != ']') {
                if (content.charAt(colorEnd) != '/') {
                    continue;
                }
                offsetIdx = content.indexOf(']', colorEnd);
                if (offsetIdx == -1) {
                    continue;
                }
                try {
                    colorAmount = Integer.parseInt(content.substring(colorEnd + 1, offsetIdx));
                } catch (NumberFormatException nfe) {
                    continue;
                }
            }
            last = i + colorEnd + offsetIdx + 1;
            if (!component.isEmpty()) {
                component = component.finish().newComponent().copyFrom(component);
            }
            appender = component.newText().startColor(start.color()).endColor(end.color()).colorAmount(colorAmount);
        }
        if (last == 0 || last + 1 != content.length()) {
            component.appendText(content.substring(last, content.length())).finish();
        }
        return builder;
    }

    private static ColorResult parseColor(String string, int startIndex) {
        int end = Math.min(startIndex + 6, string.length());
        final StringBuilder hex = new StringBuilder();
        for (int i = startIndex; i < end; i++) {
            final char ch = string.charAt(i);
            if (ch >= 'A' && ch <= 'Z') {
                hex.append((char) (ch + 32));
                continue;
            }
            if (ch >= 'a' && ch <= 'z' || ch >= '0' && ch <= '9') {
                hex.append(ch);
                continue;
            }
            break;
        }
        Color color = ColorParser.parseOrNull(hex.toString());
        if (color == null) {
            return null;
        }
        return new ColorResult(color, hex.length());
    }

    public static <S extends ComponentBuilder<?, S>> SubComponentBuilder<S> appendAction(S builder, ActionMessage message) {
        ClickEvent click = null;
        HoverEvent hover = null;
        if (message.clickAction() != null) {
            final Action clickAction = message.clickAction();
            switch (clickAction.getType()) {
            case CLICK_COPY:
                click = new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, clickAction.getValueAsString());
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
                hover = new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new Text(ComponentBuilder.create().appendContent(hoverAction.getValueAsString()).buildComponentArray()));
                break;
            default:
                break;
            }
        }
        return builder.appendContent(message.message()).click(click).hover(hover);
    }

}
