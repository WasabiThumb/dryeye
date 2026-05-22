package io.github.wasabithumb.dryeye.manager;

import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.LoggerFactory;

@NullMarked
@ApiStatus.Internal
final class DryEyeManagerAccess {

    private static @Nullable DryEyeManager INSTANCE;

    public static synchronized DryEyeManager get() {
        DryEyeManager ret = INSTANCE;
        if (ret == null) INSTANCE = ret = create();
        return ret;
    }

    private static DryEyeManager create() {
        return new DryEyeManagerImpl(
                Minecraft.getInstance(),
                LoggerFactory.getLogger("dryeye")
        );
    }

    //

    private DryEyeManagerAccess() { }

}
