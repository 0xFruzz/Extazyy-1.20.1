package ru.fruzz.extazyy.misc.util.color;

import com.mojang.blaze3d.systems.RenderSystem;

import ru.fruzz.extazyy.Extazyy;
import ru.fruzz.extazyy.misc.util.Mine;

import java.awt.*;

public class ColorUtils implements Mine {
    public static final int green = ColorUtils.rgba(36, 218, 118, 255);
    public static final int yellow = ColorUtils.rgba(255, 196, 67, 255);
    public static final int orange = ColorUtils.rgba(255, 134, 0, 255);
    public static final int red = ColorUtils.rgba(239, 72, 54, 255);

    public static void setAlphaColor(final int color, final float alpha) {
        final float red = (float) (color >> 16 & 255) / 255.0F;
        final float green = (float) (color >> 8 & 255) / 255.0F;
        final float blue = (float) (color & 255) / 255.0F;
        RenderSystem.setShaderColor(red, green, blue, alpha);
    }
    public static void setColor(int color) {
        setAlphaColor(color, (float) (color >> 24 & 255) / 255.0F);
    }



    static int calculateHueDegrees(int divisor, int offset) {
        long currentTime = System.currentTimeMillis();
        long calculatedValue = (currentTime / divisor + offset) % 360L;
        return (int) calculatedValue;
    }

    public static int getColorStyle(float index) {
        return Extazyy.themesUtil.getCurrentStyle().getColor((int) index);
    }

    public static Color getColorStyle2(float index) {
        return new Color(255,255,255,255);
    }

    public static int getColorStyleLowSpeed(float index) {
        return Extazyy.themesUtil.getCurrentStyle().getColorLowSpeed((int) index);
    }

    public static int rgba(int r, int g, int b, int a) {
        return a << 24 | r << 16 | g << 8 | b;
    }

