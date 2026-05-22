package io.github.wasabithumb.dryeye.util;

import org.jspecify.annotations.NullMarked;

import java.util.logging.Level;
import java.util.logging.Logger;

@NullMarked
public interface LoggerHolder {

    Logger getLogger();

    default void log(Level level, String message) {
        this.getLogger().log(level, message);
    }

    default void log(Level level, String message, Throwable ex) {
        this.getLogger().log(level, message, ex);
    }

}
