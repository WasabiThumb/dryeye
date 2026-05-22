package io.github.wasabithumb.dryeye.color;

import org.jspecify.annotations.NullMarked;

@NullMarked
public final class ColorMath {

    public static double similarity(Color a, Color b) {
        int dr = a.red() - b.red();
        int dg = a.green() - b.green();
        int db = a.blue() - b.blue();
        int sqrMag = dr * dr + dg * dg + db * db;
        if (sqrMag == 0) return 1d;
        if (sqrMag == 195075) return 0d;
        return Math.max(0d, 1d - Math.sqrt((double) sqrMag / 195075d));
    }

    public static double luminosity(Color color) {
        int r = color.red();
        int g = color.green();
        int b = color.blue();
        int sqrMag = r * r + g * g + b * b;
        if (sqrMag == 0) return 0d;
        if (sqrMag == 195075) return 1d;
        return Math.max(0d, Math.sqrt((double) sqrMag / 195075d));
    }

    //

    private ColorMath() { }

}
