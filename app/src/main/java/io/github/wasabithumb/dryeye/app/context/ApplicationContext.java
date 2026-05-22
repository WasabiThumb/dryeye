package io.github.wasabithumb.dryeye.app.context;

import io.github.wasabithumb.dryeye.face.Face;
import io.github.wasabithumb.dryeye.face.eye.EyeScheme;
import io.github.wasabithumb.dryeye.util.LoggerHolder;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;

import java.util.logging.Logger;

@NullMarked
public interface ApplicationContext extends LoggerHolder {

    static ApplicationContext create(Logger logger) {
        return new ApplicationContextImpl(logger);
    }

    static ApplicationContext create() {
        return create(Logger.getLogger("dryeye"));
    }

    //

    @Override
    @Contract(pure = true)
    Logger getLogger();

    Face getFace();

    void setFace(Face face);

    EyeScheme getEyeScheme();

    void setEyeScheme(EyeScheme scheme);

    Face getBlinkFace();

    long getBlinkDelay();

    void setBlinkDelay(long period);

    long getBlinkDuration();

    void setBlinkDuration(long duration);

}
