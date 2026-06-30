package io.github.wasabithumb.dryeye.config.screen;

import net.minecraft.client.gui.screens.Screen;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class DryEyeConfigScreenFactory implements IConfigScreenFactory {

    @Override
    public Screen createScreen(ModContainer container, @Nullable Screen modListScreen) {
        return new DryEyeConfigScreen(modListScreen);
    }

}
