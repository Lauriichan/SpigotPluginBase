package me.lauriichan.minecraft.pluginbase.game.phased;

import it.unimi.dsi.fastutil.objects.ObjectList;
import me.lauriichan.laylib.reflection.JavaAccess;
import me.lauriichan.minecraft.pluginbase.game.Phase;

public final class PhasedType<T> extends PhasedRef<Class<T>> {

    public static final class PhasedTypeRef<T> implements IPhased<T> {

        private final PhasedType<T> type;
        private final T instance;

        private PhasedTypeRef(PhasedType<T> type) {
            this.type = type;
            this.instance = JavaAccess.PLATFORM.instance(type.instance);
        }

        public PhasedType<T> type() {
            return type;
        }

        @Override
        public T get() {
            return instance;
        }

        @Override
        public boolean isBlacklist() {
            return type.isBlacklist();
        }

        @Override
        public ObjectList<Class<? extends Phase<?>>> phases() {
            return type.phases();
        }

        @Override
        public boolean shouldBeActive(Class<? extends Phase<?>> phase) {
            return type.shouldBeActive(phase);
        }

    }

    public PhasedType(final Class<T> type) {
        super(type);
    }

    public PhasedTypeRef<T> newRef() {
        return new PhasedTypeRef<>(this);
    }

}