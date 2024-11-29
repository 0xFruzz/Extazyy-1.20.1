package ru.fruzz.extazyy.misc.ui.clickgui.tools;


import com.mojang.blaze3d.vertex.PoseStack;

import ru.fruzz.extazyy.main.modules.tools.imp.NULka;
import ru.fruzz.extazyy.misc.ui.themeui.impl.Tool;

public class NullTools extends Tool {




    public NULka setting;
    public boolean isTyping;

    public NullTools(NULka setting) {
        this.setting = setting;
        this.s = setting;
    }


    @Override
    public void drawComponent(PoseStack matrixStack, int mouseX, int mouseY) {

        height -= 12;

       // String bindString = option.getKey() == 0 ? "NONE" : ClientUtil.getKey(option.getKey());

       // if (bindString == null) {
       //     bindString = "";
       // }

       // float width = FontRenderers.msSemi16.getStringWidth(bindString) + 4;
       // DrawHelper.rectangle(matrixStack,x + 5, y + 2, width, 10, 2, bind ? new Color(17, 18, 21).brighter().brighter().getRGB() : new Color(17, 18, 21).brighter().getRGB());
       // FontRenderers.msSemi16.drawCenteredString(matrixStack, bindString, x + 5 + (width / 2), y + 4, -1);
      //  FontRenderers.msSemi16.drawString(matrixStack, option.getName(), x + 5 + width + 3, y + 4, -1);
        //DrawHelper.rectangle(matrixStack, x, y,40, 10,1, new Color(255, 204, 0, 77).getRGB());
       // FontRenderers.msLight14.drawString(matrixStack, "Warning!", x + 5, y + 2.5f, new Color(255,255,255,255).getRGB());
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
