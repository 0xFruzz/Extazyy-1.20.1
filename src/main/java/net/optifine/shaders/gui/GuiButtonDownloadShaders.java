package net.optifine.shaders.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.optifine.gui.GuiButtonOF;

public class GuiButtonDownloadShaders extends GuiButtonOF {
   public GuiButtonDownloadShaders(int buttonID, int xPos, int yPos) {
      super(buttonID, xPos, yPos, 22, 20, "");
   }

   public void render(GuiGraphics graphicsIn, int mouseX, int mouseY, float partialTicks) {
      if (this.visible) {
         super.render(graphicsIn, mouseX, mouseY, partialTicks);
         ResourceLocation resourcelocation = new ResourceLocation("optifine/textures/icons.png");
         RenderSystem.setShaderTexture(0, resourcelocation);
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         blit(graphicsIn, resourcelocation, this.getX() + 3, this.getY() + 2, 0, 0, 16, 16);
      }
   }
}