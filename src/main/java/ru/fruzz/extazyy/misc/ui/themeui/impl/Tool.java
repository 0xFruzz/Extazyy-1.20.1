package ru.fruzz.extazyy.misc.ui.themeui.impl;


import com.mojang.blaze3d.vertex.PoseStack;
import ru.fruzz.extazyy.main.modules.tools.Tools;
import ru.fruzz.extazyy.main.modules.ModuleApi.Module;
import ru.fruzz.extazyy.misc.ui.clickgui.ClickGui;
import ru.fruzz.extazyy.misc.ui.themeui.ThemeGui;

public abstract class Tool implements ITool {
    public float x, y, width, height;
    public Module module;
    public ThemeGui parent;
    public ClickGui guiparent;
    public Tools s;


    public boolean isHovered(int mouseX, int mouseY, float width, float height) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }

    public boolean isHovered(int mouseX, int mouseY, float x,float y, float width, float height) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }

    public boolean isHovered(int mouseX, int mouseY) {
        return mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + height;
    }

    public boolean isHovered(int mouseX, int mouseY, float height) {
        return mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + height;
    }

    public void setPosition(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public abstract void drawComponent(PoseStack matrixStack, int mouseX, int mouseY);

    @Override
    public abstract void mouseClicked(int mouseX, int mouseY, int mouseButton);

    @Override
    public abstract void mouseReleased(int mouseX, int mouseY, int mouseButton);

    @Override
    public abstract void keyTyped(int keyCode, int scanCode, int modifiers);

    @Override
    public abstract void charTyped(char codePoint, int modifiers);

    public void onConfigUpdate() {

    }
}
