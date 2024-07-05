package me.lauriichan.minecraft.pluginbase.util.debug;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Objects;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public final class Tracer {
    
    private static final Object POP = new Object();

    private static record Task(String name, Task parent, ObjectArrayList<Object> stack) {
        public Task(String name, Task parent) {
            this(name, parent, new ObjectArrayList<>());
        }
    }

    private final Task root = new Task(null, null);
    private volatile Task current = root;

    public void push(String name) {
        Task tmp = new Task(name, current);
        current.stack.add(tmp);
        current = tmp;
    }

    public void log(Object object) {
        if (current.parent == null) {
            throw new IllegalStateException("You need to push() first");
        }
        current.stack.add(object);
    }

    public void pop() {
        if (current.parent == null) {
            throw new IllegalStateException("You need to push() first");
        }
        current = current.parent;
    }

    public boolean isDone() {
        return current.parent == null;
    }

    public void clear() {
        root.stack.clear();
        current = root;
    }
    
    public void print(OutputStream stream) throws IOException {
        try (OutputStreamWriter writer = new OutputStreamWriter(stream)) {
            print(writer);
        }
    }

    public void print(Writer writer) throws IOException {
        ObjectArrayList<Object> queue = new ObjectArrayList<>();
        queue.addAll(root.stack);
        String indent = "";
        writer.write("||| TRACE START\n");
        while (!queue.isEmpty()) {
            Object object = queue.remove(0);
            if (object == POP) {
                indent = indent.substring(2);
                writer.write("\n" + indent + "| TASK END");
                continue;
            }
            if (object instanceof Task task) {
                writer.write("\n" + indent + "| TASK START (" + task.name + "):");
                queue.addAll(task.stack);
                queue.add(POP);
                indent += "  ";
                continue;
            }
            writer.write("\n" + indent + Objects.toString(object));
        }
        writer.write("\n\n||| TRACE END");
    }

}
