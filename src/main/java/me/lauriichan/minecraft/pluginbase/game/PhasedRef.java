package me.lauriichan.minecraft.pluginbase.game;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import me.lauriichan.laylib.reflection.JavaAccess;

public final class PhasedRef<T> {

    public static final class PhasedType<T> {

        private final Class<T> type;

        private final boolean blacklist;
        private final ObjectList<Class<? extends Phase<?>>> phases;

        public PhasedType(final Class<T> type) {
            this.type = type;
            Phased phased = type.getDeclaredAnnotation(Phased.class);
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

        public Class<T> type() {
            return type;
        }

        public boolean isBlacklist() {
            return blacklist;
        }

        public ObjectList<Class<? extends Phase<?>>> phases() {
            return phases;
        }

        public PhasedRef<T> newRef() {
            return new PhasedRef<>(this);
        }

    }

    private final PhasedType<T> type;
    private final T instance;

    private PhasedRef(PhasedType<T> type) {
        this.type = type;
        this.instance = JavaAccess.PLATFORM.instance(type.type);
    }
    
    public T get() {
        return instance;
    }

    public boolean shouldBeActive(Class<? extends Phase<?>> phase) {
        if (type.phases.isEmpty()) {
            return true;
        }
        if (type.blacklist) {
            return !type.phases.contains(phase);
        }
        return type.phases.contains(phase);
    }

}
