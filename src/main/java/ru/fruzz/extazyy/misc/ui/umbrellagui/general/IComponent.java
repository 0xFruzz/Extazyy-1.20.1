package ru.fruzz.extazyy.misc.ui.umbrellagui.general;

import com.mojang.blaze3d.vertex.PoseStack;

public interface IComponent {

    void drawComponent(PoseStack matrixStack, int mouseX, int mouseY);

    void mouseClicked(int mouseX, int mouseY, int mouseButton);

    void mouseReleased(int mouseX, int mouseY, int mouseButton);

    void keyTyped(int keyCode, int scanCode, int modifiers);

    void charTyped(char codePoint, int modifiers);
}

