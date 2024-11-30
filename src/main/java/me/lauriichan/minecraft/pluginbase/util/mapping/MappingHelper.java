package me.lauriichan.minecraft.pluginbase.util.mapping;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;

import me.lauriichan.laylib.logger.util.StringUtil;
import me.lauriichan.minecraft.pluginbase.resource.ResourceManager;

public final class MappingHelper {

    public static final String MAPPING_FORMAT = "jar://%s/minecraft-server-%s-R0.1-SNAPSHOT-maps-%s";
    private static final String[] EMPTY = new String[0];
    private static final Class<?>[] EMPTY_CLASS = new Class[0];

    public final Mapping mojang = new Mapping();
    public final Mapping spigot = new Mapping();

    public MappingHelper(ResourceManager resources, String mappingPath, String mappingVersion) throws IOException {
        mojang.readMojang(resources.resolve(MAPPING_FORMAT.formatted(mappingPath, mappingVersion, "mojang.txt")));
        spigot.readSpigot(resources.resolve(MAPPING_FORMAT.formatted(mappingPath, mappingVersion, "spigot.csrg")),
            resources.resolve(MAPPING_FORMAT.formatted(mappingPath, mappingVersion, "spigot-members.csrg")));
    }

    public Class<?> getClass(String className) {
        String mojangMapped = mojang.getMappedClassName(className);
        String spigotMapped = spigot.getMappedClassName(mojangMapped != null ? mojangMapped : className);
        if (spigotMapped != null) {
            Class<?> clazz = findClass(spigotMapped);
            if (clazz != null) {
                return clazz;
            }
        }
        if (mojangMapped != null) {
            Class<?> clazz = findClass(mojangMapped);
            if (clazz != null) {
                return clazz;
            }
        }
        return findClass(className);
    }

    private Class<?> findClass(String name) {
        try {
            return Class.forName(name);
        } catch (Throwable exp) {
            return null;
        }
    }
    
    private Class<?> getTypeClass(String name) {
        switch(name) {
        case "byte":
            return byte.class;
        case "char":
            return char.class;
        case "double":
            return double.class;
        case "float":
            return float.class;
        case "int":
            return int.class;
        case "long":
            return long.class;
        case "short":
            return short.class;
        case "boolean":
            return boolean.class;
        case "void":
            return void.class;
        }
        return getClass(name);
    }

    public Method getMethod(String className, String methodName, Object... arguments) {
        String[] arr;
        if (arguments.length == 0) {
            arr = EMPTY;
        } else {
            arr = new String[arguments.length];
            for (int i = 0; i < arguments.length; i++) {
                arr[i] = asClassString(arguments[i]);
            }
        }
        return getMethod(className, methodName, arr);
    }

    public Field getField(String className, String fieldName) {
        Class<?> clazz = getClass(className);
        if (clazz == null) {
            return null;
        }
        String mojangMapped = mojang.getMappedFieldName(className, fieldName);
        String spigotMapped = spigot.getMappedFieldName(className, mojangMapped != null ? mojangMapped : fieldName);
        try {
            Field method = getField(clazz, spigotMapped);
            if (method != null) {
                return method;
            }
            method = getField(clazz, mojangMapped);
            if (method != null) {
                return method;
            }
            return getField(clazz, fieldName);
        } catch (SecurityException e) {
            System.out.println(StringUtil.stackTraceToString(e));
            return null;
        }
    }

    private Method getMethod(String className, String methodName, String[] arguments) {
        Class<?> clazz = getClass(className);
        if (clazz == null) {
            return null;
        }
        String mojangMapped = mojang.getMappedMethodName(className, methodName, arguments);
        String[] mojangArgs = EMPTY;
        if (arguments.length != 0) {
            mojangArgs = new String[arguments.length];
            String tmp;
            for (int i = 0; i < arguments.length; i++) {
                tmp = mojang.getMappedClassName(arguments[i]);
                if (tmp == null) {
                    tmp = arguments[i];
                }
                mojangArgs[i] = tmp;
            }
        }
        String spigotMapped = spigot.getMappedMethodName(className, mojangMapped != null ? mojangMapped : methodName, mojangArgs);
        Class<?>[] methodArguments = EMPTY_CLASS;
        if (arguments.length != 0) {
            methodArguments = new Class[arguments.length];
            for (int i = 0; i < methodArguments.length; i++) {
                methodArguments[i] = getTypeClass(arguments[i]);
            }
        }
        try {
            Method method = getMethod(clazz, spigotMapped, methodArguments);
            if (method != null) {
                return method;
            }
            method = getMethod(clazz, mojangMapped, methodArguments);
            if (method != null) {
                return method;
            }
            return getMethod(clazz, methodName, methodArguments);
        } catch (SecurityException e) {
            System.out.println(StringUtil.stackTraceToString(e));
            return null;
        }
    }

    private Method getMethod(Class<?> clazz, String name, Class<?>[] arguments) {
        if (name == null) {
            return null;
        }
        try {
            return clazz.getDeclaredMethod(name, arguments);
        } catch (NoSuchMethodException nf) {
            System.out.println(StringUtil.stackTraceToString(nf));
            return null;
        }
    }

    private Field getField(Class<?> clazz, String name) {
        if (name == null) {
            return null;
        }
        try {
            return clazz.getDeclaredField(name);
        } catch (NoSuchFieldException nf0) {
            return null;
        }
    }

    private String asClassString(Object object) {
        Objects.requireNonNull(object);
        if (object instanceof String string) {
            return string;
        } else if (object instanceof Class<?> clazz) {
            return clazz.getName();
        }
        throw new IllegalArgumentException("Unsupported object type '" + object.getClass().getName() + "'");
    }

}
