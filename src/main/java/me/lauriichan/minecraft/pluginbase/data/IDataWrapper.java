package me.lauriichan.minecraft.pluginbase.data;

import java.util.Comparator;

public interface IDataWrapper<T, D extends IDataExtension<T>> {

    final Comparator<IDataWrapper<?, ?>> ORDER_WRAPPER = (a, b) -> Integer.compare(b.order(), a.order());

    int order();

    Class<D> dataType();

    default int[] reload() {
        return reload(false, false);
    }

    int[] reload(boolean force, boolean wipeAfterLoad);

    default int[] save() {
        return save(false);
    }

    int[] save(boolean force);

}
