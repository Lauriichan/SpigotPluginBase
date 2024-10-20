package me.lauriichan.minecraft.pluginbase.util;

import java.util.logging.Logger;

import me.lauriichan.laylib.logger.AbstractSimpleLogger;

public final class BukkitSimpleLogger extends AbstractSimpleLogger {

    private final Logger logger;

    public BukkitSimpleLogger(final Logger logger) {
        this.logger = logger;
    }
    
    public final Logger getHandle() {
        return logger;
    }

    @Override
    protected void info(final String message) {
        logger.info(message);
    }

    @Override
    protected void warning(final String message) {
        logger.warning(message);
    }

    @Override
    protected void error(final String message) {
        logger.severe(message);
    }

    @Override
    protected void track(final String message) {
        logger.info("[TRACE] " + message);
    }

    @Override
    protected void debug(final String message) {
        logger.info("[DEBUG] " + message);
    }

}