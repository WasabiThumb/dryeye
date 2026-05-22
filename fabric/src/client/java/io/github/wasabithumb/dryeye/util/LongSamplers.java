package io.github.wasabithumb.dryeye.util;

import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;

import java.util.Random;

@NullMarked
public final class LongSamplers {

    @Contract(pure = true)
    public static LongSampler linear() {
        return Linear.INSTANCE;
    }

    @Contract(pure = true)
    public static LongSampler quadratic() {
        return Quadratic.INSTANCE;
    }

    @Contract(pure = true)
    public static LongSampler inverseQuadratic() {
        return InverseQuadratic.INSTANCE;
    }

    //

    private LongSamplers() { }

    //

    private static abstract class AbstractNormalTransform implements LongSampler {

        protected abstract double transform(double normal);

        @Override
        public long sample(long from, long to, Random random) {
            if (from == to) return from;

            double v = random.nextDouble();
            v = this.transform(v);

            if (v <= 0d) return from;
            if (v >= 1d) return to;

            return Math.round(
                    (1d - v) * from +
                            v * to
            );
        }

    }

    private static final class Linear implements LongSampler {

        static final Linear INSTANCE = new Linear();

        private Linear() { }

        @Override
        public long sample(long from, long to, Random random) {
            return from + random.nextLong(to - from + 1);
        }

    }

    private static final class Quadratic extends AbstractNormalTransform {

        static final Quadratic INSTANCE = new Quadratic();

        private Quadratic() { }

        @Override
        protected double transform(double normal) {
            return normal * normal;
        }

    }

    private static final class InverseQuadratic extends AbstractNormalTransform {

        static final InverseQuadratic INSTANCE = new InverseQuadratic();

        private InverseQuadratic() { }

        @Override
        protected double transform(double normal) {
            normal = 1d - normal;
            return 1d - (normal * normal);
        }

    }

}
