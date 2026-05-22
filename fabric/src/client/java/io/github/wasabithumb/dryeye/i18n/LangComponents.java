package io.github.wasabithumb.dryeye.i18n;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.PropertyKey;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class LangComponents {

    public static Component of(
            @PropertyKey(resourceBundle = "io.github.wasabithumb.dryeye.bundle.Bundle") String key
    ) {
        return Component.translatable(key);
    }

    //

    private LangComponents() { }

}
