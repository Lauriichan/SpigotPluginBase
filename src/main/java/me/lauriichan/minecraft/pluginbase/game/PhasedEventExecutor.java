package me.lauriichan.minecraft.pluginbase.game;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.RegisteredListener;

import me.lauriichan.laylib.reflection.ClassUtil;
import me.lauriichan.laylib.reflection.JavaAccess;
import me.lauriichan.minecraft.pluginbase.game.phased.PhasedRef;

final class PhasedEventExecutor implements EventExecutor {

    static interface EventCall {
        void execute(PhasedListener listener, GameState<?> state, Method method, Event event) throws Throwable;
    }

    static final EventCall EVENT = (listener, state, method, event) -> method.invoke(listener, event);
    static final EventCall EVENT_STATE = (listener, state, method, event) -> method.invoke(listener, event, state);
    static final EventCall STATE_EVENT = (listener, state, method, event) -> method.invoke(listener, state, event);

    private final EventPriority priority;
    private final boolean ignoreCancelled;

    private final PhasedRef<Method> method;
    private final Class<? extends Event> eventType;

    private final EventCall call;

    private final HandlerList list;

    public PhasedEventExecutor(EventPriority priority, boolean ignoreCancelled, Method method, Class<? extends Event> eventType,
        EventCall call) {
        this.priority = priority;
        this.ignoreCancelled = ignoreCancelled;
        this.method = new PhasedRef<>(method);
        this.eventType = eventType;
        this.call = call;
        this.list = JavaAccess.PLATFORM.invoke(ClassUtil.getMethod(eventType, "getHandlerList"));
    }

    @Override
    public void execute(Listener listener, Event event) throws EventException {
        PhasedEventListener eventListener = (PhasedEventListener) listener;
        try {
            if (!eventType.isAssignableFrom(event.getClass())) {
                return;
            }
            call.execute(eventListener.listener(), eventListener.state(), method.get(), event);
        } catch (InvocationTargetException ex) {
            eventListener.state().logger().error(
                "Failed to execute event '" + eventListener.listener().getClass().getName() + "#" + method.get().getName() + "'",
                ex.getCause());
        } catch (Throwable t) {
            eventListener.state().logger()
                .error("Failed to execute event '" + eventListener.listener().getClass().getName() + "#" + method.get().getName() + "'", t);
        }
    }

    void unregister(RegisteredListener listener) {
        list.unregister(listener);
    }

    RegisteredListener register(PhasedEventListener listener) {
        RegisteredListener registered = new RegisteredListener(listener, this, priority, listener.state().plugin().bukkitPlugin(),
            ignoreCancelled);
        list.register(registered);
        return registered;
    }

    boolean shouldBeActive(Class<? extends Phase<?>> phase) {
        return method.shouldBeActive(phase);
    }
    
    public String methodName() {
        return method.get().getName();
    }
}