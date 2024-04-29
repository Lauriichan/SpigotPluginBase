package me.lauriichan.minecraft.pluginbase.config.startup;

import java.util.function.Consumer;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import me.lauriichan.minecraft.pluginbase.config.Configuration;
import me.lauriichan.minecraft.pluginbase.config.IConfigExtension;
import me.lauriichan.minecraft.pluginbase.config.IConfigHandler;
import me.lauriichan.minecraft.pluginbase.config.handler.JsonConfigHandler;

public final class StartupConfig implements IConfigExtension {
    
    private final ObjectList<Property<?>> properties;
    
    public StartupConfig(Consumer<ObjectArrayList<Property<?>>> propergator) {
        ObjectArrayList<Property<?>> properties = new ObjectArrayList<>();
        propergator.accept(properties);
        this.properties = ObjectLists.unmodifiable(properties);
    }

    @Override
    public String path() {
        return "data://plugin.json";
    }

    @Override
    public IConfigHandler handler() {
        return JsonConfigHandler.JSON;
    }
    
    @Override
    public void onLoad(Configuration configuration) throws Exception {
        for (Property<?> property : properties) {
            property.load(configuration);
        }
    }
    
    @Override
    public void onSave(Configuration configuration) throws Exception {
        for (Property<?> property : properties) {
            property.save(configuration);
        }
    }

}
