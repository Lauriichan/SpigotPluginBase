package me.lauriichan.minecraft.pluginbase.config;

import java.util.stream.Stream;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectList;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.ConditionConstant;
import me.lauriichan.minecraft.pluginbase.util.SimpleCollectors;

public final class ConfigManager {

    private final Object2ObjectArrayMap<Class<? extends ISingleConfigExtension>, ConfigWrapper<?>> configs = new Object2ObjectArrayMap<>();
    private final Object2ObjectArrayMap<Class<? extends IMultiConfigExtension>, MultiConfigWrapper<?, ?, ?, ?>> multiConfigs = new Object2ObjectArrayMap<>();

    public ConfigManager(final BasePlugin<?> plugin) {
        if (plugin.conditionMap().value(ConditionConstant.DISABLE_CONFIGS)) {
            return;
        }
        plugin.extension(ISingleConfigExtension.class, true).callInstances(extension -> {
            configs.put(extension.getClass(), ConfigWrapper.single(plugin, extension));
        });
        plugin.extension(IMultiConfigExtension.class, true).callInstances(extension -> {
            multiConfigs.put(extension.getClass(), new MultiConfigWrapper<>(plugin, extension));
        });
    }

    public int amount() {
        return configs.size();
    }

    public Object2IntMap<ConfigWrapper<?>> reload() {
        return reload(false);
    }

    public Object2IntMap<ConfigWrapper<?>> reload(boolean force) {
        ObjectList<ConfigWrapper<?>> wrappers = wrappers();
        Object2IntArrayMap<ConfigWrapper<?>> results = new Object2IntArrayMap<>(wrappers.size());
        wrappers.forEach(wrapper -> results.put(wrapper, wrapper.reload(force)));
        return Object2IntMaps.unmodifiable(results);
    }

    public Object2IntMap<ConfigWrapper<?>> save() {
        return save(false);
    }

    public Object2IntMap<ConfigWrapper<?>> save(boolean force) {
        ObjectList<ConfigWrapper<?>> wrappers = wrappers();
        Object2IntArrayMap<ConfigWrapper<?>> results = new Object2IntArrayMap<>(wrappers.size());
        wrappers.forEach(wrapper -> results.put(wrapper, wrapper.save(force)));
        return Object2IntMaps.unmodifiable(results);
    }

    public ObjectList<ConfigWrapper<?>> wrappers() {
        return Stream.concat(configs.values().stream(), multiConfigs.values().stream().flatMap(config -> config.wrappers().stream()))
            .collect(SimpleCollectors.toList());
    }

    public <E extends ISingleConfigExtension> ConfigWrapper<E> wrapper(final Class<E> type) {
        final ConfigWrapper<?> extension = configs.get(type);
        if (extension == null) {
            return null;
        }
        return (ConfigWrapper<E>) extension;
    }

    public <E extends ISingleConfigExtension> E config(final Class<E> type) {
        final ConfigWrapper<?> wrapper = configs.get(type);
        if (wrapper == null) {
            return null;
        }
        return type.cast(wrapper.config());
    }

    public boolean has(final Class<? extends ISingleConfigExtension> type) {
        return configs.containsKey(type);
    }

    public ObjectCollection<MultiConfigWrapper<?, ?, ?, ?>> multiWrappers() {
        return multiConfigs.values();
    }

    public <T, C extends IConfigExtension, E extends IMultiConfigExtension<?, T, C>> MultiConfigWrapper<?, T, C, E> multiWrapper(
        final Class<E> type) {
        MultiConfigWrapper<?, ?, ?, ?> multiWrapper = multiConfigs.get(type);
        if (multiWrapper == null) {
            return null;
        }
        return (MultiConfigWrapper<?, T, C, E>) multiWrapper;
    }

    public <T, C extends IConfigExtension, E extends IMultiConfigExtension<?, T, C>> ConfigWrapper<C> multiWrapper(final Class<E> type,
        T element) {
        MultiConfigWrapper<?, T, C, E> multiWrapper = multiWrapper(type);
        if (multiWrapper == null) {
            return null;
        }
        return multiWrapper.wrapper(element);
    }

    public <T, C extends IConfigExtension, E extends IMultiConfigExtension<?, T, C>> ConfigWrapper<C> multiWrapperOrCreate(final Class<E> type,
        T element) {
        MultiConfigWrapper<?, T, C, E> multiWrapper = multiWrapper(type);
        if (multiWrapper == null) {
            return null;
        }
        return multiWrapper.wrapperOrCreate(element);
    }

    public <T, C extends IConfigExtension, E extends IMultiConfigExtension<?, T, C>> C multiConfig(final Class<E> type, T element) {
        ConfigWrapper<C> wrapper = multiWrapper(type, element);
        if (wrapper == null) {
            return null;
        }
        return wrapper.config();
    }

    public <T, C extends IConfigExtension, E extends IMultiConfigExtension<?, T, C>> C multiConfigOrCreate(final Class<E> type, T element) {
        return multiWrapperOrCreate(type, element).config();
    }

    public boolean hasMulti(final Class<? extends IMultiConfigExtension<?, ?, ?>> type) {
        return multiConfigs.containsKey(type);
    }

}
