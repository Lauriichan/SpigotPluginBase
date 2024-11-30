package me.lauriichan.minecraft.pluginbase.util.mapping;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.lauriichan.minecraft.pluginbase.resource.source.IDataSource;

public final class Mapping {

    public static record MappedClass(String name, Object2ObjectOpenHashMap<String, String> fieldMap,
        Object2ObjectOpenHashMap<String, Object2ObjectOpenHashMap<MappedMethod, String>> methodMap) {
        public MappedClass(String name) {
            this(name, new Object2ObjectOpenHashMap<>(16), new Object2ObjectOpenHashMap<>(16));
        }
    }

    public static record MappedMethod(String returnType, String[] arguments) {
        @Override
        public final String toString() {
            return new StringBuilder("Mapping[returnType=").append(returnType).append(", arguments=").append(Arrays.asList(arguments)).append("]").toString();
        }
    }

    private final Object2ObjectOpenHashMap<String, String> classNameMap = new Object2ObjectOpenHashMap<>(4000);
    private final Object2ObjectOpenHashMap<String, MappedClass> classMap = new Object2ObjectOpenHashMap<>(4000);

    private void clear() {
        classNameMap.clear();
        classMap.clear();
    }

    public String getMappedClassName(String className) {
        return classNameMap.get(className);
    }

    private MappedClass getClass(String className) {
        MappedClass clazz = classMap.get(className);
        if (clazz == null) {
            clazz = classMap.get(classNameMap.get(className));
            if (clazz == null) {
                return null;
            }
        }
        return clazz;
    }

    public String getMappedFieldName(String className, String fieldName) {
        MappedClass clazz = getClass(className);
        if (clazz == null) {
            return null;
        }
        return clazz.fieldMap.get(fieldName);
    }

    public Object2ObjectMap<MappedMethod, String> getMappedMethods(String className, String name) {
        MappedClass clazz = getClass(className);
        if (clazz == null) {
            return Object2ObjectMaps.emptyMap();
        }
        Object2ObjectOpenHashMap<MappedMethod, String> map = clazz.methodMap.get(name);
        if (map == null) {
            return Object2ObjectMaps.emptyMap();
        }
        return Object2ObjectMaps.unmodifiable(map);
    }

    public Object2ObjectMap<MappedMethod, String> getMappedMethods(String className, String name, String returnType) {
        MappedClass clazz = getClass(className);
        if (clazz == null) {
            return Object2ObjectMaps.emptyMap();
        }
        Object2ObjectOpenHashMap<MappedMethod, String> map = clazz.methodMap.get(name);
        if (map == null) {
            return Object2ObjectMaps.emptyMap();
        }
        Object2ObjectOpenHashMap<MappedMethod, String> out = new Object2ObjectOpenHashMap<>(map.size());
        map.object2ObjectEntrySet().stream().filter(entry -> entry.getKey().returnType().equals(returnType))
            .forEach(entry -> out.put(entry.getKey(), entry.getValue()));
        if (out.isEmpty()) {
            return Object2ObjectMaps.emptyMap();
        }
        return Object2ObjectMaps.unmodifiable(out);
    }

    public String getMappedMethodName(String className, String name, String... arguments) {
        MappedClass clazz = getClass(className);
        if (clazz == null) {
            return null;
        }
        Object2ObjectOpenHashMap<MappedMethod, String> map = clazz.methodMap.get(name);
        if (map == null) {
            return null;
        }
        return map.object2ObjectEntrySet().stream().filter(entry -> Arrays.equals(entry.getKey().arguments(), arguments)).findAny()
            .map(entry -> entry.getValue()).orElse(null);
    }

    public String getMappedMethodName(String className, String name, MappedMethod method) {
        MappedClass clazz = getClass(className);
        if (clazz == null) {
            return null;
        }
        Object2ObjectOpenHashMap<MappedMethod, String> map = clazz.methodMap.get(name);
        if (map == null) {
            return null;
        }
        return map.get(method);
    }

