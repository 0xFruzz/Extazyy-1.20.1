package ru.fruzz.extazyy.misc.ui.umbrellagui.general.components;

import com.mojang.blaze3d.vertex.PoseStack;
import ru.fruzz.extazyy.misc.font.FontRenderers;
import ru.fruzz.extazyy.main.modules.tools.imp.BindTools;
import ru.fruzz.extazyy.misc.ui.umbrellagui.general.Component;
import ru.fruzz.extazyy.misc.util.ClientUtil;
import ru.fruzz.extazyy.misc.util.anim.Animation;
import ru.fruzz.extazyy.misc.util.render.TestRender;
import ru.fruzz.extazyy.misc.util.render.DrawHelper;

import java.awt.*;

public class BindComponent extends Component {

    public BindTools option;
    boolean bind;
    Animation animoffsetwidth = new Animation(Animation.Ease.LINEAR, 0,1,200);
    Animation animoffsetheight = new Animation(Animation.Ease.LINEAR, 0,1,200);
    public BindComponent(BindTools component) {
        this.option = component;
    }



    @Override
    public void drawComponent(PoseStack matrixStack, int mouseX, int mouseY) {
        //y += 3;
        String bindString = option.getKey() == 0 ? "..." : ClientUtil.getKey(option.getKey());

        boolean enabled = !bindString.equals("...");
        //new Color(207, 54, 54).getRGB()
        //
        FontRenderers.generalgui14.drawString(matrixStack, "C",x + 118.5f, y + 1.5f, enabled ? new Color(207, 54, 54).getRGB() : new Color(102, 102, 102,255).getRGB());
        FontRenderers.umbrellatext16.drawString(matrixStack, option.getName(),x + 130.5f, y, new Color(176, 176, 176,255).getRGB());

        float offsetwidth;
        float offsetheight;
        animoffsetheight.setTarget(bind ? 2 : 0);
        animoffsetheight.setSpeed(200);
        animoffsetwidth.setTarget(bind ? 4 : 0);
        animoffsetwidth.setSpeed(200);
        offsetheight = animoffsetheight.getValue();
        offsetwidth = animoffsetwidth.getValue();
        DrawHelper.rectangle(matrixStack, x + 392 - offsetwidth /2, y + 1.4f - 4.5f - offsetheight /2, 16 + offsetwidth , 9 + offsetheight, 2f, new Color(36, 38, 46).getRGB());
        TestRender.addWindow(matrixStack, x + 392 , y + 1.4f - 4.5f , x + 392 + 16  , y + 1.4f - 4.5f + 9, 1);
        if(bindString.equals("...")) {
            FontRenderers.msSemi24.drawString(matrixStack, bindString.toUpperCase(),x + 395.5f, y - 3 - 4.5f, new Color(176, 176, 176,255).getRGB());
        } else {
            FontRenderers.msSemi16.drawString(matrixStack, bindString.toUpperCase(),x + 393f, y - 1.5f, new Color(176, 176, 176,255).getRGB());
        }
       TestRender.popWindow();
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (bind && mouseButton > 1) {
            option.setKey(-100 + mouseButton);
            bind = false;
        }
        if (DrawHelper.isInRegion(mouseX,mouseY,x + 392, y + 1.4f - 4.5f, 16,9) && mouseButton == 0) {
            bind = true;
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {

    }

    @Override
    public void keyTyped(int keyCode, int scanCode, int modifiers) {
        if (bind) {
            if (keyCode == 261) {
                option.setKey(0);
                bind = false;
                return;
            }
            option.setKey(keyCode);
            bind = false;
        }
    }

    @Override
    public void charTyped(char codePoint, int modifiers) {

    }
}
