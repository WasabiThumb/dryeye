package io.github.wasabithumb.dryeye.util;

import org.jspecify.annotations.NullMarked;

import java.util.Random;

@FunctionalInterface
@NullMarked
public interface LongSampler {

    long sample(long from, long to, Random random);

}
