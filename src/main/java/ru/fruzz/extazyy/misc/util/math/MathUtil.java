package ru.fruzz.extazyy.misc.util.math;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MathUtil {

    public static float calculateXPosition(float x, float width) {
        return x - width / 2f;
    }

    public static float calculateYPosition(float y, float height) {
        return y - height / 2;
    }

    public static double round(double num, double increment) {
        double v = (double) Math.round(num / increment) * increment;
        BigDecimal bd = new BigDecimal(v);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
