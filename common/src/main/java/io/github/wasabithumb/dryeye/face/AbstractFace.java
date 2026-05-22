package io.github.wasabithumb.dryeye.face;

import org.jspecify.annotations.NullMarked;

import java.util.Arrays;

@NullMarked
abstract class AbstractFace implements Face {

    protected static final int PIXEL_COUNT = SIZE * SIZE;
    protected static final int BYTE_LENGTH = PIXEL_COUNT * 3;

    //

    /**
     * Internal utility for dumping face data.
     * Return value may or may not be a live view.
     */
    byte[] asByteArray() {
        byte[] ret = new byte[BYTE_LENGTH];
        for (int i = 0; i < PIXEL_COUNT; i++)
            this.get(i & 7, i >>> 3).dump(ret, 3 * i);
        return ret;
    }

    @Override
    public int hashCode() {
        int h = 7;
        for (int i = 0; i < 64; i++) {
            h = 31 * h + this.get(i & 7, i >> 3).value();
        }
        return h;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Face other)) return false;
        if (other instanceof AbstractFace qual) return Arrays.equals(this.asByteArray(), qual.asByteArray());
        for (int i = 0; i < 64; i++) {
            int x = i & 7;
            int y = i >> 3;
            if (!this.get(x, y).equals(other.get(x, y))) return false;
        }
        return true;
    }

    //

    protected static void boundsCheck(int x, int y) throws IndexOutOfBoundsException {
        if (x < 0 || x >= SIZE || y < 0 || y >= SIZE) {
            throw new IndexOutOfBoundsException("coordinates " + x + ", " + y +
                    " out of bounds for raster of size " + SIZE + "x" + SIZE);
        }
    }

}
