package me.lauriichan.minecraft.pluginbase.version.api;

import me.lauriichan.minecraft.pluginbase.message.component.Component;

public interface IBrigadierSuggestions {

    void suggest(int start, String text);
    
    void suggest(int start, String text, String tooltip);
    
    void suggest(int start, String text, Component tooltip);
    
    void suggest(int start, int end, String text);
    
    void suggest(int start, int end, String text, String tooltip);
    
    void suggest(int start, int end, String text, Component tooltip);

    void suggestInt(int start, int value);
    
    void suggestInt(int start, int value, String tooltip);
    
    void suggestInt(int start, int value, Component tooltip);
    
    void suggestInt(int start, int end, int value);
    
    void suggestInt(int start, int end, int value, String tooltip);
    
    void suggestInt(int start, int end, int value, Component tooltip);

}
