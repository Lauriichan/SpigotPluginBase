package me.lauriichan.minecraft.pluginbase.game;

import java.util.Map;

import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredListener;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;

final class PhasedEventListener implements Listener {

    private final PhasedListener listener;
    private final GameState<?> state;
    
    private final ObjectList<Map.Entry<RegisteredListener, PhasedEventExecutor>> activeExecutors;
    
    PhasedEventListener(PhasedListener listener, GameState<?> state) {
        this.listener = listener;
        this.state = state;
        this.activeExecutors = ObjectLists.synchronize(new ObjectArrayList<>(listener.executors().size()));
    }

    PhasedListener listener() {
        return listener;
    }

    GameState<?> state() {
        return state;
    }
    
    void unregister() {
        if (activeExecutors.isEmpty()) {
            return;
        }
        activeExecutors.forEach(entry -> entry.getValue().unregister(entry.getKey()));
    }
    
    void update(Class<? extends Phase<?>> newPhase) {
        ObjectList<PhasedEventExecutor> stillActive;
        if (!activeExecutors.isEmpty()) {
            stillActive = new ObjectArrayList<>(activeExecutors.size());
            for (int i = 0; i < activeExecutors.size(); i++) {
                Map.Entry<RegisteredListener, PhasedEventExecutor> entry = activeExecutors.get(i);
                if (!entry.getValue().shouldBeActive(newPhase)) {
                    entry.getValue().unregister(entry.getKey());
                    activeExecutors.remove(i--);
                    continue;
                }
                stillActive.add(entry.getValue());
            }
        } else {
            stillActive = ObjectLists.emptyList();
        }
        listener.executors().forEach(executor -> {
            if (stillActive.contains(executor) || !executor.shouldBeActive(newPhase)) {
                return;
            }
            activeExecutors.add(Map.entry(executor.register(this), executor));
        });
    }
    
    boolean hasActive() {
        return !activeExecutors.isEmpty();
    }

}
