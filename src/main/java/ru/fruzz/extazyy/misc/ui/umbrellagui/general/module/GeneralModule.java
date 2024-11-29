package ru.fruzz.extazyy.misc.ui.umbrellagui.general.module;

import com.mojang.blaze3d.vertex.PoseStack;
import ru.fruzz.extazyy.misc.font.FontRenderers;
import ru.fruzz.extazyy.misc.ui.umbrellagui.general.Component;
import ru.fruzz.extazyy.misc.util.ClientUtil;
import ru.fruzz.extazyy.misc.util.anim.Animation;
import ru.fruzz.extazyy.misc.util.render.TestRender;
import ru.fruzz.extazyy.misc.util.anim.AnimMath;
import ru.fruzz.extazyy.misc.util.render.lowrender.RenderMcd;
import ru.fruzz.extazyy.misc.util.render.DrawHelper;

import java.awt.*;

public class GeneralModule extends Component {


    boolean bind;
    public float animation = 0.0F;

    Animation animoffsetwidth = new Animation(Animation.Ease.LINEAR, 0,1,200);
    Animation animoffsetheight = new Animation(Animation.Ease.LINEAR, 0,1,200);
    @Override
    public void drawComponent(PoseStack matrixStack, int mouseX, int mouseY) {

        float x2 = x;
        float y2 = y;
        boolean enabled = module.state;
        boolean enabledragged = DrawHelper.isInRegion(mouseX, mouseY, x2 + 116.5f, y2 + 55.5f, 290f,8.5f);
        DrawHelper.rectangle(matrixStack, x2 + 110, y2 + 33, 304, 56, 2f, new Color(19, 21, 25).getRGB());
        FontRenderers.umbrellatext15.drawString(matrixStack, "General",x2 + 115.5f, y2 + 40, new Color(115, 117, 130,255).getRGB());
        height += 56;


        if(!module.canrisk) {
            FontRenderers.generalgui14.drawString(matrixStack, "A", x2 + 118.5f, y2 + 58.5f, enabled ? new Color(207, 54, 54).getRGB() : new Color(102, 102, 102, 255).getRGB());

            FontRenderers.umbrellatext16.drawString(matrixStack, "Enable",x2 + 130.5f, y2 + 57, new Color(176, 176, 176,255).getRGB());
        } else {
            float width = FontRenderers.generalgui14.getStringWidth("D");
            FontRenderers.generalgui14.drawString(matrixStack, "D", x2 + 118.5f, y2 + 58.5f, new Color(207, 153, 54, 255).getRGB());

            FontRenderers.generalgui14.drawString(matrixStack, "A", x2 + 133f, y2 + 58.5f, enabled ? new Color(207, 54, 54).getRGB() : new Color(102, 102, 102, 255).getRGB());
            FontRenderers.umbrellatext16.drawString(matrixStack, "Enable",x2 + 145.5f, y2 + 57, new Color(176, 176, 176,255).getRGB());

        }
        DrawHelper.rectangle(matrixStack, x2 + 113, y2 + 69, 298, 1, 4, new Color(26, 26, 31).getRGB());



        this.animation = AnimMath.fast(this.animation, module.isEnabled() ? -1.0F : 0.0F, 15.0F);

        DrawHelper.rectangle(matrixStack, x2 + 392, y2 + 55f, 16, 9, 4.3f, enabled ? new Color(57, 28, 31).getRGB() : (enabledragged ?  new Color(50, 29, 33).getRGB() :  new Color(26, 28, 33).getRGB()));

        RenderMcd.drawBlurredShadow(matrixStack , x2 + 392 - this.animation * 7.90f, y2 + 55f, 8.5f, 8.5f, 6, new Color(17, 19, 23).getRGB());
        DrawHelper.rectangle(matrixStack, x2 + 392 - this.animation * 7.90f, y2 + 55f, 8.5f, 8.5f, 4f, enabled ? new Color(207, 54, 54).getRGB() : ( enabledragged ?  new Color(154, 78, 78).getRGB() :  new Color(102, 102, 102).getRGB()));

        FontRenderers.generalgui14.drawString(matrixStack, "C",x2 + 118.5f, y2 + 78.5f, enabled ? new Color(207, 54, 54).getRGB() : new Color(102, 102, 102,255).getRGB());
        FontRenderers.umbrellatext16.drawString(matrixStack, "Key",x2 + 130.5f, y2 + 77, new Color(176, 176, 176,255).getRGB());


       // DrawHelper.rectangle(matrixStack, x2 + 392, y2 + 75f, 16, 9, 2f, new Color(36, 38, 46).getRGB());
        //FontRenderers.msSemi24.drawString(matrixStack, "...",x2 + 395.5f, y2 + 70.6f, new Color(176, 176, 176,255).getRGB());


        String bindString = module.bind == 0 ? "..." : ClientUtil.getKey(module.bind);
        float offsetwidth;
        float offsetheight;
        animoffsetheight.setTarget(bind ? 2 : 0);
        animoffsetheight.setSpeed(200);
        animoffsetwidth.setTarget(bind ? 4 : 0);
        animoffsetwidth.setSpeed(200);
        offsetheight = animoffsetheight.getValue();
        offsetwidth = animoffsetwidth.getValue();
        DrawHelper.rectangle(matrixStack, x + 392 - offsetwidth /2, y2 + 75f - offsetheight /2, 16 + offsetwidth , 9 + offsetheight, 2f, new Color(36, 38, 46).getRGB());
        TestRender.addWindow(matrixStack, x + 392 , y2 + 75f , x + 392 + 16  , y2 + 75f + 9, 1);
        if(bindString.equals("...")) {
            FontRenderers.msSemi24.drawString(matrixStack, bindString.toUpperCase(),x + 395.5f, y2 + 70.6f, new Color(176, 176, 176,255).getRGB());
        } else {
            FontRenderers.msSemi16.drawString(matrixStack, bindString.toUpperCase(),x + 393f, y2 + 76.6f, new Color(176, 176, 176,255).getRGB());
        }
        TestRender.popWindow();
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        float x2 = x;
        float y2 = y;

        if (DrawHelper.isInRegion(mouseX, mouseY, x2 + 116.5f, y2 + 55.5f, 290f,8.5f)) {
            module.toggle();
        }

        if (bind && mouseButton > 1) {
            module.bind = -100 + mouseButton;
            bind = false;
        }
        if (DrawHelper.isInRegion(mouseX,mouseY,x + 392 , y2 + 75f, 16,9) && mouseButton == 0) {
            bind = true;
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {

    }

    @Override
    public void keyTyped(int keyCode, int scanCode, int modifiers) {
        if (bind) {
            if (module.bind == 261) {
                module.bind = 0;
                bind = false;
                return;
            }
            module.bind = keyCode;
            bind = false;
        }
    }

    @Override
    public void charTyped(char codePoint, int modifiers) {

    }
}
