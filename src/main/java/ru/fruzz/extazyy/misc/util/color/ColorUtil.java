package ru.fruzz.extazyy.misc.util.color;

import org.lwjgl.opengl.GL11;
import ru.fruzz.extazyy.Extazyy;
import ru.fruzz.extazyy.misc.util.math.MathUtils;
import ru.fruzz.extazyy.misc.util.render.DrawHelper;

import java.awt.*;

public class ColorUtil {
    public static int swapAlpha(final int n, final float n2) {
        return ColorUtil.toRGBA(n >> 16 & 0xFF, n >> 8 & 0xFF, n & 0xFF, (int) n2);
    }

    public static Color swapAlpha(final Color color, final int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), MathUtils.clamp(alpha, 0, 255));
    }

    public static Color swapAlpha(Color color, float alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    public int swapAlpha2(final int n, final float n2) {
        return ColorUtil.toRGBA(n >> 16 & 0xFF, n >> 8 & 0xFF, n & 0xFF, (int) n2);
    }

    public static int astolfo(int speed, int offset, float saturation, float brightness, float alpha) {
        float hue = (float) ColorUtils.calculateHueDegrees(speed, offset);
        hue = (float) ((double) hue % 360.0);
        float hueNormalized;
        return DrawHelper.reAlphaInt(
                Color.HSBtoRGB((double) ((hueNormalized = hue % 360.0F) / 360.0F) < 0.5 ? -(hueNormalized / 360.0F) : hueNormalized / 360.0F, saturation, brightness),
                Math.max(0, Math.min(255, (int) (alpha * 255.0F)))
        );
    }

    public static int getRainbowShadow() {
        int drgb;
        int color;
        int argb;
        float[] hue = new float[]{(float) (System.currentTimeMillis() % 11520L) / 11520.0f};
        int rgb = Color.HSBtoRGB(hue[0], 1.0f, 1.0f);
        int red = rgb >> 16 & 255;
        int green = rgb >> 8 & 255;
        int blue = rgb & 255;
        color = argb = ColorUtil.toRGBA(red, green, blue, 195);
        return color;
    }

    public static void glColor(Color color) {
        GL11.glColor4f((float) color.getRed() / 255.0f, (float) color.getGreen() / 255.0f, (float) color.getBlue() / 255.0f, (float) color.getAlpha() / 255.0f);
    }

    public static float r(int color) {
        return (float) (color >> 16 & 255) / 255.0F;
    }

    public static float g(int color) {
        return (float) (color >> 8 & 255) / 255.0F;
    }

    public static float b(int color) {
        return (float) (color & 255) / 255.0F;
    }

    public static float a(int color) {
        return (float) (color >> 24 & 255) / 255.0F;
    }

    public static int getRed(int color) {
        return color >> 16 & 255;
    }

    public static int getGreen(int color) {
        return color >> 8 & 255;
    }

    public static int getBlue(int color) {
        return color & 255;
    }

    public static int getAlpha(int color) {
        return color >> 24 & 255;
    }

    public static float[] rgb(int color) {
        return new float[]{(color >> 16 & 0xFF) / 255.0F, (color >> 8 & 0xFF) / 255.0F, (color & 0xFF) / 255.0F, (color >> 24 & 0xFF) / 255.0F};
    }

    public static float[] rgba(int color) {
        return new float[]{(color >> 16 & 0xFF) / 255.0F, (color >> 8 & 0xFF) / 255.0F, (color & 0xFF) / 255.0F, (color >> 24 & 0xFF) / 255.0F, (color >> 24 & 255) / 255.0F};
    }

    public static int getColor(int red, int green, int blue) {
        return getColor(red, green, blue, 255);
    }

    public static int getColor(int red, int green, int blue, int alpha) {
        int color = 0;
        color |= alpha << 24;
        color |= red << 16;
        color |= green << 8;
        return color |= blue;
    }

    public static int getRainbow() {
        int color;
        float[] hue = new float[]{(float) (System.currentTimeMillis() % 11520L) / 11520.0f};
        int rgb = Color.HSBtoRGB(hue[0], 1.0f, 1.0f);
        int red = rgb >> 16 & 255;
        int green = rgb >> 8 & 255;
        int blue = rgb & 255;
        color = ColorUtil.toRGBA(red, green, blue, 255);
        return color;
    }

    public static int getRainbow2() {
        int color;
        float[] hue = new float[]{(float) (System.currentTimeMillis() % 11520L) / 11520.0f};
        int rgb = Color.HSBtoRGB(hue[0], 1.0f, 1.0f);
        int red = rgb >> 16 & 255;
        int green = rgb >> 8 & 255;
        int blue = rgb & 255;
        color = toRGBA(red - 15, green - 15, blue - 15, 255);
        return color;
    }

    public static Color getRainbow3() {
        int drgb;
        int color;
        int argb;
        float[] hue = new float[]{(float) (System.currentTimeMillis() % 11520L) / 11520.0f};
        int rgb = Color.HSBtoRGB(hue[0], 1.0f, 1.0f);
        int red = rgb >> 16 & 255;
        int green = rgb >> 8 & 255;
        int blue = rgb & 255;
        Color color1 = new Color(red, green, blue);
        return color1;
    }

    public static void setColor(Color color) {
        if (color == null)
            color = Color.white;
        setColor(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, color.getAlpha() / 255F);
    }


    public static void setColor(int color) {
        setColor(color, (float) (color >> 24 & 255) / 255.0F);
    }

    public static void setColor(int color, float alpha) {
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;
        setColor(r, g, b, alpha);
    }

    public static void setColor(double red, double green, double blue, double alpha) {
        GL11.glColor4d(red, green, blue, alpha);
    }

    public static int toRGBA(int r, int g, int b, int a) {
        return (r << 16) + (g << 8) + (b << 0) + (a << 24);
    }

    public static int toRGBA3(int r, int g, int b) {
        return (r << 16) + (g << 8) + (b << 0);
    }

    public static Color TwoColoreffect(final Color color, final Color color2, final double n) {
        final float clamp = MathUtils.clamp((float) Math.sin(18.84955592153876 * (n / 4.0 % 1.0)) / 2.0f + 0.5f, 0.0f, 1.0f);
        return new Color(MathUtils.lerp(color.getRed() / 255.0f, color2.getRed() / 255.0f, clamp), MathUtils.lerp(color.getGreen() / 255.0f, color2.getGreen() / 255.0f, clamp), MathUtils.lerp(color.getBlue() / 255.0f, color2.getBlue() / 255.0f, clamp), MathUtils.lerp(color.getAlpha() / 255.0f, color2.getAlpha() / 255.0f, clamp));
    }

    public static Color TwoColoreffect(final int color, final int color2, final double n) {

        final float clamp = MathUtils.clamp((float) Math.sin(18.84955592153876 * (n / 4.0 % 1.0)) / 2.0f + 0.5f, 0.0f, 1.0f);
        return new Color(MathUtils.lerp(r(color), r(color2), clamp), MathUtils.lerp(g(color), g(color2), clamp), MathUtils.lerp(b(color), b(color2), clamp), MathUtils.lerp(a(color), a(color2), clamp));
    }

    public static int lightenColor(int color, int percentage) {
        if (percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException("Процент затемнения должен быть в диапазоне от 0 до 100.");
        }

        int alpha = (color >> 24) & 0xFF;
        int red = MathUtils.clamp((((color >> 16) & 0xFF)) + ((percentage / 100) * 255), 0, 255);
        int green = MathUtils.clamp(((color >> 8) & 0xFF) + ((percentage / 100) * 255), 0, 255);
        int blue = MathUtils.clamp((color & 0xFF) + ((percentage / 100) * 255), 0, 255);

        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    public static int darkenColor(int color, int percentage) {
        if (percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException("Процент затемнения должен быть в диапазоне от 0 до 100.");
        }
        percentage = 100 - percentage;
        int alpha = (color >> 24) & 0xFF;
        int red = MathUtils.clamp((((color >> 16) & 0xFF) / 100) * percentage, 0, 255);
        int green = MathUtils.clamp(((color >> 8) & 0xFF / 100) * percentage, 0, 255);
        int blue = MathUtils.clamp((color & 0xFF / 100) * percentage, 0, 255);

        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    public static int getColorStyle(int s) {
        return Extazyy.themesUtil.getCurrentStyle().getColor(s);
    }
}
