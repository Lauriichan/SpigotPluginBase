package me.lauriichan.minecraft.pluginbase.util.color;

import java.util.regex.Pattern;

import net.md_5.bungee.api.ChatColor;

public final class BukkitColor {

    private BukkitColor() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static final char COLOR_CHAR = '\u00A7';
    public static final char HEX_CHAR = '\u0023';
    public static final char REPLACEMENT_CHAR = '\u0026';
    public static final String ALL_CODES = "0123456789AabCcDdEeFfKkLlMmNnOoRrXx";
    public static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)" + String.valueOf(COLOR_CHAR) + "[0-9A-FK-ORX]");
    public static final Pattern STRIP_UNCOLORED_PATTERN = Pattern
        .compile("(?i)" + String.valueOf(REPLACEMENT_CHAR) + "([0-9A-FK-ORX]|" + String.valueOf(HEX_CHAR) + "([0-9A-F]{6}|[0-9A-F]{3}))");

    public static String strip(final String text) {
        return text == null ? null : STRIP_COLOR_PATTERN.matcher(text).replaceAll("");
    }

    public static String stripPlain(final String text) {
        return text == null ? null : STRIP_UNCOLORED_PATTERN.matcher(text).replaceAll("");
    }

    public static String apply(final String string) {
        return ChatColor.translateAlternateColorCodes(REPLACEMENT_CHAR, string);
    }

    public static String unapply(final String string) {
        final StringBuilder output = new StringBuilder();
        final int length = string.length();
        for (int index = 0; index < length; index++) {
            final char chr = string.charAt(index);
            if (chr == COLOR_CHAR) {
                output.append(REPLACEMENT_CHAR);
                continue;
            }
            output.append(chr);
        }
        return output.toString();
    }
}