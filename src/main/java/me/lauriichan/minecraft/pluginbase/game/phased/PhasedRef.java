package me.lauriichan.minecraft.pluginbase.game.phased;

import java.lang.reflect.AnnotatedElement;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import me.lauriichan.minecraft.pluginbase.game.Phase;

public class PhasedRef<T extends AnnotatedElement> implements IPhased<T> {

    protected final T instance;

    protected final boolean blacklist;
    protected final ObjectList<Class<? extends Phase<?>>> phases;

    public PhasedRef(T element) {
        this.instance = element;
        Phased phased = element.getDeclaredAnnotation(Phased.class);
        if (phased == null) {
            this.blacklist = false;
            this.phases = ObjectLists.emptyList();
            return;
        }
        ObjectArrayList<Class<? extends Phase<?>>> phases = new ObjectArrayList<>();
        for (Class<? extends Phase<?>> phase : phased.phase()) {
            if (phase == null || phases.contains(phase)) {
                continue;
            }
            phases.add(phase);
        }
        this.blacklist = !phases.isEmpty() && phased.blacklist();
        this.phases = phases.isEmpty() ? ObjectLists.emptyList() : ObjectLists.unmodifiable(phases);
    }
    
    @Override
    public final T get() {
        return instance;
    }

    @Override
    public final boolean isBlacklist() {
        return blacklist;
    }

    @Override
    public final ObjectList<Class<? extends Phase<?>>> phases() {
        return phases;
    }

    @Override
    public final boolean shouldBeActive(Class<? extends Phase<?>> phase) {
        if (phases.isEmpty()) {
            return true;
        }
        if (blacklist) {
            return !phases.contains(phase);
        }
        return phases.contains(phase);
    }

}
