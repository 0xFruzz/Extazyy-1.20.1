package ru.fruzz.extazyy.misc.ui.clickgui.tools;


import com.mojang.blaze3d.vertex.PoseStack;
import ru.fruzz.extazyy.misc.font.FontRenderers;
import ru.fruzz.extazyy.main.modules.tools.imp.BooleanOption;
import ru.fruzz.extazyy.misc.ui.themeui.impl.Tool;
import ru.fruzz.extazyy.misc.util.color.ColorUtil;
import ru.fruzz.extazyy.misc.util.anim.AnimMath;
import ru.fruzz.extazyy.misc.util.render.lowrender.RenderMcd;
import ru.fruzz.extazyy.misc.util.render.DrawHelper;

import java.awt.*;

public class BooleanTool extends Tool {

    public BooleanOption option;

    public BooleanTool(BooleanOption option) {
        this.option = option;
        this.s = option;
    }

    public float animationToggle;
    public float animation = 0.0F;
    @Override
    public void drawComponent(PoseStack matrixStack, int mouseX, int mouseY) {
        height = 15;
        float off = 0.5f;
        animationToggle = AnimMath.lerp(animationToggle, option.get() ? 1 : 0, 10);

       // int color = ColorUtil.interpolateColor(RenderUtil.IntColor.rgba(26, 29, 33, 255),
       //         ColorUtil.getColorStyle(90), animationToggle);
//
        //RenderUtil.Render2D.drawShadow(x + 5, y + 1 + off, 10, 10, 8, RenderUtil.reAlphaInt(color, 50));
        //RenderUtil.Render2D.drawRoundedRect(x + 5, y + 1 + off, 10, 10, 2f, color);
        //SmartScissor.push();
        int color = new Color(65, 68, 66, 255).getRGB();
        int color2 = option.get() ? ColorUtil.getColorStyle(90) : new Color(33, 33, 33, 255).getRGB();
        RenderMcd.drawBlurredShadow(matrixStack, this.x + this.width - 25.0F, this.y + 2.0F, 20.0F, 10.0F, 6, new Color(12, 12, 12,255).getRGB());
        DrawHelper.rectRGB(matrixStack,this.x + this.width - 25.0F, this.y + 2.0F, 20.0F, 10.0F, 4F, color2, color2, color2, color2);
        DrawHelper.rectangle(matrixStack,this.x + this.width - 23.5F - this.animation * 10.0F, this.y + 3.5F, 7.0F,7.0F, 3.0F,  option.get() ? new Color(255,255,255).getRGB() : new Color(95, 95, 95, 255).getRGB() );
        //SmartScissor.setFromComponentCoordinates(x + 5, y + 1 + off, 10 * animationToggle, 10);
        this.animation = AnimMath.fast(this.animation, option.get() ? -1.0F : 0.0F, 15.0F);
        //Fonts.icons[12].drawString(matrixStack, "A", x + 7, y + 6 + off, -1);
       // SmartScissor.unset();
        //SmartScissor.pop();

        FontRenderers.msSemi16.drawString(matrixStack, option.getName(), x + 6f, y + 2.5f + off, new Color(214, 214, 214, 255).getRGB());

    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (DrawHelper.isInRegion(mouseX, mouseY, x, y, width - 5, 15)) {

            option.toggle();
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
