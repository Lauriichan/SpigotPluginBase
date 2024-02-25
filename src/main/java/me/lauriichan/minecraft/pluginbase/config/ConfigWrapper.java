package me.lauriichan.minecraft.pluginbase.config;

import java.util.Objects;

import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.resource.source.IDataSource;

public final class ConfigWrapper<T extends IConfigExtension> {

    public static final int SUCCESS = 0x0;
    public static final int FAIL_IO_LOAD = 0x1;
    public static final int FAIL_DATA_LOAD = 0x2;
    public static final int FAIL_DATA_SAVE = 0x3;
    public static final int FAIL_IO_SAVE = 0x4;
    public static final int SKIPPED = 0x5;

    public static boolean isFailedState(final int state) {
        return state != SUCCESS && state != SKIPPED;
    }

    public static boolean isIOError(final int state) {
        return state == FAIL_IO_LOAD || state == FAIL_IO_SAVE;
    }

    public static boolean isDataError(final int state) {
        return state == FAIL_DATA_LOAD || state == FAIL_DATA_SAVE;
    }

    private final ISimpleLogger logger;

    private final T config;
    private final IDataSource source;
    private final IConfigHandler handler;

    private volatile long lastTimeModified = -1L;

    public ConfigWrapper(final BasePlugin<?> plugin, final T extension) {
        this.logger = plugin.logger();
        this.config = Objects.requireNonNull(extension, "Config extension can't be null");
        this.source = Objects.requireNonNull(plugin.resource(extension.path()), "Couldn't find data source at '" + extension.path() + "'");
        this.handler = Objects.requireNonNull(extension.handler(), "Config handler can't be null");
    }

    public T config() {
        return config;
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
                logger.warning("Failed to load configuration from '{0}'!", exception, config.path());
                return FAIL_IO_LOAD;
            }
        }
        try {
            config.onLoad(configuration);
        } catch (final Exception exception) {
            logger.warning("Failed to load configuration data of '{0}'!", exception, config.path());
            return FAIL_DATA_LOAD;
        }
        if (wipeAfterLoad) {
            configuration.clear();
        }
        try {
            config.onSave(configuration);
        } catch (final Exception exception) {
            logger.warning("Failed to save configuration data of '{0}'!", exception, config.path());
            return FAIL_DATA_SAVE;
        }
        try {
            handler.save(configuration, source);
            lastTimeModified = source.lastModified();
        } catch (final Exception exception) {
            logger.warning("Failed to save configuration to '{0}'!", exception, config.path());
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
            logger.warning("Failed to save configuration data of '{0}'!", exception, config.path());
            return FAIL_DATA_SAVE;
        }
        try {
            handler.save(configuration, source);
            lastTimeModified = source.lastModified();
        } catch (final Exception exception) {
            logger.warning("Failed to save configuration to '{0}'!", exception, config.path());
            return FAIL_IO_SAVE;
        }
        return SUCCESS;
    }

}
