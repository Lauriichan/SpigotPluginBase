package me.lauriichan.maven.pluginbase.util;

import java.lang.annotation.Annotation;

import org.jboss.forge.roaster.model.source.AnnotationSource;
import org.jboss.forge.roaster.model.source.AnnotationTargetSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaSource;
import org.jboss.forge.roaster.model.source.MethodSource;

public final class SourceTransformerUtils {

    private SourceTransformerUtils() {
        throw new UnsupportedOperationException();
    }

    public static void removeMethod(final JavaClassSource source, final String name) {
        final MethodSource<JavaClassSource> method = source.getMethod(name);
        if (method == null) {
            return;
        }
        source.removeMethod(method);
    }

    public static void removeMethod(final JavaClassSource source, final String name, final Class<?>... types) {
        final MethodSource<JavaClassSource> method = source.getMethod(name, types);
        if (method == null) {
            return;
        }
        source.removeMethod(method);
    }

    public static void removeMethod(final JavaClassSource source, final String name, final String... types) {
        final MethodSource<JavaClassSource> method = source.getMethod(name, types);
        if (method == null) {
            return;
        }
        source.removeMethod(method);
    }

    public static void importClass(final JavaClassSource source, final Class<?> clazz) {
        if (source.hasImport(clazz)) {
            return;
        }
        source.addImport(clazz);
    }

    public static void importClass(final JavaClassSource source, final String className) {
        if (source.hasImport(className)) {
            return;
        }
        source.addImport(className);
    }

    public static <E extends JavaSource<E>> void removeAnnotation(final AnnotationTargetSource<E, ?> source,
        final Class<? extends Annotation> annotation) {
        final AnnotationSource<E> annotationSource = source.getAnnotation(annotation);
        if (annotationSource != null) {
            source.removeAnnotation(annotationSource);
        }
    }

    public static boolean hasSuperType(final JavaClassSource source, final Class<?> clazz) {
        return source.getSuperType() != null && source.getSuperType().equals(clazz.getName());
    }

}
