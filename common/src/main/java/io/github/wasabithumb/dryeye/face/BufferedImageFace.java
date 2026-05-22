package io.github.wasabithumb.dryeye.face;

import io.github.wasabithumb.dryeye.color.Color;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Range;
import org.jspecify.annotations.NullMarked;

import java.awt.image.BufferedImage;

@NullMarked
@ApiStatus.Internal
final class BufferedImageFace
        extends AbstractFace
        implements WritableFace
{

    private final BufferedImage image;

    public BufferedImageFace(BufferedImage image) {
        if (image.getWidth() != SIZE ||
            image.getHeight() != SIZE
        ) {
            throw new IllegalArgumentException("image should be " + SIZE + "x" + SIZE + "(got " +
                    image.getWidth() + "x" + image.getHeight() + ")");
        }
        this.image = image;
    }

    //

    @Override
    public Color get(
            @Range(from = 0, to = SIZE - 1) int x,
            @Range(from = 0, to = SIZE - 1) int y
    ) throws IndexOutOfBoundsException {
        boundsCheck(x, y);
        return Color.of(this.image.getRGB(x, y));
    }

    @Override
    public void set(
            @Range(from = 0, to = SIZE - 1) int x,
            @Range(from = 0, to = SIZE - 1) int y,
            Color color
    ) throws IndexOutOfBoundsException {
        boundsCheck(x, y);
        this.image.setRGB(x, y, 0xFF000000 | color.value());
    }

}
