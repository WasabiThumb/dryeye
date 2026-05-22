package io.github.wasabithumb.dryeye.face.eye.impl;

import io.github.wasabithumb.dryeye.color.Color;
import io.github.wasabithumb.dryeye.face.Face;
import io.github.wasabithumb.dryeye.face.WritableFace;
import io.github.wasabithumb.dryeye.face.eye.EyeScheme;
import io.github.wasabithumb.dryeye.face.eye.pattern.EyePattern;
import io.github.wasabithumb.dryeye.i18n.TranslatableString;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

/**
 * Large 2x2 eyes which may have eyebrows
 */
@NullMarked
@ApiStatus.Internal
final class AnimeEyeScheme implements EyeScheme {

    private static final EyePattern PATTERN = EyePattern.builder()
            .row("01")
            .row("tt")
            .row("pi")
            .row("PI")
            .row("bb")
            .constraint('i', 'p', EyePattern.Rule.brighter(0.2), 0.5)
            .constraint('I', 'P', EyePattern.Rule.brighter(0.2), 0.5)
            .build();

    //

    @SuppressWarnings("unused")
    public AnimeEyeScheme() { }

    //

    @Override
    public TranslatableString name() {
        return TranslatableString.of("text.dryeye.scheme.anime");
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
            Color l0 = face.get(lx, y);
            Color l1 = face.get(lx, y + 1);
            Color l2 = face.get(lx + 1, y + 4);
            face.set(lx, y + 1, l0);
            face.set(lx + 1, y + 1, l0);
            face.set(lx, y + 2, l1);
            face.set(lx + 1, y + 2, l1);
            face.set(lx, y + 3, l2);
            face.set(lx + 1, y + 3, l2);

            // Right eye
            Color r0 = face.get(rx + 1, y);
            Color r1 = face.get(rx + 1, y + 1);
            Color r2 = face.get(rx, y + 4);
            face.set(rx, y + 1, r0);
            face.set(rx + 1, y + 1, r0);
            face.set(rx, y + 2, r1);
            face.set(rx + 1, y + 2, r1);
            face.set(rx, y + 3, r2);
            face.set(rx + 1, y + 3, r2);
        }

    }

}
