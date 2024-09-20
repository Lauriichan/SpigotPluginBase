package me.lauriichan.minecraft.pluginbase.game;

import java.lang.reflect.Method;

import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.laylib.reflection.ClassUtil;
import me.lauriichan.minecraft.pluginbase.extension.ExtensionPoint;
import me.lauriichan.minecraft.pluginbase.extension.IExtension;

@ExtensionPoint
public abstract class PhasedListener implements IExtension {

    private final Class<? extends Game> gameType;
    private final ObjectList<PhasedEventExecutor> executors;

    public PhasedListener(final Class<? extends Game> gameType, final ISimpleLogger logger) {
        this.gameType = gameType;
        ObjectArrayList<PhasedEventExecutor> executors = new ObjectArrayList<>();
        Class<?> listenerClass = getClass();
        Method[] methods = ClassUtil.getMethods(listenerClass);
        for (Method method : methods) {
            final EventHandler handler = method.getAnnotation(EventHandler.class);
            if (handler == null) {
                continue;
            }
            Class<?>[] types = method.getParameterTypes();
            if (types.length > 2 || types.length < 1) {
                logger.warning(
                    "Failed to register listener method '{0}' in class '{1}' as it has too many or too less parameters (expected 1-2 but got {2})",
                    method.getName(), listenerClass.getName(), types.length);
                continue;
            }
            Class<? extends Event> eventType;
            Class<?> gameStateType = null;
            if (Event.class.isAssignableFrom(types[0])) {
                eventType = types[0].asSubclass(Event.class);
                if (types.length == 2) {
                    gameStateType = types[1];
                }
            } else if (types.length == 2 && Event.class.isAssignableFrom(types[1])) {
                eventType = types[1].asSubclass(Event.class);
                gameStateType = types[0];
            } else {
                logger.warning("Failed to register listener method '{0}' in class '{1}' as it has no event parameter", method.getName(),
                    listenerClass.getName());
                continue;
            }
            if (gameStateType != null && GameState.class != gameStateType) {
                logger.warning(
                    "Failed to register listener method '{0}' in class '{1}' as it has 2 parameters but the second is not a 'GameState' parameter",
                    method.getName(), listenerClass.getName());
                continue;
            }
            PhasedEventExecutor.EventCall call = gameStateType == null ? PhasedEventExecutor.EVENT
                : (gameStateType == types[0] ? PhasedEventExecutor.STATE_EVENT : PhasedEventExecutor.EVENT_STATE);
            executors.add(new PhasedEventExecutor(handler.priority(), handler.ignoreCancelled(), method, eventType, call));
        }
        if (executors.isEmpty()) {
            throw new IllegalStateException("Phase listener has no valid listening methods");
        }
        this.executors = ObjectLists.unmodifiable(executors);
    }

    public final Class<? extends Game> gameType() {
        return gameType;
    }

    final ObjectList<PhasedEventExecutor> executors() {
        return executors;
    }

}
