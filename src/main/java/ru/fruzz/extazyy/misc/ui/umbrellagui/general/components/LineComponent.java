package ru.fruzz.extazyy.misc.ui.umbrellagui.general.components;

import com.mojang.blaze3d.vertex.PoseStack;
import ru.fruzz.extazyy.misc.ui.umbrellagui.general.Component;
import ru.fruzz.extazyy.misc.util.render.DrawHelper;

import java.awt.*;

public class LineComponent extends Component {

    @Override
    public void drawComponent(PoseStack matrixStack, int mouseX, int mouseY) {
        height -= 18;
      //  DrawHelper.rectangle(matrixStack, x + 116,y - 5, 293, 1f, 0, new Color(26, 26, 31).getRGB());
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
