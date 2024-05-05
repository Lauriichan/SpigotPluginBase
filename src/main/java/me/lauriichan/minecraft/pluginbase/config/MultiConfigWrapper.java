package me.lauriichan.minecraft.pluginbase.config;

import java.util.Objects;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import me.lauriichan.minecraft.pluginbase.BasePlugin;

public class MultiConfigWrapper<K, T, C extends IConfigExtension, E extends IMultiConfigExtension<K, T, C>> {

    private final Object2ObjectArrayMap<K, ConfigWrapper<C>> configs = new Object2ObjectArrayMap<>();

    private final BasePlugin<?> plugin;
    private final E extension;

    public MultiConfigWrapper(BasePlugin<?> plugin, E extension) {
        this.plugin = plugin;
        this.extension = extension;
    }

    public ConfigWrapper<C> wrapper(T element) {
        return configs.get(extension.getConfigKey(Objects.requireNonNull(element)));
    }
    
    public ConfigWrapper<C> wrapperOrCreate(T element) {
        K key = extension.getConfigKey(Objects.requireNonNull(element));
        ConfigWrapper<C> wrapper = configs.get(key);
        if (wrapper == null) {
            wrapper = new ConfigWrapper<>(plugin, extension.create(), extension.path(element));
            wrapper.reload(false);
            configs.put(key, wrapper);
        }
        return wrapper;
    }
    
    public C config(T element) {
        ConfigWrapper<C> wrapper = wrapper(element);
        if (wrapper == null) {
            return null;
        }
        return wrapper.config();
    }
    
    public C configOrCreate(T element) {
        return wrapperOrCreate(element).config();
    }
    
    public ObjectCollection<ConfigWrapper<C>> wrappers() {
        return configs.values();
    }
    
}
