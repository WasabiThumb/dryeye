package io.github.wasabithumb.dryeye.color;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Range;
import org.jspecify.annotations.NullMarked;

/**
 * An RGB color.
 */
@NullMarked
public sealed interface Color permits ColorImpl {

    static Color of(int value) {
        return ColorImpl.fromValue(value);
    }

    static Color of(int r, int g, int b) {
        return ColorImpl.fromComponents(r, g, b);
    }

    //

    @Range(from = 0x000000, to = 0xFFFFFF) int value();

    @Range(from = 0x00, to = 0xFF) int red();

    @Range(from = 0x00, to = 0xFF) int green();

    @Range(from = 0x00, to = 0xFF) int blue();

    @Contract(mutates = "param1")
    void dump(byte[] out, int offset);

    java.awt.Color toAWT();

}
