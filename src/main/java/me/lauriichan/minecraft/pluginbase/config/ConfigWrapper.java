package me.lauriichan.minecraft.pluginbase.config;

import java.util.Objects;

import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.resource.source.IDataSource;

public final class ConfigWrapper<T extends IConfigExtension> {

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
    
    public static <S extends ISingleConfigExtension> ConfigWrapper<S> single(final BasePlugin<?> plugin, final S extension) {
        return new ConfigWrapper<>(plugin, extension, extension.path());
    }

    private final ISimpleLogger logger;
    private final ConfigMigrator migrator;

    private final String path;
    
    private final T config;
    private final Class<T> configType;
    
    private final IDataSource source;
    private final IConfigHandler handler;

    private volatile long lastTimeModified = -1L;
    
    @SuppressWarnings("unchecked")
    public ConfigWrapper(final BasePlugin<?> plugin, final T extension, final String path) {
        this.logger = plugin.logger();
        this.migrator = plugin.configMigrator();
        this.path = path;
        this.config = Objects.requireNonNull(extension, "Config extension can't be null");
        this.configType = (Class<T>) config.getClass();
        this.source = Objects.requireNonNull(plugin.resource(path), "Couldn't find data source at '" + path + "'");
        this.handler = Objects.requireNonNull(extension.handler(), "Config handler can't be null");
    }

    public T config() {
        return config;
    }
    
    public Class<T> configType() {
        return configType;
    }
    
    public String path() {
        return path;
    }

    public IDataSource source() {
        return source;
    }

    public IConfigHandler handler() {
        return handler;
    }

    public long lastModified() {
        return lastTimeModified;
    }

    public int reload(final boolean wipeAfterLoad) {
        final Configuration configuration = new Configuration();
        if (source.exists()) {
            if (lastTimeModified == source.lastModified() && !config.isModified()) {
                return SKIPPED;
            }
            try {
                handler.load(configuration, source);
                lastTimeModified = source.lastModified();
            } catch (final Exception exception) {
                logger.warning("Failed to load configuration from '{0}'!", exception, path);
                return FAIL_IO_LOAD;
            }
            if (migrator != null) {
                int version = configuration.getInt("version", 0);
                if (migrator.needsMigration(configType, version)) {
                    try {
                        migrator.migrate(logger, version, configuration, config);
                    } catch (ConfigMigrationFailedException exception) {
                        logger.warning("Failed to migrate configuration data of '{0}'!", exception, path);
                        return FAIL_DATA_MIGRATE;
                    }
                }
            }
        } else {
            try {
                config.onPropergate(configuration);
            } catch (final Exception exception) {
                logger.warning("Failed to propergate configuration data of '{0}'!", exception, path);
                return FAIL_DATA_PROPERGATE;
            }
        }
        try {
            config.onLoad(configuration);
        } catch (final Exception exception) {
            logger.warning("Failed to load configuration data of '{0}'!", exception, path);
            return FAIL_DATA_LOAD;
        }
        if (wipeAfterLoad) {
            configuration.clear();
        }
        try {
            config.onSave(configuration);
        } catch (final Exception exception) {
            logger.warning("Failed to save configuration data of '{0}'!", exception, path);
            return FAIL_DATA_SAVE;
        }
        if (migrator != null) {
            configuration.set("version", migrator.getTargetVersion(configType));
        }
        try {
            handler.save(configuration, source);
            lastTimeModified = source.lastModified();
        } catch (final Exception exception) {
            logger.warning("Failed to save configuration to '{0}'!", exception, path);
            return FAIL_IO_SAVE;
        }
        return SUCCESS;
    }

    public int save(final boolean force) {
        if (!force && !config.isModified() && source.exists()) {
            return SKIPPED;
        }
        final Configuration configuration = new Configuration();
        try {
            config.onSave(configuration);
        } catch (final Exception exception) {
            logger.warning("Failed to save configuration data of '{0}'!", exception, path);
            return FAIL_DATA_SAVE;
        }
        if (migrator != null) {
            configuration.set("version", migrator.getTargetVersion(configType));
        }
        try {
            handler.save(configuration, source);
            lastTimeModified = source.lastModified();
        } catch (final Exception exception) {
            logger.warning("Failed to save configuration to '{0}'!", exception, path);
            return FAIL_IO_SAVE;
        }
        return SUCCESS;
    }

}
