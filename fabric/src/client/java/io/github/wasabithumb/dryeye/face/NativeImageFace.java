package io.github.wasabithumb.dryeye.face;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.wasabithumb.dryeye.color.Color;
import org.jetbrains.annotations.Range;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class NativeImageFace extends AbstractFace implements WritableFace {

    private final NativeImage image;
    private final int offsetX;
    private final int offsetY;

    public NativeImageFace(NativeImage image, int offsetX, int offsetY) {
        int width = image.getWidth();
        int height = image.getHeight();
        if (offsetX < 0) throw new IllegalArgumentException("offsetX may not be negative (got " + offsetX + ")");
        if (offsetY < 0) throw new IllegalArgumentException("offsetY may not be negative (got " + offsetY + ")");
        if (offsetX > (width - Face.SIZE)) throw new IllegalArgumentException("offsetX is too large for image of width " + width);
        if (offsetY > (height - Face.SIZE)) throw new IllegalArgumentException("offsetY is too large for image of height " + height);
        this.image = image;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    //

    @Override
    public Color get(
            @Range(from = 0, to = SIZE - 1) int x,
            @Range(from = 0, to = SIZE - 1) int y
    ) throws IndexOutOfBoundsException {
        boundsCheck(x, y);
        return Color.of(this.image.getPixel(this.offsetX + x, this.offsetY + y));
    }

    @Override
    public void set(
            @Range(from = 0, to = SIZE - 1) int x,
            @Range(from = 0, to = SIZE - 1) int y,
            Color color
    ) throws IndexOutOfBoundsException {
        boundsCheck(x, y);
        this.image.setPixel(this.offsetX + x, this.offsetY + y, 0xFF000000 | color.value());
    }

    @Override
    byte[] asByteArray() {
        int width = this.image.getWidth();
        int[] pixels = this.image.getPixelsABGR();
        byte[] ret = new byte[BYTE_LENGTH];
        int head = 0;
        int offset;
        for (int y = 0; y < Face.SIZE; y++) {
            offset = width * y;
            for (int x = 0; x < Face.SIZE; x++) {
                int pixel = pixels[offset + x];
                ret[head++] = (byte) pixel;
                ret[head++] = (byte) (pixel >> 8);
                ret[head++] = (byte) (pixel >> 16);
            }
        }
        return ret;
    }

}
