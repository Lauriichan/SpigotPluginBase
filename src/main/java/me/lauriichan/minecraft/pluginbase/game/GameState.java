package me.lauriichan.minecraft.pluginbase.game;

import java.util.concurrent.TimeUnit;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.game.phased.IPhased;
import me.lauriichan.minecraft.pluginbase.util.instance.SimpleInstanceInvoker;
import me.lauriichan.minecraft.pluginbase.util.tick.AbstractTickTimer;

public final class GameState<G extends Game<G>> {

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
    
    private final SimpleInstanceInvoker stateInvoker;

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
        this.stateInvoker = new SimpleInstanceInvoker(plugin.pluginInvoker());
        this.provider = provider;
        this.game = invoke(provider.gameType());
        ObjectArrayList<Phase<G>> phases = new ObjectArrayList<>(provider.phases().size());
        provider.phases().forEach(phaseType -> phases.add((Phase<G>) invoke(phaseType)));
        this.phases = ObjectLists.unmodifiable(phases);
        ObjectArrayList<IPhased<? extends Task<G>>> tasks = new ObjectArrayList<>(provider.tasks().size());
        provider.tasks().forEach(type -> tasks.add(type.newRef(this::invoke)));
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
    
    public <T> T invoke(Class<T> type) {
        try {
            return stateInvoker.invoke(type, this);
        } catch(Throwable throwable) {
            throw new IllegalStateException("Failed to create instance of class '" + type.getName() + "'", throwable);
        }
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
    
    public final int phaseIndex() {
        return phaseIdx;
    }
    
    public final Phase<G> phase() {
        if (phaseIdx == -1) {
            return null;
        }
        return phases.get(phaseIdx);
    }

    public final <P extends Phase<G>> P phase(Class<P> phaseType) {
        return phases.stream().filter(phase -> phaseType.isAssignableFrom(phase.getClass())).findFirst().map(phaseType::cast).orElse(null);
    }

    public final <T extends Task<G>> T task(Class<T> taskType) {
        return phases.stream().filter(phase -> taskType.isAssignableFrom(phase.getClass())).findFirst().map(taskType::cast).orElse(null);
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
                    current.onPhaseChange(this);
                    if (++phaseIdx == phases.size()) {
                        if (!game.shouldRestart(this)) {
                            terminate();
                            return;
                        }
                        game.onStop(this);
                        phaseIdx = 0;
                        game.onStart(this, timer);
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
            Phase<G> phase = phases.get(phaseIdx);
            phase.onEnd(this);
            phase.onPhaseChange(this);
            phaseIdx = index;
            activatePhase(phases.get(index));
        } finally {
            timer.start();
        }
    }
    
    private void activatePhase(Phase<G> phase) {
        phase.onBegin(this);
        if(!tasks.isEmpty()) {
            ObjectList<Task<G>> active = new ObjectArrayList<>();
            if (!activeTasks.isEmpty()) {
                active.addAll(activeTasks);
                activeTasks.clear();
            }
            Class<? extends Phase<?>> phaseType = (Class<? extends Phase<?>>) phase.getClass();
            tasks.forEach(task -> {
                if (activeTasks.contains(task.get())) {
                    return;
                }
                if (task.shouldBeActive(phaseType)) {
                    activeTasks.add(task.get());
                    if (active.remove(task.get())) {
                        task.get().onStart(this);
                    }
                }
            });
            active.forEach(task -> task.onStop(this));
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
        for (Task<G> task : activeTasks) {
            task.onStop(this);
        }        
        Phase<G> phase = phase();
        if (phase != null) {
            phase.onEnd(this);
        }
        game.onStop(this);
        activeListeners.clear();
        activeTasks.clear();
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
