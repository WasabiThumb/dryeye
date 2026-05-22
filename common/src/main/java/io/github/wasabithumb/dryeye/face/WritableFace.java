package io.github.wasabithumb.dryeye.face;

import io.github.wasabithumb.dryeye.color.Color;
import org.jetbrains.annotations.Range;
import org.jspecify.annotations.NullMarked;

import java.awt.image.BufferedImage;

/**
 * A {@link Face} which can receive pixel
 * updates
 */
@NullMarked
public interface WritableFace extends Face {

    /**
     * Creates a new {@link WritableFace} initialized to all black.
     */
    static WritableFace create() {
        return new ByteArrayFace();
    }

    /**
     * Creates a new {@link WritableFace} from a given image.
     * @throws IllegalArgumentException Image has a width/height not equal to {@link #SIZE}
     */
    static WritableFace of(BufferedImage image) throws IllegalArgumentException {
        return new BufferedImageFace(image);
    }

    /**
     * Creates a new {@link WritableFace} with the same
     * content as the given {@link Face}.
     */
    static WritableFace copyOf(Face other) {
        return ByteArrayFace.copyOf(other);
    }

    //

    void set(
            @Range(from = 0, to = SIZE - 1) int x,
            @Range(from = 0, to = SIZE - 1) int y,
            Color color
    ) throws IndexOutOfBoundsException;

}
