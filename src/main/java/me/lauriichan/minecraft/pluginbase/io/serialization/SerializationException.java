package me.lauriichan.minecraft.pluginbase.io.serialization;

public final class SerializationException extends Exception {

    private static final long serialVersionUID = -8910952882954712940L;

    public SerializationException(String message) {
        super(message);
    }

    public SerializationException(String message, Throwable cause) {
        super(message, cause);
    }

}
