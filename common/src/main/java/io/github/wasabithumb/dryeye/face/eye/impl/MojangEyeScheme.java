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
 * Applies to Steve/Alex-like eyes
 */
@NullMarked
@ApiStatus.Internal
final class MojangEyeScheme implements EyeScheme {

    private static final EyePattern PATTERN = EyePattern.builder()
            .row("tt")
            .row("12")
            .row("bb")
            .constraint('2', '1', EyePattern.Rule.brighter(0.2), 0.5)
            .constraint('1', 't', EyePattern.Rule.differ(0.2), 0.5)
            .constraint('1', 'b', EyePattern.Rule.differ(0.2), 0.5)
            .constraint('2', 't', EyePattern.Rule.differ(0.2), 0.5)
            .constraint('2', 'b', EyePattern.Rule.differ(0.2), 0.5)
            .build();

    //

    @SuppressWarnings("unused")
    public MojangEyeScheme() { }

    //

    @Override
    public TranslatableString name() {
        return TranslatableString.of("text.dryeye.scheme.mojang");
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

            // Replace left eye with cheek color
            Color lc = face.get(lx + 1, y + 2);
            face.set(lx, y + 1, lc);
            face.set(lx + 1, y + 1, lc);

            // Replace right eye with cheek color
            Color rc = face.get(rx, y + 2);
            face.set(rx, y + 1, rc);
            face.set(rx + 1, y + 1, rc);
        }

    }

}
