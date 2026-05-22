package io.github.wasabithumb.dryeye.manager;

import io.github.wasabithumb.dryeye.config.DryEyeConfig;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;

@NullMarked
public interface DryEyeManager {

    static DryEyeManager getInstance() {
        return DryEyeManagerAccess.get();
    }

    //

    Logger logger();

    DryEyeConfig config();

    void loadConfig();

    void saveConfig();

    /**
     * @apiNote Must be called from the render thread
     */
    BlinkState query(int id, Identifier skin);

    /**
     * @apiNote Must be called from the render thread
     */
    void cleanup();

    //

    interface BlinkState {

        boolean active();

        Identifier modifiedSkin() throws UnsupportedOperationException;

    }

}
