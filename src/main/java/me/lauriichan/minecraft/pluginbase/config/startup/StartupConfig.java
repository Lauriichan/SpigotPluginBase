package me.lauriichan.minecraft.pluginbase.config.startup;

import java.util.function.Consumer;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.minecraft.pluginbase.config.Configuration;
import me.lauriichan.minecraft.pluginbase.config.ISingleConfigExtension;
import me.lauriichan.minecraft.pluginbase.config.IConfigHandler;
import me.lauriichan.minecraft.pluginbase.config.handler.JsonConfigHandler;

public final class StartupConfig implements ISingleConfigExtension {
    
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
    public void onLoad(ISimpleLogger logger, Configuration configuration) throws Exception {
        for (Property<?> property : properties) {
            property.load(configuration);
        }
    }
    
    @Override
    public void onSave(ISimpleLogger logger, Configuration configuration) throws Exception {
        for (Property<?> property : properties) {
            property.save(configuration);
        }
    }

}
