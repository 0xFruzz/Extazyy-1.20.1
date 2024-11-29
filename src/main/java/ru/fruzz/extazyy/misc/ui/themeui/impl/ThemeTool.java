package ru.fruzz.extazyy.misc.ui.themeui.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Vector4f;
import ru.fruzz.extazyy.Extazyy;
import ru.fruzz.extazyy.misc.font.FontRenderers;
import ru.fruzz.extazyy.main.themes.Theme;
import ru.fruzz.extazyy.misc.util.color.ColorUtil;
import ru.fruzz.extazyy.misc.util.render.lowrender.RenderMcd;
import ru.fruzz.extazyy.misc.util.render.DrawHelper;


import java.awt.*;

public class  ThemeTool extends Tool {

    public Theme config;
    public CTWindow[] colors = new CTWindow[2];
    public static CTWindow selected = null;

    public boolean opened;


    public ThemeTool(Theme config) {
        this.config = config;
    }

    @Override
    public void onConfigUpdate() {
        super.onConfigUpdate();
        for (CTWindow CTWindow : colors) {
            if (CTWindow != null)
                CTWindow.onConfigUpdate();
        }

        if (config.name.equalsIgnoreCase("����")) {
            if (colors.length >= 2) {
                colors[0] = new CTWindow(new Color(config.colors[0]), new Vector4f(0, 0, 0, 0));
                colors[1] = new CTWindow(new Color(config.colors[1]), new Vector4f(0, 0, 0, 0));
            }
        }

    }
    PoseStack matrixStack;
    @Override
    public void drawComponent(PoseStack matrixStack, int mouseX, int mouseY) {
        this.matrixStack = matrixStack;
        if (opened) {
            height += 50;
        }
        if (!(colors[0] == null && colors[1] == null)) {
            config.colors[0] = colors[0].getColor();
            config.colors[1] = colors[1].getColor();
        }
        RenderMcd.drawBlurredShadow(matrixStack,x - 10, y - 10, width + 19, height, 12, new Color(74, 74, 74, 26).darker().darker().getRGB());

        DrawHelper.rectangle(matrixStack,x -10, y - 10, width + 18, height, 4, Extazyy.themesUtil.getCurrentStyle() == config ? new Color(32, 36, 42, 98).brighter().getRGB() : new Color(74, 74, 74, 26).getRGB());
        FontRenderers.msSemi16.drawString(matrixStack, config.name, x - 1 , y + 8.5f - 12, -1);

        int color1 = config.name.contains("�����-�������") ? ColorUtil.astolfo(10, 0, 0.7f, 1, 1) : config.getColor(0);
        int color2 = config.name.contains("�����-�������") ? ColorUtil.astolfo(10, 90, 0.7f, 1, 1) : config.getColor(90);
        int color3 = config.name.contains("�����-�������") ? ColorUtil.astolfo(10, 180, 0.7f, 1, 1) : config.getColor(180);
        int color4 = config.name.contains("�����-�������") ? ColorUtil.astolfo(10, 270, 0.7f, 1, 1) : config.getColor(270);

        DrawHelper.rectRGB(matrixStack,x + width - 12, y + 6 - 10, 15, 9, 1, color1, color2, color3, color4);

        if (opened) {
            DrawHelper.rectangle(matrixStack,x + 10, y + 19 - 10 , 8, 12, 0, colors[0].getColor());
            DrawHelper.rectangle(matrixStack,x + 10 + 12, y + 19 - 10, 8, 12, 0, colors[1].getColor());
        }

    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        boolean hover = mouseX > x - 10 && mouseX < x + width + 9 && mouseY > y + 8.5f - 20 && mouseY < y + 8.5f - 20 + height;//
        if (hover && !(Extazyy.themesUtil.getCurrentStyle() == config)) {
            Extazyy.themesUtil.setCurrentStyle(config);
        }
        FontRenderers.msSemi16.drawString(matrixStack, config.name, x + 8, y + 8.5f - 12, -1);

        if (hover && config.name.equalsIgnoreCase("����") && mouseButton == 1) {
            opened = !opened;
        }
        if (opened) {
            if (DrawHelper.isInRegion(mouseX, mouseY, x + 10, y - 20, 8, 12)) {
                if (selected == colors[0]) {
                    selected = null;
                    return;
                }
                selected = colors[0];
                selected.pos = new Vector4f(mouseX, mouseY, 0, 0);
            }
            if (DrawHelper.isInRegion(mouseX, mouseY, (float) (x + (100f + 12)), y - 20, 314 + 12, 20)) {
                if (selected == colors[1]) {
                    selected = null;
                    return;
                }
                selected = colors[1];
                selected.pos = new Vector4f(mouseX, mouseY, 0, 0);
            }
        }
        if (selected != null)
            selected.click(mouseX, mouseY);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        if (selected != null)
            selected.unclick(mouseX, mouseY);
    }

    @Override
    public void keyTyped(int keyCode, int scanCode, int modifiers) {

    }

    @Override
    public void charTyped(char codePoint, int modifiers) {

    }
}
