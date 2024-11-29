package ru.fruzz.extazyy.misc.ui.clickgui.tools;


import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.util.Mth;
import ru.fruzz.extazyy.Extazyy;
import ru.fruzz.extazyy.main.drag.Dragging;
import ru.fruzz.extazyy.misc.util.render.DrawHelper;

import java.awt.*;

public class CWindow {

    private ColorTool component;
    private float[] hsb = new float[2];
    private float alpha;
    private boolean dragging;
    private boolean draggingHue;
    private boolean draggingAlpha;

    public CWindow(ColorTool component) {
        this.component = component;
        hsb = Color.RGBtoHSB(component.option.getColor().getRed(), component.option.getColor().getGreen(),component.option.getColor().getBlue(), null);
        alpha = component.option.getColor().getAlpha() / 255f;
    }

    public static float[] copied = new float[2];


    public void draw(PoseStack stack, int mouseX, int mouseY) {
        Dragging sykarender = Extazyy.createDrag(Extazyy.moduleManager.testModule, "ClickGuiColor", component.x + component.width - 10 + 8 / 2f, component.y + component.height / 2f + 8 / 2f);

        float width = 228 / 2f;
        //float x = component.x + component.width - 10 + 8 / 2f;
        //float y = component.y + component.height / 2f + 8 / 2f;
        float x = sykarender.getX();
        float y = sykarender.getY();
        sykarender.setWidth(width);
        sykarender.setHeight(width + 14);
        DrawHelper.rectangle(stack,x, y, width, width + 14 , 2, new Color(20,20,20, 77).getRGB());
        DrawHelper.rectRGB(stack, x + 4,y + 4,width - 8,width - 8, 2, Color.BLACK.getRGB(), Color.WHITE.getRGB(),   Color.BLACK.getRGB(), Color.getHSBColor(hsb[0],1,1).getRGB());


        if (dragging) {
            float saturation = Mth.clamp((mouseX - x - 4), 0, width - 8) / (width - 8);
            float brightness = Mth.clamp((mouseY - y - 4), 0, width - 8) / (width - 8);
            hsb[1] = saturation;
            hsb[2] = 1 - brightness;
        }

        for (int i = 0; i < width - 15;i++) {
            float hue = i / (width - 15);
            DrawHelper.rectangle(stack,x + 6 + i,y + width + 2, 6, 6,1, DrawHelper.reAlphaInt(Color.HSBtoRGB(hue,1,1),255));
        }
       if (draggingHue) {
            float hue = Mth.clamp((mouseX - x - 6), 0, width - 12) / (width - 12);
            hsb[0] = hue;
        }
        if (draggingAlpha) {
            float hue = Mth.clamp((mouseX - x - 6), 0, width - 12) / (width - 12);
            alpha = hue;
        }

       if (dragging || draggingAlpha || draggingHue)
            component.option.color = DrawHelper.reAlphaInt(Color.getHSBColor(hsb[0], hsb[1],hsb[2]).getRGB(), (int) (alpha * 255));
        else {
            hsb = Color.RGBtoHSB(component.option.getColor().getRed(), component.option.getColor().getGreen(),component.option.getColor().getBlue(), null);
            alpha = component.option.getColor().getAlpha() / 255f;
        }

    }


    public boolean click(int mouseX, int mouseY) {
        float width = 228 / 2f;
        float x = component.x + component.width - 10 + 8 / 2f;
        float y = component.y + component.height / 2f + 8 / 2f;
        
        if (DrawHelper.isInRegion(mouseX,mouseY, x + 4,y + width + 1,width - 8,6)) {
            draggingHue = true;
            return false;
        }
        if (DrawHelper.isInRegion(mouseX,mouseY, x + 4, y + 321 / 2f - 36 / 2f - 4, 102 / 2f, 36 / 2f)) {
            CWindow.copied = Color.RGBtoHSB(component.option.getColor().getRed(), component.option.getColor().getGreen(),component.option.getColor().getBlue(), null);
            return false;
        }
        if (DrawHelper.isInRegion(mouseX,mouseY, x + 4 + 51 + 4, y + 321 / 2f - 36 / 2f - 4, 102 / 2f, 36 / 2f)) {
            component.option.color = Color.HSBtoRGB(CWindow.copied[0], CWindow.copied[1], CWindow.copied[2]);
            hsb = Color.RGBtoHSB(component.option.getColor().getRed(), component.option.getColor().getGreen(),component.option.getColor().getBlue(), null);
            return false;
        }
        if (DrawHelper.isInRegion(mouseX,mouseY, x + 4,y + width + 13,width - 8,6)) {
            draggingAlpha = true;
            return false;
        }
        if (DrawHelper.isInRegion(mouseX,mouseY, x + 4,y + 4,width - 8,width - 8)) {
            dragging = true;
            return false;
        }
        return true;
    }

    public void unclick(int mouseX, int mouseY) {
        dragging = false;
        draggingHue = false;
        draggingAlpha = false;
    }

}
