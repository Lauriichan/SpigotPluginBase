package me.lauriichan.minecraft.pluginbase.message.component;

import java.util.Objects;
import java.util.Optional;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.md_5.bungee.api.chat.BaseComponent;

public final class ComponentCompound extends SendableComponent {

    public static ComponentCompound create() {
        return new ComponentCompound();
    }

    private final ObjectArrayList<Component> components = new ObjectArrayList<>();

    private ComponentCompound() {}
    
    public int size() {
        return components.size();
    }
    
    public boolean isEmpty() {
        return components.isEmpty();
    }

    public ComponentCompound add(final Component component) {
        components.add(Objects.requireNonNull(component));
        return this;
    }

    public ComponentCompound remove(final int index) {
        if (index < 0 || index >= components.size()) {
            return this;
        }
        components.remove(index);
        return this;
    }

    public Optional<Component> get(final int index) {
        if (index < 0 || index >= components.size()) {
            return Optional.empty();
        }
        return Optional.of(components.get(index));
    }

    @Override
    public BaseComponent[] build() {
        if (this.components.isEmpty()) {
            return EMPTY;
        }
        int length = 0;
        final Component[] components = this.components.toArray(Component[]::new);
        final ObjectArrayList<BaseComponent[]> messages = new ObjectArrayList<>();
        for (int index = 0; index < components.length; index++) {
            final BaseComponent[] message = components[index].build();
            messages.add(message);
            length += message.length;
        }
        final BaseComponent[] output = new BaseComponent[length];
        int index = 0;
        for (final BaseComponent[] message : messages) {
            System.arraycopy(message, 0, output, index, message.length);
            index += message.length;
        }
        return output;
    }

}