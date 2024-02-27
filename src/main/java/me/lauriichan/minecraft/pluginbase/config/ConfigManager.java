package me.lauriichan.minecraft.pluginbase.config;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.ConditionConstant;

public final class ConfigManager {

    private final Object2ObjectArrayMap<Class<? extends IConfigExtension>, ConfigWrapper<?>> configs = new Object2ObjectArrayMap<>();

    public ConfigManager(final BasePlugin<?> plugin) {
        if (plugin.conditionMap().value(ConditionConstant.DISABLE_CONFIGS)) {
            return;
        }
        plugin.extension(IConfigExtension.class, true).callInstances(extension -> {
            configs.put(extension.getClass(), new ConfigWrapper<>(plugin, extension));
        });
    }

    public Object2IntMap<ConfigWrapper<?>> reload() {
        return reload(false);
    }

    public Object2IntMap<ConfigWrapper<?>> reload(boolean force) {
        Object2IntArrayMap<ConfigWrapper<?>> results = new Object2IntArrayMap<>(configs.size());
        configs.values().forEach(wrapper -> results.put(wrapper, wrapper.reload(force)));
        return Object2IntMaps.unmodifiable(results);
    }

    public Object2IntMap<ConfigWrapper<?>> save() {
        return save(false);
    }

    public Object2IntMap<ConfigWrapper<?>> save(boolean force) {
        Object2IntArrayMap<ConfigWrapper<?>> results = new Object2IntArrayMap<>(configs.size());
        configs.values().forEach(wrapper -> results.put(wrapper, wrapper.save(force)));
        return Object2IntMaps.unmodifiable(results);
    }

    public ObjectCollection<ConfigWrapper<?>> wrappers() {
        return configs.values();
    }

    public <H extends IConfigExtension> ConfigWrapper<H> wrapper(final Class<H> type) {
        final ConfigWrapper<?> extension = configs.get(type);
        if (extension == null) {
            return null;
        }
        return (ConfigWrapper<H>) extension;
    }

    public <H extends IConfigExtension> H config(final Class<H> type) {
        final ConfigWrapper<?> wrapper = configs.get(type);
        if (wrapper == null) {
            return null;
        }
        return type.cast(wrapper.config());
    }

    public boolean has(final Class<? extends IConfigExtension> type) {
        return configs.containsKey(type);
    }

}
