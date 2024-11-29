package ru.fruzz.extazyy.misc.ui.themeui.impl;


import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.util.Mth;
import org.joml.Vector4f;
import ru.fruzz.extazyy.misc.util.render.DrawHelper;

import java.awt.*;

public class CTWindow {

    private Color component;

    public Vector4f pos = new Vector4f();
    private float[] hsb = new float[2];
    private float alpha;
    private boolean dragging;
    private boolean draggingHue;
    private boolean draggingAlpha;

    public CTWindow(Color component, Vector4f pos) {
        this.pos = pos;
        this.component = component;
        hsb = Color.RGBtoHSB(component.getRed(), component.getGreen(), component.getBlue(), null);
        alpha = component.getAlpha() / 255f;
    }

    public static float[] copied = new float[2];

    public void draw(PoseStack stack, int mouseX, int mouseY) {
        component = new Color(DrawHelper.reAlphaInt(Color.getHSBColor(hsb[0], hsb[1], hsb[2]).getRGB(), (int) (alpha * 255)));

        float width = 228 / 2f;
        float x = pos.x + pos.z - 10 + 8 / 2f;
        float y = pos.y + pos.w / 2f + 8 / 2f;
        DrawHelper.rectRGB(stack,x + 4, y + 4, width - 8, width - 8, 2, Color.WHITE.getRGB(), Color.BLACK.getRGB(), Color.getHSBColor(hsb[0], 1, 1).getRGB(), Color.BLACK.getRGB());


        if (dragging) {
            float saturation = Mth.clamp((mouseX - x - 4), 0, width - 8) / (width - 8);
            float brightness = Mth.clamp((mouseY - y - 4), 0, width - 8) / (width - 8);
            hsb[1] = saturation;
            hsb[2] = 1 - brightness;
        }

       if (draggingHue) {
            float hue = Mth.clamp((mouseX - x - 6), 0, width - 12) / (width - 12);
            hsb[0] = hue;
        }
        if (draggingAlpha) {
            float hue = Mth.clamp((mouseX - x - 6), 0, width - 12) / (width - 12);
            alpha = hue;
        }

    }

    public void onConfigUpdate() {
        hsb = Color.RGBtoHSB(component.getRed(), component.getGreen(), component.getBlue(), null);
        alpha = component.getAlpha() / 255f;
    }

    public int getColor() {
        return component.getRGB();
    }

    public void click(int mouseX, int mouseY) {

        float width = 228 / 2f;
        float x = pos.x + pos.z - 10 + 8 / 2f;
        float y = pos.y + pos.w / 2f + 8 / 2f;

        if (DrawHelper.isInRegion(mouseX, mouseY, x + 4, y + width + 1, width - 8, 6)) {
            draggingHue = true;
        }
        if (DrawHelper.isInRegion(mouseX, mouseY, x + 4, y + 321 / 2f - 36 / 2f - 4, 102 / 2f, 36 / 2f)) {
            CTWindow.copied = Color.RGBtoHSB(component.getRed(), component.getGreen(), component.getBlue(), null);
        }
        if (DrawHelper.isInRegion(mouseX, mouseY, x + 4 + 51 + 4, y + 321 / 2f - 36 / 2f - 4, 102 / 2f, 36 / 2f)) {
            if (CTWindow.copied.length >= 3) {
                component = new Color(Color.HSBtoRGB(CTWindow.copied[0], CTWindow.copied[1], CTWindow.copied[2]));
                hsb = Color.RGBtoHSB(component.getRed(), component.getGreen(), component.getBlue(), null);
            }
        }
        if (DrawHelper.isInRegion(mouseX, mouseY, x + 4, y + width + 13, width - 8, 6)) {
            draggingAlpha = true;
        }
        if (DrawHelper.isInRegion(mouseX, mouseY, x + 4, y + 4, width - 8, width - 8)) {
            dragging = true;
        }

    }

    public void unclick(int mouseX, int mouseY) {
        dragging = false;
        draggingHue = false;
        draggingAlpha = false;
    }

}
