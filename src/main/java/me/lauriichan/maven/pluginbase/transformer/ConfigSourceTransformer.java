package me.lauriichan.maven.pluginbase.transformer;

import static me.lauriichan.maven.pluginbase.util.SourceTransformerUtils.importClass;
import static me.lauriichan.maven.pluginbase.util.SourceTransformerUtils.removeAnnotation;
import static me.lauriichan.maven.pluginbase.util.SourceTransformerUtils.removeMethod;

import java.util.List;
import java.util.Objects;

import org.jboss.forge.roaster.model.Type;
import org.jboss.forge.roaster.model.Visibility;
import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.lauriichan.maven.sourcemod.api.ISourceTransformer;
import me.lauriichan.minecraft.pluginbase.config.Config;
import me.lauriichan.minecraft.pluginbase.config.ConfigValue;
import me.lauriichan.minecraft.pluginbase.config.Configuration;
import me.lauriichan.minecraft.pluginbase.config.IConfigExtension;
import me.lauriichan.minecraft.pluginbase.extension.Extension;

public class ConfigSourceTransformer implements ISourceTransformer {

    private static record ConfigField(FieldSource<JavaClassSource> field, String name) {}

    @Override
    public boolean canTransform(final JavaSource<?> source) {
        if (!(source instanceof final JavaClassSource classSource)) {
            return false;
        }
        return !classSource.isAbstract() && !classSource.isRecord() && classSource.hasAnnotation(Config.class)
            && classSource.hasInterface(IConfigExtension.class);
    }

