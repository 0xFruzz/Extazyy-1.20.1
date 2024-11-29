package ru.fruzz.extazyy.misc.ui.main;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import ru.fruzz.extazyy.misc.util.render.dangertech.blur.DrawShader;
import ru.fruzz.extazyy.misc.util.gif.Gif;
import ru.fruzz.extazyy.misc.util.render.DrawHelper;

import java.awt.*;
import java.io.IOException;

public class MainMenu extends Screen {

    Gif gif;

    public MainMenu(Component pTitle) {
        super(pTitle);
       try {
           gif = new Gif(new ResourceLocation("minecraft", "extazyy/loading/minecraft.gif"));
       } catch (IOException e) {
       }
    }


    @Override
    public void render(GuiGraphics matrixStack, int mouseX, int mouseY, float partialticks) {
        float width = matrixStack.guiWidth();
        float height = matrixStack.guiHeight();
        DrawHelper.rectangle(matrixStack.pose(), 0,0,width,height, 0, new Color(0, 1, 8, 255).getRGB());
        DrawHelper.drawTextureAlpha(gif.getResourceBySpeed(0.4f), matrixStack.pose().last().pose(), 0,0,width,height, new Color(255,255,255,255).getRGB(), 10);
        DrawHelper.rectangle(matrixStack.pose(), (width /2) - 175 , (height/2) - 75, 350,150, 3.5f, new Color(0, 1, 8, 255).getRGB());
        DrawShader.drawRoundBlur(matrixStack.pose(), 0,0,width,height,0,new Color(255, 255, 255,5).getRGB());
    }
}
