package me.lauriichan.minecraft.pluginbase.data;

import java.io.File;
import java.io.FileFilter;
import java.util.Objects;

import it.unimi.dsi.fastutil.objects.Object2LongArrayMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.data.IDirectoryDataExtension.FileData;
import me.lauriichan.minecraft.pluginbase.resource.source.FileDataSource;
import me.lauriichan.minecraft.pluginbase.resource.source.IDataSource;
import me.lauriichan.minecraft.pluginbase.resource.source.PathDataSource;

public final class DirectoryDataWrapper<T, D extends IDirectoryDataExtension<T>> implements IDataWrapper<T, D> {

    private static class ExtensionFileFilter implements FileFilter {

        private final IDirectoryDataExtension<?> extension;

        public ExtensionFileFilter(final IDirectoryDataExtension<?> extension) {
            this.extension = extension;
        }

        @Override
        public boolean accept(File file) {
            String fileName = file.getName();
            String fileExtension;
            boolean isFile;
            if (isFile = file.isFile()) {
                int index = fileName.lastIndexOf('.');
                if (index != -1) {
                    fileExtension = fileName.substring(index + 1, fileName.length());
                    fileName = fileName.substring(0, index);
                } else {
                    fileExtension = "";
                }
            } else {
                fileExtension = null;
            }
            return extension.isSupported(file, fileName, fileExtension, isFile);
        }

    }

    public static <T, D extends IDirectoryDataExtension<T>> DirectoryDataWrapper<T, D> create(final BasePlugin<?> plugin,
        final D extension) {
        return new DirectoryDataWrapper<>(plugin, extension, extension.path());
    }

    private final Object2LongMap<String> modified = Object2LongMaps.synchronize(new Object2LongArrayMap<>());

    private final ISimpleLogger logger;
    private final DataMigrator migrator;

    private final String path;

    private final D data;
    private final Class<D> dataType;

    private final File root;
    private final IDataSource rootSource;
    private final IDataHandler<T> handler;

    private final ExtensionFileFilter filter;

