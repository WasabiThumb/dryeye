package io.github.wasabithumb.dryeye.face;

import io.github.wasabithumb.dryeye.color.Color;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Range;
import org.jspecify.annotations.NullMarked;

@NullMarked
@ApiStatus.Internal
final class ImmutableFace extends AbstractFace {

    static ImmutableFace of(Face face) {
        if (face instanceof ImmutableFace qual) return qual;
        return new ImmutableFace(face);
    }

    //

    private final Face backing;

    private ImmutableFace(Face backing) {
        this.backing = backing;
    }

    //


    @Override
    byte[] asByteArray() {
        if (this.backing instanceof AbstractFace qual) return qual.asByteArray();
        return super.asByteArray();
    }

    @Override
    public Color get(
            @Range(from = 0, to = SIZE - 1) int x,
            @Range(from = 0, to = SIZE - 1) int y
    ) throws IndexOutOfBoundsException {
        return this.backing.get(x, y);
    }

}