    public static int rgba(double r, double g, double b, double a) {
        return rgba((int) r, (int) g, (int) b, (int) a);
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

    public static int getColor(int red, int green, int blue, int alpha) {
        int color = 0;
        color |= alpha << 24;
        color |= red << 16;
        color |= green << 8;
        return color |= blue;
    }

    public static int getColor(int bright) {
        return getColor(bright, bright, bright, 255);
    }


    //    gradient with more than two colors
    public static int gradient(int speed, int index, int... colors) {
        int angle = (int) ((System.currentTimeMillis() / speed + index) % 360);
        angle = (angle > 180 ? 360 - angle : angle) + 180;
        int colorIndex = (int) (angle / 360f * colors.length);
        if (colorIndex == colors.length) {
            colorIndex--;
        }
        int color1 = colors[colorIndex];
        int color2 = colors[colorIndex == colors.length - 1 ? 0 : colorIndex + 1];
        return interpolateColor(color1, color2, angle / 360f * colors.length - colorIndex);
    }


    public static int interpolateColor(int color1, int color2, float amount) {
        amount = Math.min(1, Math.max(0, amount));

        int red1 = getRed(color1);
        int green1 = getGreen(color1);
        int blue1 = getBlue(color1);
        int alpha1 = getAlpha(color1);

        int red2 = getRed(color2);
        int green2 = getGreen(color2);
        int blue2 = getBlue(color2);
        int alpha2 = getAlpha(color2);

        int interpolatedRed = interpolateInt(red1, red2, amount);
        int interpolatedGreen = interpolateInt(green1, green2, amount);
        int interpolatedBlue = interpolateInt(blue1, blue2, amount);
        int interpolatedAlpha = interpolateInt(alpha1, alpha2, amount);

        return (interpolatedAlpha << 24) | (interpolatedRed << 16) | (interpolatedGreen << 8) | interpolatedBlue;
    }

    public static Double interpolate(double oldValue, double newValue, double interpolationValue) {
        return (oldValue + (newValue - oldValue) * interpolationValue);
    }

    public static int interpolateInt(int oldValue, int newValue, double interpolationValue) {
        return interpolate(oldValue, newValue, (float) interpolationValue).intValue();
    }
    public static int HSBtoRGB(float hue, float saturation, float brightness) {
        int r, g, b;

        if (saturation == 0) {
            int value = (int) (brightness * 255.0f + 0.5f);
            return 0xff000000 | (value << 16) | (value << 8) | value;
        }

        float h = (hue - (float) Math.floor(hue)) * 6.0f;
        float f = h - (float) Math.floor(h);
        float p = brightness * (1.0f - saturation);
        float q = brightness * (1.0f - saturation * f);
        float t = brightness * (1.0f - (saturation * (1.0f - f)));

        switch ((int) h) {
            case 0:
                r = (int) (brightness * 255.0f + 0.5f);
                g = (int) (t * 255.0f + 0.5f);
                b = (int) (p * 255.0f + 0.5f);
                break;
            case 1:
                r = (int) (q * 255.0f + 0.5f);
                g = (int) (brightness * 255.0f + 0.5f);
                b = (int) (p * 255.0f + 0.5f);
                break;
            case 2:
                r = (int) (p * 255.0f + 0.5f);
                g = (int) (brightness * 255.0f + 0.5f);
                b = (int) (t * 255.0f + 0.5f);
                break;
            case 3:
                r = (int) (p * 255.0f + 0.5f);
                g = (int) (q * 255.0f + 0.5f);
                b = (int) (brightness * 255.0f + 0.5f);
                break;
            case 4:
                r = (int) (t * 255.0f + 0.5f);
                g = (int) (p * 255.0f + 0.5f);
                b = (int) (brightness * 255.0f + 0.5f);
                break;
            case 5:
                r = (int) (brightness * 255.0f + 0.5f);
                g = (int) (p * 255.0f + 0.5f);
                b = (int) (q * 255.0f + 0.5f);
                break;
            default:
                throw new IllegalArgumentException("Invalid hue value");
        }

        return 0xff000000 | (r << 16) | (g << 8) | b;
    }

    public static int interpolateColor(int colorStyle, float hoveredFirstAn) {
        int startColor = getColorStyle(colorStyle);
        int endColor = getColorStyle(colorStyle + 1);

        int startRed = (startColor >> 16) & 0xFF;
        int startGreen = (startColor >> 8) & 0xFF;
        int startBlue = startColor & 0xFF;

        int endRed = (endColor >> 16) & 0xFF;
        int endGreen = (endColor >> 8) & 0xFF;
        int endBlue = endColor & 0xFF;

        int interpolatedRed = (int) (startRed + (endRed - startRed) * hoveredFirstAn);
        int interpolatedGreen = (int) (startGreen + (endGreen - startGreen) * hoveredFirstAn);
        int interpolatedBlue = (int) (startBlue + (endBlue - startBlue) * hoveredFirstAn);

        return (interpolatedRed << 16) | (interpolatedGreen << 8) | interpolatedBlue;
    }
    public static int getColor(float r, float g, float b, float a) {
        return new Color((int) r, (int) g, (int) b, (int) a).getRGB();
    }

    public static int red(int c) {
        return c >> 16 & 0xFF;
    }

    public static int green(int c) {
        return c >> 8 & 0xFF;
    }

    public static int blue(int c) {
        return c & 0xFF;
    }


    public static int getColor(int r, int g, int b) {
        return new Color(r, g, b, 255).getRGB();
    }

    public static int getColor(int br, int a) {
        return new Color(br, br, br, a).getRGB();
    }
    public static int replAlpha(int c, int a) {
        return getColor(red(c), green(c), blue(c), a);
    }
    public static int invertColor(int color) {
        int invertedRed = 255 - getRed(color);
        int invertedGreen = 255 - getGreen(color);
        int invertedBlue = 255 - getBlue(color);

        return getColor(invertedRed, invertedGreen, invertedBlue, getAlpha(color));
    }
    public static int getColorByName(String colorName) {
        if (colorName.equalsIgnoreCase("blue")) {
            return Color.BLUE.getRGB();
        } else {
            return Color.BLACK.getRGB(); // Замените на реальные значения цветов по именам
        }
    }
    public static int increaseBrightness(int color, float percent) {
        float[] hsb = Color.RGBtoHSB(getRed(color), getGreen(color), getBlue(color), null);
        hsb[2] = Math.min(1.0f, hsb[2] + percent);
        return Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
    }
    public static int decreaseSaturation(int color, float percent) {
        float[] hsb = Color.RGBtoHSB(getRed(color), getGreen(color), getBlue(color), null);
        hsb[1] = Math.max(0.0f, hsb[1] - percent);
        return Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
    }
    public static int createGradient(int color1, int color2, float percentage) {
        int red = (int) (getRed(color1) * (1 - percentage) + getRed(color2) * percentage);
        int green = (int) (getGreen(color1) * (1 - percentage) + getGreen(color2) * percentage);
        int blue = (int) (getBlue(color1) * (1 - percentage) + getBlue(color2) * percentage);

        return getColor(red, green, blue, getAlpha(color1));
    }
    public static int getColorFromHSL(float hue, float saturation, float lightness) {
        return Color.HSBtoRGB(hue, saturation, lightness);
    }
    public static int twinkleEffect(int baseColor, float twinklingRate) {
        float twinklingFactor = (float) (Math.sin(System.currentTimeMillis() * twinklingRate) + 1) / 2;
        return createGradient(baseColor, Color.WHITE.getRGB(), twinklingFactor);
    }
    public static int blendColors(int color1, int color2, float blendFactor) {
        int red = (int) (getRed(color1) * (1 - blendFactor) + getRed(color2) * blendFactor);
        int green = (int) (getGreen(color1) * (1 - blendFactor) + getGreen(color2) * blendFactor);
        int blue = (int) (getBlue(color1) * (1 - blendFactor) + getBlue(color2) * blendFactor);

        return getColor(red, green, blue, getAlpha(color1));
    }

    public static String getColorAsHex(int color) {
        return String.format("#%06X", (0xFFFFFF & color));
    }
    public static int getHueInRange(int baseColor, float minHue, float maxHue) {
        float[] hsb = Color.RGBtoHSB(getRed(baseColor), getGreen(baseColor), getBlue(baseColor), null);
        float hue = hsb[0];

        if (hue < minHue) {
            hue = minHue;
        } else if (hue > maxHue) {
            hue = maxHue;
        }

        return Color.HSBtoRGB(hue, hsb[1], hsb[2]);
    }

    public static int interpolateColors(int color1, int color2, float ratio) {
        if (ratio < 0) ratio = 0;
        if (ratio > 1) ratio = 1;

        int alpha1 = (color1 >> 24) & 0xFF;
        int red1 = (color1 >> 16) & 0xFF;
        int green1 = (color1 >> 8) & 0xFF;
        int blue1 = color1 & 0xFF;

        int alpha2 = (color2 >> 24) & 0xFF;
        int red2 = (color2 >> 16) & 0xFF;
        int green2 = (color2 >> 8) & 0xFF;
        int blue2 = color2 & 0xFF;

        int interpolatedAlpha = (int) (alpha1 + (alpha2 - alpha1) * ratio);
        int interpolatedRed = (int) (red1 + (red2 - red1) * ratio);
        int interpolatedGreen = (int) (green1 + (green2 - green1) * ratio);
        int interpolatedBlue = (int) (blue1 + (blue2 - blue1) * ratio);

        return (interpolatedAlpha << 24) | (interpolatedRed << 16) | (interpolatedGreen << 8) | interpolatedBlue;
    }

    public static int setAlpha(int color, int alpha) {
        if (alpha < 0) alpha = 0;
        if (alpha > 255) alpha = 255;

        return (alpha << 24) | (color & 0x00FFFFFF);
    }
}
