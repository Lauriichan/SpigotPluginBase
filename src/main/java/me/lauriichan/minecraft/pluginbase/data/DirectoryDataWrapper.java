package me.lauriichan.minecraft.pluginbase.data;

import java.io.File;
import java.util.Collections;
import java.util.Objects;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2LongArrayMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.data.IDirectoryDataExtension.FileData;
import me.lauriichan.minecraft.pluginbase.data.IDirectoryDataExtension.FileKey;
import me.lauriichan.minecraft.pluginbase.extension.Order;
import me.lauriichan.minecraft.pluginbase.resource.source.FileDataSource;
import me.lauriichan.minecraft.pluginbase.resource.source.IDataSource;
import me.lauriichan.minecraft.pluginbase.resource.source.PathDataSource;

public final class DirectoryDataWrapper<T, D extends IDirectoryDataExtension<T>> implements IDirectDataWrapper<T, D> {

    private static final int[] EMPTY = new int[0];

    private static record Result(long timestamp, int state) {}

    public static <T, D extends IDirectoryDataExtension<T>> DirectoryDataWrapper<T, D> create(final BasePlugin<?> plugin,
        final D extension) {
        return new DirectoryDataWrapper<>(plugin, extension, extension.path());
    }

    private final Object2ObjectMap<String, FileKey> pathToKey = Object2ObjectMaps.synchronize(new Object2ObjectArrayMap<>());
    private final Plugin plugin;

    private final Object2LongMap<FileKey> modified = Object2LongMaps.synchronize(new Object2LongArrayMap<>());

    private final ISimpleLogger logger;
    private final DataMigrator migrator;

    private final String path;

    private final int order;

    private final D data;
    private final Class<D> dataType;

    private final File root;
    private final IDataSource rootSource;
    private final IDataHandler<T> handler;

    @SuppressWarnings("unchecked")
    public DirectoryDataWrapper(final BasePlugin<?> plugin, final D extension, final String path) {
        this.plugin = plugin.bukkitPlugin();
        this.logger = plugin.logger();
        this.migrator = plugin.dataMigrator();
        this.path = path;
        this.data = Objects.requireNonNull(extension, "Data extension can't be null");
        this.dataType = (Class<D>) data.getClass();
        this.rootSource = Objects.requireNonNull(plugin.resource(path), "Couldn't find data source at '" + path + "'");
        if (rootSource instanceof PathDataSource) {
            root = ((PathDataSource) rootSource).getSource().toFile();
        } else if (rootSource instanceof FileDataSource) {
            root = ((FileDataSource) rootSource).getSource();
        } else {
            throw new IllegalStateException("Unsupported data source for path '" + path + "': " + rootSource.getClass().getName());
        }
        this.handler = Objects.requireNonNull(extension.handler(), "Data handler can't be null");
        modified.defaultReturnValue(0);
        Order order = dataType.getAnnotation(Order.class);
        this.order = order == null ? 0 : order.value();
        data.keyWrapper(this::keyOf);
    }

    protected FileKey keyOf(String path, String extension) {
        FileKey key = pathToKey.get(path = path.replace('\\', '/'));
        if (key != null) {
            return key;
        }
        pathToKey.put(path, key = new FileKey(new NamespacedKey(plugin, path), extension));
        return key;
    }

    @Override
    public int order() {
        return order;
    }

    @Override
    public D data() {
        return data;
    }

    @Override
    public Class<D> dataType() {
        return dataType;
    }

    @Override
    public String path() {
        return path;
    }

    public File root() {
        return root;
    }

    @Override
    public IDataSource source() {
        return rootSource;
    }

    @Override
    public IDataHandler<T> handler() {
        return handler;
    }

    @Override
    public int[] reload(boolean force, boolean wipeAfterLoad) {
        return reloadDirectory(force, wipeAfterLoad);
    }

