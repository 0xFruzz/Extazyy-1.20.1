package ru.fruzz.extazyy.misc.ui.clickgui.tools;


import com.mojang.blaze3d.vertex.PoseStack;
import ru.fruzz.extazyy.misc.font.FontRenderers;
import ru.fruzz.extazyy.main.modules.tools.imp.BindTools;
import ru.fruzz.extazyy.misc.ui.themeui.impl.Tool;
import ru.fruzz.extazyy.misc.util.ClientUtil;
import ru.fruzz.extazyy.misc.util.render.DrawHelper;

import java.awt.*;

public class BindTool extends Tool {

    public BindTools option;
    boolean bind;


    public BindTool(BindTools option) {
        this.option = option;
        this.s = option;
    }

    @Override
    public void drawComponent(PoseStack matrixStack, int mouseX, int mouseY) {

        height -= 3;

        String bindString = option.getKey() == 0 ? "NONE" : ClientUtil.getKey(option.getKey());

        if (bindString == null) {
            bindString = "";
        }

        float width = FontRenderers.msSemi16.getStringWidth(bindString) + 4;
        DrawHelper.rectangle(matrixStack,x + 5, y + 2, width, 10, 2, bind ? new Color(17, 18, 21).brighter().brighter().getRGB() : new Color(17, 18, 21).brighter().getRGB());
        FontRenderers.msSemi16.drawCenteredString(matrixStack, bindString, x + 5 + (width / 2), y + 4, -1);
        FontRenderers.msSemi16.drawString(matrixStack, option.getName(), x + 5 + width + 3, y + 4, -1);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (bind && mouseButton > 1) {
            option.setKey(-100 + mouseButton);
            bind = false;
        }
        if (isHovered(mouseX, mouseY) && mouseButton == 0) {
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
