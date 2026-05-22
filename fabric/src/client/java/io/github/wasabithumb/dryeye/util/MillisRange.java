package io.github.wasabithumb.dryeye.util;

import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@NullMarked
public final class MillisRange {

    @Contract("_, _ -> new")
    public static MillisRange of(long from, long to) throws IllegalArgumentException {
        if (from < 0) throw new IllegalArgumentException("Range start may not be negative (got " + from + ")");
        if (to < 0) throw new IllegalArgumentException("Range end may not be negative (got " + to + ")");
        if (to < from) {
            throw new IllegalArgumentException(
                    "End of range may not be less than start (" +
                            to + " < " + from + ")"
            );
        }
        return new MillisRange(from, to);
    }

    @Contract("_ -> new")
    public static MillisRange fixed(long value) {
        return new MillisRange(value, value);
    }

    @Contract("_, _ -> new")
    public static MillisRange lenient(long from, long to) {
        if (from < 0) from = 0;
        if (to < 0) to = 0;
        return new MillisRange(from, Math.max(to, from));
    }

    //

    private final long from;
    private final long to;

    private MillisRange(long from, long to) {
        this.from = from;
        this.to = to;
    }

    //

    public long from() {
        return this.from;
    }

    public long to() {
        return this.to;
    }

    public long sample(LongSampler sampler) {
        return this.sample(sampler, ThreadLocalRandom.current());
    }

    public long sample(LongSampler sampler, Random random) {
        return sampler.sample(this.from, this.to, random);
    }

    public double fraction(long value) {
        if (value <= this.from) return 0d;
        if (value >= this.to) return 1d;
        return ((double) (value - this.from)) / (double) ((this.to - this.from));
    }

    public long atFraction(double fraction) {
        if (fraction <= 0d) return this.from;
        if (fraction >= 1d) return this.to;
        return this.from + Math.round((this.to - this.from) * fraction);
    }

    @Override
    public int hashCode() {
        return Long.hashCode(this.from) ^ Long.hashCode(this.to);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MillisRange other)) return false;
        return this.from == other.from &&
                this.to == other.to;
    }

    @Override
    public String toString() {
        return "MillisRange{from=" + this.from +
                ", to=" + this.to +
                "}";
    }

}
