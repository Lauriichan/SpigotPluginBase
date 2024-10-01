package me.lauriichan.minecraft.pluginbase.game;

import java.lang.reflect.Type;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectCollections;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.util.ReflectionUtil;

public final class GameManager {

    public static final Pattern ID_PATTERN = Pattern.compile("[a-zA-Z0-9_]+");
    public static final Predicate<String> ID_PREDICATE = ID_PATTERN.asMatchPredicate();

    public static final Pattern NAME_PATTERN = Pattern.compile("[a-zA-Z0-9_/.]+[ a-zA-Z0-9_/.]*");
    public static final Predicate<String> NAME_PREDICATE = NAME_PATTERN.asMatchPredicate();

    private final BasePlugin<?> plugin;
    
    private final Object2ObjectMap<Class<? extends Game<?>>, GameProvider<?>> type2Game;
    private final Object2ObjectMap<String, GameProvider<?>> id2Game;

    public GameManager(final BasePlugin<?> plugin) {
        this.plugin = plugin;
        Object2ObjectArrayMap<Class<? extends Game<?>>, ObjectArrayList<Class<? extends Phase<?>>>> phaseMap = new Object2ObjectArrayMap<>();
        plugin.extension(Phase.class, false).callClasses(phaseType -> {
            GamePhase phaseInfo = phaseType.getDeclaredAnnotation(GamePhase.class);
            if (phaseInfo == null || phaseInfo.game() == null) {
                throw new IllegalStateException("Phase '" + phaseType.getName() + "' doesn't provide the game it is related to");
            }
            Class<?> actualGameType = ReflectionUtil.getGenericOf(phaseType, Phase.class, 0);
            if (!actualGameType.equals(phaseInfo.game())) {
                throw new IllegalStateException("Phase '" + phaseType.getName() + "' declares to be related to '" + phaseInfo.game()
                    + "' but is related to '" + actualGameType.getTypeName() + "'");
            }
            ObjectArrayList<Class<? extends Phase<?>>> list = phaseMap.get(phaseInfo.game());
            if (list == null) {
                list = new ObjectArrayList<>();
                phaseMap.put((Class<? extends Game<?>>) phaseInfo.game(), list);
            }
            list.add((Class<? extends Phase<?>>) phaseType);
        });
        Object2ObjectArrayMap<Class<? extends Game<?>>, ObjectArrayList<Class<? extends Task<?>>>> taskMap = new Object2ObjectArrayMap<>();
        plugin.extension(Task.class, false).callClasses(taskType -> {
            GameTask taskInfo = taskType.getDeclaredAnnotation(GameTask.class);
            if (taskInfo.game() == null) {
                throw new IllegalStateException("Task '" + taskType.getName() + "' doesn't provide the game it is related to");
            }
            Class<?> actualGameType = ReflectionUtil.getGenericOf(taskType, Task.class, 0);
            if (!actualGameType.equals(taskInfo.game())) {
                throw new IllegalStateException("Task '" + taskType.getName() + "' declares to be related to '" + taskInfo.game()
                    + "' but is related to '" + actualGameType.getTypeName() + "'");
            }
            ObjectArrayList<Class<? extends Task<?>>> list = taskMap.get(taskInfo.game());
            if (list == null) {
                list = new ObjectArrayList<>();
                taskMap.put((Class<? extends Game<?>>) taskInfo.game(), list);
            }
            list.add((Class<? extends Task<?>>) taskType);
        });
        Object2ObjectArrayMap<Class<? extends Game>, ObjectArrayList<PhasedListener>> listenerMap = new Object2ObjectArrayMap<>();
        plugin.extension(PhasedListener.class, true).callInstances(listener -> {
            if (listener.gameType() == null) {
                throw new IllegalStateException("Listener '" + listener.getClass().getName() + "' doesn't provide the game it is related to");
            }
            ObjectArrayList<PhasedListener> list = listenerMap.get(listener.gameType());
            if (list == null) {
                list = new ObjectArrayList<>();
                listenerMap.put(listener.gameType(), list);
            }
        });
        Object2ObjectOpenHashMap<Class<? extends Game<?>>, GameProvider<?>> type2Game = new Object2ObjectOpenHashMap<>();
        Object2ObjectOpenHashMap<String, GameProvider<?>> id2Game = new Object2ObjectOpenHashMap<>();
        plugin.extension(Game.class, false).callClasses(gameType -> {
            GameId gameId = gameType.getDeclaredAnnotation(GameId.class);
            if (gameId == null || gameId.value() == null) {
                throw new IllegalArgumentException("No game id provided by game type '" + gameType.getName() + "'.");
            }
            if (!ID_PREDICATE.test(gameId.value())) {
                throw new IllegalArgumentException(
                    "Invalid game id '" + gameId.value() + "' provided by game type '" + gameType.getName() + "'");
            }
            ObjectList<Class<? extends Phase<?>>> phases = phaseMap.get(gameType);
            if (phases == null) {
                phases = ObjectLists.emptyList();
            } else {
                phases.sort((p1, p2) -> Integer.compare(p1.getDeclaredAnnotation(GamePhase.class).orderId(),
                    p2.getDeclaredAnnotation(GamePhase.class).orderId()));
                phases = ObjectLists.unmodifiable(phases);
            }
            ObjectList<Class<? extends Task<?>>> tasks = taskMap.get(gameType);
            if (tasks == null) {
                tasks = ObjectLists.emptyList();
            } else {
                tasks.sort((p1, p2) -> Integer.compare(p2.getDeclaredAnnotation(GameTask.class).orderId(),
                    p1.getDeclaredAnnotation(GameTask.class).orderId()));
                tasks = ObjectLists.unmodifiable(tasks);
            }
            ObjectList<PhasedListener> listeners = listenerMap.get(gameType);
            if (listeners == null) {
                listeners = ObjectLists.emptyList();
            } else {
                listeners = ObjectLists.unmodifiable(listeners);
            }
            GameProvider<?> provider = new GameProvider<>(this, gameId.value(), gameType, phases, tasks, listeners);
            type2Game.put((Class<? extends Game<?>>) gameType, provider);
            id2Game.put(gameId.value(), provider);
        });
        phaseMap.clear();
        this.type2Game = Object2ObjectMaps.unmodifiable(type2Game);
        this.id2Game = Object2ObjectMaps.unmodifiable(id2Game);
    }
    
    public BasePlugin<?> plugin() {
        return plugin;
    }
    
    public <G extends Game<G>> GameProvider<G> get(String id) {
        return (GameProvider<G>) id2Game.get(id);
    }
    
    public <G extends Game<G>> GameProvider<G> get(Class<G> gameType) {
        return (GameProvider<G>) type2Game.get(gameType);
    }
    
    public ObjectCollection<GameProvider<?>> getGames() {
        return ObjectCollections.unmodifiable(id2Game.values());
    }

}
