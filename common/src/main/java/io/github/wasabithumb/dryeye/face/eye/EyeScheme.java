package io.github.wasabithumb.dryeye.face.eye;

import io.github.wasabithumb.dryeye.face.Face;
import io.github.wasabithumb.dryeye.face.WritableFace;
import io.github.wasabithumb.dryeye.i18n.TranslatableString;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;

/**
 * Describes a "style" of eyes which should hopefully
 * apply to several skins. The degree to which an eye scheme
 * matches a given skin can be measured heuristically with
 * the {@link #weight(Face) weight} method.
 */
@NullMarked
public interface EyeScheme {

    /**
     * Print name of this eye scheme
     */
    TranslatableString name();

    /**
     * Computes a new {@link Mapper mapper} object
     * for applying this eye scheme to the
     * given {@link Face face} (or other similar faces)
     */
    Mapper newMapper(Face face);

    /**
     * The degree to which an eye scheme matches a given
     * skin. Higher values mean greater correspondence.
     */
    default double weight(Face face) {
        return newMapper(face).weight();
    }

    /**
     * Applies the blink transformation to the given
     * face, assuming it matches this scheme.
     */
    @Contract(mutates = "param1")
    default void blink(WritableFace face) {
        newMapper(face).blink(face);
    }

    //

    interface Mapper {

        /**
         * Strength of the result represented
         * by this object in 0-1.
         */
        double weight();

        /**
         * Applies the blink transformation to the given
         * face, assuming this mapper was created from
         * a version of it.
         */
        @Contract(mutates = "param1")
        void blink(WritableFace face);

    }

}
