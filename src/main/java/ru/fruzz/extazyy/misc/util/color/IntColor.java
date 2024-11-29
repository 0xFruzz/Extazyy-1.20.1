package ru.fruzz.extazyy.misc.util.color;

public class IntColor {

    public static float[] rgb(final int color) {
        return new float[]{
                (color >> 16 & 0xFF) / 255f,
                (color >> 8 & 0xFF) / 255f,
                (color & 0xFF) / 255f,
                (color >> 24 & 0xFF) / 255f
        };
    }

    public static int rgba(final int r,
                           final int g,
                           final int b,
                           final int a) {
        return a << 24 | r << 16 | g << 8 | b;
    }

    public static int getRed(final int hex) {
        return hex >> 16 & 255;
    }

    public static int getGreen(final int hex) {
        return hex >> 8 & 255;
    }

    public static int getBlue(final int hex) {
        return hex & 255;
    }

    public static int getAlpha(final int hex) {
        return hex >> 24 & 255;
    }
}