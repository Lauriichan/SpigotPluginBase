package me.lauriichan.minecraft.pluginbase.game;

import java.util.concurrent.TimeUnit;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.laylib.reflection.JavaAccess;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.game.phased.IPhased;
import me.lauriichan.minecraft.pluginbase.util.tick.AbstractTickTimer;

public final class GameState<G extends Game> {

    private static class StateTickTimer extends AbstractTickTimer {
        private final GameState<?> state;

        public StateTickTimer(final GameState<?> state) {
            this.state = state;
        }

        @Override
        protected void tick(long delta) {
            state.tick(delta);
        }
    }

    private final String name;
    
    private final BasePlugin<?> plugin;
    private final ISimpleLogger logger;

    private final StateTickTimer timer = new StateTickTimer(this);

    private final GameProvider<G> provider;
    private final G game;
    
    private final ObjectList<Phase<G>> phases;
    private final ObjectList<IPhased<? extends Task<G>>> tasks;
    private final ObjectList<PhasedEventListener> listeners;

    private final ObjectList<Task<G>> activeTasks;
    private final ObjectList<PhasedEventListener> activeListeners;

    private volatile int phaseIdx;
    
    GameState(final String name, final GameProvider<G> provider) {
        if (!GameManager.NAME_PREDICATE.test(name)) {
            throw new IllegalArgumentException("Invalid state name '" + name + "'");
        }
        this.plugin = provider.manager().plugin();
        this.logger = plugin.logger();
        this.name = name;
        this.provider = provider;
        this.game = JavaAccess.PLATFORM.instance(provider.gameType());
        ObjectArrayList<Phase<G>> phases = new ObjectArrayList<>(provider.phases().size());
        provider.phases().forEach(phaseType -> phases.add((Phase<G>) JavaAccess.PLATFORM.instance(phaseType)));
        this.phases = ObjectLists.unmodifiable(phases);
        ObjectArrayList<IPhased<? extends Task<G>>> tasks = new ObjectArrayList<>(provider.tasks().size());
        provider.tasks().forEach(type -> tasks.add(type.newRef()));
        this.tasks = ObjectLists.unmodifiable(tasks);
        this.activeTasks = tasks.isEmpty() ? ObjectLists.emptyList() :  ObjectLists.synchronize(new ObjectArrayList<>(tasks.size()));
        ObjectArrayList<PhasedEventListener> listeners = new ObjectArrayList<>(provider.listeners().size());
        provider.listeners().forEach(listener -> listeners.add(new PhasedEventListener(listener, this)));
        this.listeners = ObjectLists.unmodifiable(listeners);
        this.activeListeners = listeners.isEmpty() ? ObjectLists.emptyList() :  ObjectLists.synchronize(new ObjectArrayList<>(listeners.size()));
        this.phaseIdx = phases.isEmpty() ? -1 : 0;
        timer.setLength(50, TimeUnit.MILLISECONDS);
        timer.setPauseLength(1, TimeUnit.SECONDS);
        timer.setName(provider.id() + "/" + name);
        game.onStart(this, timer);
        if (phaseIdx != -1) {
            phases.get(phaseIdx).onBegin(this);
        }
        timer.start();
    }
    
    public BasePlugin<?> plugin() {
        return plugin;
    }
    
    public ISimpleLogger logger() {
        return logger;
    }

    public final String name() {
        return name;
    }

    public final GameProvider<G> provider() {
        return provider;
    }

    public final G game() {
        return game;
    }

    private void tick(long delta) {
        game.onTick(this, delta);
        if (phaseIdx != -1) {
            Phase<G> current = phases.get(phaseIdx);
            current.onTick(this, delta);
            if (current.nextPhase()) {
                timer.pause();
                try {
                    current.onEnd(this);
                    if (++phaseIdx == phases.size()) {
                        if (!game.shouldRestart(this)) {
                            terminate();
                            return;
                        }
                        phaseIdx = 0;
                    }
                    activatePhase(phases.get(phaseIdx));
                } finally {
                    timer.start();
                }
            }
            game.onTickPostPhase(this, delta);
        }
        if (!activeTasks.isEmpty()) {
            for (Task<G> task : activeTasks) {
                task.onTick(this, delta);
            }
            game.onTickPostTask(this, delta);
        }
    }
    
    public void setPhase(Class<? extends Phase<?>> phaseType) {
        if (!timer.isAlive()) {
            return;
        }
        int index = provider.phases().indexOf(phaseType);
        if (index == -1) {
            throw new IllegalArgumentException("Unknown phase '" + phaseType.getName() + "' for game '" + provider.id() + "'.");
        }
        if (phaseIdx == index) {
            return;
        }
        timer.pause();
        try {
            phases.get(phaseIdx).onEnd(this);
            phaseIdx = index;
            activatePhase(phases.get(index));
        } finally {
            timer.start();
        }
    }
    
    private void activatePhase(Phase<G> phase) {
        phase.onBegin(this);
        if(!tasks.isEmpty()) {
            if (!activeTasks.isEmpty()) {
                activeTasks.clear();
            }
            Class<? extends Phase<?>> phaseType = (Class<? extends Phase<?>>) phase.getClass();
            tasks.forEach(task -> {
                if (task.shouldBeActive(phaseType)) {
                    activeTasks.add(task.get());
                }
            });
        }
        if (!provider.listeners().isEmpty()) {
            if (!activeListeners.isEmpty()) {
                activeListeners.clear();
            }
            Class<? extends Phase<?>> phaseType = (Class<? extends Phase<?>>) phase.getClass();
            listeners.forEach(listener -> {
                listener.update(phaseType);
                if (listener.hasActive()) {
                    activeListeners.add(listener);
                }
            });
        }
    }

    public final void terminate() {
        if (!timer.isAlive()) {
            return;
        }
        // Stop timer before game
        timer.stop();
        for (PhasedEventListener listener : activeListeners) {
            listener.unregister();
        }
        activeListeners.clear();
        activeTasks.clear();
        game.onStop(this);
    }

    public final void pause() {
        if (timer.isPaused() || !timer.isAlive()) {
            return;
        }
        timer.pause();
    }

    public final void start() {
        if (!timer.isPaused() || !timer.isAlive()) {
            return;
        }
        timer.start();
    }

}
