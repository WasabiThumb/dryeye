package io.github.wasabithumb.dryeye.face;

import io.github.wasabithumb.dryeye.color.Color;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Range;
import org.jspecify.annotations.NullMarked;

@NullMarked
@ApiStatus.Internal
final class ByteArrayFace
        extends AbstractFace
        implements WritableFace
{

    static ByteArrayFace copyOf(Face other) {
        ByteArrayFace ret = new ByteArrayFace();
        if (other instanceof AbstractFace qual) {
            System.arraycopy(qual.asByteArray(), 0, ret.data, 0, 192);
        } else {
            for (int i = 0; i < 64; i++) {
                int offset = 3 * i;
                Color color = other.get(i & 7, i >> 3);
                color.dump(ret.data, offset);
            }
        }
        return ret;
    }

    //

    private final byte[] data;

    ByteArrayFace() {
        this.data = new byte[BYTE_LENGTH];
    }

    //

    @Override
    byte[] asByteArray() {
        return this.data;
    }

    @Override
    public Color get(
            @Range(from = 0, to = SIZE - 1) int x,
            @Range(from = 0, to = SIZE - 1) int y
    ) throws IndexOutOfBoundsException {
        boundsCheck(x, y);
        int index = (y * SIZE) + x;
        int offset = 3 * index;
        return Color.of(
                this.data[offset],
                this.data[offset + 1],
                this.data[offset + 2]
        );
    }

    @Override
    public void set(
            @Range(from = 0, to = SIZE - 1) int x,
            @Range(from = 0, to = SIZE - 1) int y,
            Color color
    ) throws IndexOutOfBoundsException {
        boundsCheck(x, y);
        int index = (y * SIZE) + x;
        int offset = 3 * index;
        color.dump(this.data, offset);
    }

}
