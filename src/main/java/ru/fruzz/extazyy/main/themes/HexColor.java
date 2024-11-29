package ru.fruzz.extazyy.main.themes;


import net.minecraft.util.Mth;

public class HexColor {
    public static int toColor(String hexColor) {
        int argb = Integer.parseInt(hexColor.substring(1), 16);
        return reAlphaInt(argb, 255);
    }

    public static int reAlphaInt(final int color, final int alpha) {
        return (Mth.clamp(alpha, 0, 255) << 24) | (color & 16777215);
    }
}
