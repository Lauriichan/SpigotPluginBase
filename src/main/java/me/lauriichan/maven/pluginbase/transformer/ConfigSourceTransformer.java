package me.lauriichan.maven.pluginbase.transformer;

import static me.lauriichan.maven.sourcemod.api.SourceTransformerUtils.importClass;
import static me.lauriichan.maven.sourcemod.api.SourceTransformerUtils.removeAnnotation;
import static me.lauriichan.maven.sourcemod.api.SourceTransformerUtils.removeMethod;

import java.util.List;
import java.util.Map;
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
import me.lauriichan.minecraft.pluginbase.config.ConfigValueValidator;
import me.lauriichan.minecraft.pluginbase.config.Configuration;
import me.lauriichan.minecraft.pluginbase.config.IConfigExtension;
import me.lauriichan.minecraft.pluginbase.config.ISingleConfigExtension;
import me.lauriichan.minecraft.pluginbase.extension.Extension;

public class ConfigSourceTransformer implements ISourceTransformer {

    private static final class ConfigField {
        private final FieldSource<JavaClassSource> field;
        private final String name;
        
        public ConfigField(final FieldSource<JavaClassSource> field, final String name) {
            this.field = field;
            this.name = name;
        }
        
        public final FieldSource<JavaClassSource> field() {
            return field;
        }
        
        public final String name() {
            return name;
        }
    }