    public void readMojang(IDataSource mappings) throws IOException {
        clear();
        if (!mappings.exists()) {
            return;
        }
        try (BufferedReader reader = mappings.openReader()) {
            MappedClass clazz = null;
            String line;
            SimpleReader lineReader, methodReader;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#")) {
                    continue;
                }
                lineReader = new SimpleReader(line);
                if (!Character.isWhitespace(lineReader.peek())) {
                    String originalName = lineReader.readUntil(' ');
                    String newName = lineReader.skip().skipUntil(' ').skip().readUntilEnd();
                    if (newName.endsWith(":")) {
                        newName = newName.substring(0, newName.length() - 1);
                    }
                    classNameMap.put(originalName, newName);
                    clazz = new MappedClass(newName);
                    classMap.put(newName, clazz);
                    continue;
                }
                if (clazz == null) {
                    continue;
                }
                lineReader.skipWhitespace();
                String type = lineReader.readUntil(' ');
                int lastIndex = type.lastIndexOf(':');
                if (lastIndex != -1) {
                    type = type.substring(lastIndex + 1);
                }
                methodReader = new SimpleReader(lineReader.skip().readUntil(' '));
                String originalName = methodReader.readUntil('(');
                String newName = lineReader.skip().skipUntil(' ').skip().readUntilEnd();
                if (!methodReader.hasNext()) {
                    // This is a field
                    clazz.fieldMap.put(originalName, newName);
                    continue;
                }
                if (originalName.charAt(0) == '<' || originalName.startsWith("lambda")) {
                    continue;
                }
                ObjectArrayList<String> arguments = new ObjectArrayList<>(8);
                while (methodReader.skip().hasNext()) {
                    String argument = methodReader.readUntil(',');
                    if (argument.endsWith(")")) {
                        argument = argument.substring(0, argument.length() - 1);
                    }
                    if (argument.isEmpty()) {
                        continue;
                    }
                    arguments.add(argument);
                }
                Object2ObjectOpenHashMap<MappedMethod, String> map = clazz.methodMap.get(originalName);
                if (map == null) {
                    map = new Object2ObjectOpenHashMap<>(5);
                    clazz.methodMap.put(originalName, map);
                }
                map.put(new MappedMethod(type, arguments.toArray(String[]::new)), newName);
            }
        }
    }

    public void readSpigot(IDataSource classes, IDataSource members) throws IOException {
        clear();
        if (!classes.exists()) {
            return;
        }
        try (BufferedReader reader = classes.openReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#")) {
                    continue;
                }
                String[] map = line.replace('/', '.').split(" ", 2);
                classNameMap.put(map[0], map[1]);
            }
        }
        if (!members.exists()) {
            return;
        }
        try (BufferedReader reader = members.openReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#")) {
                    continue;
                }
                String[] mapping = line.split(" ", 4);
                String className = mapping[0].replace('/', '.');
                MappedClass clazz = classMap.get(className);
                if (clazz == null) {
                    clazz = new MappedClass(className);
                    classMap.put(className, clazz);
                }
                if (mapping.length == 3) {
                    // Field
                    clazz.fieldMap.put(mapping[1], mapping[2]);
                } else {
                    // Method
                    SimpleReader methodType = new SimpleReader(mapping[2]);
                    ObjectArrayList<String> arguments = new ObjectArrayList<>(8);
                    methodType.skip();
                    while (methodType.hasNext()) {
                        if (methodType.peek() == ')') {
                            methodType.skip();
                            break;
                        }
                        arguments.add(readSpigotTypeAsMojang(methodType));
                    }
                    String returnType = readSpigotTypeAsMojang(methodType);
                    String originalName = mapping[1].replace('/', '.');
                    Object2ObjectOpenHashMap<MappedMethod, String> map = clazz.methodMap.get(originalName);
                    if (map == null) {
                        map = new Object2ObjectOpenHashMap<>();
                        clazz.methodMap.put(originalName, map);
                    }
                    map.put(new MappedMethod(returnType, arguments.toArray(String[]::new)), mapping[3].replace('/', '.'));
                }
            }
        }
    }

    private String readSpigotTypeAsMojang(SimpleReader type) {
        char found = type.next();
        switch (found) {
        case 'B':
            return "byte";
        case 'C':
            return "char";
        case 'D':
            return "double";
        case 'F':
            return "float";
        case 'I':
            return "int";
        case 'J':
            return "long";
        case 'S':
            return "short";
        case 'Z':
            return "boolean";
        case 'V':
            return "void";
        case 'L':
            try {
                return type.readUntil(';').replace('/', '.');
            } finally {
                type.skip();
            }
        case '[':
            return readSpigotTypeAsMojang(type) + "[]";
        }
        throw new IllegalArgumentException("Unparsable type '" + found + "'");
    }

    private static class SimpleReader {

        private final String string;
        private final int length;
        private volatile int index = 0;

        public SimpleReader(String string) {
            this.string = string;
            this.length = string.length();
        }

        public char peek() {
            return string.charAt(index);
        }

        public SimpleReader skip() {
            index++;
            return this;
        }

        public SimpleReader skipUntil(char ch) {
            int i;
            for (i = index; i < length; i++) {
                if (string.charAt(i) == ch) {
                    break;
                }
            }
            index = i;
            return this;
        }

        public SimpleReader skipWhitespace() {
            while (Character.isWhitespace(peek())) {
                skip();
            }
            return this;
        }

        public char next() {
            return string.charAt(index++);
        }

        public boolean hasNext() {
            return index < length;
        }

        public String readUntil(char ch) {
            StringBuilder builder = new StringBuilder();
            char current;
            int i;
            for (i = index; i < length; i++) {
                current = string.charAt(i);
                if (current == ch) {
                    break;
                }
                builder.append(current);
            }
            index = i;
            return builder.toString();
        }

        public String readUntilEnd() {
            try {
                return string.substring(index);
            } finally {
                index = length;
            }
        }

    }

    @Override
    public String toString() {
        return new StringBuilder("Mapping[class_names=").append(classNameMap).append(", classes=").append(classMap).append("]").toString();
    }

}
