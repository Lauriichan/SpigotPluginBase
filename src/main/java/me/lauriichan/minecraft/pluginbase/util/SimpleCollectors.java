package me.lauriichan.minecraft.pluginbase.util;

import static me.lauriichan.minecraft.pluginbase.util.SimpleCollector.*;

import java.util.stream.Collector;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public final class SimpleCollectors {

    private SimpleCollectors() {
        throw new UnsupportedOperationException();
    }

    public static <T> Collector<T, ObjectArrayList<T>, ObjectArrayList<T>> toList() {
        return new SimpleCollector<>(ObjectArrayList::new, ObjectArrayList::add, toBinary(ObjectArrayList::addAll), passthrough());
    }

}
