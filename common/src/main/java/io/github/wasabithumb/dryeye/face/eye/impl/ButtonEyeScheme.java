package io.github.wasabithumb.dryeye.face.eye.impl;

import io.github.wasabithumb.dryeye.face.Face;
import io.github.wasabithumb.dryeye.face.WritableFace;
import io.github.wasabithumb.dryeye.face.eye.EyeScheme;
import io.github.wasabithumb.dryeye.face.eye.pattern.EyePattern;
import io.github.wasabithumb.dryeye.i18n.TranslatableString;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

/**
 * Similar to the Mojang scheme, except the eye is considered to be
 * 1 pixel wide (context suggests this)
 */
@NullMarked
@ApiStatus.Internal
final class ButtonEyeScheme implements EyeScheme {

    private static final EyePattern PATTERN = EyePattern.builder()
            .row("iaj")
            .row("aoa")
            .row("kal")
            .constraint('o', 'a', EyePattern.Rule.brighter(0.5), 0.5)
            .build();

    //

    @SuppressWarnings("unused")
    public ButtonEyeScheme() { }

    //

    @Override
    public TranslatableString name() {
        return TranslatableString.of("text.dryeye.scheme.button");
    }

    @Override
    public Mapper newMapper(Face face) {
        return new Mapper(PATTERN.test(face));
    }

    //

    record Mapper(
            EyePattern.TestResult result
    ) implements EyeScheme.Mapper {

        @Override
        public double weight() {
            return this.result.weight();
        }

        @Override
        public void blink(WritableFace face) {
            final int lx = this.result.leftX();
            final int rx = this.result.rightX();
            final int y = this.result.y();

            // Left eye
            face.set(
                    lx + 1,
                    y + 1,
                    face.get(
                            lx + 1,
                            y
                    )
            );

            // Right eye
            face.set(
                    rx + 1,
                    y + 1,
                    face.get(
                            rx + 1,
                            y
                    )
            );
        }

    }

}
