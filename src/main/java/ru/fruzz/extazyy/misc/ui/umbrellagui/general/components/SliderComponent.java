package ru.fruzz.extazyy.misc.ui.umbrellagui.general.components;

import com.mojang.blaze3d.vertex.PoseStack;
import ru.fruzz.extazyy.misc.font.FontRenderers;
import ru.fruzz.extazyy.main.modules.tools.imp.NumberTools;
import ru.fruzz.extazyy.misc.ui.umbrellagui.general.Component;
import ru.fruzz.extazyy.misc.util.anim.AnimMath;
import ru.fruzz.extazyy.misc.util.math.MathUtil;
import ru.fruzz.extazyy.misc.util.render.DrawHelper;

import java.awt.*;

public class SliderComponent extends Component {

    public NumberTools option;

    public SliderComponent(NumberTools component) {
        this.option = component;
    }


    boolean drag;

    float anim = 1;

    @Override
    public void drawComponent(PoseStack matrixStack, int mouseX, int mouseY) {
        height += 3;
        y -= 5;
        width += 202f;
        float sliderWidth = ((option.getValue().floatValue() - option.getMin()) / (option.getMax() - option.getMin())) * (width - 12);
        anim = AnimMath.lerp(anim, sliderWidth, 10);
        float offset = 117.5f;
        //y -= 1;
        x += offset;
        FontRenderers.generalgui14.drawString(matrixStack, "G", x + 0.5f, y + 3f, new Color(102, 102, 102, 255).getRGB());

        FontRenderers.umbrellatext16.drawString(matrixStack, option.getName(), x + 13, y + 2f, new Color(176, 176, 176, 255).getRGB());
        FontRenderers.umbrellatext16.drawString(matrixStack, String.valueOf(option.getValue().floatValue()), x + width - FontRenderers.umbrellatext16.getStringWidth(String.valueOf(option.getValue().floatValue())) - 11, y + 2f, new Color(176, 176, 176, 255).getRGB());
        DrawHelper.rectangle(matrixStack,x, y + 12, width - 12, 4f, 1.5f, new Color(33, 38, 44).getRGB());
        DrawHelper.rectangle(matrixStack,x, y + 12, anim, 4f, 1.5f, new Color(239, 61, 61).getRGB());
        float circleoff = drag ? 1 : 0;
        DrawHelper.rectangle(matrixStack, x - 4 + anim-  circleoff/2, y + 11 - circleoff/2, 6f + circleoff, 6 + circleoff,3 + circleoff /2,new Color(247, 246, 241,255).getRGB() );
        if (drag) {
            float value = (float) ((mouseX - x - 6) / (width - 11) * (option.getMax() - option.getMin()) + option.getMin());
            value = (float) MathUtil.round(value, option.getIncrement());
            option.setValue(value);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (DrawHelper.isInRegion(mouseX, mouseY, x, y + 11, width - 11, 4.7f)) {
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
