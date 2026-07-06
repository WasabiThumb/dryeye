package io.github.wasabithumb.dryeye.manager;

import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

@NullMarked
@ApiStatus.Internal
final class InactiveBlinkState implements DryEyeManager.BlinkState {

    static final InactiveBlinkState INSTANCE = new InactiveBlinkState();

    //

    private InactiveBlinkState() { }

    //

    @Override
    public boolean active() {
        return false;
    }

    @Override
    public Identifier modifiedSkin() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

}