    @SuppressWarnings("unchecked")
    public DirectoryDataWrapper(final BasePlugin<?> plugin, final D extension, final String path) {
        this.logger = plugin.logger();
        this.migrator = plugin.dataMigrator();
        this.path = path;
        this.data = Objects.requireNonNull(extension, "Data extension can't be null");
        this.dataType = (Class<D>) data.getClass();
        this.filter = new ExtensionFileFilter(data);
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
    public int reload(boolean wipeAfterLoad) {
        reloadDirectory(wipeAfterLoad);
        return SUCCESS;
    }

    public void reloadDirectory(final boolean wipeAfterLoad) {
        if (!root.isDirectory()) {
            if (root.exists()) {
                root.delete();
            }
            root.mkdirs();
            return;
        }
        data.onLoadStart(logger);
        ObjectArraySet<String> pending = new ObjectArraySet<>(modified.keySet());
        if (data.removeNewDataOnReload()) {
            pending.addAll(data.newData());
        }
        try {
            File[] files = root.listFiles(filter);
            if (files == null || files.length == 0) {
                modified.clear();
                return;
            }
            for (File file : files) {
                String name = file.getName();
                pending.remove(name);
                long lastModified = modified.getLong(name);
                long newModified = reload(file, name, lastModified, wipeAfterLoad);
                if (newModified == Long.MIN_VALUE) {
                    modified.removeLong(name);
                    continue;
                }
                if (newModified != lastModified) {
                    modified.put(name, newModified);
                }
            }
        } finally {
            data.onLoadEnd(logger);
            if (data.removeNewDataOnReload()) {
                data.clearNewData();
            }
            for (String string : pending) {
                modified.removeLong(string);
                data.onDeleted(logger, string);
            }
        }
    }

    private long reload(File file, String name, long modified, boolean wipeAfterLoad) {
        long lastTimeModified = file.lastModified();
        if (modified == lastTimeModified) {
            return lastTimeModified;
        }
        FileDataSource source = new FileDataSource(file);
        FileData<T> value = new FileData<>(file, name);
        if (migrator != null) {
            try {
                handler.load(value, source);
                lastTimeModified = source.lastModified();
            } catch (final Exception exception) {
                logger.warning("Failed to load data from '{0}'!", exception, path);
                return lastTimeModified;
            }
            int version = value.version();
            if (migrator.needsMigration(dataType, version)) {
                try {
                    int newVersion = migrator.migrate(logger, version, value, data);
                    value.version(newVersion);
                } catch (DataMigrationFailedException exception) {
                    logger.warning("Failed to migrate data of '{0}'!", exception, path);
                    return lastTimeModified;
                }
                try {
                    handler.save(value, source);
                } catch (final Exception exception) {
                    logger.warning("Failed to save migrated to '{0}'!", exception, path);
                    return lastTimeModified;
                }
            }
            try {
                handler.load(value, source);
                lastTimeModified = source.lastModified();
            } catch (final Exception exception) {
                logger.warning("Failed to load data from '{0}'!", exception, path);
                return lastTimeModified;
            }
        }
        try {
            data.onLoad(logger, value);
        } catch (final Exception exception) {
            logger.warning("Failed to load data of '{0}'!", exception, path);
            return lastTimeModified;
        }
        if (wipeAfterLoad) {
            value.value(null);
        }
        return save(source, value, lastTimeModified);
    }

    @Override
    public int save(boolean force) {
        saveDirectory(force);
        return SUCCESS;
    }

    public void saveDirectory(final boolean force) {
        try {
            data.onSaveStart(logger);
            ObjectArraySet<String> saved = new ObjectArraySet<>();
            if (data.saveKnownFiles() && !modified.isEmpty()) {
                for (Object2LongMap.Entry<String> entry : modified.object2LongEntrySet()) {
                    File file = new File(root, entry.getKey());
                    saved.add(entry.getKey());
                    long newModified = save(file, entry.getKey(), entry.getLongValue(), force);
                    if (newModified == Long.MIN_VALUE) {
                        modified.removeLong(entry.getKey());
                        continue;
                    }
                    if (newModified != entry.getLongValue()) {
                        modified.put(entry.getKey(), newModified);
                    }
                }
            }
            ObjectSet<String> set = data.newData();
            if (set.isEmpty()) {
                return;
            }
            ObjectIterator<String> iterator = set.iterator();
            while (iterator.hasNext()) {
                String name = iterator.next();
                if (saved.contains(name)) {
                    continue;
                }
                File file = new File(root, name);
                long newModified = save(file, name, Long.MIN_VALUE, force);
                if (newModified == Long.MIN_VALUE) {
                    continue;
                }
                modified.put(name, newModified);
                iterator.remove();
            }
        } finally {
            data.onSaveEnd(logger);
        }
    }

    private long save(File file, String name, long modified, boolean force) {
        long lastTimeModified = file.lastModified();
        if (!force && modified == lastTimeModified) {
            return lastTimeModified;
        }
        FileDataSource source = new FileDataSource(file);
        FileData<T> value = new FileData<>(file, name);
        return save(source, value, modified);
    }
    
    private long save(FileDataSource source, FileData<T> value, long lastTimeModified) {
        try {
            data.onSave(logger, value);
        } catch (final Exception exception) {
            logger.warning("Failed to save data of '{0}'!", exception, path);
            return lastTimeModified;
        }
        if (migrator != null) {
            value.version(migrator.getTargetVersion(dataType));
        }
        if (value.shouldBeDeleted()) {
            value.file().delete();
            return Long.MIN_VALUE;
        }
        try {
            handler.save(value, source);
            lastTimeModified = source.lastModified();
        } catch (final Exception exception) {
            logger.warning("Failed to save data to '{0}'!", exception, path);
            return lastTimeModified;
        }
        return lastTimeModified;
    }

}
