package me.lauriichan.minecraft.pluginbase.util.instance;

import java.util.Objects;

import org.bukkit.plugin.Plugin;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class SimpleInstanceInvoker implements IInstanceInvoker {
    
    private static class InstanceList extends ObjectArrayList<Object> {
        private static final long serialVersionUID = -7046029254386353185L;

        @Override
        public int indexOf(Object k) {
            for (int i = 0; i < size; i++) if (equals(k, a[i])) return i;
            return -1;
        }
        
        private boolean equals(Object a, Object b) {
            if (a instanceof Plugin && b instanceof Plugin) {
                return a == b;
            }
            return Objects.equals(a, b);
        }
    }

    private final IInstanceInvoker invoker;

    private final InstanceList extraArguments = new InstanceList();
    private volatile Object[] extraArgumentArray;

    public SimpleInstanceInvoker() {
        this(DEFAULT);
    }

    public SimpleInstanceInvoker(final IInstanceInvoker invoker) {
        this.invoker = invoker;
    }

    public void addExtra(Object object) {
        Objects.requireNonNull(object, "Null objects not allowed");
        if (extraArguments.contains(object)) {
            throw new IllegalArgumentException("Object is already added to extra argument list");
        }
        extraArguments.add(object);
        update();
    }

    public void removeExtra(Object object) {
        if (extraArguments.isEmpty() || !extraArguments.remove(object)) {
            return;
        }
        update();
    }

    public void clearExtra() {
        if (extraArguments.isEmpty()) {
            return;
        }
        extraArguments.clear();
        update();
    }

    private void update() {
        if (extraArguments.isEmpty()) {
            extraArgumentArray = null;
            return;
        }
        extraArgumentArray = extraArguments.toArray();
    }

    @Override
    public <T> T invoke(Class<T> clazz, Object... arguments) throws Throwable {
        final Object[] extra = this.extraArgumentArray;
        if (extra == null || extra.length == 0) {
            return invoker.invoke(clazz, arguments);
        }
        if (arguments == null || arguments.length == 0) {
            return invoker.invoke(clazz, extra);
        }
        Object[] finalArguments = new Object[extra.length + arguments.length];
        System.arraycopy(arguments, 0, finalArguments, 0, arguments.length);
        System.arraycopy(extra, 0, finalArguments, arguments.length, extra.length);
        return invoker.invoke(clazz, finalArguments);
    }

}
