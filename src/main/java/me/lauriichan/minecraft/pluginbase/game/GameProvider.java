package me.lauriichan.minecraft.pluginbase.game;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectCollections;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import me.lauriichan.minecraft.pluginbase.game.phased.PhasedType;

public final class GameProvider<G extends Game<G>> {

    private final GameManager manager;

    private final String id;

    private final Class<G> gameType;

    private final ObjectList<Class<? extends Phase<?>>> phases;
    private final ObjectList<PhasedType<? extends Task<G>>> tasks;
    private final ObjectList<PhasedListener> listeners;

    private final Object2ObjectMap<String, GameState<G>> states = Object2ObjectMaps.synchronize(new Object2ObjectArrayMap<>());

    GameProvider(final GameManager manager, final String id, final Class<G> gameType, final ObjectList<Class<? extends Phase<?>>> phaseList,
        final ObjectList<Class<? extends Task<?>>> taskList, final ObjectList<PhasedListener> listeners) {
        this.manager = manager;
        this.id = id;
        this.gameType = gameType;
        this.phases = phaseList;
        if (taskList.isEmpty()) {
            this.tasks = ObjectLists.emptyList();
        } else {
            ObjectArrayList<PhasedType<? extends Task<G>>> tasks = new ObjectArrayList<>(taskList.size());
            for (Class<? extends Task<?>> taskType : taskList) {
                tasks.add(new PhasedType<>((Class<? extends Task<G>>) taskType));
            }
            this.tasks = ObjectLists.unmodifiable(tasks);
        }
        this.listeners = listeners;
    }

    public GameManager manager() {
        return manager;
    }

    public String id() {
        return id;
    }

    public Class<G> gameType() {
        return gameType;
    }

    public ObjectList<Class<? extends Phase<?>>> phases() {
        return phases;
    }
    
    public ObjectList<PhasedType<? extends Task<G>>> tasks() {
        return tasks;
    }

    public ObjectList<PhasedListener> listeners() {
        return listeners;
    }

    public boolean hasState(String name) {
        return states.containsKey(name);
    }

    public GameState<G> newState(String name) {
        GameState<G> state = states.get(name);
        if (state != null) {
            throw new IllegalArgumentException("There is already a game instance with the name '" + name + "'");
        }
        state = new GameState<>(name, this);
        states.put(name, state);
        return state;
    }

    public GameState<G> getState(String name) {
        return states.get(name);
    }
    
    public ObjectCollection<GameState<G>> states() {
        return ObjectCollections.unmodifiable(states.values());
    }

    void remove(GameState<G> state) {
        states.remove(state.name(), state);
    }

}
