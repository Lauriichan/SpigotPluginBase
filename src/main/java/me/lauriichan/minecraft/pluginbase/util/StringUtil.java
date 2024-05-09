package me.lauriichan.minecraft.pluginbase.util;

public final class StringUtil {

    private StringUtil() {
        throw new UnsupportedOperationException();
    }

    public static String formatPascalCase(final String string) {
        final String[] parts = string.split(" ");
        final StringBuilder builder = new StringBuilder();
        for (int index = 0; index < parts.length; index++) {
            if (index != 0) {
                builder.append(' ');
            }
            final String part = parts[index];
            builder.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1).toLowerCase());
        }
        return builder.toString();
    }

    public static String prettyFormat(String string) {
        StringBuilder output = new StringBuilder();
        int indent = 0;
        for (int i = 0; i < string.length(); i++) {
            char ch = string.charAt(i);
            if (ch == '{' || ch == '[' || ch == '(') {
                repeat(output.append(ch).append('\n'), ++indent, ' ');
                continue;
            }
            if (ch == ',') {
                repeat(output.append(ch).append('\n'), indent, ' ');
                output.append(ch);
                continue;
            }
            if (ch == '}' || ch == ']' || ch == ')') {
                repeat(output.append('\n'), --indent, ' ');
                output.append(ch);
                continue;
            }
            if (ch == '§') {
                ch = '&';
            }
            output.append(ch);
        }
        return output.toString();
    }

    private static void repeat(StringBuilder builder, int amount, char ch) {
        if (amount <= 0) {
            return;
        }
        for (int i = 0; i < amount; i++) {
            builder.append(ch);
        }
    }

}