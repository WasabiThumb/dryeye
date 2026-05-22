package io.github.wasabithumb.dryeye.face;

import io.github.wasabithumb.dryeye.color.Color;
import org.jetbrains.annotations.Range;
import org.jspecify.annotations.NullMarked;

import java.util.Objects;

/**
 * Abstraction for the "face" part of the
 * base layer of a Minecraft skin.
 */
@NullMarked
public interface Face {

    int SIZE = 8;

    /**
     * Provides a read-only {@link Face} instance
     * which reads through to the specified object.
     * If the object is already trivially immutable,
     * returns the same object.
     */
    static Face unmodifiableView(Face face) {
        return ImmutableFace.of(Objects.requireNonNull(face, "face must not be null"));
    }

    /**
     * Provides an immutable copy of the specified
     * {@link Face} instance
     */
    static Face copyOf(Face other) {
        return unmodifiableView(WritableFace.copyOf(other));
    }

    /**
     * Serializes a {@link Face} to a base64 string
     * in a tailored compact format.
     */
    static String serialize(Face face) {
        return FaceIO.writeBase64(face);
    }

    /**
     * Deserializes a base64 string produced by
     * {@link #serialize(Face)} into a new
     * immutable {@link Face} object.
     * @throws IllegalArgumentException Malformed data
     */
    static Face deserialize(String string) throws IllegalArgumentException {
        return FaceIO.readBase64(string);
    }

    //

    Color get(
            @Range(from = 0, to = SIZE - 1) int x,
            @Range(from = 0, to = SIZE - 1) int y
    ) throws IndexOutOfBoundsException;

}
