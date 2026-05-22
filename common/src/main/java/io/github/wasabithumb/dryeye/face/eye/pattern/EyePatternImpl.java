package io.github.wasabithumb.dryeye.face.eye.pattern;

import io.github.wasabithumb.dryeye.color.Color;
import io.github.wasabithumb.dryeye.color.ColorMath;
import io.github.wasabithumb.dryeye.face.Face;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

import java.util.*;

@NullMarked
@ApiStatus.Internal
final class EyePatternImpl implements EyePattern {

    private final int width;
    private final int height;
    private final int groupCount;
    private final byte[] indices;
    private final List<Constraint> constraints;

    private EyePatternImpl(
            int width,
            int height,
            int groupCount,
            byte[] indices,
            List<Constraint> constraints
    ) {
        this.width = width;
        this.height = height;
        this.groupCount = groupCount;
        this.indices = indices;
        this.constraints = constraints;
    }

    //

    @Override
    public TestResult test(Face face) {
        int flexX = 4 - this.width;
        int flexY = 8 - this.height;

        double resultWeight = -1d;
        int resultLeftX = 0;
        int resultRightX = 0;
        int resultY = 0;

        for (int y = 0; y <= flexY; y++) {
            for (int leftX = 0; leftX <= flexX; leftX++) {
                int rightX = Face.SIZE - this.width - leftX;
                double weight = this.testAt(face, leftX, rightX, y);
                if (weight <= resultWeight) continue;
                resultWeight = weight;
                resultLeftX = leftX;
                resultRightX = rightX;
                resultY = y;
            }
        }

        return new Result(
                resultLeftX,
                resultRightX,
                resultY,
                resultWeight
        );
    }

    private double testAt(Face face, int leftX, int rightX, int y) {
        // Calculate group average colors
        Color[] avg = this.groupAveragesAt(face, leftX, rightX, y);

        // Calculate initial weight based on homogeneity
        double weight = 1d;
        for (int dy = 0; dy < this.height; dy++) {
            for (int dx = 0; dx < this.width; dx++) {
                Color leftPixel = face.get(leftX + dx, y + dy);
                int leftGroup = this.groupAt(dx, dy, false);
                weight *= ColorMath.similarity(leftPixel, avg[leftGroup]);

                Color rightPixel = face.get(rightX + dx, y + dy);
                int rightGroup = this.groupAt(dx, dy, true);
                weight *= ColorMath.similarity(rightPixel, avg[rightGroup]);
            }
        }

        // Apply constraints
        for (Constraint c : this.constraints) {
            Color a = avg[c.a];
            Color b = avg[c.b];
            double adherence = c.modifier.applyAsDouble(a, b);
            if (adherence >= 0.5) {
                double factor = ((adherence - 0.5) * 2) * c.weight;
                factor *= factor; // quadratic falloff in gains
                weight = factor + ((1 - factor) * weight);
            } else {
                double factor = ((0.5 - adherence) * 2) * c.weight;
                factor = 1 - Math.pow(factor - 1, 2); // inverse quadratic falloff in losses
                weight = (1 - factor) * weight;
            }
        }

        return weight;
    }

    private Color[] groupAveragesAt(Face face, int leftX, int rightX, int y) {
        int groupCount = this.groupCount;
        int[] red = new int[groupCount];
        int[] green = new int[groupCount];
        int[] blue = new int[groupCount];
        int[] nelem = new int[groupCount];
        Color[] ret = new Color[groupCount];

        for (int dy = 0; dy < this.height; dy++) {
            for (int dx = 0; dx < this.width; dx++) {
                Color leftPixel = face.get(leftX + dx, y + dy);
                int leftGroup = this.groupAt(dx, dy, false);
                red[leftGroup] += leftPixel.red();
                green[leftGroup] += leftPixel.green();
                blue[leftGroup] += leftPixel.blue();
                nelem[leftGroup]++;

                Color rightPixel = face.get(rightX + dx, y + dy);
                int rightGroup = this.groupAt(dx, dy, true);
                red[rightGroup] += rightPixel.red();
                green[rightGroup] += rightPixel.green();
                blue[rightGroup] += rightPixel.blue();
                nelem[rightGroup]++;
            }
        }

        for (int i = 0; i < groupCount; i++) {
            ret[i] = Color.of(
                    red[i] / nelem[i],
                    green[i] / nelem[i],
                    blue[i] / nelem[i]
            );
        }

        return ret;
    }

    private int groupAt(int x, int y, boolean flip) {
        if (x < 0 || x >= this.width || y < 0 || y >= this.height) throw new IndexOutOfBoundsException();
        if (flip) x = this.width - 1 - x;
        int i = (y * this.width) + x;
        return (i & 1) == 0 ?
                ((this.indices[i >> 1] & 0xF0) >> 4) :
                (this.indices[i >> 1] & 0x0F);
    }

    //

    static final class Builder implements EyePattern.Builder {

        private final Map<Character, Integer> groups = HashMap.newHashMap(16);
        private final List<Constraint> constraints = new LinkedList<>();
        private final byte[] indices = new byte[8];
        private int width = -1;
        private int height = 0;

        Builder() { }

        //

        private int indexOf(char c) throws IllegalStateException {
            Integer index = this.groups.get(c);
            if (index != null) return index;

            int n = this.groups.size();
            if (n >= 16) throw new IllegalStateException("cannot have over 16 unique groups");

            this.groups.put(c, n);
            return n;
        }

        @Override
        public Builder constraint(char a, char b, Rule modifier, double weight) {
            this.constraints.add(new Constraint(
                    this.indexOf(a),
                    this.indexOf(b),
                    modifier,
                    weight
            ));
            return this;
        }

        @Override
        public Builder row(String pattern) {
            int width = this.width;
            if (width == -1) {
                width = pattern.length();
                if (width < 1 || width > 4) throw new IllegalArgumentException("Row must have width of 1-4 (got " + width + ")");
                this.width = width;
            } else if (pattern.length() != width) {
                throw new IllegalArgumentException("All rows should have same width (expected " + width + ", got " + pattern.length() + ")");
            }

            int y = this.height;
            if (y >= 8) throw new IllegalStateException("Cannot add another row (reached maximum height)");
            this.height = y + 1;

            int offset = y * width;
            for (int x = 0; x < width; x++) {
                char c = pattern.charAt(x);
                int dest = offset + x;
                int hdest = dest >> 1;
                this.indices[hdest] = ((dest & 1) == 0) ?
                        (byte) ((this.indices[hdest] & 0x0F) | (this.indexOf(c) << 4)) :
                        (byte) ((this.indices[hdest] & 0xF0) | this.indexOf(c));
            }

            return this;
        }

        @Override
        public EyePatternImpl build() {
            if (this.width == -1) throw new IllegalStateException("Must add at least 1 row before call to #build()");
            byte[] indicesCopy = new byte[((this.width * this.height) + 1) >> 1];
            System.arraycopy(this.indices, 0, indicesCopy, 0, indicesCopy.length);
            return new EyePatternImpl(
                    this.width,
                    this.height,
                    this.groups.size(),
                    indicesCopy,
                    List.copyOf(this.constraints)
            );
        }

    }

    private record Constraint(
            int a,
            int b,
            Rule modifier,
            double weight
    ) { }

    private record Result(
            int leftX,
            int rightX,
            int y,
            double weight
    ) implements EyePattern.TestResult { }

}