    public int[] reloadDirectory(final boolean force, final boolean wipeAfterLoad) {
        if (!root.isDirectory()) {
            if (root.exists()) {
                root.delete();
            }
            root.mkdirs();
            return EMPTY;
        }
        data.onLoadStart(logger);
        ObjectArraySet<FileKey> pending = new ObjectArraySet<>(modified.keySet());
        if (data.removeNewDataOnReload()) {
            pending.addAll(data.newData());
        }
        try {
            File[] files = root.listFiles();
            if (files == null || files.length == 0) {
                modified.clear();
                return EMPTY;
            }
            ReferenceArrayList<File> fileQueue = new ReferenceArrayList<>();
            Collections.addAll(fileQueue, files);
            IntArrayList stateList = new IntArrayList();
            File file;
            String name, path, extension;
            boolean isFile;
            int pathLength = rootSource.getPath().length() + 1, index;
            while (!fileQueue.isEmpty()) {
                file = fileQueue.pop();
                name = file.getName();
                extension = null;
                if (isFile = file.isFile()) {
                    index = name.lastIndexOf('.');
                    if (index == -1) {
                        extension = null;
                    } else {
                        extension = name.substring(index + 1, name.length());
                        name = name.substring(0, index);
                    }
                }
                if ((!data.searchSupportedDirectories() && !isFile) || !data.isSupported(file, name, extension, isFile)) {
                    continue;
                }
                if (!isFile) {
                    files = file.listFiles();
                    if (files == null || files.length == 0) {
                        continue;
                    }
                    Collections.addAll(fileQueue, files);
                    continue;
                }
                path = file.getAbsolutePath();
                path = path.substring(pathLength, path.length() - (extension == null ? 0 : extension.length() + 1));
                if (path.isBlank()) {
                    logger.warning("Failed to check file '{0}' as its' path '{1}' is not a valid key path.", name, path);
                    continue;
                }
                FileKey key = keyOf(path, extension);
                if (key == null) {
                    logger.warning("Failed to check file '{0}' as its' path '{1}' is not a valid key path.", name, path);
                    continue;
                }
                pending.remove(key);
                long lastModified = modified.getLong(name);
                Result newModified = reload(file, key, lastModified, force, wipeAfterLoad);
                stateList.add(newModified.state());
                if (newModified.timestamp() == Long.MIN_VALUE) {
                    modified.removeLong(key);
                    continue;
                }
                if (newModified.timestamp() != lastModified) {
                    modified.put(key, newModified.timestamp());
                }
            }
            return stateList.toIntArray();
        } finally {
            data.onLoadEnd(logger);
            if (data.removeNewDataOnReload()) {
                data.clearNewData();
            }
            for (FileKey key : pending) {
                modified.removeLong(key);
                data.onDeleted(logger, key);
            }
            data.onDeleteDone(logger, this);
        }
    }

    private Result reload(File file, FileKey key, long modified, boolean force, boolean wipeAfterLoad) {
        long lastTimeModified = file.lastModified();
        if (!force && modified == lastTimeModified) {
            return new Result(lastTimeModified, SKIPPED);
        }
        FileDataSource source = new FileDataSource(file);
        FileData<T> value = new FileData<>(file, key);
        if (migrator != null) {
            try {
                handler.load(value, source);
                lastTimeModified = source.lastModified();
            } catch (final Exception exception) {
                logger.warning("Failed to load data from '{0}/{1}'!", exception, path, key.location().getKey());
                return new Result(lastTimeModified, FAIL_IO_LOAD);
            }
            int version = value.version();
            if (migrator.needsMigration(dataType, version)) {
                try {
                    int newVersion = migrator.migrate(logger, version, value, data);
                    value.version(newVersion);
                } catch (DataMigrationFailedException exception) {
                    logger.warning("Failed to migrate data of '{0}/{1}'!", exception, path, key.location().getKey());
                    return new Result(lastTimeModified, FAIL_DATA_MIGRATE);
                }
                try {
                    handler.save(value, source);
                } catch (final Exception exception) {
                    logger.warning("Failed to save migrated to '{0}/{1}'!", exception, path, key.location().getKey());
                    return new Result(lastTimeModified, FAIL_IO_SAVE);
                }
            }
            try {
                handler.load(value, source);
                lastTimeModified = source.lastModified();
            } catch (final Exception exception) {
                logger.warning("Failed to load data from '{0}/{1}'!", exception, path, key.location().getKey());
                return new Result(lastTimeModified, FAIL_IO_LOAD);
            }
        }
        try {
            data.onLoad(logger, value);
        } catch (final Exception exception) {
            logger.warning("Failed to load data of '{0}/{1}'!", exception, path, key.location().getKey());
            return new Result(lastTimeModified, FAIL_DATA_LOAD);
        }
        if (wipeAfterLoad) {
            value.value(null);
        }
        return save(source, value, lastTimeModified);
    }

