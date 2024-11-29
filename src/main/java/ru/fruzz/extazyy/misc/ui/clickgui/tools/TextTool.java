package ru.fruzz.extazyy.misc.ui.clickgui.tools;


import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;

import net.minecraft.client.gui.screens.Screen;
import org.lwjgl.glfw.GLFW;
import ru.fruzz.extazyy.misc.font.FontRenderers;
import ru.fruzz.extazyy.main.modules.tools.imp.TextTools;
import ru.fruzz.extazyy.misc.ui.themeui.impl.Tool;
import ru.fruzz.extazyy.misc.util.render.lowrender.RenderMcd;
import ru.fruzz.extazyy.misc.util.render.DrawHelper;


import java.awt.*;

public class TextTool extends Tool {

    public TextTools setting;
    public boolean isTyping;

    public TextTool(TextTools setting) {
        this.setting = setting;
        this.s = setting;
    }

    @Override
    public void drawComponent(PoseStack matrixStack, int mouseX, int mouseY) {
        height -= 3;
        y += 1;
        String text = setting.get();

        float width = FontRenderers.msSemi16.getStringWidth(text) + 4;
        RenderMcd.drawBlurredShadow(matrixStack,x + 35, y + 2, width, 11, 6, new Color(12,12,12,255).getRGB());
        DrawHelper.rectangle(matrixStack,x + 35, y + 2, width, 11, 2, isTyping ? new Color(65, 65, 65, 200).getRGB() : new Color(65, 65, 65, 150).getRGB());
        FontRenderers.msSemi16.drawCenteredString(matrixStack, text, x + 35 + (width / 2), y + 4.5f, -1);
        FontRenderers.msSemi16.drawString(matrixStack, setting.getName(), x + 6, y + 4, -1);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (isHovered(mouseX,mouseY)) {
            isTyping = !isTyping;
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {

    }

    @Override
    public void keyTyped(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            if (!setting.text.isEmpty())
                setting.text = setting.text.substring(0, setting.text.length() - 1);
        } else if (keyCode == GLFW.GLFW_KEY_ENTER) {
            isTyping = false;
        }
        if (isTyping) {
            if (Screen.isCopy(keyCode)) {
                Minecraft.getInstance().keyboardHandler.setClipboard(setting.text);
            } else if (Screen.isPaste(keyCode)) {
                setting.text += Minecraft.getInstance().keyboardHandler.getClipboard();
            }
        }

    }

    @Override
    public void charTyped(char codePoint, int modifiers) {
        if (isTyping) {
            if (SharedConstants.isAllowedChatCharacter(codePoint))
                setting.text += Character.toString(codePoint);
        }
    }
}
