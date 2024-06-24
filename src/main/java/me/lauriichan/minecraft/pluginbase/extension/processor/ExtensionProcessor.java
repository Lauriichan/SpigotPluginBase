package me.lauriichan.minecraft.pluginbase.extension.processor;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.NoType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import me.lauriichan.laylib.logger.util.StringUtil;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.extension.ExtensionPoint;
import me.lauriichan.minecraft.pluginbase.extension.IExtension;

public class ExtensionProcessor extends AbstractProcessor {

    public static final String EXTENSION_RESOURCE = "META-INF/extension/";

    public static String extensionPath(final String typeName) {
        return EXTENSION_RESOURCE + typeName;
    }

    private HashSet<String> extensions;
    private HashMap<String, HashSet<String>> extensionPoints;

    private Types typeHelper;
    private Elements elementHelper;

    private TypeMirror extensionType;

    @Override
    public synchronized void init(final ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        this.typeHelper = processingEnv.getTypeUtils();
        this.elementHelper = processingEnv.getElementUtils();
        this.extensionType = elementHelper.getTypeElement(IExtension.class.getName()).asType();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(Extension.class.getName());
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            return false;
        }
        extensions = new HashSet<>();
        extensionPoints = new HashMap<>();

        log(Kind.NOTE, "Processing @%s", Extension.class.getSimpleName());
        for (final Element element : roundEnv.getElementsAnnotatedWith(Extension.class)) {
            if (element.getKind() == ElementKind.ANNOTATION_TYPE) {
                continue;
            }
            preProcessExtension(element);
        }

        log(Kind.NOTE, "Processing nested @%s", Extension.class.getSimpleName());
        for (final TypeElement typeElement : annotations) {
            if (getAnnotationMirror(typeElement, Extension.class) == null) {
                continue;
            }
            for (final Element element : roundEnv.getElementsAnnotatedWith(typeElement)) {
                preProcessExtension(element);
            }
        }

        log(Kind.NOTE, "Saving Extensions (%s) to file", extensions.size());
        try {
            for (final HashMap.Entry<String, HashSet<String>> entry : extensionPoints.entrySet()) {
                if (entry.getValue().isEmpty()) {
                    continue;
                }
                final FileObject file = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "",
                    EXTENSION_RESOURCE + entry.getKey());
                try (BufferedWriter writer = new BufferedWriter(file.openWriter())) {
                    for (final String value : entry.getValue()) {
                        writer.write(value);
                        writer.write('\n');
                    }
                }
            }
        } catch (final IOException e) {
            log(Kind.ERROR, StringUtil.stackTraceToString(e));
        }

        return false;
    }

    private void preProcessExtension(final Element element) {
        if (element instanceof TypeElement) {
            final TypeElement typeElement = (TypeElement) element;
            if (getAnnotationMirror(typeElement, ExtensionPoint.class) != null
                && isInterfaceNested(typeElement.getInterfaces(), extensionType)) {
                log(Kind.ERROR, "'%s' ExtensionPoint can't be a Extension at the same time!", typeElement);
                return;
            }
        }
        processExtension(element);
    }

    private void processExtension(final Element element) {
        log(Kind.NOTE, "Processing Extension '%s'", element.asType().toString());
        if (!(element instanceof TypeElement)) {
            log(Kind.ERROR, "Extension annotation is only available for classes");
            return;
        }
        final TypeMirror type = element.asType();
        if (!typeHelper.isAssignable(type, extensionType)) {
            log(Kind.ERROR, "'%s' is not an Extension (doesn't implement IExtension)", element);
            return;
        }
        final TypeElement typeElement = (TypeElement) element;
        if (typeElement.getModifiers().contains(Modifier.ABSTRACT)) {
            log(Kind.WARNING, "%s is an abstract class and can't be a Extension", typeElement);
            return;
        }
        final String typeName = typeHelper.asElement(type).toString();
        if (extensions.contains(typeName)) {
            return; // Don't know if that will even happen
        }
        extensions.add(typeName);
        log(Kind.NOTE, "Collecting ExtensionPoints for '%s'", typeName);
        addToPoints(typeName, typeElement);
    }

    private void addToPoints(final String name, final TypeElement superElement) {
        final ArrayList<TypeMirror> queue = new ArrayList<>();
        queue.addAll(superElement.getInterfaces());
        if (!(superElement.getSuperclass() instanceof NoType)) {
            queue.add(superElement.getSuperclass());
        }
        while (!queue.isEmpty()) {
            final TypeMirror mirror = queue.remove(0);
            final String typeName = typeHelper.asElement(mirror).toString();
            if (extensionPoints.containsKey(typeName)) {
                HashSet<String> set = extensionPoints.get(typeName);
                if (set.contains(name)) {
                    continue;
                }
                log(Kind.NOTE, "Adding ExtensionPoint '%s' for '%s'", typeName, name);
                set.add(name);
                continue;
            }
            if (mirror == extensionType || !typeHelper.isAssignable(mirror, extensionType)
                || !(typeHelper.asElement(mirror) instanceof final TypeElement typeElement)) {
                continue;
            }
            if (getAnnotationMirror(typeElement, ExtensionPoint.class) == null) {
                queue.addAll(typeElement.getInterfaces());
                continue;
            }
            log(Kind.NOTE, "Adding ExtensionPoint '%s' for '%s'", typeName, name);
            final HashSet<String> set = new HashSet<>();
            set.add(name);
            extensionPoints.put(typeName, set);
        }
        final TypeMirror mirror = superElement.getSuperclass();
        if (!typeHelper.isAssignable(mirror, extensionType) || !(typeHelper.asElement(mirror) instanceof final TypeElement typeElement)) {
            return;
        }
        addToPoints(name, typeElement);
    }

    /*
     * Logging
     */

    public void log(final Kind kind, final String message, final Object... arguments) {
        final String out = String.format(message, arguments);
        processingEnv.getMessager().printMessage(kind, out);
        if (kind == Kind.ERROR) {
            System.out.println("[ERROR] " + out);
            return;
        }
        if (kind == Kind.WARNING) {
            System.out.println("[WARNING] " + out);
            return;
        }
        System.out.println("[INFO] " + out);
    }

    /*
     * Helper
     */

    public boolean isInterfaceNested(final List<? extends TypeMirror> list, final TypeMirror searched) {
        for (final TypeMirror mirror : list) {
            if (mirror == searched) {
                return true;
            }
            final Element element = typeHelper.asElement(mirror);
            if (!(element instanceof TypeElement) || !isInterfaceNested(((TypeElement) element).getInterfaces(), searched)) {
                continue;
            }
            return true;
        }
        return false;
    }

    public AnnotationMirror getAnnotationMirror(final TypeElement element, final Class<?> annotation) {
        final String annotationName = annotation.getName();
        for (final AnnotationMirror mirror : element.getAnnotationMirrors()) {
            if (mirror.getAnnotationType().toString().equals(annotationName)) {
                return mirror;
            }
        }
        return null;
    }

}
