package io.github.wasabithumb.dryeye.i18n;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.ResourceBundle;

@NullMarked
@ApiStatus.Internal
record TranslatableStringImpl(
        ResourceBundle bundle,
        @NonNls String key
) implements TranslatableString {

    private static @Nullable ResourceBundle LIBRARY_BUNDLE_INSTANCE;

    static synchronized ResourceBundle libraryBundle() {
        ResourceBundle instance = LIBRARY_BUNDLE_INSTANCE;
        if (instance == null) {
            LIBRARY_BUNDLE_INSTANCE = instance = ResourceBundle.getBundle(LIBRARY_BUNDLE);
        }
        return instance;
    }

    //

    @Override
    public int length() {
        return this.toString().length();
    }

    @Override
    public char charAt(int index) {
        return this.toString().charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return this.toString().subSequence(start, end);
    }

    @Override
    public @Nls String toString() {
        return this.bundle.getString(this.key);
    }

}
