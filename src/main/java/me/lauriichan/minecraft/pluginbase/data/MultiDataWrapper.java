package me.lauriichan.minecraft.pluginbase.data;

import java.util.Objects;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.extension.Order;

public final class MultiDataWrapper<K, E, T, D extends IFileDataExtension<T>, M extends IMultiDataExtension<K, E, T, D>>
    implements IDataWrapper<T, D> {

    private final Object2ObjectArrayMap<K, DataWrapper<T, D>> data = new Object2ObjectArrayMap<>();

    private final BasePlugin<?> plugin;
    private final M extension;

    private final int order;

    public MultiDataWrapper(BasePlugin<?> plugin, M extension) {
        this.plugin = plugin;
        this.extension = extension;
        Order order = extension.type().getAnnotation(Order.class);
        this.order = order == null ? 0 : order.value();
    }

    public M extension() {
        return extension;
    }

    public DataWrapper<T, D> wrapper(E element) {
        return data.get(extension.getDataKey(Objects.requireNonNull(element)));
    }

    public DataWrapper<T, D> wrapperOrCreate(E element) {
        K key = extension.getDataKey(Objects.requireNonNull(element));
        DataWrapper<T, D> wrapper = data.get(key);
        if (wrapper == null) {
            wrapper = new DataWrapper<>(plugin, extension.create(), extension.path(element));
            wrapper.reload();
            data.put(key, wrapper);
        }
        return wrapper;
    }

    public D data(E element) {
        DataWrapper<T, D> wrapper = wrapper(element);
        if (wrapper == null) {
            return null;
        }
        return wrapper.data();
    }

    public D dataOrCreate(E element) {
        return wrapperOrCreate(element).data();
    }

    public ObjectCollection<DataWrapper<T, D>> wrappers() {
        return data.values();
    }

    @Override
    public int order() {
        return order;
    }

    @Override
    public Class<D> dataType() {
        return extension.type();
    }

    @Override
    public int[] reload(boolean force, boolean wipeAfterLoad) {
        try {
            extension.onLoad(plugin.logger());
        } catch (RuntimeException exp) {
            plugin.logger().warning("Something went wrong while loading multi data '{0}'", exp, getClass().getName());
        }
        int index = 0;
        int[] states = new int[data.size()];
        for (DataWrapper<T, D> wrapper : data.values()) {
            states[index++] = wrapper.reloadSingle(force, wipeAfterLoad);
        }
        return states;
    }

    @Override
    public int[] save(boolean force) {
        try {
            extension.onSave(plugin.logger());
        } catch (RuntimeException exp) {
            plugin.logger().warning("Something went wrong while loading multi data '{0}'", exp, getClass().getName());
        }
        int index = 0;
        int[] states = new int[data.size()];
        for (DataWrapper<T, D> wrapper : data.values()) {
            states[index++] = wrapper.saveSingle(force);
        }
        return states;
    }

}
