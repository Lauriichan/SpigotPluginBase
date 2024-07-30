package me.lauriichan.minecraft.pluginbase.config;

public final class ConfigMigrationFailedException extends Exception {

    private static final long serialVersionUID = -8543752837139689144L;

    public ConfigMigrationFailedException(String message) {
        super(message);
    }

    public ConfigMigrationFailedException(String message, Throwable cause) {
        super(message, cause);
    }

}
