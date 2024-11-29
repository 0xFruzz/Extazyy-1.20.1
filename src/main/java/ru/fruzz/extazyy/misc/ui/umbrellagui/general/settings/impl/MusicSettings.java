package ru.fruzz.extazyy.misc.ui.umbrellagui.general.settings.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import ru.fruzz.extazyy.misc.font.FontRenderers;
import ru.fruzz.extazyy.misc.ui.umbrellagui.general.Component;
import ru.fruzz.extazyy.misc.util.render.DrawHelper;

import java.awt.*;

public class MusicSettings extends Component {


    @Override
    public void drawComponent(PoseStack matrixStack, int mouseX, int mouseY) {
        height += 62;
        DrawHelper.rectangle(matrixStack, x,y, width, height, 5, new Color(19, 21, 25).getRGB());

        FontRenderers.umbrellatext17.drawString(matrixStack,"Music Settings", x + 4,y +4, new Color(88, 90, 100).getRGB());
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {

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
