package ru.fruzz.extazyy.misc.ui.clickgui.tools;


import com.mojang.blaze3d.vertex.PoseStack;
import ru.fruzz.extazyy.misc.font.FontRenderers;
import ru.fruzz.extazyy.main.modules.tools.imp.ColorTools;
import ru.fruzz.extazyy.misc.ui.themeui.impl.Tool;
import ru.fruzz.extazyy.misc.util.render.DrawHelper;

public class ColorTool extends Tool {

    public static CWindow opened;
    public ColorTools option;
    public CWindow setted;

    public ColorTool(ColorTools option) {
        this.option = option;
        setted = new CWindow(this);
        this.s = option;
    }

    @Override
    public void drawComponent(PoseStack matrixStack, int mouseX, int mouseY) {
        height -= 4;
        FontRenderers.msSemi16.drawString(matrixStack, option.getName(), x + 6f, y + height / 2f - 2 , -1);
        float size = 8;

        DrawHelper.rectangle(matrixStack,x + width - 21 - size /2f, y + height / 2f - size /2f - 0, 20,10, 4, option.getColor().getRGB());
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        float size = 12;
        if (DrawHelper.isInRegion(mouseX,mouseY, x + width - 10 - size /2f, y + height / 2f - size /2f, size,size)) {
            if (setted == opened) {
                opened = null;
                return;
            }
            opened = setted;
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

    @Override
    public void onConfigUpdate() {
        super.onConfigUpdate();
       // setted.onConfigUpdate();
    }
}
