package io.github.wasabithumb.dryeye.face.eye.pattern;

import io.github.wasabithumb.dryeye.color.Color;
import io.github.wasabithumb.dryeye.color.ColorMath;
import io.github.wasabithumb.dryeye.face.Face;
import org.intellij.lang.annotations.Pattern;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;

import java.util.function.ToDoubleBiFunction;

/**
 * Internal utility for
 * defining eye shapes and finding
 * them within a {@link io.github.wasabithumb.dryeye.face.Face}
 */
@NullMarked
@ApiStatus.Internal
public interface EyePattern {

    @Contract("-> new")
    static Builder builder() {
        return new EyePatternImpl.Builder();
    }

    //

    /**
     * Finds the most likely location
     * of the left and right eye for this pattern
     * on the given face.
     */
    TestResult test(Face face);

    //

    interface TestResult {

        int leftX();

        int rightX();

        int y();

        double weight();

    }

    @FunctionalInterface
    interface Rule extends ToDoubleBiFunction<Color, Color> {

        /** Expects group B to be at least "threshold" brighter than group A */
        static Rule brighter(double threshold) {
            return (a, b) -> {
                double al = ColorMath.luminosity(a);
                double bl = ColorMath.luminosity(b);
                if (bl <= al) return 0d;
                double point = al + threshold;
                if (bl >= point) return 1d;
                return (bl - al) / threshold;
            };
        }

        /** Expects 2 groups to differ by at least "threshold" */
        static Rule differ(double threshold) {
            return (Color a, Color b) -> {
                double difference = 1d - ColorMath.similarity(a, b);
                if (difference >= threshold) return 1d;
                return difference / threshold;
            };
        }

        //

        @Override
        double applyAsDouble(Color a, Color b);

    }

    interface Builder {

        @Contract("_, _, _, _ -> this")
        Builder constraint(char a, char b, Rule modifier, double weight);

        @Contract("_ -> this")
        Builder row(@Pattern("^[\\x21-\\x7E\\u00A1-\\uD7FF\\uE000-\\uFFFF]{1,4}$") String pattern);

        @Contract("-> new")
        EyePattern build();

    }

}
