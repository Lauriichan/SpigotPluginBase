package me.lauriichan.minecraft.pluginbase.util;

import java.util.stream.Stream;

import me.lauriichan.laylib.command.Suggestions;

public final class StringDistance {
    
    public static void suggestFor(String input, Suggestions suggestions, Stream<String> stream) {
        new StringDistance(input).suggest(suggestions, stream);
    }
    
    public static void suggestFor(String input, Suggestions suggestions, Stream<String> stream, int threshhold) {
        new StringDistance(input).suggest(suggestions, stream, threshhold);
    }

    private final char[] chars;

    public StringDistance(String input) {
        this.chars = input.trim().toCharArray();
    }

    public void suggest(Suggestions suggestions, Stream<String> stream) {
        suggest(suggestions, stream, 0);
    }

    public void suggest(Suggestions suggestions, Stream<String> stream, int threshhold) {
        stream.forEach(str -> {
            int score = score(str);
            if (score < threshhold) {
                return;
            }
            suggestions.suggest(score, str);
        });
    }

    public int score(String originalStr) {
        if (chars.length == 0) {
            return 0;
        }
        char[] original = originalStr.toCharArray();
        int size = Math.min(chars.length, original.length);
        int score = startsWith(original, size);
        if (original.length == score) {
            if (chars.length > score) {
                return score + (original.length - chars.length);
            }
            return Integer.MAX_VALUE;
        }
        score += endsWith(original, size);
        score *= 4;
        score += contains(original);
        score += (original.length - chars.length) * 6;
        return score;
    }

    private int contains(char[] original) {
        int score = 0;
        for (int i = 0; i < original.length; i++) {
            for (int j = 0; j < chars.length; j++) {
                if (chars[j] != original[i]) {
                    score -= 2;
                    break;
                }
                score += j + 1;
            }
        }
        return score;
    }

    private int endsWith(char[] original, int size) {
        int score = 0;
        int jc = chars.length - 1;
        int jo = original.length - 1;
        for (int i = 0; i < size; i++) {
            if (chars[jc - i] == original[jo - i]) {
                score++;
            } else {
                return score;
            }
        }
        return score;
    }

    private int startsWith(char[] original, int size) {
        int score = 0;
        for (int i = 0; i < size; i++) {
            if (chars[i] == original[i]) {
                score++;
            } else {
                return score;
            }
        }
        return score;
    }

}
