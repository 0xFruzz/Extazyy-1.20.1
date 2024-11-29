package ru.fruzz.extazyy.misc.ui.umbrellagui.general.components;

import com.mojang.blaze3d.vertex.PoseStack;
import ru.fruzz.extazyy.misc.font.FontRenderers;
import ru.fruzz.extazyy.main.modules.tools.imp.ModeTools;
import ru.fruzz.extazyy.misc.ui.umbrellagui.general.Component;
import ru.fruzz.extazyy.misc.util.anim.AnimMath;
import ru.fruzz.extazyy.misc.util.anim.Animation;
import ru.fruzz.extazyy.misc.util.render.DrawHelper;

import java.awt.*;

public class ModeComponent extends Component {

    public ModeTools option;

    //Constants
    public boolean opened;
    float targetoffset = 0;
    Animation animation = new Animation(Animation.Ease.LINEAR, 0,1, 200);

    public ModeComponent(ModeTools tools) {
        this.option = tools;

        //init
        init();
    }

    public void init() {
        opened = false;


    }

    @Override
    public void drawComponent(PoseStack matrixStack, int mouseX, int mouseY) {

        x += 0;
        //y += 0;
        height += 0.5;
        width += 0;
        float offset = -8;

        for (String s : option.modes) {
            offset += 8f;
        }

        targetoffset = AnimMath.lerp(targetoffset, opened ? offset : 0, 10);
        height += targetoffset;
        FontRenderers.generalgui14.drawString(matrixStack, "E",x + 118f, y + 1.5f, new Color(102, 102, 102,255).getRGB());
        FontRenderers.umbrellatext16.drawString(matrixStack, option.getName(),x + 130.5f, y, new Color(176, 176, 176,255).getRGB());


        DrawHelper.rectangle(matrixStack, x + 348 - 0.3f, y - 1.5f - 0.3f, 60.6f, 10.6f + targetoffset, 1.5f, new Color(31, 34, 35).getRGB());
        DrawHelper.rectangle(matrixStack, x + 348, y - 1.5f, 60, 10 + targetoffset, 1.5f, new Color(23, 26, 28).getRGB());
        if(!opened) {
            FontRenderers.umbrellatext16.drawString(matrixStack, option.get(),x + 350, y + 1f, opened ? new Color(105, 104, 104,255).getRGB() : new Color(150, 150, 151,255).getRGB());
        }
        animation.setTarget(opened ? 1 : 0);
        animation.setSpeed(300);
        if(opened) {

            float off = 3f;
            for (String s : option.modes) {
                boolean hover = DrawHelper.isInRegion(mouseX, mouseY, x + 348f, y - 1.5f - 0.3f + off, 60, 8);
                FontRenderers.umbrellatext16.drawString(matrixStack, s,x + 350f, y - 1.5f - 0.3f + off, s.equals(option.get()) ? new Color(146 / 255f, 44 / 255f, 45 / 255f, animation.getValue()).getRGB() : (hover ? new Color(153 / 255f, 97 / 255f, 98 / 255f,animation.getValue()).getRGB() : new Color(150/ 255f, 150/ 255f, 151 / 255f,animation.getValue()).getRGB()));
                off += 8;
            }
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if(isHovered(mouseX,mouseY,x + 348, y - 1.5f, 60, 10) && mouseButton == 1) {
            opened = !opened;
        }

        if (!opened) return;
        float offset = -8;

        for (String s : option.modes) {
            offset += 8f;

        }
        float off = 2.5f;
        for (String s : option.modes) {

            if(DrawHelper.isInRegion(mouseX, mouseY, x + 348f, y - 1.5f - 0.3f + off, 60, 8) && mouseButton == 0) {
                option.set(s);
            }
            off += 8;
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