    @Override
    public boolean canTransform(final JavaSource<?> source) {
        if (!(source instanceof JavaClassSource)) {
            return false;
        }
        final JavaClassSource classSource = (JavaClassSource) source;
        return !classSource.isAbstract() && !classSource.isRecord() && classSource.hasAnnotation(Config.class)
            && (classSource.hasInterface(IConfigExtension.class) || classSource.hasInterface(ISingleConfigExtension.class));
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

        Object2ObjectArrayMap<String, ObjectArrayList<MethodSource<JavaClassSource>>> validators = new Object2ObjectArrayMap<>();
        final List<MethodSource<JavaClassSource>> methods = clazz.getMethods();
        for (final MethodSource<JavaClassSource> method : methods) {
            if (!method.hasAnnotation(ConfigValueValidator.class) || method.getReturnType().isType(void.class)
                || method.getReturnType().isType(Void.class)) {
                continue;
            }
            String[] values = method.getAnnotation(ConfigValueValidator.class).getStringArrayValue();
            for (String value : values) {
                ObjectArrayList<MethodSource<JavaClassSource>> validatorList = validators.get(value);
                if (validatorList == null) {
                    validatorList = new ObjectArrayList<>();
                    validators.put(value, validatorList);
                } else if (validatorList.contains(method)) {
                    throw new IllegalStateException("Duplicated field '" + value + "' for validator: " + method.getName());
                }
                validatorList.add(method);
            }
        }

        if (!clazz.hasAnnotation(Extension.class) && clazz.hasInterface(ISingleConfigExtension.class)) {
            clazz.addAnnotation(Extension.class);
            importClass(clazz, Extension.class);
        }

        clazz.setPublic();
        clazz.setFinal(true);

        if (configFields.isEmpty()) {
            return;
        }

        importClass(clazz, Configuration.class);
        clazz.addField("private volatile boolean generated$modified0 = false;");

        StringBuilder loadBuilder = new StringBuilder();
        StringBuilder saveBuilder = new StringBuilder();
        StringBuilder propergateBuilder = new StringBuilder();
        append(loadBuilder, new String[] {
            "@Override",
            "public void onLoad(Configuration configuration) throws Exception {",
            "    this.generated$modified0 = false;"
        });
        append(saveBuilder, new String[] {
            "@Override",
            "public void onSave(Configuration configuration) throws Exception {",
            "    this.generated$modified0 = false;"
        });
        append(propergateBuilder, new String[] {
            "@Override",
            "public void onPropergate(Configuration configuration) throws Exception {"
        });

        ConfigField configField;
        FieldSource<JavaClassSource> field;
        ObjectArrayList<MethodSource<JavaClassSource>> validatorList;
        ObjectArrayList<String> visitedFields = new ObjectArrayList<>();
        boolean needObjectsImport = false;
        for (int index = 0; index < configFields.size(); index++) {
            field = (configField = configFields.get(index)).field();
            if (visitedFields.contains(configField.name())) {
                throw new IllegalStateException("Duplicated config field: " + configField.name());
            }
            visitedFields.add(configField.name());
            validatorList = validators.remove(configField.name());
            if (validatorList != null) {
                for (int i = 0; i < validatorList.size(); i++) {
                    MethodSource<JavaClassSource> validator = validatorList.get(i);
                    if (validator.getReturnType().isType(field.getType().getQualifiedName())) {
                        validator.setVisibility(Visibility.PRIVATE);
                        validator.setStatic(false);
                        validator.setFinal(true);
                    } else {
                        validatorList.remove(i--);
                    }
                }
                if (validatorList.isEmpty()) {
                    validatorList = null;
                }
            }
            field.setVisibility(Visibility.PRIVATE);
            field.setStatic(false);
            field.setVolatile(false);
            field.setFinal(false);
            if (!field.getType().isPrimitive()) {
                needObjectsImport = true;
            }
            clazz.addField("private final %2$s generatedDefault$%1$s = %1$s;".formatted(field.getName(),
                field.getType().getQualifiedNameWithGenerics()));
            removeMethod(clazz, field.getName());
            removeMethod(clazz, "default$" + field.getName());
            removeMethod(clazz, field.getName(), field.getType().getQualifiedNameWithGenerics());
            addFieldMethod(clazz, field, new String[] {
                "public void %1$s(%2$s %1$s) {",
                "    if (Objects.equals(this.%1$s, %1$s)) {",
                "        return;",
                "    this.%1$s = %1$s;",
                "    this.generated$modified0 = true;",
                "}"
            }, new String[] {
                "public void %1$s(%2$s %1$s) {",
                "    if (this.%1$s == %1$s) {",
                "        return;",
                "    }",
                "    this.%1$s = %1$s;",
                "    this.generated$modified0 = true;",
                "}"
            });
            addFieldMethod(clazz, field, new String[] {
                "public %2$s %1$s() {",
                "    return this.%1$s;",
                "}"
            });
            addFieldMethod(clazz, field, new String[] {
                "public %2$s default$%1$s() {",
                "    return this.generatedDefault$%1$s;",
                "}"
            });
            if (automatic) {
                if (index != 0) {
                    loadBuilder.append('\n');
                }
                loadBuilder.append("this.").append(field.getName()).append(" = ");
                if (validatorList != null) {
                    for (MethodSource<JavaClassSource> validator : validatorList) {
                        loadBuilder.append(validator.getName()).append('(');
                    }
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
                } else if (type.isType(List.class)) {
                    loadBuilder.append("List");
                } else if (type.isType(Map.class)) {
                    loadBuilder.append("Map");
                }
                loadBuilder.append("(\"").append(configField.name).append('"');
                if (!primitive) {
                    if (type.isType(List.class)) {
                        loadBuilder.append(", ").append(type.getTypeArguments().get(0).getQualifiedName()).append(".class");
                    } else if (type.isType(Map.class)) {
                        loadBuilder.append(", ").append(type.getTypeArguments().get(0).getQualifiedName()).append(".class").append(", ")
                            .append(type.getTypeArguments().get(1).getQualifiedName()).append(".class");
                    } else {
                        loadBuilder.append(", ").append(type.getQualifiedName()).append(".class");
                    }
                }
                if (!type.isType(List.class) && !type.isType(Map.class)) {
                    loadBuilder.append(", generatedDefault$").append(field.getName());
                }
                if (validatorList != null) {
                    for (int i = 0; i < validatorList.size(); i++) {
                        loadBuilder.append(')');
                    }
                }
                loadBuilder.append(");");
                saveBuilder.append("configuration.set(\"").append(configField.name).append("\", this.").append(field.getName())
                    .append(");");
                propergateBuilder.append("configuration.set(\"").append(configField.name).append("\", this.generatedDefault$")
                    .append(field.getName()).append(");");
            }
        }
        if (needObjectsImport) {
            importClass(clazz, Objects.class);
        }
        MethodSource<JavaClassSource> method = clazz.getMethod("isModified");
        if (method != null) {
            method.setName("user$isModified");
            method.setPrivate();
            removeAnnotation(method, Override.class);
            clazz.addMethod(string(new String[] {
                "@Override",
                "public boolean isModified() {",
                "    return this.generated$modified0 || user$isModified();",
                "}"
            }));

        } else {
            clazz.addMethod(string(new String[] {
                "@Override",
                "public boolean isModified() {",
                "    return this.generated$modified0",
                "}"
            }));
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
        method = clazz.getMethod("onPropergate", Configuration.class);
        if (method != null) {
            method.setName("user$onPropergate");
            method.setPrivate();
            removeAnnotation(method, Override.class);
            propergateBuilder.append("\nuser$onPropergate(configuration);");
        }
        clazz.addMethod(propergateBuilder.append("\n}").toString());
    }

    private String string(String[] content) {
        return String.join("\n", content);
    }

    private void append(StringBuilder builder, String[] content) {
        for (int i = 0; i < content.length; i++) {
            if (i != 0) {
                builder.append("\n");
            }
            builder.append(content[i]);
        }
    }

    private void addFieldMethod(final JavaClassSource source, final FieldSource<JavaClassSource> field, final String[] content) {
        source.addMethod(string(content).formatted(field.getName(), field.getType().getQualifiedNameWithGenerics()));
    }

    private void addFieldMethod(final JavaClassSource source, final FieldSource<JavaClassSource> field, final String[] complex,
        final String[] primitive) {
        source.addMethod(string(field.getType().isPrimitive() ? primitive : complex).formatted(field.getName(),
            field.getType().getQualifiedNameWithGenerics()));
    }

}
