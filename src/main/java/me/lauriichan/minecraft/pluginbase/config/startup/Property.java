package me.lauriichan.minecraft.pluginbase.config.startup;

import java.util.function.Consumer;

import me.lauriichan.minecraft.pluginbase.config.Configuration;

public final class Property<T> {
    
    private final String path;
    private final String comment;
    
    private final IPropertyIO<T> io;
    private final Consumer<T> onLoad;
    
    private final T defaultValue;
    private volatile T value;
    
    public Property(String path, String comment, IPropertyIO<T> io, T defaultValue) {
        this(path, comment, io, defaultValue, null);
    }
    
    public Property(String path, String comment, IPropertyIO<T> io, T defaultValue, Consumer<T> onLoad) {
        this.path = path;
        this.comment = comment;
        this.io = io;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.onLoad = onLoad;
    }
    
    public T value() {
        return value;
    }
    
    final void load(Configuration configuration) {
        Configuration section = configuration.getConfiguration(path, false);
        if (section == null) {
            return;
        }
        T value = io.read(section, "value");
        this.value = value == null ? defaultValue : value;
        if (onLoad != null) {
            onLoad.accept(value);
        }
    }
    
    final void save(Configuration configuration) {
        Configuration section = configuration.getConfiguration(path, true);
        section.set("description", comment);
        io.write(configuration, "value", value);
    }

}
