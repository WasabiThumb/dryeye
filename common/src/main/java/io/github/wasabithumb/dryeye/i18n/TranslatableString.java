package io.github.wasabithumb.dryeye.i18n;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.PropertyKey;
import org.jspecify.annotations.NullMarked;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * A locale dependent string corresponding to a
 * key-value in a resource bundle.
 */
@NullMarked
public sealed interface TranslatableString
        extends CharSequence
        permits TranslatableStringImpl
{

    String LIBRARY_BUNDLE = "io.github.wasabithumb.dryeye.bundle.Bundle";

    /**
     * Creates a translatable string with the {@link Locale#getDefault() default locale}.
     * @param key A key in the library resource bundle
     */
    @Contract("_ -> new")
    static TranslatableString of(
            @NonNls @PropertyKey(resourceBundle = LIBRARY_BUNDLE) String key
    ) {
        return new TranslatableStringImpl(TranslatableStringImpl.libraryBundle(), key);
    }

    //

    /**
     * The bundle used to resolve this string
     * by default
     */
    ResourceBundle bundle();

    /**
     * The bundle key which this object is wrapping
     */
    @NonNls String key();

    /**
     * Resolves the {@link #key() key} of this
     * object with the {@link #bundle() bundle} of this object,
     * producing a translated value.
     */
    @Override
    @Nls String toString();

}
