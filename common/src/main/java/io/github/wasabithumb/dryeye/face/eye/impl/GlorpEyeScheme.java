package io.github.wasabithumb.dryeye.face.eye.impl;

import io.github.wasabithumb.dryeye.face.Face;
import io.github.wasabithumb.dryeye.face.WritableFace;
import io.github.wasabithumb.dryeye.face.eye.EyeScheme;
import io.github.wasabithumb.dryeye.face.eye.pattern.EyePattern;
import io.github.wasabithumb.dryeye.i18n.TranslatableString;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

/**
 * 3x2 eyes with 1x1 pupils.
 * Looks like an alien/bug.
 */
@NullMarked
@ApiStatus.Internal
final class GlorpEyeScheme implements EyeScheme {

    private static final EyePattern PATTERN = EyePattern.builder()
            .row("abc")
            .row("def")
            .row("xxo")
            .row("xxx")
            .row("ghi")
            .constraint('x', 'o', EyePattern.Rule.brighter(0.6), 0.5)
            .build();

    //

    @SuppressWarnings("unused")
    public GlorpEyeScheme() { }

    //

    @Override
    public TranslatableString name() {
        return TranslatableString.of("text.dryeye.scheme.glorp");
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

            for (int dy = 2; dy >= 1; dy--) {
                for (int dx = 0; dx < 3; dx++) {
                    // Left eye
                    face.set(
                            lx + dx,
                            y + dy,
                            face.get(
                                    lx + dx,
                                    y + dy - 1
                            )
                    );

                    // Right eye
                    face.set(
                            rx + dx,
                            y + dy,
                            face.get(
                                    rx + dx,
                                    y + dy - 1
                            )
                    );
                }
            }

            for (int dx = 0; dx < 3; dx++) {
                face.set(lx + dx, y + 3, face.get(lx + dx, y + 4)); // Left eye
                face.set(rx + dx, y + 3, face.get(rx + dx, y + 4)); // Right eye
            }
        }

    }

}