    @Override
    public void transform(final JavaSource<?> source) {
        final JavaClassSource clazz = (JavaClassSource) source;
        final boolean automatic = "true".equals(clazz.getAnnotation(Config.class).getLiteralValue("automatic"));

        final ObjectArrayList<ConfigField> configFields = new ObjectArrayList<>();
        final List<FieldSource<JavaClassSource>> fields = clazz.getFields();
        for (final FieldSource<JavaClassSource> field : fields) {
            if (!field.hasAnnotation(ConfigValue.class) || field.getType().isType(void.class) || field.getType().isType(Void.class)) {
                continue;
            }
            configFields.add(new ConfigField(field, field.getAnnotation(ConfigValue.class).getStringValue()));
        }
        
        Object2ObjectArrayMap<String, MethodSource<JavaClassSource>> validators = new Object2ObjectArrayMap<>();
        final List<MethodSource<JavaClassSource>> methods = clazz.getMethods();
        for (final MethodSource<JavaClassSource> method : methods) {
            if (!method.hasAnnotation(ConfigValue.class) || method.getReturnType().isType(void.class) || method.getReturnType().isType(Void.class)) {
                continue;
            }
            String value = method.getAnnotation(ConfigValue.class).getStringValue();
            if (validators.containsKey(value)) {
                throw new IllegalStateException("Duplicated validator for field '" + value + "': " + method.getName());
            }
            validators.put(value, method);
        }

        if (!clazz.hasAnnotation(Extension.class)) {
            clazz.addAnnotation(Extension.class);
            importClass(clazz, Extension.class);
        }

        clazz.setPublic();
        clazz.setFinal(true);
        
        if (configFields.isEmpty()) {
            return;
        }

        importClass(clazz, Objects.class);
        importClass(clazz, Configuration.class);
        clazz.addField("private volatile boolean generated$modified0 = false;");

        StringBuilder loadBuilder = new StringBuilder();
        StringBuilder saveBuilder = new StringBuilder();
        loadBuilder.append("""
            @Override
            public void onLoad(Configuration configuration) throws Exception {
                this.generated$modified0 = false;
            """);
        saveBuilder.append("""
            @Override
            public void onSave(Configuration configuration) throws Exception {
                this.generated$modified0 = false;
            """);

        ConfigField configField;
        FieldSource<JavaClassSource> field;
        MethodSource<JavaClassSource> method;
        for (int index = 0; index < configFields.size(); index++) {
            field = (configField = configFields.get(index)).field();
            method = validators.get(configField.name());
            if (method != null) {
                if (method.getReturnType().equals(field.getType())) {
                    method.setVisibility(Visibility.PRIVATE);
                    method.setStatic(false);
                    method.setFinal(true);
                } else {
                    method = null;
                }
            }
            field.setVisibility(Visibility.PRIVATE);
            field.setStatic(false);
            field.setVolatile(false);
            field.setFinal(false);
            clazz.addField("private final %2$s generatedDefault$%1$s = %1$s;".formatted(field.getName(),
                field.getType().getQualifiedNameWithGenerics()));
            removeMethod(clazz, field.getName());
            removeMethod(clazz, "default$" + field.getName());
            removeMethod(clazz, field.getName(), field.getType().getQualifiedNameWithGenerics());
            addFieldMethod(clazz, field, """
                public void %1$s(%2$s %1$s) {
                    if (Objects.equals(this.%1$s, %1$s)) {
                        return;
                    }
                    this.%1$s = %1$s;
                    this.generated$modified0 = true;
                }
                """, """
                public void %1$s(%2$s %1$s) {
                    if (this.%1$s == %1$s) {
                        return;
                    }
                    this.%1$s = %1$s;
                    this.generated$modified0 = true;
                }
                """);
            addFieldMethod(clazz, field, """
                public %2$s %1$s() {
                    return this.%1$s;
                }
                """);
            addFieldMethod(clazz, field, """
                public %2$s default$%1$s() {
                    return this.generatedDefault$%1$s;
                }
                """);
            if (automatic) {
                if (index != 0) {
                    loadBuilder.append('\n');
                }
                loadBuilder.append("this.").append(field.getName()).append(" = ");
                if (method != null) {
                    loadBuilder.append(method.getName()).append('(');
                }
                loadBuilder.append("configuration.get");
                final Type<JavaClassSource> type = field.getType();
                boolean primitive = false;
                if (type.isPrimitive()) {
                    if (type.isType(boolean.class)) {
                        loadBuilder.append("Boolean");
                        primitive = true;
                    } else if (type.isType(byte.class)) {
                        loadBuilder.append("Byte");
                        primitive = true;
                    } else if (type.isType(short.class)) {
                        loadBuilder.append("Short");
                        primitive = true;
                    } else if (type.isType(int.class)) {
                        loadBuilder.append("Int");
                        primitive = true;
                    } else if (type.isType(long.class)) {
                        loadBuilder.append("Long");
                        primitive = true;
                    } else if (type.isType(float.class)) {
                        loadBuilder.append("Float");
                        primitive = true;
                    } else if (type.isType(double.class)) {
                        loadBuilder.append("Double");
                        primitive = true;
                    }
                }
                loadBuilder.append("(\"").append(configField.name()).append('"');
                if (!primitive) {
                    loadBuilder.append(", ").append(type.getQualifiedName()).append(".class");
                }
                loadBuilder.append(", generatedDefault$").append(field.getName());
                if (method != null) {
                    loadBuilder.append(')');
                }
                loadBuilder.append(");");
                saveBuilder.append("configuration.set(\"").append(configField.name()).append("\", this.").append(field.getName())
                    .append(");");
            }
        }
        method = clazz.getMethod("isModified", Configuration.class);
        if (method != null) {
            method.setName("user$isModified");
            method.setPrivate();
            removeAnnotation(method, Override.class);
            clazz.addMethod("""
                @Override
                public boolean isModified() {
                    return this.generated$modified0 || user$isModified();
                }
                """);
            
        } else {
            clazz.addMethod("""
                @Override
                public boolean isModified() {
                    return this.generated$modified0;
                }
                """);
        }
        method = clazz.getMethod("onLoad", Configuration.class);
        if (method != null) {
            method.setName("user$onLoad");
            method.setPrivate();
            removeAnnotation(method, Override.class);
            loadBuilder.append("\nuser$onLoad(configuration);");
        }
        clazz.addMethod(loadBuilder.append("\n}").toString());
        method = clazz.getMethod("onSave", Configuration.class);
        if (method != null) {
            method.setName("user$onSave");
            method.setPrivate();
            removeAnnotation(method, Override.class);
            saveBuilder.append("\nuser$onSave(configuration);");
        }
        clazz.addMethod(saveBuilder.append("\n}").toString());
    }

    private void addFieldMethod(final JavaClassSource source, final FieldSource<JavaClassSource> field, final String content) {
        source.addMethod(content.formatted(field.getName(), field.getType().getQualifiedNameWithGenerics()));
    }

    private void addFieldMethod(final JavaClassSource source, final FieldSource<JavaClassSource> field, final String complex,
        final String primitive) {
        source.addMethod((field.getType().isPrimitive() ? primitive : complex).formatted(field.getName(),
            field.getType().getQualifiedNameWithGenerics()));
    }

}
