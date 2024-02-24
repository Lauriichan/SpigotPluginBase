package me.lauriichan.minecraft.pluginbase.version.impl;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.IntegerSuggestion;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;

import me.lauriichan.minecraft.pluginbase.message.component.Component;
import me.lauriichan.minecraft.pluginbase.util.BukkitColor;
import me.lauriichan.minecraft.pluginbase.version.api.IBrigadierSuggestions;

public final class BrigadierSuggestions implements IBrigadierSuggestions {

    private final String string;
    private final ArrayList<Suggestion> suggestions = new ArrayList<>();

    public BrigadierSuggestions(String string) {
        this.string = string;
    }

    @Override
    public void suggest(int start, String text) {
        suggestions.add(new Suggestion(StringRange.between(start, string.length()), text));
    }

    @Override
    public void suggest(int start, String text, String tooltip) {
        suggestions.add(new Suggestion(StringRange.between(start, string.length()), text, new LiteralMessage(BukkitColor.apply(tooltip))));
    }

    @Override
    public void suggest(int start, String text, Component tooltip) {
        suggestions.add(new Suggestion(StringRange.between(start, string.length()), text, new LiteralMessage(tooltip.toString())));
    }

    @Override
    public void suggest(int start, int end, String text) {
        suggestions.add(new Suggestion(StringRange.between(start, end), text));

    }

    @Override
    public void suggest(int start, int end, String text, String tooltip) {
        suggestions.add(new Suggestion(StringRange.between(start, end), text, new LiteralMessage(BukkitColor.apply(tooltip))));

    }

    @Override
    public void suggest(int start, int end, String text, Component tooltip) {
        suggestions.add(new Suggestion(StringRange.between(start, end), text, new LiteralMessage(tooltip.toString())));

    }

    @Override
    public void suggestInt(int start, int value) {
        suggestions.add(new IntegerSuggestion(StringRange.between(start, string.length()), value));
    }

    @Override
    public void suggestInt(int start, int value, String tooltip) {
        suggestions
            .add(new IntegerSuggestion(StringRange.between(start, string.length()), value, new LiteralMessage(BukkitColor.apply(tooltip))));
    }

    @Override
    public void suggestInt(int start, int value, Component tooltip) {
        suggestions.add(new IntegerSuggestion(StringRange.between(start, string.length()), value, new LiteralMessage(tooltip.toString())));
    }

    @Override
    public void suggestInt(int start, int end, int value) {
        suggestions.add(new IntegerSuggestion(StringRange.between(start, end), value));
    }

    @Override
    public void suggestInt(int start, int end, int value, String tooltip) {
        suggestions.add(new IntegerSuggestion(StringRange.between(start, end), value, new LiteralMessage(BukkitColor.apply(tooltip))));
    }

    @Override
    public void suggestInt(int start, int end, int value, Component tooltip) {
        suggestions.add(new IntegerSuggestion(StringRange.between(start, end), value, new LiteralMessage(tooltip.toString())));
    }

    public CompletableFuture<Suggestions> buildFuture() {
        return CompletableFuture.completedFuture(Suggestions.create(string, suggestions));
    }

}
