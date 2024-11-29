package ru.fruzz.extazyy.misc.ui.clickgui.tools;


import com.mojang.blaze3d.vertex.PoseStack;
import ru.fruzz.extazyy.misc.font.FontRenderers;
import ru.fruzz.extazyy.main.modules.tools.imp.NumberTools;
import ru.fruzz.extazyy.misc.ui.themeui.impl.Tool;
import ru.fruzz.extazyy.misc.util.color.ColorUtil;
import ru.fruzz.extazyy.misc.util.anim.AnimMath;
import ru.fruzz.extazyy.misc.util.math.MathUtil;
import ru.fruzz.extazyy.misc.util.render.lowrender.RenderMcd;
import ru.fruzz.extazyy.misc.util.render.DrawHelper;

import java.awt.*;

public class SliderTool extends Tool {

    public NumberTools option;

    public SliderTool(NumberTools option) {
        this.option = option;
        this.s = option;
    }

    boolean drag;

    float anim = 1;

    @Override
    public void drawComponent(PoseStack matrixStack, int mouseX, int mouseY) {
        height -= 0;
        float sliderWidth = ((option.getValue().floatValue() - option.getMin()) / (option.getMax() - option.getMin())) * (width - 12);
        anim = AnimMath.lerp(anim, sliderWidth, 10);
        FontRenderers.msSemi16.drawString(matrixStack, option.getName(), x + 6, y + 2, new Color(214, 214, 214, 255).getRGB());
        FontRenderers.msSemi16.drawString(matrixStack, String.valueOf(option.getValue().floatValue()), x + width - FontRenderers.msSemi16.getStringWidth(String.valueOf(option.getValue().floatValue())) - 6, y + 2, new Color(214, 214, 214, 255).getRGB());
        RenderMcd.drawBlurredShadow(matrixStack,x + 6, y + 13, width - 12.1f, 4f, 10, new Color(7, 7, 7,255).getRGB());
        DrawHelper.rectangle(matrixStack,x + 6, y + 13, width - 12, 4f, 1, new Color(65, 65, 65).getRGB());
        DrawHelper.rectangle(matrixStack,x + 6, y + 13, anim, 4f, 1f, ColorUtil.getColorStyle(90));
        RenderMcd.drawBlurredShadow(matrixStack, x + 5 + anim, y + 12, 6,6,3, new Color(7,7,7,255).getRGB());
        DrawHelper.rectangle(matrixStack, x+5f + anim, y + 12, 6f, 6,3,new Color(255,255,255,255).getRGB() );
        if (drag) {
            float value = (float) ((mouseX - x - 6) / (width - 12) * (option.getMax() - option.getMin()) + option.getMin());
            value = (float) MathUtil.round(value, option.getIncrement());
            option.setValue(value);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (isHovered(mouseX,mouseY)) {
            drag = true;
        }
    }
    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        drag = false;
    }
    @Override
    public void keyTyped(int keyCode, int scanCode, int modifiers) {
    }
    @Override
    public void charTyped(char codePoint, int modifiers) {
    }
}