    @Override
    public int[] save(boolean force) {
        return saveDirectory(force);
    }

    public int[] saveDirectory(final boolean force) {
        try {
            IntArrayList states = new IntArrayList();
            data.onSaveStart(logger);
            ObjectArraySet<FileKey> saved = new ObjectArraySet<>();
            if (data.saveKnownFiles() && !modified.isEmpty()) {
                for (Object2LongMap.Entry<FileKey> entry : modified.object2LongEntrySet()) {
                    File file = new File(root, entry.getKey().filePath());
                    saved.add(entry.getKey());
                    Result newModified = save(file, entry.getKey(), entry.getLongValue(), force);
                    states.add(newModified.state());
                    if (newModified.timestamp() == Long.MIN_VALUE) {
                        modified.removeLong(entry.getKey());
                        continue;
                    }
                    if (newModified.timestamp() != entry.getLongValue()) {
                        modified.put(entry.getKey(), newModified.timestamp());
                    }
                }
            }
            ObjectSet<FileKey> set = data.newData();
            if (set.isEmpty()) {
                return states.toIntArray();
            }
            ObjectIterator<FileKey> iterator = set.iterator();
            while (iterator.hasNext()) {
                FileKey key = iterator.next();
                if (saved.contains(key)) {
                    continue;
                }
                File file = new File(root, key.filePath());
                Result newModified = save(file, key, Long.MIN_VALUE, force);
                states.add(newModified.state());
                if (newModified.timestamp() == Long.MIN_VALUE) {
                    continue;
                }
                modified.put(key, newModified.timestamp());
                iterator.remove();
            }
            return states.toIntArray();
        } finally {
            data.onSaveEnd(logger);
        }
    }

    private Result save(File file, FileKey key, long modified, boolean force) {
        long lastTimeModified = file.lastModified();
        if (!force && modified == lastTimeModified) {
            return new Result(lastTimeModified, SKIPPED);
        }
        FileDataSource source = new FileDataSource(file);
        FileData<T> value = new FileData<>(file, key);
        return save(source, value, modified);
    }

    private Result save(FileDataSource source, FileData<T> value, long lastTimeModified) {
        try {
            data.onSave(logger, value);
        } catch (final Exception exception) {
            logger.warning("Failed to save data of '{0}/{1}'!", exception, path, value.key().location().getKey());
            return new Result(lastTimeModified, FAIL_DATA_SAVE);
        }
        if (migrator != null) {
            value.version(migrator.getTargetVersion(dataType));
        }
        if (value.shouldBeDeleted()) {
            value.file().delete();
            return new Result(Long.MIN_VALUE, SUCCESS);
        }
        try {
            handler.save(value, source);
            lastTimeModified = source.lastModified();
        } catch (final Exception exception) {
            logger.warning("Failed to save data to '{0}/{1}'!", exception, path, value.key().location().getKey());
            return new Result(lastTimeModified, FAIL_IO_SAVE);
        }
        return new Result(lastTimeModified, SUCCESS);
    }

}
