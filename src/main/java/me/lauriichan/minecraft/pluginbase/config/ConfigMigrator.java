package me.lauriichan.minecraft.pluginbase.config;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.laylib.logger.util.StringUtil;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.ConditionConstant;

public final class ConfigMigrator {

    private static final class Migration {
        private final ObjectList<ConfigMigrationExtension<?>> migrations;
        private final int targetVersion;

        public Migration(int targetVersion, ObjectList<ConfigMigrationExtension<?>> migrations) {
            this.targetVersion = targetVersion;
            this.migrations = migrations;
        }

        public int targetVersion() {
            return targetVersion;
        }

        public ObjectList<ConfigMigrationExtension<?>> migrations() {
            return migrations;
        }
    }

    private final Object2ObjectArrayMap<Class<? extends IConfigExtension>, Migration> migrations = new Object2ObjectArrayMap<>();

    public ConfigMigrator(final BasePlugin<?> plugin) {
        if (plugin.conditionMap().value(ConditionConstant.DISABLE_CONFIGS)) {
            return;
        }
        Object2ObjectArrayMap<Class<? extends IConfigExtension>, ObjectArrayList<ConfigMigrationExtension<?>>> tmpMigrations = new Object2ObjectArrayMap<>();
        plugin.extension(ConfigMigrationExtension.class, true).callInstances(extension -> {
            Class<?> target = extension.targetType();
            if (target == null) {
                plugin.logger().warning("Couldn't register migration as it doesn't define a target: {0}", extension.getClass().getName());
                return;
            }
            if (extension.minVersion() < 0 || extension.minVersion() >= extension.targetVersion()) {
                plugin.logger().warning(
                    "Couldn't register migration as the min and/or target version are invalid: {0} (min: {1}, target: {2})",
                    extension.getClass().getName(), extension.minVersion(), extension.targetVersion());
                return;
            }
            Class<? extends IConfigExtension> configTarget = target.asSubclass(IConfigExtension.class);
            ObjectArrayList<ConfigMigrationExtension<?>> migrationList = tmpMigrations.get(configTarget);
            if (migrationList == null) {
                migrationList = new ObjectArrayList<>();
                tmpMigrations.put(configTarget, migrationList);
            }
            migrationList.add(extension);
        });
        if (tmpMigrations.isEmpty()) {
            return;
        }
        tmpMigrations.keySet().forEach(key -> {
            ObjectList<ConfigMigrationExtension<?>> extensions = tmpMigrations.get(key);
            extensions.sort((m1, m2) -> {
                int tmp = Integer.compare(m1.minVersion(), m2.minVersion());
                if (tmp != 0) {
                    return tmp;
                }
                return Integer.compare(m1.targetVersion(), m2.targetVersion());
            });
            migrations.put(key, new Migration(extensions.get(extensions.size() - 1).targetVersion(), extensions));
        });
    }

    public int getTargetVersion(Class<? extends IConfigExtension> extension) {
        Migration migration = migrations.get(extension);
        return migration == null ? 0 : migration.targetVersion();
    }

    public boolean needsMigration(Class<? extends IConfigExtension> extension, int version) {
        Migration migration = migrations.get(extension);
        return migration != null && version < migration.targetVersion();
    }

    public <T extends IConfigExtension> int migrate(ISimpleLogger logger, int version, Configuration configuration, T extension)
        throws ConfigMigrationFailedException {
        Migration migration = migrations.get(extension.getClass());
        if (migration == null || version >= migration.targetVersion()) {
            return version;
        }
        for (ConfigMigrationExtension<?> migrationExt : migration.migrations()) {
            if (migrationExt.targetVersion() <= version) {
                continue;
            }
            logger.info("Applying migration '{3}' (version {1} to {2}) for config '{0}'", extension.name(), version,
                migrationExt.targetVersion(), migrationExt.description());
            try {
                migrationExt.migrate(configuration);
                version = migrationExt.targetVersion();
            } catch (Throwable throwable) {
                throw new ConfigMigrationFailedException(
                    StringUtil.format("Failed to apply migration '{3}' (version {1} to {2}) for config '{0}'", new Object[] {
                        extension.name(),
                        version,
                        migrationExt.targetVersion(),
                        migrationExt.description()
                    }), throwable);
            }
        }
        return migration.targetVersion();
    }

}
