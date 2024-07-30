package me.lauriichan.minecraft.pluginbase.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;

import me.lauriichan.laylib.reflection.ClassUtil;
import me.lauriichan.laylib.reflection.JavaAccess;

public final class ReflectionUtil {

    private ReflectionUtil() {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("rawtypes")
    public static <T> T createInstance(final Class<T> type, final Object... arguments) {
        final Constructor[] constructors = ClassUtil.getConstructors(type);
        if (constructors.length == 0) {
            return null;
        }
        final Class[] args = new Class[arguments.length];
        for (int index = 0; index < args.length; index++) {
            final Object value = arguments[index];
            if (value == null) {
                throw new IllegalArgumentException("Found unsupported null value at index " + index);
            }
            args[index] = value.getClass();
        }
        Constructor matching = null;
        int satisfiedArguments = -1;
        int[] indices = {};
        for (final Constructor constructor : constructors) {
            final Parameter[] params = constructor.getParameters();
            if (params.length == 0 && satisfiedArguments == -1) {
                matching = constructor;
                satisfiedArguments = 0;
                continue;
            }
            int satisfied = 0;
            final int[] indices0 = new int[params.length];
            for (int idx = 0; idx < params.length; idx++) {
                final Class<?> paramType = params[idx].getType();
                for (int index = 0; index < args.length; index++) {
                    if (paramType.isAssignableFrom(args[index])) {
                        indices0[idx] = index;
                        satisfied++;
                        break;
                    }
                }
            }
            if (satisfied == params.length && satisfiedArguments < satisfied) {
                matching = constructor;
                satisfiedArguments = satisfied;
                indices = indices0;
            }
        }
        if (satisfiedArguments == -1) {
            return null;
        }
        final Object[] argumentArray = new Object[satisfiedArguments];
        for (int index = 0; index < argumentArray.length; index++) {
            argumentArray[index] = arguments[indices[index]];
        }
        return type.cast(JavaAccess.instance(matching, argumentArray));
    }

    @SuppressWarnings("rawtypes")
    public static <T> T createInstanceThrows(final Class<T> type, final Object... arguments) throws Throwable {
        Constructor[] constructors = ClassUtil.getConstructors(type);
        if (constructors.length == 0) {
            throw new ReflectiveOperationException("No constructors available");
        }
        final Class[] args = new Class[arguments.length];
        for (int index = 0; index < args.length; index++) {
            final Object value = arguments[index];
            if (value == null) {
                throw new IllegalArgumentException("Found unsupported null value at index " + index);
            }
            args[index] = value.getClass();
        }
        Constructor matching = null;
        int satisfiedArguments = -1;
        int[] indices = {};
        for (final Constructor constructor : constructors) {
            final Parameter[] params = constructor.getParameters();
            if (params.length == 0 && satisfiedArguments == -1) {
                matching = constructor;
                satisfiedArguments = 0;
                continue;
            }
            int satisfied = 0;
            final int[] indices0 = new int[params.length];
            for (int idx = 0; idx < params.length; idx++) {
                final Class<?> paramType = params[idx].getType();
                for (int index = 0; index < args.length; index++) {
                    if (paramType.isAssignableFrom(args[index])) {
                        indices0[idx] = index;
                        satisfied++;
                        break;
                    }
                }
            }
            if (satisfied == params.length && satisfiedArguments < satisfied) {
                matching = constructor;
                satisfiedArguments = satisfied;
                indices = indices0;
            }
        }
        if (satisfiedArguments == -1) {
            throw new ReflectiveOperationException("Arguments not satisfied");
        }
        final Object[] argumentArray = new Object[satisfiedArguments];
        for (int index = 0; index < argumentArray.length; index++) {
            argumentArray[index] = arguments[indices[index]];
        }
        return type.cast(JavaAccess.instanceThrows(matching, argumentArray));
    }

}
