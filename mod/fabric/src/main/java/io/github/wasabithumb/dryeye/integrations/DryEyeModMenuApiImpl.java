package io.github.wasabithumb.dryeye.integrations;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import io.github.wasabithumb.dryeye.config.screen.DryEyeConfigScreen;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class DryEyeModMenuApiImpl implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return DryEyeConfigScreen::new;
    }

}
