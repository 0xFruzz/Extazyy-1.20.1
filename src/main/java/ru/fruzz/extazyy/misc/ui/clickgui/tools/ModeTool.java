package ru.fruzz.extazyy.misc.ui.clickgui.tools;



import com.mojang.blaze3d.vertex.PoseStack;
import ru.fruzz.extazyy.misc.font.FontRenderers;
import ru.fruzz.extazyy.main.modules.tools.imp.ModeTools;
import ru.fruzz.extazyy.misc.ui.themeui.impl.Tool;
import ru.fruzz.extazyy.misc.util.color.ColorUtil;
import ru.fruzz.extazyy.misc.util.anim.AnimMath;
import ru.fruzz.extazyy.misc.util.render.DrawHelper;

import java.awt.*;
import java.util.HashMap;

public class ModeTool extends Tool {

    public ModeTools option;

    public boolean opened;
    public HashMap<String, Float> animation = new HashMap<>();

    public ModeTool(ModeTools option) {
        this.option = option;
        for (String s : option.modes) {
            animation.put(s, 0f);
        }
        this.s = option;
    }

    private float currentHeight = 0.0f;
    private float targetHeight = 0.0f;

    @Override
    public void drawComponent(PoseStack matrixStack, int mouseX, int mouseY) {
        float off = 4;

        float baseOffset = 10.5f;
        float expandedOffset = baseOffset;
        for (String s : option.modes) {
            expandedOffset += 8f;
        }
        if (opened) {
            targetHeight = expandedOffset;
        } else {
            targetHeight = 0;
        }
        currentHeight = AnimMath.lerp(currentHeight, targetHeight, 10);
        FontRenderers.msSemi16.drawString(matrixStack, option.getName(), x + 6, y + 3, -1);
        off += FontRenderers.msSemi16.getFontHeight("1") / 2f + 2;
        height = off + currentHeight + 15.5f;
        DrawHelper.rectangle(matrixStack, x + 5, y + off, width - 10, 20 - 6, 2, new Color(74, 74, 74, 26).getRGB());
        DrawHelper.rectangle(matrixStack, x + 5, y + off + 17, width - 10, currentHeight, 2, new Color(74, 74, 74, 26).getRGB());
        FontRenderers.msSemi16.drawString(matrixStack, option.get(), x + 8, y + 20 - 4, new Color(214, 214, 214, 255).getRGB());
        if (opened) {
            int i = 1;
            for (String s : option.modes) {
                boolean hovered = DrawHelper.isInRegion(mouseX, mouseY, x, y + off + 20 + i, width, 8);
                animation.put(s, AnimMath.lerp(animation.get(s), hovered ? 2 : 0, 10));
                FontRenderers.msSemi16.drawString(matrixStack, s, x + 9 + animation.get(s), y + off + 20.5F + i, option.get().equals(s) ? ColorUtil.getColorStyle(90) : new Color(132, 132, 132, 255).getRGB());
                i += 9;
            }
            height += 3;
        }
    }


    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        float off = 3;
        off += FontRenderers.msSemi16.getFontHeight("1") / 2f + 2;
        if (DrawHelper.isInRegion(mouseX, mouseY, x + 5, y + off, width - 10, 20 - 5)) {
            opened = !opened;
        }


        if (!opened) return;
        int i = 1;
        for (String s : option.modes) {
            if (DrawHelper.isInRegion(mouseX, mouseY, x, y + off + 20.5F + i, width, 8))
                option.set((i - 1) / 8);
            i += 9;
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {

    }

    @Override
    public void keyTyped(int keyCode, int scanCode, int modifiers) {

    }

    @Override
    public void charTyped(char codePoint, int modifiers) {

    }
}