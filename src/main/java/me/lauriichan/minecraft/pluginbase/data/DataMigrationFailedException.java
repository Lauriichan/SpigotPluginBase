package me.lauriichan.minecraft.pluginbase.data;

public final class DataMigrationFailedException extends Exception {

    private static final long serialVersionUID = -8543752837139689143L;

    public DataMigrationFailedException(String message) {
        super(message);
    }

    public DataMigrationFailedException(String message, Throwable cause) {
        super(message, cause);
    }

}
