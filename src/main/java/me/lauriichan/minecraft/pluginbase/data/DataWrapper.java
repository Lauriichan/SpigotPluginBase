package me.lauriichan.minecraft.pluginbase.data;

import java.util.Objects;

import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.data.IDataHandler.Wrapper;
import me.lauriichan.minecraft.pluginbase.resource.source.IDataSource;

public final class DataWrapper<T, D extends IFileDataExtension<T>> implements IDataWrapper<T, D> {

    public static final int SUCCESS = 0x00;
    public static final int SKIPPED = 0x01;
    
    public static final int FAIL_IO_LOAD = 0x11;
    public static final int FAIL_IO_SAVE = 0x12;
    
    public static final int FAIL_DATA_PROPERGATE = 0x21;
    public static final int FAIL_DATA_LOAD = 0x22;
    public static final int FAIL_DATA_SAVE = 0x23;
    public static final int FAIL_DATA_MIGRATE = 0x24;

    public static boolean isFailedState(final int state) {
        return state != SUCCESS && state != SKIPPED;
    }

    public static boolean isIOError(final int state) {
        return state == FAIL_IO_LOAD || state == FAIL_IO_SAVE;
    }

    public static boolean isDataError(final int state) {
        return state == FAIL_DATA_LOAD || state == FAIL_DATA_PROPERGATE || state == FAIL_DATA_MIGRATE || state == FAIL_DATA_SAVE;
    }
    
    public static <T, D extends ISingleDataExtension<T>> DataWrapper<T, D> single(final BasePlugin<?> plugin, final D extension) {
        return new DataWrapper<>(plugin, extension, extension.path());
    }

    private final ISimpleLogger logger;
    private final DataMigrator migrator;

    private final String path;
    
    private final D data;
    private final Class<D> dataType;
    
    private final IDataSource source;
    private final IDataHandler<T> handler;

    private volatile long lastTimeModified = -1L;
    
    @SuppressWarnings("unchecked")
    public DataWrapper(final BasePlugin<?> plugin, final D extension, final String path) {
        this.logger = plugin.logger();
        this.migrator = plugin.dataMigrator();
        this.path = path;
        this.data = Objects.requireNonNull(extension, "Data extension can't be null");
        this.dataType = (Class<D>) data.getClass();
        this.source = Objects.requireNonNull(plugin.resource(path), "Couldn't find data source at '" + path + "'");
        this.handler = Objects.requireNonNull(extension.handler(), "Data handler can't be null");
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

    @Override
    public IDataSource source() {
        return source;
    }

    @Override
    public IDataHandler<T> handler() {
        return handler;
    }

    public long lastModified() {
        return lastTimeModified;
    }

    @Override
    public int reload(final boolean wipeAfterLoad) {
        Wrapper<T> value = new Wrapper<>();
        if (source.exists()) {
            if (lastTimeModified == source.lastModified() && !data.isModified()) {
                return SKIPPED;
            }
            if (migrator != null) {
                try {
                    handler.load(value, source);
                    lastTimeModified = source.lastModified();
                } catch (final Exception exception) {
                    logger.warning("Failed to load data from '{0}'!", exception, path);
                    return FAIL_IO_LOAD;
                }
                int version = value.version();
                if (migrator.needsMigration(dataType, version)) {
                    try {
                        int newVersion = migrator.migrate(logger, version, value, data);
                        value.version(newVersion);
                    } catch (DataMigrationFailedException exception) {
                        logger.warning("Failed to migrate data of '{0}'!", exception, path);
                        return FAIL_DATA_MIGRATE;
                    }
                    try {
                        handler.save(value, source);
                    } catch(final Exception exception) {
                        logger.warning("Failed to save migrated to '{0}'!", exception, path);
                        return FAIL_IO_SAVE;
                    }
                }
            }
            try {
                handler.load(value, source);
                lastTimeModified = source.lastModified();
            } catch (final Exception exception) {
                logger.warning("Failed to load data from '{0}'!", exception, path);
                return FAIL_IO_LOAD;
            }
        } else {
            try {
                data.onPropergate(logger, value);
            } catch (final Exception exception) {
                logger.warning("Failed to propergate data of '{0}'!", exception, path);
                return FAIL_DATA_PROPERGATE;
            }
        }
        try {
            data.onLoad(logger, value);
        } catch (final Exception exception) {
            logger.warning("Failed to load data of '{0}'!", exception, path);
            return FAIL_DATA_LOAD;
        }
        if (wipeAfterLoad) {
            value.value(null);
        }
        try {
            data.onSave(logger, value);
        } catch (final Exception exception) {
            logger.warning("Failed to save data of '{0}'!", exception, path);
            return FAIL_DATA_SAVE;
        }
        if (migrator != null) {
            value.version(migrator.getTargetVersion(dataType));
        }
        try {
            handler.save(value, source);
            lastTimeModified = source.lastModified();
        } catch (final Exception exception) {
            logger.warning("Failed to save data to '{0}'!", exception, path);
            return FAIL_IO_SAVE;
        }
        return SUCCESS;
    }

    @Override
    public int save(final boolean force) {
        if (!force && !data.isModified() && source.exists()) {
            return SKIPPED;
        }
        final Wrapper<T> value = new Wrapper<>();
        try {
            data.onSave(logger, value);
        } catch (final Exception exception) {
            logger.warning("Failed to save data of '{0}'!", exception, path);
            return FAIL_DATA_SAVE;
        }
        if (migrator != null) {
            value.version(migrator.getTargetVersion(dataType));
        }
        try {
            handler.save(value, source);
            lastTimeModified = source.lastModified();
        } catch (final Exception exception) {
            logger.warning("Failed to save data to '{0}'!", exception, path);
            return FAIL_IO_SAVE;
        }
        return SUCCESS;
    }

}
